package com.example.frontendintegradora;

public class Usuario {
    private int id;
    private String name, email, password, rango;

    public Usuario(int id, String name, String email, String password, String rango) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.rango = rango;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRango() { return rango; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRango(String rango) { this.rango = rango; }
}
