package com.example.appfirebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appfirebase.ui.theme.AppFirebaseTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppFirebaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(db = db, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class Cliente(
    val id: String,
    val nome: String,
    val telefone: String
)

@Composable
fun ClienteItem(cliente: Cliente, onUpdate: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = cliente.nome,
                fontWeight = FontWeight.Bold
            )
            Text(text = cliente.telefone)
        }
        Row {
            FilledTonalIconButton(
                onClick = onUpdate
            ) {
                Icon(Icons.Default.Create, contentDescription = "Editar")
            }

            FilledTonalIconButton(
                onClick = onDelete
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir")
            }
        }
    }
}

@Composable
fun App(db: FirebaseFirestore, modifier: Modifier = Modifier) {
    var nome by remember {
        mutableStateOf("")
    }
    var telefone by remember {
        mutableStateOf("")
    }
    var updateId by remember {
        mutableStateOf("")
    }

    val clientes = remember {
        mutableStateListOf<Cliente>()
    }

    fun getClientes() {
        db.collection("clientes")
            .get()
            .addOnSuccessListener { documents ->
                clientes.clear()
                for (document in documents) {
                    clientes.add(Cliente(
                        id = document.id,
                        nome = document.data["nome"].toString(),
                        telefone = document.data["telefone"].toString()
                    ))
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    LaunchedEffect(Unit) {
        getClientes()
    }

    fun handleSubmit() {
        if (nome.isEmpty() || telefone.isEmpty()) {
            return
        }

        val cliente = hashMapOf(
            "nome" to nome,
            "telefone" to telefone
        )

        if (updateId.isNotEmpty()) {
            db.collection("clientes")
                .document(updateId)
                .set(cliente)
                .addOnSuccessListener {
                    println("DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e ->
                    println("Error updating document: $e")
                }
        } else {
            db.collection("clientes")
                .add(cliente)
                .addOnSuccessListener { documentReference ->
                    println("DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    println("Error adding document: $e")
                }
        }

        nome = ""
        telefone = ""
        updateId = ""

        getClientes()
    }

    fun handleUpdate(cliente: Cliente) {
        nome = cliente.nome
        telefone = cliente.telefone
        updateId = cliente.id

        getClientes()
    }

    fun handleDelete(cliente: Cliente) {
        db.collection("clientes")
            .document(cliente.id)
            .delete()
            .addOnSuccessListener {
                println("DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                println("Error deleting document: $e")
            }
        getClientes()
    }

    Column(
        Modifier
            .then(modifier)
            .padding(20.dp)
            .fillMaxWidth()
    ){
        Row(
            Modifier
                .fillMaxWidth(),
            Arrangement.Center
        ){Text(text = "Cadastro de Clientes")
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ){
        }
        Row(
            Modifier
                .fillMaxWidth()
        ){
            Column(
                Modifier
                    .fillMaxWidth(0.3f)
            ){
                Text(text = "Nome:")
            }
            Column {
                TextField(
                    value = nome,
                    onValueChange = { nome = it }
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
        ){
            Column(
                Modifier
                    .fillMaxWidth(0.3f)
            ){
                Text(text = "Telefone:")
            }
            Column {
                TextField(
                    value = telefone,
                    onValueChange = { telefone = it }
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ){}
        Row(
            Modifier
                .fillMaxWidth(),
            Arrangement.Center
        ){
            Button(onClick = { handleSubmit() }) {
                Text(text = if (updateId.isEmpty()) "Cadastrar" else "Atualizar")
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ){}
        LazyColumn(
            Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(clientes) { cliente ->
                ClienteItem(cliente = cliente, onUpdate = { handleUpdate(cliente) }, onDelete = { handleDelete(cliente) })
            }
        }
    }
}
