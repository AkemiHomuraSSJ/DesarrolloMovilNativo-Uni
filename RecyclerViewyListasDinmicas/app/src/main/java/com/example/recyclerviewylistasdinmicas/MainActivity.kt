package com.example.recyclerviewylistasdinmicas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Producto(
    val nombre: String,
    val descripcion: String,
    val precio: Int
)

class ProductoAdapter(
    private val listaProductos: List<Producto>,
    private val alHacerClic: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val textoNombre: TextView = vista.findViewById(R.id.textoNombre)
        val textoDescripcion: TextView = vista.findViewById(R.id.textoDescripcion)
        val textoPrecio: TextView = vista.findViewById(R.id.textoPrecio)
    }

    override fun onCreateViewHolder(padre: ViewGroup, tipoVista: Int): ProductoViewHolder {
        val vista = LayoutInflater.from(padre.context)
            .inflate(R.layout.item_producto, padre, false)
        return ProductoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, posicion: Int) {
        val producto = listaProductos[posicion]
        holder.textoNombre.text = producto.nombre
        holder.textoDescripcion.text = producto.descripcion
        holder.textoPrecio.text = "Precio: $" + producto.precio
        holder.itemView.setOnClickListener {
            alHacerClic(producto)
        }
    }

    override fun getItemCount(): Int {
        return listaProductos.size
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listaProductos = mutableListOf<Producto>()
        for (i in 1..30) {
            listaProductos.add(
                Producto("Producto $i", "Descripción del producto $i", i * 10)
            )
        }

        val recyclerProductos = findViewById<RecyclerView>(R.id.recyclerProductos)
        recyclerProductos.layoutManager = LinearLayoutManager(this)
        recyclerProductos.setHasFixedSize(true)
        recyclerProductos.adapter = ProductoAdapter(listaProductos) { producto ->
            mostrarDetalle(producto)
        }
    }

    private fun mostrarDetalle(producto: Producto) {
        AlertDialog.Builder(this)
            .setTitle(producto.nombre)
            .setMessage(producto.descripcion + "\nPrecio: $" + producto.precio)
            .setPositiveButton("Cerrar", null)
            .show()
    }
}