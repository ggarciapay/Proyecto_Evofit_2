package com.example.evofit.ui

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.evofit.data.PasswordHasher
import com.example.evofit.data.ResultadoRegistro
import com.example.evofit.data.UsuarioRepository
import com.example.evofit.data.local.EvoFitDatabase
import com.example.evofit.data.local.Usuario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

object SexoOpciones {
    val lista = listOf("Masculino", "Femenino", "Prefiero no especificar")
}

data class ErroresFormulario(
    val correo: String? = null,
    val nombreUsuario: String? = null,
    val contrasena: String? = null,
    val confirmar: String? = null,
    val edad: String? = null,
    val sexo: String? = null
) {
    val tieneErrores: Boolean
        get() = listOf(correo, nombreUsuario, contrasena, confirmar, edad, sexo).any { it != null }
}

data class FormularioRegistro(
    val correo: String = "",
    val nombreUsuario: String = "",
    val contrasena: String = "",
    val confirmar: String = "",
    val edad: String = "",
    val sexo: String = "",
    val errores: ErroresFormulario = ErroresFormulario(),
    val errorGeneral: String? = null,
    val registrando: Boolean = false
)

sealed interface Pantalla {
    data object Cargando : Pantalla
    data object Registro : Pantalla
    data class Principal(val nombreUsuario: String) : Pantalla
}

class EvoFitViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = UsuarioRepository(
        EvoFitDatabase.obtener(application).usuarioDao()
    )

    private val _pantalla = MutableStateFlow<Pantalla>(Pantalla.Cargando)
    val pantalla: StateFlow<Pantalla> = _pantalla.asStateFlow()

    private val _formulario = MutableStateFlow(FormularioRegistro())
    val formulario: StateFlow<FormularioRegistro> = _formulario.asStateFlow()

    init {
        viewModelScope.launch {
            _pantalla.value = try {
                val usuario = repositorio.obtenerUsuarioActual()
                if (usuario == null) {
                    Pantalla.Registro
                } else {
                    Pantalla.Principal(usuario.nombreUsuario)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Pantalla.Registro
            }
        }
    }

    fun onCorreoChange(valor: String) = actualizar { it.copy(correo = valor, errores = it.errores.copy(correo = null), errorGeneral = null) }

    fun onNombreChange(valor: String) = actualizar { it.copy(nombreUsuario = valor, errores = it.errores.copy(nombreUsuario = null), errorGeneral = null) }

    fun onContrasenaChange(valor: String) = actualizar { it.copy(contrasena = valor, errores = it.errores.copy(contrasena = null, confirmar = null), errorGeneral = null) }

    fun onConfirmarChange(valor: String) = actualizar { it.copy(confirmar = valor, errores = it.errores.copy(confirmar = null), errorGeneral = null) }

    fun onEdadChange(valor: String) = actualizar { it.copy(edad = valor, errores = it.errores.copy(edad = null), errorGeneral = null) }

    fun onSexoChange(valor: String) = actualizar { it.copy(sexo = valor, errores = it.errores.copy(sexo = null), errorGeneral = null) }

    fun registrar() {
        val actual = _formulario.value
        if (actual.registrando) return
        val errores = validar(actual)
        if (errores.tieneErrores) {
            _formulario.value = actual.copy(errores = errores, errorGeneral = null)
            return
        }

        val correo = actual.correo.trim().lowercase(Locale.ROOT)
        val nombre = normalizarNombre(actual.nombreUsuario)
        val contrasena = actual.contrasena
        val edad = actual.edad.trim().toInt()
        val sexo = actual.sexo
        _formulario.value = actual.copy(errores = ErroresFormulario(), errorGeneral = null, registrando = true)

        viewModelScope.launch {
            try {
                val hash = withContext(Dispatchers.Default) {
                    PasswordHasher.hash(contrasena.toCharArray())
                }
                when (val resultado = repositorio.registrar(
                    Usuario(
                        correo = correo,
                        nombreUsuario = nombre,
                        hashContrasena = hash,
                        edad = edad,
                        sexo = sexo
                    )
                )) {
                    is ResultadoRegistro.Exito -> {
                        _formulario.value = FormularioRegistro()
                        _pantalla.value = Pantalla.Principal(resultado.usuario.nombreUsuario)
                    }
                    is ResultadoRegistro.Duplicado -> {
                        _formulario.update { estado ->
                            estado.copy(
                                registrando = false,
                                errores = ErroresFormulario(
                                    correo = if (resultado.correo) "Este correo ya está registrado." else null,
                                    nombreUsuario = if (resultado.nombre) "Este nombre de usuario ya está en uso." else null
                                )
                            )
                        }
                    }
                    is ResultadoRegistro.Error -> {
                        _formulario.update { it.copy(registrando = false, errorGeneral = resultado.mensaje) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _formulario.update {
                    it.copy(
                        registrando = false,
                        errorGeneral = "No se pudo completar el registro. Inténtalo de nuevo."
                    )
                }
            }
        }
    }

    private fun actualizar(cambio: (FormularioRegistro) -> FormularioRegistro) {
        _formulario.update { actual ->
            if (actual.registrando) actual else cambio(actual)
        }
    }

    private fun validar(formulario: FormularioRegistro): ErroresFormulario {
        val correo = formulario.correo.trim()
        val nombre = normalizarNombre(formulario.nombreUsuario)
        val edad = formulario.edad.trim().toIntOrNull()
        return ErroresFormulario(
            correo = when {
                correo.isEmpty() -> "Completa este campo."
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Ingresa un correo electrónico válido."
                else -> null
            },
            nombreUsuario = if (nombre.isEmpty()) "Completa este campo." else null,
            contrasena = if (formulario.contrasena.isEmpty()) "Completa este campo." else null,
            confirmar = when {
                formulario.confirmar.isEmpty() -> "Completa este campo."
                formulario.contrasena.isNotEmpty() && formulario.contrasena != formulario.confirmar ->
                    "Las contraseñas no coinciden."
                else -> null
            },
            edad = when {
                formulario.edad.isBlank() -> "Completa este campo."
                edad == null || edad !in EDAD_MINIMA..EDAD_MAXIMA -> "Ingresa una edad entre 13 y 120."
                else -> null
            },
            sexo = when {
                formulario.sexo.isBlank() -> "Selecciona una opción."
                formulario.sexo !in SexoOpciones.lista -> "Selecciona una opción válida."
                else -> null
            }
        )
    }

    private fun normalizarNombre(nombre: String): String {
        return nombre.trim().replace(ESPACIOS, " ")
    }

    private companion object {
        const val EDAD_MINIMA = 13
        const val EDAD_MAXIMA = 120
        val ESPACIOS = Regex("\\s+")
    }
}
