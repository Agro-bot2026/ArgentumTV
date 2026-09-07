package com.labolilla.argentumtv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.List;

public class CanalAdapter extends RecyclerView.Adapter<CanalAdapter.CanalViewHolder> {

    public interface OnCanalClickListener {
        void onCanalClick(JSONObject canal);
    }

    private final Context context;
    private final List<JSONObject> canales;
    private final OnCanalClickListener listener;

    public CanalAdapter(Context context, List<JSONObject> canales, OnCanalClickListener listener) {
        this.context = context;
        this.canales = canales;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CanalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_canal, parent, false);
        return new CanalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CanalViewHolder holder, int position) {
        JSONObject canal = canales.get(position);
        String nombre = canal.optString("nombre", "Canal");
        String logo = canal.optString("logo", "");

        holder.textoNombre.setText(nombre);
        if (logo != null && !logo.isEmpty()) {
            Glide.with(context)
                    .load(logo)
                    .placeholder(R.drawable.bg_buscador)
                    .error(R.drawable.bg_buscador)
                    .into(holder.imagenLogo);
        } else {
            holder.imagenLogo.setImageResource(R.drawable.bg_buscador);
        }

        holder.itemView.setOnClickListener(v -> listener.onCanalClick(canal));
    }

    @Override
    public int getItemCount() {
        return canales.size();
    }

    public static class CanalViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenLogo;
        TextView textoNombre;

        public CanalViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenLogo = itemView.findViewById(R.id.logo_canal);
            textoNombre = itemView.findViewById(R.id.nombre_canal);
        }
    }
}
