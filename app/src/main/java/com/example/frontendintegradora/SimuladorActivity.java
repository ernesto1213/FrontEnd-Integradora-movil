package com.example.frontendintegradora;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimuladorActivity extends AppCompatActivity {

    private TextView tvAmbiente, tvObjeto, tvHora;

    private final Handler handler = new Handler();
    private final int REFRESH_INTERVAL = 5000; // 5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulador);

        tvAmbiente = findViewById(R.id.tvAmbiente);
        tvObjeto = findViewById(R.id.tvObjeto);
        tvHora = findViewById(R.id.tvHora);

        // Primera carga
        new TemperaturaTask().execute();

        // Recargar cada 5 segundos
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new TemperaturaTask().execute();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }


    // Clase para consumir API asincrónicamente
    private class TemperaturaTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                URL url = new URL("https://nuevo-production-e70c.up.railway.app/api/evaluaciones/ultimaTemperatura");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                return new JSONObject(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            if (data != null) {
                try {
                    double ambiente = data.getDouble("ambiente");
                    double objeto = data.getDouble("objeto");
                    long timestamp = data.optLong("timestamp", System.currentTimeMillis());

                    tvAmbiente.setText("Ambiente: " + ambiente + " °C");
                    tvObjeto.setText("Objeto: " + objeto + " °C");
                    tvHora.setText("Última actualización: " + android.text.format.DateFormat.format("hh:mm:ss a", timestamp));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SimuladorActivity.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SimuladorActivity.this, "No se pudo obtener temperatura", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
