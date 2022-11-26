package ru.yakovlevdmv.nordvpn.scanner.data

data class Server(
    val categories: List<Category>,
    val country: String,
    val domain: String,
    val features: Features,
    val flag: String,
    val id: Int,
    val ip_address: String,
    val load: Int,
    val location: Location,
    val name: String,
    val price: Int,
    val search_keywords: List<String>
)