package com.example.frontendintegradora

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ LEER DE APP_PREFS (donde guardaste el login)
        prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)

        val nombreUsuario = prefs.getString("userName", "Usuario")
        val rango = prefs.getString("rango", "Estudiante")  // ← ya lo va a extraer bien

        val tvSaludo = findViewById<TextView>(R.id.tvSaludo)
        tvSaludo.text = "Bienvenido a Mantenimiento Naval ⚓ "

        val btnCursos = findViewById<LinearLayout>(R.id.btnCursos)
        val btnSimulador = findViewById<LinearLayout>(R.id.btnSimulador)
        val btnEvaluaciones = findViewById<LinearLayout>(R.id.btnEvaluaciones)
        val btnCertificados = findViewById<LinearLayout>(R.id.btnCertificados)
        val btnCrearExamenes = findViewById<LinearLayout>(R.id.btnCrearExamenes)
        val btnGestionUsuarios = findViewById<LinearLayout>(R.id.btnGestionUsuarios)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // ================= VALIDACIÓN POR RANGO =================
        if (rango == "Admin") {

            btnCursos.setOnClickListener {
                startActivity(Intent(this, CursosActivity::class.java))
            }
            btnSimulador.setOnClickListener {
                startActivity(Intent(this, SimuladorActivity::class.java))
            }
            btnEvaluaciones.setOnClickListener {
                startActivity(Intent(this, EvaluacionesActivity::class.java))
            }
            btnCertificados.setOnClickListener {
                startActivity(Intent(this, CertificadosActivity::class.java))
            }
            btnCrearExamenes.setOnClickListener {
                startActivity(Intent(this, CrearExamenesActivity::class.java))
            }
            btnGestionUsuarios.setOnClickListener {
                startActivity(Intent(this, GestionUsuariosActivity::class.java))
            }

        } else {

            btnCursos.setOnClickListener {
                startActivity(Intent(this, CursosActivity::class.java))
            }
            btnSimulador.setOnClickListener {
                startActivity(Intent(this, SimuladorActivity::class.java))
            }
            btnEvaluaciones.setOnClickListener {
                startActivity(Intent(this, EvaluacionesActivity::class.java))
            }
            btnCertificados.setOnClickListener {
                startActivity(Intent(this, CertificadosActivity::class.java))
            }

            // 🔥 OCULTAR BOTONES QUE NO PUEDE USAR
            btnCrearExamenes.visibility = View.GONE
            btnGestionUsuarios.visibility = View.GONE
        }

        // ========== CERRAR SESIÓN ==========
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
