package com.example.frontendintegradora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CursosActivity extends AppCompatActivity {

    static class Curso {
        int id;
        String nombre;
        String descripcion;
        String nivel;
        String url;

        Curso(int id, String nombre, String descripcion, String nivel, String url) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.nivel = nivel;
            this.url = url;
        }
    }

    private List<Curso> cursos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursos);

        LinearLayout contenedor = findViewById(R.id.listaCursos);

        // Simulación del JSON directamente en el código
        cursos = new ArrayList<>();
        cursos.add(new Curso(1, "Transporte Marítimo",
                "Curso básico sobre transporte marítimo.",
                "Básico",
                "https://edutin.com/curso-de-transporte-maritimo-2127?utm_source=chatgpt.com"));

        cursos.add(new Curso(2, "Diploma en Construcción Naval",
                "Curso básico sobre construcción naval.",
                "Básico",
                "https://alison.com/es?utm_source=chatgpt.com"));

        cursos.add(new Curso(3, "Mantenimiento y Conservación del Buque",
                "Curso intermedio sobre mantenimiento de buques.",
                "Intermedio",
                "https://www.academiaintegral.com.es/cursos-gratis/certificados-de-profesionalidad/maritimo-pesquera/mf1298-1-mantenimiento-y-conservacion-del-buque-18461.html?utm_source=chatgpt.com"));

        cursos.add(new Curso(4, "Operaciones de Bombeo en Buques",
                "Curso intermedio sobre carga y descarga de buques.",
                "Intermedio",
                "https://www.cursos-gratis-online.com/curso-gratis-online-trabajadores-mapn0412-operaciones-de-bombeo-para-carga-y-descarga-del-buque.html?utm_source=chatgpt.com"));

        cursos.add(new Curso(5, "Máster en Reparación Naval",
                "Curso avanzado sobre reparación y mantenimiento de buques.",
                "Avanzado",
                "https://www.ime.es/curso-ime/master-reparacion-naval/?utm_source=chatgpt.com"));

        cursos.add(new Curso(6, "Ingeniero en Mantenimiento Petrolero",
                "Curso avanzado en mantenimiento de equipos petroleros.",
                "Avanzado",
                "https://www.apoia.com.br/es/cursos/cursos-de-petroleo-y-gas-es/lista/?utm_source=chatgpt.com"));

        // Agregar los cursos visualmente
        for (Curso curso : cursos) {
            LinearLayout card = (LinearLayout) getLayoutInflater().inflate(R.layout.item_curso, contenedor, false);

            TextView titulo = card.findViewById(R.id.txtTitulo);
            TextView descripcion = card.findViewById(R.id.txtDescripcion);
            TextView nivel = card.findViewById(R.id.txtNivel);
            Button btnVer = card.findViewById(R.id.btnVerCurso);

            titulo.setText(curso.nombre);
            descripcion.setText(curso.descripcion);
            nivel.setText("Nivel: " + curso.nivel);

            btnVer.setOnClickListener(v -> {
                Intent intent = new Intent(CursosActivity.this, CursoDetalleActivity.class);
                intent.putExtra("nombre", curso.nombre);
                intent.putExtra("descripcion", curso.descripcion);
                intent.putExtra("nivel", curso.nivel);
                intent.putExtra("url", curso.url);
                startActivity(intent);
            });

            contenedor.addView(card);
        }
    }
}
