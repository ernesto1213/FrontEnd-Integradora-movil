package com.example.frontendintegradora;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.graphics.drawable.GradientDrawable;   // ⭐ ESTE ES EL BUENO
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;

public class CrearExamenesActivity extends AppCompatActivity {

    private EditText txtTitulo, txtDescripcion;
    private LinearLayout contenedorPreguntas;
    private Button btnAgregarPregunta, btnCrearExamen;
    private TextView txtMensaje;
    private List<PreguntaUI> listaPreguntas = new ArrayList<>();

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_examenes);

        userId = getSharedPreferences("APP_PREFS", MODE_PRIVATE).getInt("userId", -1);

        txtTitulo = findViewById(R.id.txtTitulo);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        contenedorPreguntas = findViewById(R.id.contenedorPreguntas);
        btnAgregarPregunta = findViewById(R.id.btnAgregarPregunta);
        btnCrearExamen = findViewById(R.id.btnCrearExamen);
        txtMensaje = findViewById(R.id.txtMensaje);

        // ⭐ Estilos globales
        txtTitulo.setTextColor(0xFF000000);
        txtDescripcion.setTextColor(0xFF000000);
        txtTitulo.setHintTextColor(0xFF555555);
        txtDescripcion.setHintTextColor(0xFF555555);

        btnAgregarPregunta.setBackgroundColor(0xFF1E88E5);
        btnAgregarPregunta.setTextColor(0xFFFFFFFF);

        btnCrearExamen.setBackgroundColor(0xFF43A047);
        btnCrearExamen.setTextColor(0xFFFFFFFF);

        btnAgregarPregunta.setOnClickListener(v -> agregarPregunta());
        btnCrearExamen.setOnClickListener(v -> crearExamen());
    }

    // ==============================
    //  CREAR UNA PREGUNTA DINÁMICA
    // ==============================
    private void agregarPregunta() {

        // ------------ TARJETA DE PREGUNTA ----------------
        LinearLayout layoutPregunta = new LinearLayout(this);
        layoutPregunta.setOrientation(LinearLayout.VERTICAL);
        layoutPregunta.setPadding(32, 32, 32, 32);
        layoutPregunta.setElevation(10);

        GradientDrawable fondoPregunta = new GradientDrawable();
        fondoPregunta.setColor(0xFFE8F0FE); // azul grisáceo suave
        fondoPregunta.setCornerRadius(40);  // ⭐ ESQUINAS REDONDEADAS
        layoutPregunta.setBackground(fondoPregunta);

        // ------------ CAMPO DE TEXTO ----------------
        EditText txtPregunta = new EditText(this);
        txtPregunta.setHint("Texto de la pregunta");
        txtPregunta.setPadding(24, 24, 24, 24);
        txtPregunta.setTextSize(16);

        GradientDrawable fondoInput = new GradientDrawable();
        fondoInput.setColor(0xFFFFFFFF);
        fondoInput.setCornerRadius(35);   // ⭐ redondeado
        fondoInput.setStroke(3, 0xFFB0BEC5); // borde gris suave
        txtPregunta.setBackground(fondoInput);

        // ------------ LABEL ----------------
        TextView tipoLabel = new TextView(this);
        tipoLabel.setText("Tipo: Opción múltiple");
        tipoLabel.setPadding(0, 16, 0, 16);
        tipoLabel.setTextSize(15);

        // ------------ CONTENEDOR OPCIONES ----------------
        LinearLayout layoutOpciones = new LinearLayout(this);
        layoutOpciones.setOrientation(LinearLayout.VERTICAL);
        layoutOpciones.setPadding(20, 20, 20, 20);

        GradientDrawable fondoOpciones = new GradientDrawable();
        fondoOpciones.setColor(0xFFF4F6F8);
        fondoOpciones.setCornerRadius(30);   // ⭐ redondeado
        layoutOpciones.setBackground(fondoOpciones);

        // ------------ BOTÓN AGREGAR OPCIÓN ----------------
        Button btnAgregarOpcion = new Button(this);
        btnAgregarOpcion.setText("➕ Agregar opción");

        GradientDrawable fondoAgregar = new GradientDrawable();
        fondoAgregar.setColor(0xFF4CAF50);
        fondoAgregar.setCornerRadius(35);   // ⭐ redondeado
        btnAgregarOpcion.setBackground(fondoAgregar);
        btnAgregarOpcion.setTextColor(0xFFFFFFFF);

        // ------------ BOTÓN ELIMINAR ----------------
        Button btnEliminarPregunta = new Button(this);
        btnEliminarPregunta.setText("❌ Eliminar pregunta");

        GradientDrawable fondoEliminar = new GradientDrawable();
        fondoEliminar.setColor(0xFFFF5252);
        fondoEliminar.setCornerRadius(35);  // ⭐ redondeado
        btnEliminarPregunta.setBackground(fondoEliminar);
        btnEliminarPregunta.setTextColor(0xFFFFFFFF);

        // ------------ CREAR OBJETO UI ----------------
        PreguntaUI preguntaUI = new PreguntaUI(layoutPregunta, txtPregunta, layoutOpciones);
        listaPreguntas.add(preguntaUI);

        btnAgregarOpcion.setOnClickListener(v -> agregarOpcion(preguntaUI));

        btnEliminarPregunta.setOnClickListener(v -> {
            listaPreguntas.remove(preguntaUI);
            contenedorPreguntas.removeView(layoutPregunta);
        });

        // ------------ ORDEN ----------------
        layoutPregunta.addView(txtPregunta);
        layoutPregunta.addView(tipoLabel);
        layoutPregunta.addView(layoutOpciones);

        Space space = new Space(this);
        space.setMinimumHeight(20);

        layoutPregunta.addView(space);
        layoutPregunta.addView(btnAgregarOpcion);
        layoutPregunta.addView(btnEliminarPregunta);

        contenedorPreguntas.addView(layoutPregunta);
    }


    // ==============================
    //  AGREGAR OPCIÓN A UNA PREGUNTA
    // ==============================
    private void agregarOpcion(PreguntaUI preguntaUI) {

        LinearLayout opcionLayout = new LinearLayout(this);
        opcionLayout.setOrientation(LinearLayout.HORIZONTAL);
        opcionLayout.setPadding(16, 16, 16, 16);

        GradientDrawable fondoOpcion = new GradientDrawable();
        fondoOpcion.setColor(0xFFFFFFFF);
        fondoOpcion.setCornerRadius(25);
        fondoOpcion.setStroke(2, 0xFFC5C8CF);
        opcionLayout.setBackground(fondoOpcion);

        EditText txtOpcion = new EditText(this);
        txtOpcion.setHint("Texto de la opción");
        txtOpcion.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        CheckBox chkCorrecta = new CheckBox(this);
        chkCorrecta.setText("Correcta");

        Button btnEliminar = new Button(this);
        btnEliminar.setText("🗑️");

        GradientDrawable fondoEliminar = new GradientDrawable();
        fondoEliminar.setColor(0xFFFF5252);
        fondoEliminar.setCornerRadius(50);
        btnEliminar.setBackground(fondoEliminar);
        btnEliminar.setTextColor(0xFFFFFFFF);

        OpcionUI nueva = new OpcionUI(opcionLayout, txtOpcion, chkCorrecta);
        preguntaUI.opciones.add(nueva);

        btnEliminar.setOnClickListener(v -> {
            preguntaUI.opciones.remove(nueva);
            preguntaUI.layoutOpciones.removeView(opcionLayout);
        });

        opcionLayout.addView(txtOpcion);
        opcionLayout.addView(chkCorrecta);
        opcionLayout.addView(btnEliminar);

        preguntaUI.layoutOpciones.addView(opcionLayout);
    }


    // ==============================
    //   CREAR JSON Y ENVIAR EXAMEN
    // ==============================
    private void crearExamen() {
        String titulo = txtTitulo.getText().toString().trim();
        String descripcion = txtDescripcion.getText().toString().trim();

        txtMensaje.setTextColor(0xFFD32F2F);

        if (titulo.isEmpty() || descripcion.isEmpty()) {
            txtMensaje.setText("⚠️ Todos los campos son obligatorios.");
            return;
        }

        JSONArray preguntasArray = new JSONArray();

        for (PreguntaUI preguntaUI : listaPreguntas) {

            JSONArray opcionesArray = new JSONArray();

            for (OpcionUI opcion : preguntaUI.opciones) {
                try {
                    JSONObject opObj = new JSONObject();
                    opObj.put("texto", opcion.txtOpcion.getText().toString());
                    opObj.put("correcta", opcion.chkCorrecta.isChecked());
                    opcionesArray.put(opObj);
                } catch (Exception ignored) {}
            }

            try {
                JSONObject pObj = new JSONObject();
                pObj.put("texto", preguntaUI.txtPregunta.getText().toString());
                pObj.put("tipo", "multiple-choice");
                pObj.put("opciones", opcionesArray);

                preguntasArray.put(pObj);
            } catch (Exception ignored) {}
        }

        JSONObject examenData = new JSONObject();
        try {
            examenData.put("titulo", titulo);
            examenData.put("descripcion", descripcion);

            JSONObject instructorObj = new JSONObject();
            instructorObj.put("id", userId);

            examenData.put("instructor", instructorObj);
            examenData.put("preguntas", preguntasArray);

        } catch (Exception ignored) {}

        enviarExamen(examenData);
    }

    // ==============================
    //     PETICIÓN HTTP
    // ==============================
    private void enviarExamen(JSONObject examenData) {

        new Thread(() -> {
            try {
                URL url = new URL("https://nuevo-production-e70c.up.railway.app/api/examenes/crear");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(examenData.toString().getBytes());
                }

                int code = conn.getResponseCode();
                String response = leerRespuesta(
                        code == 200 || code == 201 ? conn.getInputStream() : conn.getErrorStream()
                );

                runOnUiThread(() -> {
                    if (code == 200 || code == 201) {
                        txtMensaje.setTextColor(0xFF2E7D32);

                        try {
                            JSONObject obj = new JSONObject(response);
                            txtMensaje.setText("✅ Examen creado con ID " + obj.optInt("id", -1));
                        } catch (Exception e) {
                            txtMensaje.setText("❌ Error en la respuesta del servidor.");
                        }

                        contenedorPreguntas.removeAllViews();
                        listaPreguntas.clear();

                    } else {
                        txtMensaje.setText("❌ Error: " + response);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtMensaje.setTextColor(0xFFD32F2F);
                    txtMensaje.setText("⚠️ Error de conexión.");
                });
            }

        }).start();
    }

    private String leerRespuesta(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String linea;
        while ((linea = br.readLine()) != null) sb.append(linea);
        return sb.toString();
    }

    // ==============================
    //   CLASES PARA MANTENER ORDEN
    // ==============================
    static class PreguntaUI {
        LinearLayout layout;
        EditText txtPregunta;
        LinearLayout layoutOpciones;
        List<OpcionUI> opciones = new ArrayList<>();

        PreguntaUI(LinearLayout layout, EditText txtPregunta, LinearLayout layoutOpciones) {
            this.layout = layout;
            this.txtPregunta = txtPregunta;
            this.layoutOpciones = layoutOpciones;
        }
    }

    static class OpcionUI {
        LinearLayout layout;
        EditText txtOpcion;
        CheckBox chkCorrecta;

        OpcionUI(LinearLayout layout, EditText txtOpcion, CheckBox chkCorrecta) {
            this.layout = layout;
            this.txtOpcion = txtOpcion;
            this.chkCorrecta = chkCorrecta;
        }
    }
}
