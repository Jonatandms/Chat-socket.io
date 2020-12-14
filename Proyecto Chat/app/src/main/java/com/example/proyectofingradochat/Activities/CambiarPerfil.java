package com.example.proyectofingradochat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.proyectofingradochat.util.ChatApplication;
import com.example.proyectofingradochat.util.Constants;
import com.example.proyectofingradochat.R;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;
import static com.example.proyectofingradochat.util.Constants.email;

public class CambiarPerfil extends AppCompatActivity {

    TextInputEditText correo,password,confirmarpasword,usuario;
    MaterialCheckBox cambiarpassword;
    FirebaseAuth mauth;
    Options moptions;
    Button actualizar;
    ImageView imagephoto;
    File mimagefile;
    private StorageReference mStorage;
    LinearLayout unacontraseña,doscontraseña;
    String contra;
    Socket mSocket;
    Boolean sabersihayerror = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_perfil);

        correo = findViewById(R.id.textInputCambioEmail);
        password = findViewById(R.id.textInputCambioPassword);
        confirmarpasword = findViewById(R.id.textInputConfirmCambioPassword);
        cambiarpassword = findViewById(R.id.cambiarcontraseña);
        usuario = findViewById(R.id.textInputCambiarUsername);
        imagephoto = findViewById(R.id.imagen);
        unacontraseña = findViewById(R.id.unacontraseña);
        doscontraseña = findViewById(R.id.doscontraseña);
        mauth = FirebaseAuth.getInstance();
        actualizar = findViewById(R.id.btnactualizarperfil);
        mStorage = FirebaseStorage.getInstance().getReference();
        incluirdatos();




        cambiarpassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    unacontraseña.setVisibility(View.VISIBLE);
                    doscontraseña.setVisibility(View.VISIBLE);
                }else{
                    unacontraseña.setVisibility(View.INVISIBLE);
                    doscontraseña.setVisibility(View.INVISIBLE);
                }
            }
        });



        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              comprobarusuarioduplicado();
            }
        });


        /*
         * OPCIONES DE PIXIMAGE PARA LAS IMAGENES
         */
        ArrayList<String> mreturnvalues = new ArrayList<>();
        moptions = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setCount(1)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setPreSelectedUrls(mreturnvalues)                             //Pre selected Image Urls
                .setExcludeVideos(true)                                        //Option to exclude videos
                .setVideoDurationLimitinSeconds(0)                             //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_USER)         //Orientaion
                .setPath("/pix/images");                                       //Custom Path For media Storage



    }
    //Incluye los datos del usuario
    private void incluirdatos() {
        String email = mauth.getCurrentUser().getEmail();
        correo.setText(email);
        correo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                comprobarusuarioduplicado();
            }

            @Override
            public void afterTextChanged(Editable s) {
            comprobarusuarioduplicado();
            }
        });
        usuario.setText(Constants.username);
        Glide.with(this).load(Constants.url).error(R.drawable.imagen).centerCrop().into(imagephoto);
    }




    //Inicia PixImage
    public void startPix(View view) {
        Pix.start(CambiarPerfil.this,moptions);
    }
    /*
     * ONACTIVERESULT NECESARIO PARA PIXIMAGE DONDE SACAMOS LA IMAGEN SUBIDA E IMPORTAMOS EN LA IMAGENVIEW
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            mimagefile = new File(returnValue.get(0));
            Constants.uri = data.getData();
            imagephoto.setImageBitmap(BitmapFactory.decodeFile(mimagefile.getAbsolutePath()));
            saveImage();
        }
    }
    /*
     * PERMISOS DE PIXIMAGE
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(CambiarPerfil.this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(CambiarPerfil.this, "Por favor, concede los permisos para acceder a la camara", LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    //GUARDAMOS LA IMAGEN EN FIREBASE STORAGE Y ALMACENAMOS LA URL EN UN CONSTANTS
    private void saveImage() {
        StorageReference filePath = mStorage.child(correo.getText().toString());
        filePath.putFile(Uri.fromFile(mimagefile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(CambiarPerfil.this, "La imagen se almaceno correctamente", LENGTH_LONG).show();
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Constants.url = uri.toString();
                    }
                });
            }
        });
    }



    //ACTUALIZAMOS PERFIL, CON LA IMAGEN Y/O CONTRASEÑA SI SE HAN MODIFICADO
    private void actualizarPerfil() {
        if (cambiarpassword.isChecked()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(email, Constants.contrasena);
            if (cambiarpassword.isChecked()) {
                if (password.getText().toString().equals(confirmarpasword.getText().toString())) {
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(CambiarPerfil.this, "CONTRASEÑA CAMBIADA", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                }
            }
            }else{
            password.setText(Constants.contrasena);
        }

            ChatApplication app = new ChatApplication();
            mSocket = app.getSocket();
            mSocket.disconnect();
            Constants.username = usuario.getText().toString();
            mSocket.connect();
            mSocket.emit("add user",Constants.username);
            mSocket.emit("users connected");
            cambiarusuario();
            finish();
        }


    private void cambiarusuario() {
        String URL_SERVIDOR_PRUEBA = Constants.IP+"/actualizarusuario.php";
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, URL_SERVIDOR_PRUEBA,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        if(response.equals("ERROR")) {
                            Log.i("ERROR","FALLA CONEXION");
                        } else if(response.equals("ERROR 1")) {
                            Log.i("ERROR","FALTAN DATOS");
                        } else if (response.equals("ERROR 2")){
                            Log.i("ERROR","NO HAY USUARIOS");
                        }else{
                            Log.i("CORRECTO","USUARIO "+ Constants.username);
                            Log.i("CORRECTO","CORREO "+ Constants.email);
                            Log.i("CORRECTO","URL "+ Constants.url);
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
                parametros.put("usuario", Constants.username);
                parametros.put("url", Constants.url);
                parametros.put("contrasena",password.getText().toString());
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CambiarPerfil.this);
        requestQueue.add(stringRequest);
    }
    private void comprobarusuarioduplicado() {
        String URL_SERVIDOR_PRUEBA = Constants.IP+"/comprobarusuarioduplicado.php";
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
                            Log.i("CORRECTO","USUARIO MODIFICADO");
                            actualizarPerfil();
                        }else{
                            comprobarUsername();
                            Log.i("INCORRECTO","USUARIO UTILIZANDOSE");
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
                parametros.put("usuario", usuario.getText().toString());
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CambiarPerfil.this);
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
                            if (response.equals(usuario.getText().toString())){
                                actualizarPerfil();
                            }else{
                                usuario.setError("USUARIO UTILIZANDOSE, PRUEBE CON OTRO");
                            }

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

        RequestQueue requestQueue = Volley.newRequestQueue(CambiarPerfil.this);
        requestQueue.add(stringRequest);
    }




    public void atras(View view) {
        finish();
    }
}
