package com.example.projeto4bim

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val fileUrl = intent.getStringExtra("fileUrl")
        val webView = findViewById<WebView>(R.id.webView)
        val downloadButton = findViewById<Button>(R.id.downloadButton)

        // Configuração da WebView
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(fileUrl.toString())

        // Configura o botão de download
        downloadButton.setOnClickListener {
            if (fileUrl != null) {
                downloadFile(fileUrl)
            }
        }
    }

    // Função para baixar o arquivo
    private fun downloadFile(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle("Baixando arquivo")
        request.setDescription("O arquivo está sendo baixado...")

        // Configura o destino do arquivo para o diretório de downloads
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Uri.parse(url).lastPathSegment)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Inicia o download
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}