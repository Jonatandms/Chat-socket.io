package com.example.proyectofingradochat.util;

import android.net.Uri;

import com.example.proyectofingradochat.Clases.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Constants {
    /*
     * CONSTANTES NECESARIAS PARA EL USO CORRECTO DE LA APLICACION
     */
    public static final String CHAT_SERVER_URL = "http://10.0.2.2:3000";
    public static final String IP = "http://10.30.8.69";
    public static String username = "";
    public static FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    public static AuthStateListener authListener;
    public static FirebaseAuth mauth;
    public static String email = "";
    public static String url = "";
    public static String contrasena = "";
    public static Uri uri = null;
    public static ArrayList<Usuario> usuarios;

}
