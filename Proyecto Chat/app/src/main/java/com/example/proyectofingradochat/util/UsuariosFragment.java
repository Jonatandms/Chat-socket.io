package com.example.proyectofingradochat.util;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.proyectofingradochat.Adapters.UsuarioAdapter;
import com.example.proyectofingradochat.R;
import com.example.proyectofingradochat.util.ChatApplication;
import com.example.proyectofingradochat.util.Constants;
import com.example.proyectofingradochat.Clases.Usuario;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.util.ArrayList;


public class UsuariosFragment extends Fragment {
    ArrayList<Usuario> usuarios;
    RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Socket mSocket;

    public UsuariosFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_usuarios, container, false);
        ChatApplication app = new ChatApplication();
        mSocket = app.getSocket();
        mSocket.connect();
        mSocket.emit("add user","Pedro");
        mSocket.on("users", onNewUsers);
        recyclerView = view.findViewById(R.id.listadeusuarios);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        // Lo asociamos al RecyclerView
        recyclerView.setLayoutManager(mLayoutManager);
        // Creamos un ArrayList de usuarios
        usuarios = new ArrayList<Usuario>();
        // Creamos un UsuariosAdapter pasándole todos nuestro Usuarios
        mAdapter = new UsuarioAdapter(usuarios);
        // Asociamos el adaptador al RecyclerView
        recyclerView.setAdapter(mAdapter);
        mSocket.emit("users connected");
        return view;
    }
    /*
     * METODO PROPIO DE SOCKET.IO PARA GUARDAR LOS USUARIOS Y AÑADIRLOS AL ADAPTER
     */
    private Emitter.Listener onNewUsers = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        for (int i = 0; i < data.getJSONArray("users").length(); i++) {
                            JSONObject user = data.getJSONArray("users").getJSONObject(i);
                            if (!Constants.username.equalsIgnoreCase(user.getString("username"))) {
                                usuarios.add(new Usuario(user.getString("id"), user.getString("username")));
                                mAdapter.notifyDataSetChanged();
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }
            });
        }
    };




}
