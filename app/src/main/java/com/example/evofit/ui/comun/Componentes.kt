package com.example.evofit.ui.comun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evofit.ui.theme.EvoBackground
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoError
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoOnGreen
import com.example.evofit.ui.theme.EvoTextSecondary

@Composable
fun BarraPantalla(
    titulo: String,
    onAtras: () -> Unit,
    accion: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(88.dp), contentAlignment = Alignment.CenterStart) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onAtras)
                    .padding(8.dp)
            )
        }
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Box(modifier = Modifier.width(88.dp), contentAlignment = Alignment.CenterEnd) {
            if (accion != null) accion()
        }
    }
}

@Composable
fun BotonVerde(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    icono: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (habilitado) EvoGreen else EvoGreen.copy(alpha = 0.45f))
            .clickable(enabled = habilitado && !cargando, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = EvoOnGreen,
                strokeWidth = 2.5.dp
            )
        } else {
            if (icono != null) {
                icono()
                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            }
            Text(texto, color = EvoOnGreen, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EvoGreen,
    unfocusedBorderColor = EvoBorder,
    errorBorderColor = EvoError,
    focusedContainerColor = EvoCard,
    unfocusedContainerColor = EvoCard,
    errorContainerColor = EvoCard,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = EvoGreen,
    focusedPlaceholderColor = EvoTextSecondary,
    unfocusedPlaceholderColor = EvoTextSecondary,
    focusedLeadingIconColor = EvoGreen,
    unfocusedLeadingIconColor = EvoTextSecondary,
    focusedTrailingIconColor = EvoTextSecondary,
    unfocusedTrailingIconColor = EvoTextSecondary
)

fun Modifier.tarjetaEvo(): Modifier = this
    .clip(RoundedCornerShape(18.dp))
    .background(EvoCard)
    .border(1.dp, EvoBorder, RoundedCornerShape(18.dp))

val FondoEvo = EvoBackground
