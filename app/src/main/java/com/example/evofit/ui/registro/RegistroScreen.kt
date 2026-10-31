package com.example.evofit.ui.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evofit.ui.FormularioRegistro
import com.example.evofit.ui.SexoOpciones
import com.example.evofit.ui.theme.EvoBackground
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoError
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoOnGreen
import com.example.evofit.ui.theme.EvoTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    estado: FormularioRegistro,
    onCorreo: (String) -> Unit,
    onNombre: (String) -> Unit,
    onContrasena: (String) -> Unit,
    onConfirmar: (String) -> Unit,
    onEdad: (String) -> Unit,
    onSexo: (String) -> Unit,
    onRegistrar: () -> Unit
) {
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }
    var confirmarVisible by rememberSaveable { mutableStateOf(false) }
    var sexoExpandido by rememberSaveable { mutableStateOf(false) }
    val colores = coloresCampo()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EvoBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = EvoGreen,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Crear cuenta",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Completa tus datos para comenzar",
                color = EvoTextSecondary,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 34.dp, bottom = 6.dp)
            )

            CampoTexto(
                valor = estado.nombreUsuario,
                onValor = onNombre,
                placeholder = "Nombre de usuario",
                icono = Icons.Filled.Person,
                error = estado.errores.nombreUsuario,
                habilitado = !estado.registrando,
                colores = colores,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
            )
            CampoTexto(
                valor = estado.correo,
                onValor = onCorreo,
                placeholder = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = estado.errores.correo,
                habilitado = !estado.registrando,
                colores = colores,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            CampoTexto(
                valor = estado.contrasena,
                onValor = onContrasena,
                placeholder = "Contraseña",
                icono = Icons.Filled.Lock,
                error = estado.errores.contrasena,
                habilitado = !estado.registrando,
                colores = colores,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                transformacion = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                accionFinal = {
                    IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                        Icon(
                            imageVector = if (contrasenaVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = EvoTextSecondary
                        )
                    }
                }
            )
            CampoTexto(
                valor = estado.confirmar,
                onValor = onConfirmar,
                placeholder = "Confirmar contraseña",
                icono = Icons.Filled.Lock,
                error = estado.errores.confirmar,
                habilitado = !estado.registrando,
                colores = colores,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                transformacion = if (confirmarVisible) VisualTransformation.None else PasswordVisualTransformation(),
                accionFinal = {
                    IconButton(onClick = { confirmarVisible = !confirmarVisible }) {
                        Icon(
                            imageVector = if (confirmarVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (confirmarVisible) "Ocultar confirmación" else "Mostrar confirmación",
                            tint = EvoTextSecondary
                        )
                    }
                }
            )
            CampoTexto(
                valor = estado.edad,
                onValor = onEdad,
                placeholder = "Edad",
                icono = Icons.Filled.CalendarMonth,
                error = estado.errores.edad,
                habilitado = !estado.registrando,
                colores = colores,
                teclado = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ExposedDropdownMenuBox(
                    expanded = sexoExpandido,
                    onExpandedChange = { if (!estado.registrando) sexoExpandido = it }
                ) {
                    OutlinedTextField(
                        value = estado.sexo,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !estado.registrando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        placeholder = { Text("Sexo") },
                        leadingIcon = { Icon(Icons.Filled.Wc, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexoExpandido) },
                        singleLine = true,
                        isError = estado.errores.sexo != null,
                        shape = RoundedCornerShape(16.dp),
                        colors = colores
                    )
                    ExposedDropdownMenu(
                        expanded = sexoExpandido,
                        onDismissRequest = { sexoExpandido = false },
                        containerColor = EvoCard
                    ) {
                        SexoOpciones.lista.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion, color = Color.White) },
                                onClick = {
                                    onSexo(opcion)
                                    sexoExpandido = false
                                },
                                colors = MenuDefaults.itemColors(textColor = Color.White)
                            )
                        }
                    }
                }
                TextoError(estado.errores.sexo)
            }

            if (estado.errorGeneral != null) {
                Text(
                    text = estado.errorGeneral,
                    color = EvoError,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = onRegistrar,
                enabled = !estado.registrando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EvoGreen,
                    contentColor = EvoOnGreen,
                    disabledContainerColor = EvoGreen.copy(alpha = 0.45f),
                    disabledContentColor = EvoOnGreen.copy(alpha = 0.7f)
                )
            ) {
                if (estado.registrando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = EvoOnGreen,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Registrarse",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp, top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("¿Ya tienes cuenta? ", color = EvoTextSecondary, fontSize = 14.sp)
            Text("Inicia sesión", color = EvoGreen, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String,
    icono: ImageVector,
    error: String?,
    habilitado: Boolean,
    colores: androidx.compose.material3.TextFieldColors,
    teclado: KeyboardOptions,
    transformacion: VisualTransformation = VisualTransformation.None,
    accionFinal: @Composable (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValor,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = accionFinal,
            singleLine = true,
            isError = error != null,
            visualTransformation = transformacion,
            keyboardOptions = teclado,
            shape = RoundedCornerShape(16.dp),
            colors = colores
        )
        TextoError(error)
    }
}

@Composable
private fun TextoError(error: String?) {
    if (error != null) {
        Text(
            text = error,
            color = EvoError,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EvoGreen,
    unfocusedBorderColor = EvoBorder,
    disabledBorderColor = EvoBorder,
    errorBorderColor = EvoError,
    focusedContainerColor = EvoCard,
    unfocusedContainerColor = EvoCard,
    disabledContainerColor = EvoCard,
    errorContainerColor = EvoCard,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color.White.copy(alpha = 0.7f),
    cursorColor = EvoGreen,
    focusedLeadingIconColor = EvoTextSecondary,
    unfocusedLeadingIconColor = EvoTextSecondary,
    disabledLeadingIconColor = EvoTextSecondary,
    errorLeadingIconColor = EvoTextSecondary,
    focusedTrailingIconColor = EvoTextSecondary,
    unfocusedTrailingIconColor = EvoTextSecondary,
    disabledTrailingIconColor = EvoTextSecondary,
    focusedPlaceholderColor = EvoTextSecondary,
    unfocusedPlaceholderColor = EvoTextSecondary,
    disabledPlaceholderColor = EvoTextSecondary,
    focusedLabelColor = EvoTextSecondary,
    unfocusedLabelColor = EvoTextSecondary
)
