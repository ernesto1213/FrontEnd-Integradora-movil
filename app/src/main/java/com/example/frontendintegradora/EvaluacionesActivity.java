package com.example.frontendintegradora;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;
import android.view.LayoutInflater;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class EvaluacionesActivity extends AppCompatActivity {

    private TextView mensaje;
    private LinearLayout lista;
    private String token;
    private int userId;

    // 🔹 Guardar exámenes respondidos (examenId → calificación)
    private final HashMap<Integer, Double> examenesRespondidos = new HashMap<>();

    // 🌐 URL BASE DE PRODUCCIÓN
    private static final String BASE_URL = "https://nuevo-production-e70c.up.railway.app/api/examenes/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluaciones);

        mensaje = findViewById(R.id.txtMensaje);
        lista = findViewById(R.id.listaExamenes);

        token = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("token", null);
        userId = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getInt("userId", -1);

        if (token == null || userId == -1) {
            mensaje.setText("⚠️ Debes iniciar sesión para continuar.");
            Toast.makeText(this, "Inicia sesión primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Primero cargar los resultados del alumno
        cargarResultadosAlumno();
    }

    private void cargarResultadosAlumno() {
        new Thread(() -> {
            try {
                URL urlResultados = new URL(BASE_URL + "resultados/alumno/" + userId);
                HttpURLConnection conn = (HttpURLConnection) urlResultados.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    throw new Exception("Respuesta HTTP " + responseCode);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                conn.disconnect();

                JSONArray resultados = new JSONArray(sb.toString());

                for (int i = 0; i < resultados.length(); i++) {
                    JSONObject resultado = resultados.getJSONObject(i);

                    JSONObject examen = resultado.optJSONObject("examen");
                    int examenId = examen != null ? examen.optInt("id", -1)
                            : resultado.optInt("examenId", -1);

                    double calificacion = resultado.optDouble("calificacion", -1);

                    if (examenId > 0) {
                        examenesRespondidos.put(examenId, calificacion);
                    }
                }

                cargarExamenes();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "No se pudieron cargar los resultados.", Toast.LENGTH_SHORT).show();
                    cargarExamenes();
                });
            }
        }).start();
    }

    private void cargarExamenes() {
        new Thread(() -> {
            try {
                URL urlExamenes = new URL(BASE_URL + "todos");
                HttpURLConnection conn = (HttpURLConnection) urlExamenes.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    throw new Exception("Respuesta HTTP " + responseCode);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();
                conn.disconnect();

                JSONArray examenes = new JSONArray(sb.toString());

                runOnUiThread(() -> mostrarExamenes(examenes));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> mensaje.setText("❌ No se pudieron cargar los exámenes."));
            }
        }).start();
    }

    private void mostrarExamenes(JSONArray examenes) {
        lista.removeAllViews();

        if (examenes.length() == 0) {
            mensaje.setText("No hay exámenes disponibles.");
            return;
        }

        mensaje.setText("📘 Exámenes disponibles:");
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < examenes.length(); i++) {
            try {
                JSONObject examen = examenes.getJSONObject(i);
                View card = inflater.inflate(R.layout.item_examen, lista, false);

                TextView titulo = card.findViewById(R.id.txtTitulo);
                TextView descripcion = card.findViewById(R.id.txtDescripcion);
                Button btnVer = card.findViewById(R.id.btnVerExamen);

                int examenId = examen.optInt("id", -1);
                titulo.setText(examen.optString("titulo", "Examen sin título"));
                descripcion.setText(examen.optString("descripcion", "Sin descripción disponible"));

                if (examenesRespondidos.containsKey(examenId)) {
                    double calificacion = examenesRespondidos.get(examenId);
                    btnVer.setEnabled(false);
                    btnVer.setText("✅ Respondido — " + calificacion + "/10");
                    btnVer.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                } else {
                    btnVer.setOnClickListener(v -> {
                        Intent intent = new Intent(EvaluacionesActivity.this, ExamenDetalleActivity.class);
                        intent.putExtra("examenId", examenId);
                        startActivity(intent);
                    });
                }

                lista.addView(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
