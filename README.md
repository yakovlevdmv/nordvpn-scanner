### Использование:
Собрать jar из исходников:
```shell
gradlew :shadowJar
```
jar сохранится в папку `<repositoryRoot>\build\libs`

Создать файл конфигурации и переопределить необходимые параметры, указанные в [application.conf](src/main/resources/application.conf). Запуск:
```shell
 java -Dconfig.file=<путь к файлу конфигурации> -jar <путь к jar>
```
Результатом работы программы является csv файл с двумя колонками: имя сервера(например, Australia #753) и булево значение, где true означает, что был успешно запрошен ресурс, указанный в конфигурации `ru.yakovlevdmv.nordvpn.scanner.ping-host`. Значение true не является 100% доказательством успешной работы vpn: программа запускает подключение к vpn через интерфейс командной строки NordVPN, который только запускает подключение к серверу, но не ожидает результата. Поэтому ping ресурса, указанного в конфигурации `ru.yakovlevdmv.nordvpn.scanner.ping-host` выполняется с задержкой, указанной в конфигурации `ru.yakovlevdmv.nordvpn.scanner.ping-timeout` (по умолчанию 5 секунд). Чем больше таймаут, тем больше вероятность усипешного подключения и достоверности работы программы.