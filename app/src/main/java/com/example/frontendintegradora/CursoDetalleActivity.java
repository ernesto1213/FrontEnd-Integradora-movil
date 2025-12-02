package com.example.frontendintegradora;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CursoDetalleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curso_detalle);

        TextView titulo = findViewById(R.id.txtTituloDetalle);
        TextView descripcion = findViewById(R.id.txtDescripcionDetalle);
        TextView nivel = findViewById(R.id.txtNivelDetalle);
        Button btnAbrir = findViewById(R.id.btnAbrirCurso);
        Button btnVolver = findViewById(R.id.btnVolver);

        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String desc = intent.getStringExtra("descripcion");
        String nivelTxt = intent.getStringExtra("nivel");
        String url = intent.getStringExtra("url");

        titulo.setText(nombre);
        descripcion.setText(desc);
        nivel.setText("Nivel: " + nivelTxt);

        btnAbrir.setOnClickListener(v -> {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        });

        btnVolver.setOnClickListener(v -> finish());
    }
}
