package com.example.frontendintegradora;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.*;
import android.os.Build;  // ←🔥 ESTE IMPORT ES OBLIGATORIO


public class GestionUsuariosActivity extends AppCompatActivity {

    // 🔥 PRODUCCIÓN: tu backend en Render
    private static final String BASE_URL_PROD = "https://nuevo-production-e70c.up.railway.app";

    // 🔥 LOCAL: cuando usas el emulador Android Studio
    private static final String BASE_URL_LOCAL = "http://10.0.2.2:8080";

    // 🔥 Auto–switch: ¿estamos en emulador?
    private static String getBaseUrl() {
        return Build.FINGERPRINT.contains("generic") ? BASE_URL_LOCAL : BASE_URL_PROD;
    }

    private static final String API_SIN_RANGO = getBaseUrl() + "/api/login/sin-rango";
    private static final String API_USER_PUT = getBaseUrl() + "/api/login/";

    private TextView status, error;
    private ProgressBar loading;
    private RecyclerView recyclerView;
    private UsuariosAdapter adapter;
    private ArrayList<Usuario> listaUsuarios = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        status = findViewById(R.id.status);
        error = findViewById(R.id.error);
        loading = findViewById(R.id.loading);
        recyclerView = findViewById(R.id.recyclerUsuarios);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuariosAdapter(listaUsuarios, this::actualizarUsuario);
        recyclerView.setAdapter(adapter);

        status.setText("Gestion De Usuarios");
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        loading.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);

        Request request = new Request.Builder().url(API_SIN_RANGO).build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    error.setVisibility(View.VISIBLE);
                    error.setText("❌ Error de conexión con el servidor");
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                        error.setText("❌ Error al obtener usuarios");
                    });
                    return;
                }

                try {
                    String json = response.body().string();
                    JSONArray array = new JSONArray(json);
                    listaUsuarios.clear();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Usuario u = new Usuario(
                                obj.getInt("id"),
                                obj.optString("name", ""),
                                obj.optString("email", ""),
                                obj.optString("password", ""),
                                obj.isNull("rango") ? "Sin rango" : obj.getString("rango")
                        );
                        listaUsuarios.add(u);
                    }

                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                        error.setText("⚠️ Error procesando los datos");
                    });
                }
            }
        });
    }

    private void actualizarUsuario(Usuario u) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", u.getId());
            json.put("name", u.getName());
            json.put("email", u.getEmail());
            json.put("password", u.getPassword());
            json.put("rango", u.getRango().equals("Sin rango") ? JSONObject.NULL : u.getRango());

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(API_USER_PUT + u.getId())
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(GestionUsuariosActivity.this, "❌ Error al actualizar", Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(Call call, Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(GestionUsuariosActivity.this, "✅ Usuario actualizado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GestionUsuariosActivity.this, "⚠️ Falló la actualización", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error al preparar datos", Toast.LENGTH_SHORT).show();
        }
    }
}
