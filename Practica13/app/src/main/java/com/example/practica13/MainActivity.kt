package com.example.practica13

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var administradorSensores: SensorManager
    private var acelerometro: Sensor? = null

    private var ejeX by mutableFloatStateOf(0f)
    private var ejeY by mutableFloatStateOf(0f)
    private var ejeZ by mutableFloatStateOf(0f)
    private var fuerzaG by mutableFloatStateOf(0f)
    private var sacudidas by mutableIntStateOf(0)

    private var ultimaSacudida = 0L
    private val umbralSacudida = 2.5f
    private val tiempoEntreSacudidas = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        administradorSensores = getSystemService(SENSOR_SERVICE) as SensorManager
        acelerometro = administradorSensores.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            PantallaAcelerometro(
                hayAcelerometro = acelerometro != null,
                x = ejeX,
                y = ejeY,
                z = ejeZ,
                fuerza = fuerzaG,
                sacudidas = sacudidas
            )
        }
    }

    override fun onResume() {
        super.onResume()
        acelerometro?.let {
            administradorSensores.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        administradorSensores.unregisterListener(this)
    }

    override fun onSensorChanged(evento: SensorEvent) {
        val x = evento.values[0]
        val y = evento.values[1]
        val z = evento.values[2]

        ejeX = x
        ejeY = y
        ejeZ = z

        val fuerza = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        fuerzaG = fuerza

        val ahora = System.currentTimeMillis()
        if (fuerza > umbralSacudida && ahora - ultimaSacudida > tiempoEntreSacudidas) {
            ultimaSacudida = ahora
            sacudidas = sacudidas + 1
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, precision: Int) {
    }
}

@Composable
fun PantallaAcelerometro(
    hayAcelerometro: Boolean,
    x: Float,
    y: Float,
    z: Float,
    fuerza: Float,
    sacudidas: Int
) {
    var sacudiendo by remember { mutableStateOf(false) }

    LaunchedEffect(sacudidas) {
        if (sacudidas > 0) {
            sacudiendo = true
            delay(1000)
            sacudiendo = false
        }
    }

    val colorFondo = if (sacudiendo) Color.Red else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondo),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(text = "Acelerómetro", fontSize = 28.sp, color = Color.Black)

        if (hayAcelerometro) {
            Text(text = "Eje X: " + "%.2f".format(x), fontSize = 22.sp, color = Color.Black)
            Text(text = "Eje Y: " + "%.2f".format(y), fontSize = 22.sp, color = Color.Black)
            Text(text = "Eje Z: " + "%.2f".format(z), fontSize = 22.sp, color = Color.Black)
            Text(text = "Fuerza: " + "%.2f".format(fuerza) + " G", fontSize = 22.sp, color = Color.Black)
            Text(text = "Sacudidas: " + sacudidas, fontSize = 22.sp, color = Color.Black)

            if (sacudiendo) {
                Text(text = "Sacudida detectada", fontSize = 26.sp, color = Color.White)
            }
        } else {
            Text(text = "Este dispositivo no tiene acelerómetro", fontSize = 20.sp, color = Color.Black)
        }
    }
}