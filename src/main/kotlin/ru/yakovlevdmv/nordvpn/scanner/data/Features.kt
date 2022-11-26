package ru.yakovlevdmv.nordvpn.scanner.data

data class Features(
    val ikev2: Boolean,
    val ikev2_v6: Boolean,
    val l2tp: Boolean,
    val mesh_relay: Boolean,
    val openvpn_dedicated_tcp: Boolean,
    val openvpn_dedicated_udp: Boolean,
    val openvpn_tcp: Boolean,
    val openvpn_tcp_tls_crypt: Boolean,
    val openvpn_tcp_v6: Boolean,
    val openvpn_udp: Boolean,
    val openvpn_udp_tls_crypt: Boolean,
    val openvpn_udp_v6: Boolean,
    val openvpn_xor_tcp: Boolean,
    val openvpn_xor_udp: Boolean,
    val pptp: Boolean,
    val proxy: Boolean,
    val proxy_cybersec: Boolean,
    val proxy_ssl: Boolean,
    val proxy_ssl_cybersec: Boolean,
    val skylark: Boolean,
    val socks: Boolean,
    val wireguard_udp: Boolean
)