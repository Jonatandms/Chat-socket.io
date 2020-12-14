package com.example.proyectofingradochat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.proyectofingradochat.R;
import com.example.proyectofingradochat.util.Constants;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.auth.User;
//import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.proyectofingradochat.util.Constants.email;

public class Registro extends AppCompatActivity {
    CircleImageView mCircleImageViewBack;
    TextInputEditText mTextInputUsername;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPassword;
    TextInputEditText mTextInputConfirmPassword;
    TextInputEditText mTextInputPhone;
    Button mButtonRegister;
    String username,password, confirmPassword;
    FirebaseAuth mauth;
    ImageView imagefoto;
    Options moptions;
    File mimagefile;
    TextView añadirimagen;
    private StorageReference mStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mCircleImageViewBack = findViewById(R.id.circleimageback);
        mStorage = FirebaseStorage.getInstance().getReference();
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputUsername = findViewById(R.id.textInputUsername);
        mTextInputPassword = findViewById(R.id.textInputPassword);
        mTextInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);
        mButtonRegister = findViewById(R.id.btnRegister);
        mauth = FirebaseAuth.getInstance();
        imagefoto = findViewById(R.id.imagephoto);
        añadirimagen = findViewById(R.id.añadirimagen);


        ArrayList<String> mreturnvalues = new ArrayList<>();
        /*
         * OPCIONES DE PIXIMAGE PARA LAS IMAGENES
         */
        moptions = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setCount(1)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setPreSelectedUrls(mreturnvalues)                             //Pre selected Image Urls
                .setExcludeVideos(true)                                        //Option to exclude videos
                .setVideoDurationLimitinSeconds(0)                             //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_USER)         //Orientaion
                .setPath("/pix/images");                                       //Custom Path For media Storage


        añadirimagen.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startPix();
            }
        });
    }




    /*
     * METODO QUE INICIA PIXIMAGE PARA SELECCIONAR LA IMAGEN DE PERFIL
     */
    private void startPix() {
        Pix.start(Registro.this,moptions);
    }


    /*
     * VERIFICAR DATOS INTRODUCIDOS
     */
    private void verificardatos() {
        username = mTextInputUsername.getText().toString();
        new Constants().email = mTextInputEmail.getText().toString();
        password  = mTextInputPassword.getText().toString();
        confirmPassword = mTextInputConfirmPassword.getText().toString();


        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
            if (isEmailValid(email)) {
                if (password.equals(confirmPassword)) {
                    if (password.length() >= 6) {
                       // createUser(email, password);
                        comprobarusuarioduplicado();
                    }
                    else {
                        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(this, "Las contraseña no coinciden", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "Insertaste todos los campos pero el correo no es valido", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(this, "Para continuar inserta todos los campos", Toast.LENGTH_SHORT).show();
        }

    }
    /*
     *METODO QUE GUARDA LA IMAGEN EN FIREBASE Y LLAMA AL METODO CREATEUSER
     */

    private void saveImage() {
        StorageReference filePath = mStorage.child(mTextInputEmail.getText().toString());
        filePath.putFile(Uri.fromFile(mimagefile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Registro.this, "La imagen se almaceno correctamente", Toast.LENGTH_LONG).show();
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        new Constants().url = uri.toString();
                        añadirusuarioMysql();
                    }
                });
            }
        });
    }


    /*
     * CREAR USUARIO CON FIREBASE
     */
    private void createUser(final String email, final String password) {
        mauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(Registro.this
                            , "Usuario creado correctamente"
                            , Toast.LENGTH_SHORT).show();
                   saveImage();
                }else{
                    Toast.makeText(Registro.this
                            , "No se ha podido crerar el usuario"
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /*
     * VERIFICAR QUE SEA UN EMAIL VALIDO
     */
    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
            imagefoto.setImageBitmap(BitmapFactory.decodeFile(mimagefile.getAbsolutePath()));
            añadirimagen.setText("");
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
                    Pix.start(Registro.this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(Registro.this, "Por favor, concede los permisos para acceder a la camara", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    /*
     * ON CLICK BOTON REGISTRAR
     */
    public void registrar(View view) {
        verificardatos();
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
                            createUser(email, password);
                        }else{
                            Log.i("INCORRECTO","USUARIO UTILIZANDOSE");
                            mTextInputUsername.setError("USUARIO UTILIZANDOSE, PRUEBE CON OTRO");
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
                parametros.put("usuario", mTextInputUsername.getText().toString());
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(Registro.this);
        requestQueue.add(stringRequest);
    }






    private void añadirusuarioMysql() {
        String URL_SERVIDOR = Constants.IP+"/registrarUsuario.php";
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, URL_SERVIDOR,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // En este apartado se programa lo que deseamos hacer en caso de no haber errores
                        if(response.equals("ERROR 1")) {
                            Log.i("ERROR1","FALTAN DATOS");
                        } else if(response.equals("ERROR")) {
                            Log.i("ERROR","USUARIO NO CREADO");
                        } else if (response.equals("OK")){
                            Log.i("CORRECTO","SE CREA USUARIO EN MYSQL");
                            Intent intent = new Intent(Registro.this, Botoneschats.class);
                            startActivity(intent);
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
                parametros.put("correo", correo);
                parametros.put("url",url);
                parametros.put("usuario", mTextInputUsername.getText().toString());
                parametros.put("contraseña", mTextInputConfirmPassword.getText().toString());
                new Constants().contrasena = mTextInputConfirmPassword.getText().toString();
                new Constants().username = mTextInputUsername.getText().toString();
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(Registro.this);
        requestQueue.add(stringRequest);

    }


    public void atras(View view) {
        onBackPressed();
    }

    public void finish(View view) {
        finish();
    }
}
