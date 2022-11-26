package ru.yakovlevdmv.nordvpn.scanner

import java.time.Duration

data class Config(
    val getServerUrl: String,
    val proxy: ProxySettings?,
    val executablePath: String,
    val connectCommand: String,
    val disconnectCommand: String,
    val outputFile: String,
    val outputFileHeaders: List<String>,
    val csvSeparator: String,
    val executeCommandTimeout: Duration,
    val pingHost: String,
    val pingTimeout: Duration,
    val pingDelay: Duration,
)

data class ProxySettings(
    val host: String,
    val port: Int,
    val credentials: Credentials?
) {
    data class Credentials(
        val username: String,
        val password: String,
    )
}
