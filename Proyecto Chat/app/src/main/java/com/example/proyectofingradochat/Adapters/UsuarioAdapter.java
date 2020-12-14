package com.example.proyectofingradochat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.proyectofingradochat.Activities.ConversacionPrivada;
import com.example.proyectofingradochat.Clases.Usuario;
import com.example.proyectofingradochat.R;
import com.example.proyectofingradochat.util.Constants;

import java.util.ArrayList;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.MyViewHolder> {


    Context context;
    ArrayList<Usuario> usuarios;


    public UsuarioAdapter(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;

    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView usuario;
        ImageView imagen;
        Button hablar;

        public MyViewHolder(View v) {
            super(v);
            usuario = (TextView) v.findViewById(R.id.txtusername);
            imagen = v.findViewById(R.id.imagendeperfil);
            hablar = v.findViewById(R.id.btnhablar);
        }


    }


     @NonNull
    @Override
    public UsuarioAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =     LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuarios, parent, false);
        UsuarioAdapter.MyViewHolder vh = new UsuarioAdapter.MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

     @Override
     public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
         int index = -1;
         for (int i = 0; i < new Constants().usuarios.size();i++){
             if (new Constants().usuarios.get(i).getUsuario().equals(usuarios.get(position).getUsuario())){
                 index = i;
             }
         }
         if (index != -1){
             Glide.with(context).load(new Constants().usuarios.get(index).getUrl()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).error(R.drawable.imagen).centerCrop().into(holder.imagen);
         }
         holder.usuario.setText(usuarios.get(position).getUsuario());
         holder.hablar.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(context.getApplicationContext(), ConversacionPrivada.class);
                 intent.putExtra("socketId", usuarios.get(position).getSocketid());
                 intent.putExtra("username", usuarios.get(position).getUsuario());
                 context.startActivity(intent);
             }
         });
     }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

}
