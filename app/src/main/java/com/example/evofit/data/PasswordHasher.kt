package com.example.evofit.data

import android.util.Base64
import java.nio.CharBuffer
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal object PasswordHasher {
    private const val ALGORITMO = "pbkdf2-sha256"
    private const val ITERACIONES = 210_000
    private const val LONGITUD_BITS = 256
    private const val LONGITUD_SALT = 16

    fun hash(contrasena: CharArray): String {
        val salt = ByteArray(LONGITUD_SALT)
        SecureRandom().nextBytes(salt)
        var derivado = ByteArray(0)
        try {
            derivado = derivar(contrasena, salt)
            val saltCodificado = Base64.encodeToString(salt, Base64.NO_WRAP)
            val hashCodificado = Base64.encodeToString(derivado, Base64.NO_WRAP)
            return "$ALGORITMO:$ITERACIONES:$saltCodificado:$hashCodificado"
        } finally {
            contrasena.fill('\u0000')
            salt.fill(0)
            derivado.fill(0)
        }
    }

    private fun derivar(contrasena: CharArray, salt: ByteArray): ByteArray {
        return try {
            val spec = PBEKeySpec(contrasena, salt, ITERACIONES, LONGITUD_BITS)
            try {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .encoded
            } finally {
                spec.clearPassword()
            }
        } catch (_: GeneralSecurityException) {
            pbkdf2HmacSha256(contrasena, salt)
        }
    }

    private fun pbkdf2HmacSha256(contrasena: CharArray, salt: ByteArray): ByteArray {
        val password = aUtf8(contrasena)
        try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(password, "HmacSHA256"))
            val longitud = LONGITUD_BITS / 8
            val tamanoHash = mac.macLength
            val bloques = (longitud + tamanoHash - 1) / tamanoHash
            val resultado = ByteArray(bloques * tamanoHash)
            val bloque = ByteArray(salt.size + 4)
            System.arraycopy(salt, 0, bloque, 0, salt.size)
            for (indice in 1..bloques) {
                bloque[salt.size] = (indice ushr 24).toByte()
                bloque[salt.size + 1] = (indice ushr 16).toByte()
                bloque[salt.size + 2] = (indice ushr 8).toByte()
                bloque[salt.size + 3] = indice.toByte()
                var u = mac.doFinal(bloque)
                val acumulado = u.copyOf()
                for (iteracion in 1 until ITERACIONES) {
                    u = mac.doFinal(u)
                    for (posicion in acumulado.indices) {
                        acumulado[posicion] =
                            (acumulado[posicion].toInt() xor u[posicion].toInt()).toByte()
                    }
                }
                System.arraycopy(acumulado, 0, resultado, (indice - 1) * tamanoHash, tamanoHash)
            }
            return resultado.copyOf(longitud)
        } finally {
            password.fill(0)
        }
    }

    private fun aUtf8(contrasena: CharArray): ByteArray {
        val buffer = Charsets.UTF_8.encode(CharBuffer.wrap(contrasena))
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return bytes
    }
}
