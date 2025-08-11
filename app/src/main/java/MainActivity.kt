package com.example.appclima.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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


interface RealtimeWeatherApi {
    @GET("weather/realtime")
    suspend fun getRealtimeWeather(
        @Query("location") location: String,
        @Query("apikey") apiKey: String
    ): RealtimeResponse
}


val retrofit = Retrofit.Builder()
    .baseUrl("https://api.tomorrow.io/v4/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val realtimeApi: RealtimeWeatherApi = retrofit.create(RealtimeWeatherApi::class.java)


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            weather != null -> {
                val values = weather!!.data.values
                Text("Ubicación: $name", fontSize = 6.2.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("🌡️ Temp: ${values.temperature}°C", fontSize = 16.sp)
                Text("💧 Humedad: ${values.humidity}%", fontSize = 16.sp)
                Text("🌬️ Viento: ${values.windSpeed} km/h", fontSize = 16.sp)
                Text("☁️ Código: ${values.weatherCode}", fontSize = 16.sp)
            }
            error != null -> {
                Text(error ?: "Error desconocido", color = MaterialTheme.colors.error, fontSize = 14.sp)
            }
            else -> {
                Text("Cargando clima...", fontSize = 16.sp)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val location = "20.2767,-97.960"
        val name = "Xicotepec, Pue, 73080, México"
        val apiKey = "vHk9DFO3uauGfZ1r0PvcawkwQuhF02UA"

        setContent {
            MaterialTheme {
                RealtimeWeatherScreen(location = location, name = name, apiKey = apiKey)
            }
        }
    }
}
