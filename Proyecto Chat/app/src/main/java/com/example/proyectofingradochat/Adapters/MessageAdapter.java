package com.example.proyectofingradochat.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.example.proyectofingradochat.util.Constants;
import com.example.proyectofingradochat.Clases.Message;
import com.example.proyectofingradochat.R;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    Context context;
    ArrayList<Message> messages;
    String usu;

    public MessageAdapter(ArrayList<Message> messages) {

        this.messages = messages;

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView usuario;
        TextView mensaje;

        public MyViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.imagenusuario);
            usuario = (TextView) v.findViewById(R.id.txtusuario);
            mensaje = (TextView) v.findViewById(R.id.txtmensaje);
        }
    }
    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =     LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        context = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        usu = messages.get(position).getUsername();
        int index = -1;
        for (int i = 0; i < new Constants().usuarios.size();i++){
            if (new Constants().usuarios.get(i).getUsuario().equals(messages.get(position).getUsername())){
                index = i;
            }
        }
        if (index != -1){
            String url = new Constants().usuarios.get(index).getUrl();
            Glide.with(context).load(new Constants().usuarios.get(index).getUrl()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).error(R.drawable.imagen).centerCrop().into(holder.imagen);
        }

        holder.usuario.setText(messages.get(position).getUsername()+ ": ");
        holder.mensaje.setText(messages.get(position).getMessage());
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }


}
