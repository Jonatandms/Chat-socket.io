package com.example.proyectofingradochat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.example.proyectofingradochat.util.ChatApplication;
import com.example.proyectofingradochat.util.Constants;
import com.example.proyectofingradochat.R;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Hashtable;
import java.util.Map;

import static com.example.proyectofingradochat.util.Constants.email;

public class InicioSesion extends AppCompatActivity {


    TextInputEditText textemail, textpassword;
    Button login, cancelar;
    FirebaseAuth mauth;
    MaterialCheckBox chkRemember;
    private StorageReference mStorage;
    Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciosesion);
        mStorage = FirebaseStorage.getInstance().getReference();
        login = findViewById(R.id.iniciosesion);
        cancelar = findViewById(R.id.cancelar);
        textemail = findViewById(R.id.iniEmail);
        textpassword = findViewById(R.id.iniPassword);
        mauth = FirebaseAuth.getInstance();
    }

    /*
     * INICIO SESION USUARIO Y CONTRASEÑA
     */
    public void login(View view) {
        final String email = textemail.getText().toString();
        final String password = textpassword.getText().toString();

        mauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    StorageReference filePath = mStorage.child(textemail.getText().toString());
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            new Constants().url = uri.toString();
                            new Constants().email = email;
                            comprobarUsername();
                        }
                    });

                }else{
                    Toast.makeText(InicioSesion.this,"USUARIO Y/O CONTRASEÑA INCORRECTOS",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void finish(View view) {
        finish();
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
                            ChatApplication app = new ChatApplication();
                            mSocket = app.getSocket();
                            mSocket.connect();
                            String username = Constants.username;
                            mSocket.emit("add user",username);
                            mSocket.emit("users connected");
                            Intent intent = new Intent(InicioSesion.this, Botoneschats.class);
                            startActivity(intent);
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

        RequestQueue requestQueue = Volley.newRequestQueue(InicioSesion.this);
        requestQueue.add(stringRequest);
    }


}
