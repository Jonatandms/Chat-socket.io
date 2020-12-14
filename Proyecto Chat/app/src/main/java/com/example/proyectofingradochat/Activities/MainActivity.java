package com.example.proyectofingradochat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyectofingradochat.Clases.Usuario;
import com.example.proyectofingradochat.util.ChatApplication;
import com.example.proyectofingradochat.util.Constants;
import com.example.proyectofingradochat.R;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;

import static com.example.proyectofingradochat.util.Constants.authListener;
import static com.example.proyectofingradochat.util.Constants.email;
import static com.example.proyectofingradochat.util.Constants.mUser;
import static com.example.proyectofingradochat.util.Constants.mauth;


public class MainActivity extends AppCompatActivity {

    Socket mSocket;
    Button btninicio, btnregistar;
    SignInButton btngoogle;
    String correogoogle, usuariogoogle,urlimagengoogle;
    private GoogleSignInClient mGoogleSignInClient;
    private final int REQUEST_GOOGLE_CODE = 1;
    AlertDialog mdialog;
    private StorageReference mStorage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btngoogle = findViewById(R.id.iniciosesiongoogle);
        btninicio = findViewById(R.id.iniciosesion);
        btnregistar = findViewById(R.id.registrar);
        mStorage = FirebaseStorage.getInstance().getReference();
        new Constants().mauth = FirebaseAuth.getInstance();

        mdialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("CARGANDO")
                .setCancelable(false).build();

        // Configure Google Sign In
        GoogleSignInOptions  gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btngoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });

        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
               Log.i("MENSAJE","NO HAY USUARIO CONECTADO");
                return;
            }else{
                comprobarUsername();
                //RECUPERAMOS EL USERNAME Y LA URL CON EL EMAIL DE FIREBASE Y PONEMOS EN LAS CONSTANTES
            }
        };
    }

    /*
     * ACCION SEGUN ID DE BOTONES
     */
    public void botones(View view) {
        switch (view.getId()){
            case R.id.iniciosesion:
                mdialog.show();
                Intent intent = new Intent(this,InicioSesion.class);
                startActivity(intent);
                mdialog.dismiss();
                break;
            case R.id.registrar:
                mdialog.show();
                Intent intent1 = new Intent(this, Registro.class);
                startActivity(intent1);
                mdialog.dismiss();
                break;

        }
    }


    /*
     * INICIAR SESION CON GOOGLE
     */

    private void signInGoogle() {
        //introducir nombre de usuario
        mdialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_GOOGLE_CODE);
    }
    /*
     * ON ACTIVITY RESULT PARA EL INICIO DE GOOGLE
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_GOOGLE_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                correogoogle = account.getEmail();
                usuariogoogle = account.getDisplayName();
                urlimagengoogle = String.valueOf(account.getPhotoUrl());
                añadirusuarioMysqlGoogle();
                comprobarUsername();
                obtenerUrlFotos();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("ERROR", "Google sign in failed", e);
                // ...
            }
        }
    }


    /*
     * AUTENTICACION CON GOOGLE
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        Constants.mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //enviar email y usuario de esta persona a la BBDD
                            new Constants().username = usuariogoogle;
                            new Constants().url = urlimagengoogle;
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(MainActivity.this,Botoneschats.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("ERROR", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this,"NO SE PUDO INICIAR SESION CON GOOGLE",Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }
    /*
     * ON START PARA INICIAR SESION AUTOMATICAMENTE
     */
    @Override
    protected void onStart() {
        super.onStart();
        Constants.mauth.addAuthStateListener(authListener);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            mUser.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        new Constants().email = mUser.getEmail();
                        StorageReference filePath = mStorage.child(Constants.email);
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                new Constants().url = uri.toString();
                                comprobarUsername();
                                obtenerUrlFotos();
                            }
                        });
                    });
        } else {
            Log.i("MENSAJE2","NO HAY USUARIO CONECTADO");
        }
    }

    private void obtenerUrlFotos() {
        String URL_SERVIDOR_PRUEBA = Constants.IP+"/comprobarUrl.php";
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, URL_SERVIDOR_PRUEBA,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        if(response.equals("ERROR 1")) {
                            Log.i("ERROR1","FALLA CONEXION");
                        } else if (response.equals("0 results")){
                            Log.i("ERROR","NO HAY USUARIOS");
                        }else{
                            ArrayList<Usuario> usuarios = new ArrayList<>();
                            String[] users = response.split("\\|");
                            for (String u : users){
                                usuarios.add(new Usuario(u.split(";")[0],u.split(";")[1],true));
                            }
                            new Constants().usuarios = usuarios;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.i("ERROR","PROBLEMAS AL COMPROBAR USUARIO");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                // En este metodo se hace el envio de valores de la aplicacion al servidor
                Map<String, String> parametros = new Hashtable<String, String>();
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }


    private void comprobarUsername() {
        String URL_SERVIDOR_PRUEBA = Constants.IP+"/consultarusuario.php";
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, URL_SERVIDOR_PRUEBA,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        if(response.equals("ERROR 1")) {
                            Log.i("ERROR1","FALLA CONEXION");
                        } else if(response.equals("ERROR 2")) {
                            Log.i("ERROR","FALTAN DATOS");
                        } else if (response.equals("0 results")){
                            Log.i("ERROR","NO HAY USUARIOS");
                        }else{

                            new Constants().username = response;
                            Log.i("CORRECTO","USUARIO "+ Constants.username);
                            Log.i("CORRECTO","CORREO "+ Constants.email);
                            Log.i("CORRECTO","URL "+ Constants.url);
                            ChatApplication app = new ChatApplication();
                            mSocket = app.getSocket();
                            mSocket.connect();
                            mSocket.emit("add user",Constants.username);
                            mSocket.emit("users connected");
                            startActivity(new Intent(MainActivity.this, Botoneschats.class));
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.i("ERROR","PROBLEMAS AL COMPROBAR USUARIO");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                // En este metodo se hace el envio de valores de la aplicacion al servidor
                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("correo", email);
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    private void añadirusuarioMysqlGoogle() {
        String URL_SERVIDOR = Constants.IP+"/registrarUsuarioGoogle.php";
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, URL_SERVIDOR,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        if(response.equals("ERROR 1")) {
                            Log.i("ERROR1","FALTAN DATOS");
                        } else if(response.equals("ERROR 2")) {
                            Log.i("ERROR","USUARIO NO CREADO");
                        } else if(response.equals("ERROR")) {
                            Log.i("ERROR","USUARIO YA CREADO");
                        }else if (response.equals("OK")){
                            Log.i("CORRECTO","SE CREA USUARIO EN MYSQL");
                        }

                    }


                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.i("ERROR","NO SE GUARDO EL USUARIO EN MYSQL");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                // En este metodo se hace el envio de valores de la aplicacion al servidor
                Map<String, String> parametros = new Hashtable<String, String>();
                String correo = email;
                String url = Constants.url;
                parametros.put("correo", correogoogle);
                parametros.put("url",urlimagengoogle);
                parametros.put("usuario", usuariogoogle);
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }






}




