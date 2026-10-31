package com.example.evofit.data

import android.database.sqlite.SQLiteConstraintException
import com.example.evofit.data.local.Usuario
import com.example.evofit.data.local.UsuarioDao
import kotlinx.coroutines.CancellationException

sealed interface ResultadoRegistro {
    data class Exito(val usuario: Usuario) : ResultadoRegistro
    data class Duplicado(val correo: Boolean, val nombre: Boolean) : ResultadoRegistro
    data class Error(val mensaje: String) : ResultadoRegistro
}

class UsuarioRepository(private val dao: UsuarioDao) {
    suspend fun obtenerUsuarioActual(): Usuario? = dao.obtenerPrimero()

    suspend fun registrar(usuario: Usuario): ResultadoRegistro {
        duplicado(usuario.correo, usuario.nombreUsuario)?.let { return it }
        return try {
            val id = dao.insertar(usuario)
            if (id <= 0L) {
                ResultadoRegistro.Error("No se pudo guardar el usuario.")
            } else {
                ResultadoRegistro.Exito(usuario.copy(id = id))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: SQLiteConstraintException) {
            duplicado(usuario.correo, usuario.nombreUsuario)
                ?: ResultadoRegistro.Error("Ese correo o nombre de usuario ya está registrado.")
        } catch (_: Exception) {
            ResultadoRegistro.Error("No se pudo completar el registro. Inténtalo de nuevo.")
        }
    }

    private suspend fun duplicado(correo: String, nombre: String): ResultadoRegistro.Duplicado? {
        val correoExiste = dao.existeCorreo(correo)
        val nombreExiste = dao.existeNombreUsuario(nombre)
        return if (correoExiste || nombreExiste) {
            ResultadoRegistro.Duplicado(correo = correoExiste, nombre = nombreExiste)
        } else {
            null
        }
    }
}
