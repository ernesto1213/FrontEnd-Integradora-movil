package com.example.frontendintegradora;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.drawable.GradientDrawable;   // ⭐ ESTE ES EL BUENO

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ExamenDetalleActivity extends AppCompatActivity {

    private LinearLayout preguntasLayout;
    private TextView mensaje;
    private Button btnEnviarExamen;

    private String token;
    private int userId;
    private int examenId;

    private JSONArray preguntasCargadas;
    private final List<View> vistasDeRespuestas = new ArrayList<>();

    // 🔥 URL base de producción
    private final String BASE_URL = "https://nuevo-production-e70c.up.railway.app/api";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examen_detalle);

        preguntasLayout = findViewById(R.id.preguntasLayout);
        mensaje = findViewById(R.id.txtMensaje);
        btnEnviarExamen = findViewById(R.id.btnEnviarExamen);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        token = prefs.getString("token", null);
        userId = prefs.getInt("userId", -1);
        examenId = getIntent().getIntExtra("examenId", -1);

        if (token == null || userId == -1 || examenId == -1) {
            mensaje.setText("⚠️ No se puede cargar el examen. Falta información.");
            btnEnviarExamen.setEnabled(false);
            return;
        }

        new CargarPreguntasTask().execute();
        btnEnviarExamen.setOnClickListener(v -> new EnviarYEvaluarTask().execute());
    }

    // ================================
    //   1) Cargar preguntas
    // ================================
    private class CargarPreguntasTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(BASE_URL + "/examenes/" + examenId + "/preguntas");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                return sb.toString();

            } catch (Exception e) {
                Log.e("EXAMEN_ERROR", "Error cargando preguntas", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                mensaje.setText("❌ No se pudieron cargar las preguntas.");
                return;
            }

            try {
                if (response.trim().startsWith("[")) {
                    preguntasCargadas = new JSONArray(response);
                } else {
                    preguntasCargadas = new JSONObject(response).optJSONArray("preguntas");
                }

                mostrarPreguntas(preguntasCargadas);

            } catch (Exception e) {
                mensaje.setText("❌ Error procesando preguntas del examen.");
                Log.e("EXAMEN_ERROR", "json", e);
            }
        }
    }

    // ================================
    //   2) Mostrar preguntas dinámicas
    // ================================
    private void mostrarPreguntas(JSONArray preguntas) throws Exception {
        preguntasLayout.removeAllViews();
        vistasDeRespuestas.clear();

        if (preguntas == null || preguntas.length() == 0) {
            mensaje.setText("No hay preguntas disponibles.");
            return;
        }

        mensaje.setText("📘 Responde el examen:");

        for (int i = 0; i < preguntas.length(); i++) {
            JSONObject p = preguntas.getJSONObject(i);

            // ============================
            //      TARJETA CONTENEDORA
            // ============================
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(30, 30, 30, 30);

            // Fondo redondeado gris suave
            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(0xFFFFFFFF);          // Blanco
            fondo.setCornerRadius(35);           // Esquinas redondeadas
            fondo.setStroke(3, 0xFFCFD8DC);      // Borde gris suave
            card.setBackground(fondo);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 35);
            card.setLayoutParams(params);

            // ============================
            //      TEXTO DE LA PREGUNTA
            // ============================
            TextView txt = new TextView(this);
            txt.setText((i + 1) + ". " + p.getString("texto"));
            txt.setTextSize(18);
            txt.setPadding(0, 0, 0, 12);
            txt.setTextColor(0xFF1A237E);  // Azul oscuro elegante
            txt.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(txt);

            // ============================
            //      RESPUESTA
            // ============================
            String tipo = p.optString("tipo", "texto");

            if (tipo.toLowerCase().contains("multiple")) {

                RadioGroup group = new RadioGroup(this);

                JSONArray opciones = p.optJSONArray("opciones");
                if (opciones != null) {
                    for (int j = 0; j < opciones.length(); j++) {
                        JSONObject op = opciones.getJSONObject(j);

                        RadioButton rb = new RadioButton(this);
                        rb.setText(op.getString("texto"));
                        rb.setTag(op.getInt("id"));
                        rb.setTextColor(0xFF37474F);  // Gris elegante fuerte
                        rb.setTextSize(16);

                        group.addView(rb);
                    }
                }

                card.addView(group);
                vistasDeRespuestas.add(group);

            } else {
                EditText edit = new EditText(this);
                edit.setHint("Escribe tu respuesta...");
                edit.setTextColor(0xFF263238);        // Texto
                edit.setHintTextColor(0xFF90A4AE);    // Hint gris claro
                edit.setPadding(20, 20, 20, 20);
                edit.setBackground(null);             // Quitamos el feo underline

                // Fondo redondeado para edittext
                GradientDrawable fondoEdit = new GradientDrawable();
                fondoEdit.setColor(0xFFF7F9FA);
                fondoEdit.setCornerRadius(25);
                fondoEdit.setStroke(2, 0xFFCFD8DC);
                edit.setBackground(fondoEdit);

                card.addView(edit);
                vistasDeRespuestas.add(edit);
            }

            // Añadimos la tarjeta completa al layout general
            preguntasLayout.addView(card);
        }
    }


    // ================================
    //   3) Enviar respuestas + Evaluar
    // ================================
    private class EnviarYEvaluarTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... v) {
            try {
                JSONArray respuestasArray = new JSONArray();

                for (int i = 0; i < vistasDeRespuestas.size(); i++) {
                    View vista = vistasDeRespuestas.get(i);
                    JSONObject pregunta = preguntasCargadas.getJSONObject(i);

                    JSONObject jsonRespuesta = new JSONObject();
                    jsonRespuesta.put("pregunta", new JSONObject().put("id", pregunta.getInt("id")));

                    if (vista instanceof RadioGroup) {
                        RadioGroup g = (RadioGroup) vista;
                        int sel = g.getCheckedRadioButtonId();

                        if (sel != -1) {
                            RadioButton rb = g.findViewById(sel);
                            jsonRespuesta.put("opcion",
                                    new JSONObject().put("id", (int) rb.getTag()));
                            jsonRespuesta.put("respuestaTexto", JSONObject.NULL);
                        }

                    } else if (vista instanceof EditText) {
                        EditText ed = (EditText) vista;
                        jsonRespuesta.put("opcion", JSONObject.NULL);
                        jsonRespuesta.put("respuestaTexto", ed.getText().toString());
                    }

                    respuestasArray.put(jsonRespuesta);
                }

                // 1) Guardar respuestas (POST)
                URL urlGuardar = new URL(BASE_URL + "/examenes/" + examenId + "/responder/" + userId);
                HttpURLConnection connG = (HttpURLConnection) urlGuardar.openConnection();
                connG.setRequestMethod("POST");
                connG.setDoOutput(true);
                connG.setRequestProperty("Authorization", "Bearer " + token);
                connG.setRequestProperty("Content-Type", "application/json");
                connG.getOutputStream().write(respuestasArray.toString().getBytes());
                connG.getOutputStream().close();

                Log.d("EXAMEN_DEBUG", "Guardar respuestas HTTP: " + connG.getResponseCode());

                // 2) Evaluar (GET)
                URL urlEva = new URL(BASE_URL + "/examenes/" + examenId + "/evaluar/" + userId);
                HttpURLConnection connE = (HttpURLConnection) urlEva.openConnection();
                connE.setRequestMethod("GET");
                connE.setRequestProperty("Authorization", "Bearer " + token);

                BufferedReader br = new BufferedReader(new InputStreamReader(connE.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) sb.append(line);

                return sb.toString();

            } catch (Exception e) {
                Log.e("EXAMEN_ERROR", "Error enviando examen", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String res) {
            if (res == null) {
                Toast.makeText(ExamenDetalleActivity.this, "❌ Error enviando o evaluando.", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject json = new JSONObject(res);
                double cal = json.optDouble("calificacion", -1);

                mensaje.setText("🎯 Calificación final: " + cal + "/100");

                if (cal >= 8) {
                    Toast.makeText(ExamenDetalleActivity.this, "✅ ¡Aprobado!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ExamenDetalleActivity.this, "❌ Reprobado.", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                mensaje.setText("Examen enviado, pero error procesando calificación.");
            }
        }
    }
}
