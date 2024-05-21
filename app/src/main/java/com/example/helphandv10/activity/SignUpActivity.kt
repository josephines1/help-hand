package com.example.helphandv10.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.helphandv10.R
import com.example.helphandv10.model.Users
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var tv_alreadyRegistered : TextView

    private val ALGORITHM = "PBKDF2WithHmacSHA512"
    private val ITERATIONS = 120_000
    private val KEY_LENGTH = 256
    private val SECRET = "SomeRandomSecret"

    fun generateRandomSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun generateHash(password: String, salt: String): String {
        val combinedSalt = "$salt$SECRET".toByteArray()
        val factory: SecretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), combinedSalt, ITERATIONS, KEY_LENGTH)
        val key: SecretKey = factory.generateSecret(spec)
        val hash: ByteArray = key.encoded
        return hash.toHexString()
    }

    private fun initComponents() {
        tv_alreadyRegistered = findViewById(R.id.tv_alreadyRegistered)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        initComponents()
        auth = Firebase.auth

        val signUpUsername = findViewById<EditText>(R.id.et_signUpUsername)
        val signUpEmail = findViewById<EditText>(R.id.et_signUpEmail)
        val signUpPass = findViewById<EditText>(R.id.et_signUpPassword)
        val signUpPassConf = findViewById<EditText>(R.id.et_signUpPasswordConf)
        val signUpBtn = findViewById<Button>(R.id.btn_signUp)

        signUpBtn.setOnClickListener{
            val email = signUpEmail.text.toString()
            if (email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val password = signUpPass.text.toString()
            if (password.isEmpty() && password.length < 8) {
                Toast.makeText(this, "Password tidak boleh kurang dari 8 dan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val confPassword = signUpPassConf.text.toString()
            if(password != confPassword) {
                Toast.makeText(this, "Konfirmasi password tidak sesuai", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {

                    val firebaseUser = auth.currentUser

                    val uid = firebaseUser?.uid

                    if (uid != null) {
                        val randomSalt = generateRandomSalt()
                        val randomSaltString = randomSalt.toHexString()
                        val hashedPassword = generateHash(password, randomSaltString)

                        // Buat objek User dengan informasi pengguna
                        val user = Users(
                            username = signUpUsername.text.toString(),
                            email = signUpEmail.text.toString(),
                            password = hashedPassword,
                            phoneNumber = "-",
                            photoProfileURL = "-"
                        )

                        val firestore = Firebase.firestore

                        // Simpan data pengguna ke Firestore dengan menggunakan uid sebagai ID dokumen
                        firestore.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                // Arahkan menuju login
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.putExtra("message", "Akun Anda berhasil dibuat! Silahkan login dengan akun baru Anda.")
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("UsersRepository: Insert", e.toString())
                            }
                    }
                }
        }

        tv_alreadyRegistered.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}