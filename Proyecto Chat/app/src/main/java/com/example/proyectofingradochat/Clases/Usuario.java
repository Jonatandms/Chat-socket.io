package com.example.proyectofingradochat.Clases;

public class Usuario {
    String socketid, usuario, url;


    public Usuario(String socketid, String usuario) {
        this.socketid = socketid;
        this.usuario = usuario;
    }

    public Usuario(String url, String usuario, Boolean flag) {
        this.url = url;
        this.usuario = usuario;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSocketid() {
        return socketid;
    }

    public void setSocketid(String socketid) {
        this.socketid = socketid;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
