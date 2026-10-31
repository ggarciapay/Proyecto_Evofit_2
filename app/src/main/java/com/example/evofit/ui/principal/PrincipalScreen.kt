package com.example.evofit.ui.principal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.evofit.ui.theme.EvoBackground
import com.example.evofit.ui.theme.EvoBorder
import com.example.evofit.ui.theme.EvoCard
import com.example.evofit.ui.theme.EvoFitTheme
import com.example.evofit.ui.theme.EvoGreen
import com.example.evofit.ui.theme.EvoNavBar
import com.example.evofit.ui.theme.EvoOnGreen
import com.example.evofit.ui.theme.EvoTextSecondary

private data class AccionInicio(val titulo: String, val icono: ImageVector)

private data class DiaGym(val letra: String, val nombre: String)

private data class DestinoNav(val etiqueta: String, val icono: ImageVector, val seleccionado: Boolean)

private val acciones = listOf(
    AccionInicio("Crear rutina", Icons.Filled.Add),
    AccionInicio("Rutinas recomendadas", Icons.Filled.Star),
    AccionInicio("Mis rutinas", Icons.AutoMirrored.Filled.List),
    AccionInicio("Temporizador", Icons.Filled.Timer)
)

private val dias = listOf(
    DiaGym("L", "Lun"),
    DiaGym("M", "Mar"),
    DiaGym("M", "Mié"),
    DiaGym("J", "Jue"),
    DiaGym("V", "Vie"),
    DiaGym("S", "Sáb"),
    DiaGym("D", "Dom")
)

private val destinos = listOf(
    DestinoNav("Inicio", Icons.Filled.Home, true),
    DestinoNav("Resumen mensual", Icons.Filled.BarChart, false),
    DestinoNav("Historial", Icons.Filled.History, false),
    DestinoNav("Perfil", Icons.Filled.Person, false)
)

@Composable
fun PrincipalScreen(
    nombreUsuario: String,
    onRealizar: () -> Unit = {},
    onCrear: () -> Unit = {},
    onRecomendadas: () -> Unit = {},
    onMisRutinas: () -> Unit = {},
    onResumen: () -> Unit = {},
    onHistorial: () -> Unit = {}
) {
    Scaffold(
        containerColor = EvoBackground,
        bottomBar = { BarraInferior(onResumen, onHistorial) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hola, $nombreUsuario 👋",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, EvoBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = EvoGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Text(
                text = "¡Vamos por más!",
                color = EvoTextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EvoGreen)
                    .clickable(onClick = onRealizar)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = EvoOnGreen,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Realizar rutina",
                    color = EvoOnGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = EvoOnGreen,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            acciones.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { accion ->
                        val click = when (accion.titulo) {
                            "Crear rutina" -> onCrear
                            "Rutinas recomendadas" -> onRecomendadas
                            "Mis rutinas" -> onMisRutinas
                            else -> ({})
                        }
                        TarjetaAccion(
                            titulo = accion.titulo,
                            icono = accion.icono,
                            onClick = click,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(EvoCard)
                    .border(1.dp, EvoBorder, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "Check de días en el gym",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Marca los días que fuiste",
                    color = EvoTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    dias.forEach { dia ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .border(1.5.dp, EvoBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dia.letra,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = dia.nombre,
                                color = EvoTextSecondary,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TarjetaAccion(
    titulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(124.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(EvoCard)
            .border(1.dp, EvoBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = EvoGreen,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun BarraInferior(onResumen: () -> Unit, onHistorial: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EvoNavBar)
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        destinos.forEach { destino ->
            val color = if (destino.seleccionado) EvoGreen else EvoTextSecondary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        when (destino.etiqueta) {
                            "Resumen mensual" -> Modifier.clickable(onClick = onResumen)
                            "Historial" -> Modifier.clickable(onClick = onHistorial)
                            else -> Modifier
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = destino.icono,
                    contentDescription = destino.etiqueta,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = destino.etiqueta,
                    color = color,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PrincipalPreview() {
    EvoFitTheme {
        PrincipalScreen(nombreUsuario = "Alejandro")
    }
}
