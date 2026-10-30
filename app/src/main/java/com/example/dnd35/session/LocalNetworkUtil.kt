package com.example.dnd35.session

import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/** Descobre o IP do próprio aparelho na rede local, para colocar no QR Code da sessão. */
object LocalNetworkUtil {
    /** Retorna `null` se nenhuma interface de rede ativa com IPv4 for encontrada. */
    fun findLocalIpAddress(): String? {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            val candidates = interfaces
                .filter { it.isUp && !it.isLoopback && !it.isVirtual }
                .flatMap { iface -> Collections.list(iface.inetAddresses).map { iface to it } }
                .filter { (_, addr) -> addr is Inet4Address }

            // Prioriza interfaces de Wi-Fi (nome costuma conter "wlan") quando existirem.
            val preferred = candidates.firstOrNull { (iface, _) -> iface.name.contains("wlan", ignoreCase = true) }
                ?: candidates.firstOrNull()

            preferred?.second?.hostAddress
        } catch (e: Exception) {
            null
        }
    }
}
