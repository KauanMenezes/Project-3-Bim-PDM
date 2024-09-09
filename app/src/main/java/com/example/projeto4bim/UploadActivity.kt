package com.example.projeto4bim

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage

// Classe de dados para armazenar o nome e a URL
data class ImageItem(val name: String, val url: String)

class UploadActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private val imageItems = mutableListOf<ImageItem>() // Lista de nomes e URLs
    private lateinit var btn_logout: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val emailUsuario = intent.getStringExtra("emailUsuario")
        val senhaUsuario = intent.getStringExtra("senhaUsuario")
        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        val listView = findViewById<ListView>(R.id.imagesListView)
        val putAccount = findViewById<Button>(R.id.putAccount)
        btn_logout = findViewById<Button>(R.id.btn_logout)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imageUri = result.data!!.data!!
            }
        }

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf", "application/msword"))
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            resultLauncher.launch(intent)
        }

        // Botão para fazer upload da imagem selecionada
        uploadImageButton.setOnClickListener {
            if (::imageUri.isInitialized) {
                val storageReference = FirebaseStorage.getInstance().reference
                    .child("files/${System.currentTimeMillis()}.${getMimeType(imageUri)}")
                storageReference.putFile(imageUri)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Upload bem-sucedido", Toast.LENGTH_SHORT).show()
                        // Atualiza a lista após o upload
                        fetchFiles(listView)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Falha no upload", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Selecione um arquivo primeiro", Toast.LENGTH_SHORT).show()
            }
        }

        // Carrega os arquivos existentes no Firebase Storage e exibe no ListView
        fetchFiles(listView)

        // Ação ao clicar em um item do ListView
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFile = imageItems[position]
            val intent = Intent(this, ImageViewerActivity::class.java)
            intent.putExtra("fileUrl", selectedFile.url)
            startActivity(intent)
        }

        putAccount.setOnClickListener {
            val intent = Intent(this, PutActivity::class.java)
            intent.putExtra("emailUsuario", emailUsuario)
            intent.putExtra("senhaUsuario", senhaUsuario)
            startActivity(intent)
        }
        btn_logout.setOnClickListener {
            logout()
        }
    }

    // Método para buscar e exibir os arquivos do Firebase Storage
    private fun fetchFiles(listView: ListView) {
        val storageReference = FirebaseStorage.getInstance().reference.child("files/")
        storageReference.listAll().addOnSuccessListener { listResult ->
            imageItems.clear() // Limpa a lista antes de adicionar novos itens
            listResult.items.forEach { item ->
                val fileName = item.name
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageItems.add(ImageItem(fileName, uri.toString()))
                    // Atualiza o ListView com os nomes dos arquivos
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, imageItems.map { it.name })
                    listView.adapter = adapter
                }
            }
        }
    }

    // Método para obter o tipo de mídia do arquivo
    private fun getMimeType(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        return mimeType?.split("/")?.last() ?: ""
    }

    private fun logout() {
        Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}