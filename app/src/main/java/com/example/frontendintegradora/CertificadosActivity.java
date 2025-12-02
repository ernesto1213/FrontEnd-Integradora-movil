package com.example.frontendintegradora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class CertificadosActivity extends AppCompatActivity {

    private TextView mensaje;
    private LinearLayout listaCursos;
    private Button btnCertificado;
    private String token;
    private int userId;

    private boolean certificadoExiste = false;

    private static final String TAG = "CertificadosActivity";
    private static final String BASE_URL = "https://nuevo-production-e70c.up.railway.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificados);

        mensaje = findViewById(R.id.mensaje);
        listaCursos = findViewById(R.id.listaCursos);
        btnCertificado = findViewById(R.id.btnCertificado);

        // Recuperar token y userId
        token = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getString("token", null);
        userId = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getInt("userId", -1);

        if (token == null || userId == -1) {
            mensaje.setText("⚠️ No has iniciado sesión.");
            btnCertificado.setEnabled(false);
            return;
        }

        cargarEstadoCertificado();
    }

    // ============================================================
    //   🟦 1) Verificar si ya existe un certificado en el backend
    // ============================================================
    private void cargarEstadoCertificado() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/api/certificados/existe/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                InputStream in = conn.getInputStream();
                String jsonText = new Scanner(in).useDelimiter("\\A").next();

                JSONObject obj = new JSONObject(jsonText);
                certificadoExiste = obj.optBoolean("existe", false);

                conn.disconnect();
                in.close();

                runOnUiThread(this::cargarDatos);

            } catch (Exception e) {
                Log.e(TAG, "Error verificando si existe certificado", e);
                runOnUiThread(this::cargarDatos); // Continuar aunque falle
            }
        }).start();
    }

    // ============================================================
    //   🟩 2) Carga de exámenes, resultados y UI
    // ============================================================
    private void cargarDatos() {
        new Thread(() -> {
            try {
                JSONArray examenes = getJsonArray(BASE_URL + "/api/examenes/todos");
                JSONArray resultados = getJsonArray(BASE_URL + "/api/examenes/resultados/alumno/" + userId);

                HashMap<Integer, Double> resultadosMap = new HashMap<>();

                for (int i = 0; i < resultados.length(); i++) {
                    JSONObject r = resultados.getJSONObject(i);
                    int examenId = r.optInt("examenId", -1);
                    double calificacion = r.optDouble("calificacion", 0.0);

                    if (examenId != -1) resultadosMap.put(examenId, calificacion);
                }

                boolean todosRespondidos = true;
                boolean todosAprobados = true;

                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < examenes.length(); i++) {
                    JSONObject examen = examenes.getJSONObject(i);
                    int id = examen.getInt("id");
                    String titulo = examen.optString("titulo", "Examen sin título");
                    String descripcion = examen.optString("descripcion", "");

                    boolean respondido = resultadosMap.containsKey(id);
                    double calificacion = respondido ? resultadosMap.get(id) : 0.0;
                    boolean aprobado = respondido && calificacion >= 8;

                    if (!respondido) todosRespondidos = false;
                    if (!aprobado) todosAprobados = false;

                    builder.append("\n📘 ").append(titulo)
                            .append("\nDescripción: ").append(descripcion)
                            .append("\nRespondido: ").append(respondido ? "✅ Sí" : "❌ No")
                            .append("\nCalificación: ").append(respondido ? calificacion : "—")
                            .append("\nAprobado: ").append(aprobado ? "✅ Sí" : "❌ No")
                            .append("\n---------------------------");
                }

                boolean finalTodosRespondidos = todosRespondidos;
                boolean finalTodosAprobados = todosAprobados;

                runOnUiThread(() -> {
                    listaCursos.removeAllViews();
                    TextView txt = new TextView(this);
                    txt.setText(builder.toString());
                    listaCursos.addView(txt);

                    // Mostrar mensaje según el estado
                    if (certificadoExiste) {
                        mensaje.setText("🎓 Ya tienes un certificado generado. Puedes descargarlo nuevamente.");
                        btnCertificado.setEnabled(true);
                    } else if (finalTodosRespondidos && finalTodosAprobados) {
                        mensaje.setText("🎉 ¡Felicidades! Puedes generar tu certificado.");
                        btnCertificado.setEnabled(true);
                    } else {
                        mensaje.setText("⚠️ Debes aprobar todos los exámenes con calificación minima de 8");
                        btnCertificado.setEnabled(false);
                    }

                    btnCertificado.setOnClickListener(v -> generarODescargar());
                });

            } catch (Exception e) {
                Log.e(TAG, "Error al cargar datos", e);
            }
        }).start();
    }

    private JSONArray getJsonArray(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");
        conn.connect();

        InputStream in = new BufferedInputStream(conn.getInputStream());
        String jsonText = new Scanner(in).useDelimiter("\\A").next();
        in.close();
        conn.disconnect();
        return new JSONArray(jsonText);
    }

    // ============================================================
    //   🟨 3) Lógica FINAL → si existe: descargar, si no: generar + descargar
    // ============================================================
    private void generarODescargar() {
        if (certificadoExiste) {
            descargarCertificado();
        } else {
            generarCertificado();
        }
    }

    // ============================================================
    //   🟧 GENERAR certificado una sola vez
    // ============================================================
    private void generarCertificado() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/api/certificados/generar");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"alumnoId\":" + userId + ",\"titulo\":\"Certificación de Capacitación\",\"emitidoPor\":\"Centro de Capacitación Naval\",\"guardarRegistro\":true}";

                conn.getOutputStream().write(json.getBytes());
                conn.getOutputStream().flush();
                conn.getOutputStream().close();

                if (conn.getResponseCode() != 200 && conn.getResponseCode() != 201) {
                    throw new Exception("Error generando certificado");
                }

                certificadoExiste = true;

                descargarCertificado();

            } catch (Exception e) {
                Log.e(TAG, "Error generando certificado", e);
            }
        }).start();
    }

    // ============================================================
    //   🟦 DESCARGAR certificado a Downloads
    // ============================================================
    private void descargarCertificado() {
        new Thread(() -> {
            try {
                URL urlDescarga = new URL(BASE_URL + "/api/certificados/descargar/" + userId);
                HttpURLConnection conn = (HttpURLConnection) urlDescarga.openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);

                InputStream input = new BufferedInputStream(conn.getInputStream());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int nRead;

                while ((nRead = input.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                buffer.flush();
                byte[] pdfBytes = buffer.toByteArray();

                savePdfToDownloads(pdfBytes, "certificado_" + userId + ".pdf");

            } catch (Exception e) {
                Log.e(TAG, "Error al descargar certificado", e);
            }
        }).start();
    }

    // ============================================================
    //   🟩 Guardar PDF en carpeta Downloads (Android 10+)
    // ============================================================
    private void savePdfToDownloads(byte[] pdfBytes, String fileName) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/");

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                OutputStream out = getContentResolver().openOutputStream(uri);
                out.write(pdfBytes);
                out.close();

                runOnUiThread(() ->
                        Toast.makeText(this, "📥 Certificado guardado en Descargas", Toast.LENGTH_LONG).show()
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Error guardando PDF", e);
        }
    }

    public void volverAlMenu(View view) {
        finish();
    }
}
