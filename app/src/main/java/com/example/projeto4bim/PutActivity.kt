package com.example.projeto4bim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class PutActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_account)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        // Recebe o email e a senha antiga do intent
        val emailUsuario = intent.getStringExtra("emailUsuario")
        val senhaUsuario = intent.getStringExtra("senhaUsuario")

        if (currentUser == null || emailUsuario == null || senhaUsuario == null) {
            // Verifica se o usuário está autenticado e se as informações necessárias foram recebidas
            Toast.makeText(this, "Erro: Falta de informações ou usuário não autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val passwordEditText = findViewById<EditText>(R.id.updatePasswordEditText)
        val updateButton = findViewById<Button>(R.id.updateButton)
        val updateStatusTextView = findViewById<TextView>(R.id.updateStatusTextView)

        updateButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString().trim()

            if (newPassword.isNotEmpty()) {
                // Verifica se a nova senha é diferente da senha antiga
                if (newPassword == senhaUsuario) {
                    updateStatusTextView.text = "A nova senha deve ser diferente da antiga."
                    updateStatusTextView.visibility = TextView.VISIBLE
                    return@setOnClickListener
                }

                // Reautentica o usuário antes de atualizar a senha
                val credential = EmailAuthProvider.getCredential(emailUsuario, senhaUsuario)

                currentUser?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Se a reautenticação for bem-sucedida, atualiza a senha
                        currentUser?.updatePassword(newPassword)?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Senha atualizada com sucesso", Toast.LENGTH_SHORT).show()

                                // Redireciona para a UploadActivity após o sucesso da atualização
                                val intent = Intent(this, UploadActivity::class.java)
                                startActivity(intent)
                                finish() // Finaliza a PutActivity
                            } else {
                                Toast.makeText(this, "Erro ao atualizar senha", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Falha na reautenticação", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Caso a senha não seja preenchida
                updateStatusTextView.text = "Preencha a nova senha para atualizar"
                updateStatusTextView.visibility = TextView.VISIBLE
            }
        }
    }
}