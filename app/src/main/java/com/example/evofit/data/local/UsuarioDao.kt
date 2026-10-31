package com.example.evofit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios ORDER BY id ASC LIMIT 1")
    suspend fun obtenerPrimero(): Usuario?

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE correo = :correo)")
    suspend fun existeCorreo(correo: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE nombreUsuario = :nombre COLLATE NOCASE)")
    suspend fun existeNombreUsuario(nombre: String): Boolean
}
