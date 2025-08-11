package com.example.appclima.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appclima.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// -------------------------
// Data classes
// -------------------------
data class RealtimeResponse(
    val data: RealtimeData
)

data class RealtimeData(
    val values: RealtimeValues
)

data class RealtimeValues(
    val temperature: Float,
    val humidity: Float,
    val windSpeed: Float,
    val weatherCode: Int
)

// -------------------------
// API Interface
// -------------------------
interface RealtimeWeatherApi {
    @GET("weather/realtime")
    suspend fun getRealtimeWeather(
        @Query("location") location: String,
        @Query("apikey") apiKey: String
    ): RealtimeResponse
}

// -------------------------
// Retrofit Instance
// -------------------------
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.tomorrow.io/v4/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val realtimeApi: RealtimeWeatherApi = retrofit.create(RealtimeWeatherApi::class.java)

// -------------------------
// UI Composables
// -------------------------
@Composable
fun RealtimeWeatherScreen(location: String, apiKey: String, name: String) {
    var weather by remember { mutableStateOf<RealtimeResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(location, key2 = name) {
        try {
            weather = realtimeApi.getRealtimeWeather(location, apiKey)
        } catch (e: Exception) {
            Log.e("Weather", "Error al obtener clima", e)
            error = "Error: ${e.localizedMessage}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2196F3), // Azul
                        Color.White        // Blanco
                    )
                )
            )
    ) {
        // Imagen de nubes semitransparentes
        Image(
            painter = painterResource(id = R.drawable.img), // coloca tu imagen en res/drawable
            contentDescription = "Nubes",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.2f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                weather != null -> {
                    val values = weather!!.data.values

                    Text(
                        text = name,
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${values.temperature.toInt()}°",
                            fontSize = 50.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getWeatherIcon(values.weatherCode),
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = getWeatherDescription(values.weatherCode),
                        fontSize = 17.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Humedad: ${values.humidity}%",
                        fontSize = 15.sp,
                        color = Color.White
                    )

                    Text(
                        text = "Viento: ${values.windSpeed} km/h",
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Error desconocido",
                        color = Color.Red
                    )
                }
                else -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando clima...", color = Color.White)
                }
            }
        }
    }
}

// -------------------------
// Funciones para iconos y descripción
// -------------------------
fun getWeatherIcon(code: Int): String {
    return when (code) {
        1000 -> "☀️"
        1001 -> "☁️"
        1100 -> "🌤️"
        1101 -> "⛅"
        1102 -> "🌥️"
        2000 -> "🌫️"
        4000 -> "🌧️"
        4001 -> "🌧️"
        4200 -> "🌦️"
        4201 -> "🌧️"
        else -> "❓"
    }
}

fun getWeatherDescription(code: Int): String {
    return when (code) {
        1000 -> "Soleado"
        1001 -> "Nublado"
        1100 -> "Mayormente soleado"
        1101 -> "Parcialmente nublado"
        1102 -> "Mayormente nublado"
        2000 -> "Niebla"
        4000 -> "Llovizna"
        4001 -> "Lluvia"
        4200 -> "Lluvia ligera"
        4201 -> "Lluvia fuerte"
        else -> "Desconocido"
    }
}

// -------------------------
// Main Activity
// -------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val location = "20.2767,-97.960"
        val name = "Tierra Negra"
        val apiKey = "vHk9DFO3uauGfZ1r0PvcawkwQuhF02UA"

        setContent {
            MaterialTheme {
                RealtimeWeatherScreen(location = location, name = name, apiKey = apiKey)
            }
        }
    }
}
