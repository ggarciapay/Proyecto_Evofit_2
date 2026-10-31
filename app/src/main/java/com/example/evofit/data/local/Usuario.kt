package com.example.evofit.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo"], unique = true),
        Index(value = ["nombreUsuario"], unique = true)
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val correo: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val nombreUsuario: String,
    val hashContrasena: String,
    val edad: Int,
    val sexo: String
)
