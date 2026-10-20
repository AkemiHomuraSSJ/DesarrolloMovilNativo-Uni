package com.example.practica12

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ElementoApi(
    @SerializedName("name") val nombre: String,
    val url: String
)

data class RespuestaTipos(
    @SerializedName("results") val resultados: List<ElementoApi>
)

data class PokemonDeTipo(
    @SerializedName("pokemon") val pokemon: ElementoApi
)

data class RespuestaTipo(
    @SerializedName("pokemon") val pokemones: List<PokemonDeTipo>
)

data class TipoDePokemon(
    @SerializedName("type") val tipo: ElementoApi
)

data class Estadistica(
    @SerializedName("base_stat") val valor: Int,
    @SerializedName("stat") val estadistica: ElementoApi
)

data class ArteOficial(
    @SerializedName("front_default") val frente: String?
)

data class OtrasImagenes(
    @SerializedName("official-artwork") val arteOficial: ArteOficial?
)

data class Imagenes(
    @SerializedName("other") val otras: OtrasImagenes?
)

data class Pokemon(
    val id: Int,
    @SerializedName("name") val nombre: String,
    @SerializedName("height") val altura: Int,
    @SerializedName("weight") val peso: Int,
    @SerializedName("types") val tipos: List<TipoDePokemon>,
    @SerializedName("stats") val estadisticas: List<Estadistica>,
    @SerializedName("sprites") val imagenes: Imagenes
)

interface PokeApi {
    @GET("type?limit=30")
    suspend fun obtenerTipos(): RespuestaTipos

    @GET("type/{nombre}")
    suspend fun obtenerTipo(@Path("nombre") nombre: String): RespuestaTipo

    @GET("pokemon/{id}")
    suspend fun obtenerPokemon(@Path("id") id: String): Pokemon
}

val servicio: PokeApi = Retrofit.Builder()
    .baseUrl("https://pokeapi.co/api/v2/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(PokeApi::class.java)

val tiposExcluidos = listOf("unknown", "shadow", "stellar")

val nombresTipos = mapOf(
    "normal" to "Normal", "fire" to "Fuego", "water" to "Agua", "grass" to "Planta",
    "electric" to "Eléctrico", "ice" to "Hielo", "fighting" to "Lucha", "poison" to "Veneno",
    "ground" to "Tierra", "flying" to "Volador", "psychic" to "Psíquico", "bug" to "Bicho",
    "rock" to "Roca", "ghost" to "Fantasma", "dragon" to "Dragón", "dark" to "Siniestro",
    "steel" to "Acero", "fairy" to "Hada"
)

val nombresEstadisticas = mapOf(
    "hp" to "Puntos de vida", "attack" to "Ataque", "defense" to "Defensa",
    "special-attack" to "Ataque especial", "special-defense" to "Defensa especial",
    "speed" to "Velocidad"
)

val coloresTipos = mapOf(
    "fire" to Color(0xFFFFB38A), "water" to Color(0xFF9FCBFF), "grass" to Color(0xFFA8E6A1),
    "electric" to Color(0xFFFFF29A), "psychic" to Color(0xFFFFA6D5), "ice" to Color(0xFFBFEFF5),
    "poison" to Color(0xFFD7A6E8), "ground" to Color(0xFFE3C99A), "fighting" to Color(0xFFF0A0A0),
    "bug" to Color(0xFFD4E79A), "ghost" to Color(0xFFB9B0D9), "dragon" to Color(0xFFB5A6F5)
)

val margenPantalla = Modifier
    .fillMaxSize()
    .systemBarsPadding()
    .padding(16.dp)

fun mayuscula(texto: String) = texto.replaceFirstChar { it.uppercase() }

fun obtenerId(url: String) = url.trimEnd('/').substringAfterLast('/')

fun traducirTipo(tipo: String) = nombresTipos[tipo] ?: mayuscula(tipo)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppPokedex()
        }
    }
}

@Composable
fun AppPokedex() {
    val navegador = rememberNavController()

    NavHost(navController = navegador, startDestination = "tipos") {
        composable("tipos") {
            PantallaTipos { tipo -> navegador.navigate("lista/" + tipo) }
        }
        composable("lista/{tipo}") { entrada ->
            val tipo = entrada.arguments?.getString("tipo").orEmpty()
            PantallaLista(tipo) { id -> navegador.navigate("detalle/" + id) }
        }
        composable("detalle/{id}") { entrada ->
            PantallaDetalle(entrada.arguments?.getString("id").orEmpty())
        }
    }
}

@Composable
fun Estado(hayError: Boolean) {
    Text(
        text = if (hayError) "No se pudo cargar la información" else "Cargando...",
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
fun PantallaTipos(alElegir: (String) -> Unit) {
    var tipos by remember { mutableStateOf<List<ElementoApi>?>(null) }
    var hayError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            tipos = servicio.obtenerTipos().resultados.filter { it.nombre !in tiposExcluidos }
        } catch (e: Exception) {
            hayError = true
        }
    }

    val lista = tipos

    Column(modifier = margenPantalla) {
        Text(text = "Pokédex - Tipos", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        if (lista == null) {
            Estado(hayError)
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(lista, key = { it.url }) { tipo ->
                    Button(
                        onClick = { alElegir(tipo.nombre) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = traducirTipo(tipo.nombre))
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaLista(tipo: String, alElegir: (String) -> Unit) {
    var pokemones by remember { mutableStateOf<List<ElementoApi>?>(null) }
    var hayError by remember { mutableStateOf(false) }

    LaunchedEffect(tipo) {
        try {
            pokemones = servicio.obtenerTipo(tipo).pokemones.map { it.pokemon }
        } catch (e: Exception) {
            hayError = true
        }
    }

    val lista = pokemones

    Column(modifier = margenPantalla) {
        Text(
            text = "Pokémon de tipo " + traducirTipo(tipo),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (lista == null) {
            Estado(hayError)
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(lista, key = { it.url }) { pokemon ->
                    val id = obtenerId(pokemon.url)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { alElegir(id) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png",
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = mayuscula(pokemon.nombre),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalle(id: String) {
    var pokemon by remember { mutableStateOf<Pokemon?>(null) }
    var hayError by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        try {
            pokemon = servicio.obtenerPokemon(id)
        } catch (e: Exception) {
            hayError = true
        }
    }

    val carta = pokemon

    Column(modifier = margenPantalla.verticalScroll(rememberScrollState())) {
        if (carta == null) {
            Estado(hayError)
        } else {
            val primerTipo = carta.tipos.firstOrNull()?.tipo?.nombre
            val puntosVida = carta.estadisticas.find { it.estadistica.nombre == "hp" }?.valor ?: 0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(6.dp, Color(0xFFE6B800))
                    .background(coloresTipos[primerTipo] ?: Color(0xFFE0E0E0))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = mayuscula(carta.nombre), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "PS " + puntosVida, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                AsyncImage(
                    model = carta.imagenes.otras?.arteOficial?.frente,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(250.dp)
                        .background(Color.White)
                )

                Text(text = "Tipo: " + carta.tipos.joinToString(", ") { traducirTipo(it.tipo.nombre) })
                Text(text = "Número: " + carta.id)
                Text(text = "Altura: " + carta.altura / 10.0 + " m")
                Text(text = "Peso: " + carta.peso / 10.0 + " kg")

                Text(
                    text = "Estadísticas",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                for (estadistica in carta.estadisticas) {
                    val nombre = estadistica.estadistica.nombre
                    Text(text = (nombresEstadisticas[nombre] ?: nombre) + ": " + estadistica.valor)
                }
            }
        }
    }
}