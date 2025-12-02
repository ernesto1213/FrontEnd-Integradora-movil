package com.example.frontendintegradora;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoRegister;

    // 🔥 PRODUCCIÓN
    private static final String BASE_URL_PROD = "https://nuevo-production-e70c.up.railway.app";

    // 🔥 LOCAL PARA EMULADOR
    private static final String BASE_URL_LOCAL = "http://10.0.2.2:8080";

    // 🔥 Auto–switch (si es emulador usa local, si es teléfono usa Render)
    private static String getBaseUrl() {
        return android.os.Build.FINGERPRINT.contains("generic")
                ? BASE_URL_LOCAL
                : BASE_URL_PROD;
    }

    // ENDPOINT LOGIN
    private static final String LOGIN_URL = getBaseUrl() + "/api/login/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject loginData = new JSONObject();
            try {
                loginData.put("email", email);
                loginData.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new LoginTask().execute(loginData.toString());
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String jsonInput = params[0];
            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInput.getBytes("UTF-8"));
                }

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject data = new JSONObject(result);
                    if (data.getString("status").equals("ok")) {
                        Toast.makeText(LoginActivity.this, "✅ Login exitoso", Toast.LENGTH_SHORT).show();

                        String token = data.getString("token");
                        int userId = data.optInt("id", -1);
                        String rango = data.getString("rango");

                        // 🔥 Guardamos token + ID en APP_PREFS
                        getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                .edit()
                                .putString("token", token)
                                .putInt("userId", userId)
                                .putString("rango", rango)
                                .apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, "❌ " + data.optString("message", "Error desconocido"), Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "❌ Error parseando respuesta", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "❌ Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
