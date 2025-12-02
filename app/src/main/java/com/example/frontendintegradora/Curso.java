package com.example.frontendintegradora;

public class Curso {
    private int id;
    private String nombre;
    private String descripcion;
    private String nivel;
    private String url;

    public Curso(int id, String nombre, String descripcion, String nivel, String url) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.url = url;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getNivel() { return nivel; }
    public String getUrl() { return url; }
}
