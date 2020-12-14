package com.example.proyectofingradochat.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyectofingradochat.Adapters.MessageAdapter;
import com.example.proyectofingradochat.Clases.Message;
import com.example.proyectofingradochat.Clases.Usuario;
import com.example.proyectofingradochat.R;
import com.example.proyectofingradochat.util.ChatApplication;
import com.example.proyectofingradochat.util.Constants;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;


public class ConversacionPrivada extends AppCompatActivity {
    Socket mSocket;
    String username;
    private TextView mensajes;
    private EditText mInputMessageView;
    ImageButton enviar;
    ArrayList<Message> messages;
    RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversacion_privada);

        String id = getIntent().getStringExtra("socketId");
        username = getIntent().getStringExtra("username");
        obtenerUrlFotos();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle("Conversacion Privada con: " + username);
        }
        ChatApplication app = new ChatApplication();
        mSocket = app.getSocket();

        mSocket.connect();
        if (id != null){
            mSocket.emit("join me", id );
            mSocket.emit("join", "room" + id);
        }

        mSocket.on("update", onNewMessage);

        mInputMessageView = findViewById(R.id.message_input2);
        recyclerView = findViewById(R.id.mensajes2);
        // Mejoramos rendimiento con esta configuración
        recyclerView.setHasFixedSize(true);

        // Creamos un LinearLayoutManager para gestionar el item.xml creado antes
        mLayoutManager = new LinearLayoutManager(this);
        // Lo asociamos al RecyclerView
        recyclerView.setLayoutManager(mLayoutManager);

        // Creamos un ArrayList de Mensajes
        messages = new ArrayList<Message>();
        // Creamos un MessageAdapter pasándole todos nuestro Mensajes
        mAdapter = new MessageAdapter(messages);
        // Asociamos el adaptador al RecyclerView
        recyclerView.setAdapter(mAdapter);

        enviar = findViewById(R.id.send_button2);

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });





    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.i("MENSAJE","NUEVO MENSAJE");
            ConversacionPrivada.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage(username, args[0].toString());
                }
            });
        }
    };

    //Metodo que envia el mensaje a Socket.io
    private void attemptSend() {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mInputMessageView.setText("");
        addMessage(new Constants().username,message);
        mSocket.emit("private message", message);
    }



    //Metodo que añade el mensaje al Arraylist y al actualiza el adapter
    private void addMessage(String usuario, String message) {
        Message m = new Message(usuario,message);
        messages.add(m);
        mAdapter.notifyDataSetChanged();
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

        RequestQueue requestQueue = Volley.newRequestQueue(ConversacionPrivada.this);
        requestQueue.add(stringRequest);
    }




}
