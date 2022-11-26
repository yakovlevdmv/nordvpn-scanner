package ru.yakovlevdmv.nordvpn.scanner

import akka.Done
import akka.actor.ActorSystem
import akka.http.javadsl.ClientTransport
import akka.http.javadsl.Http
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.headers.HttpCredentials
import akka.http.javadsl.settings.ClientConnectionSettings
import akka.http.javadsl.settings.ConnectionPoolSettings
import akka.stream.javadsl.FileIO
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Source
import akka.util.ByteString
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import mu.KotlinLogging
import ru.yakovlevdmv.nordvpn.scanner.data.GetServerResponse
import ru.yakovlevdmv.nordvpn.scanner.data.Server
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

val logger = KotlinLogging.logger {}

val CONFIG_BASE = "ru.yakovlevdmv.nordvpn.scanner"
val system = ActorSystem.create("nordvpn-scanner")
val typesafeConfig = ConfigFactory.load()
val config = typesafeConfig.extract<Config>(CONFIG_BASE)
val objectMapper = jacksonObjectMapper()

fun fetchNordServers(): CompletionStage<List<Server>> {
    val proxy = config.proxy?.let {
        val address = InetSocketAddress.createUnresolved(it.host, it.port)
        val credentials = it.credentials?.let { HttpCredentials.createBasicHttpCredentials(it.username, it.password) }
        ClientTransport.httpsProxy(address, credentials)
    }

    val makeRequest = if (proxy == null) Http.get(system).singleRequest(HttpRequest.GET(config.getServerUrl))
    else Http.get(system).singleRequest(
        HttpRequest.GET(config.getServerUrl),
        Http.get(system).defaultClientHttpsContext(),
        ConnectionPoolSettings.create(system)
            .withConnectionSettings(ClientConnectionSettings.create(system).withTransport(proxy)),
        system.log()
    )
    return makeRequest
        .thenCompose {
            if (it.status().isSuccess) Jackson.unmarshaller(objectMapper, GetServerResponse::class.java)
                .unmarshal(it.entity(), system)
                .thenApply { servers -> servers.orEmpty().sortedBy { server -> server.name } }
            else CompletableFuture.completedFuture(emptyList())
        }
}

fun connect(serverName: String) {
    val command = config.connectCommand.replace("\${serverName}", serverName)
    logger.debug { "Executing command: ${config.executablePath}$command" }
    executeCommand("${config.executablePath}$command")
}

fun disconnect() {
    val command = config.disconnectCommand
    logger.debug { "Executing command: ${config.executablePath}$command" }
    executeCommand("${config.executablePath}$command")
}

private fun executeCommand(command: String) {
    try {
        val exec = Runtime.getRuntime().exec(command)
        val exitCode = exec.waitFor(config.executeCommandTimeout.toMillis(), TimeUnit.MILLISECONDS)
        logger.debug { "Process completed with code $exitCode" }
    } catch (ex: IOException) {
        logger.error(ex) { ex.message }
    }
}

fun ping(): Result<Done> {
    val singleRequest = Http.get(system).singleRequest(HttpRequest.GET(config.pingHost))
    return try {
        singleRequest.toCompletableFuture().get(config.pingTimeout.toMillis(), TimeUnit.MILLISECONDS)
        Result.success(Done.getInstance())
    } catch (ex: Exception) {
        Result.failure(ex)
    }
}

fun main() {
    val testServer = Flow.create<Server>()
        .map {
            connect(serverName = it.name)
            Thread.sleep(config.pingDelay.toMillis())
            val result = ping()
            disconnect()
            it to result.isSuccess
        }
    val formatCsv = Flow.create<Pair<Server, Boolean>>()
        .map { ByteString.fromString("${it.first.name}${config.csvSeparator}${it.second}\n") }
    val saveToCsv = Flow.create<ByteString>()
        .prepend(Source.single(ByteString.fromString(config.outputFileHeaders.joinToString(separator = config.csvSeparator, postfix = "\n"))))
    Source.completionStage(fetchNordServers())
        .flatMapConcat { Source.from(it) }
        .via(testServer)
        .via(formatCsv)
        .via(saveToCsv)
        .runWith(FileIO.toPath(Paths.get(config.outputFile)), system)
        .whenComplete { _, _ -> system.terminate() }
}

