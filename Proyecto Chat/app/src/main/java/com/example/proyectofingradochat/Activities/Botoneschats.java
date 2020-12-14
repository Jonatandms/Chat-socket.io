package com.example.proyectofingradochat.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyectofingradochat.R;
import com.example.proyectofingradochat.util.ChatApplication;
import com.example.proyectofingradochat.util.Constants;
import com.example.proyectofingradochat.util.UsuariosFragment;
import com.firebase.ui.auth.AuthUI;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Hashtable;
import java.util.Map;

import static com.example.proyectofingradochat.util.Constants.email;


public class Botoneschats extends AppCompatActivity {
    Socket mSocket;
    UsuariosFragment fragment;
    FragmentTransaction ft;
    PendingIntent pendingIntent;
    String CHANNEL_ID = "NOTIFICACION";
    int NOTIFICACION_ID=0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_botoneschats);
        ChatApplication app = new ChatApplication();
        mSocket = app.getSocket();
        mSocket.connect();
        mSocket.on("join me",onNewMessage);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
           Log.i("MENSAJE", "AVISO RECIBIDO");
            String username = args[0].toString().split(";")[1];
            mSocket.emit("join", "room" + mSocket.id());
            notificacion(username);
            notificacionchannel();
        }
    };


    private void notificacion(String username) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.charla);
        builder.setContentTitle("SOLICITUD CONVERSACION");
        builder.setContentText(username + " QUIERE CONVERSAR CONTIGO");
        builder.setColor(Color.BLUE);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        Intent notificationIntent = new Intent(this,ConversacionPrivada.class);
        notificationIntent.putExtra("username",username);
        PendingIntent conPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(conPendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICACION_ID, builder.build());
    }

    private void notificacionchannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Notificacion";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


    //OnClick boton "general"
    public void general(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    //OnClick boton "Chat individual" que añade el fragment, con las personas activas en la aplicaicon
    public void personas(View view) {
        fragment = new UsuariosFragment();
        ft = Botoneschats.this.getSupportFragmentManager().beginTransaction();  // si usas import android.support.v4.app.Fragment;
        ft.replace(R.id.listausuarios, fragment, "listado de usuarios");    // frame es el id del FrameLayout que va contener los fragments
        ft.addToBackStack(null);                              // para agregarlo a la pila
        ft.commit();
    }
    //Oncreate del Menu de opciones
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    //Opciones selecionables del menu
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSignOut:
                signOut();
                return true;
            case R.id.perfil:
                 comprobarcontraseña();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //Cierre de sesion, ya sea por metodo correo y contraseña, como por Google
    private void signOut() {
        // Firebase sign out
        Constants.mauth.signOut();

        // Google sign out
        AuthUI.getInstance().signOut(this).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(Botoneschats.this, "ADIOS", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Botoneschats.this,MainActivity.class);
                    startActivity(intent);
            }
        });
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
                            Log.i("CORRECTO","USUARIO EXISTE");
                            new Constants().username = response;

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

        RequestQueue requestQueue = Volley.newRequestQueue(Botoneschats.this);
        requestQueue.add(stringRequest);
    }


    private void comprobarcontraseña() {
        String URL_SERVIDOR_PRUEBA = Constants.IP+"/comprobarcontrasena.php";
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, URL_SERVIDOR_PRUEBA,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        if(response.equals("ERROR 1")) {
                            Log.i("ERROR","FALLA CONEXION");
                        } else if(response.equals("ERROR 2")) {
                            Log.i("ERROR","FALTAN DATOS");
                        } else if (response.equals("ERROR 3")){
                            Log.i("ERROR","NO HAY USUARIOS");
                        }else{
                            Log.i("CONTRA",response);
                            new Constants().contrasena = response;
                            Intent intent = new Intent(Botoneschats.this, CambiarPerfil.class);
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

        RequestQueue requestQueue = Volley.newRequestQueue(Botoneschats.this);
        requestQueue.add(stringRequest);
    }

}