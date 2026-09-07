package com.labolilla.argentumtv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder> {

    public interface OnCategoriaClickListener {
        void onCategoriaClick(String categoria);
    }

    private final Context context;
    private final List<String> categorias;
    private final OnCategoriaClickListener listener;

    public CategoriaAdapter(Context context, List<String> categorias, OnCategoriaClickListener listener) {
        this.context = context;
        this.categorias = categorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        String categoria = categorias.get(position);
        holder.textoCategoria.setText(categoria);
        holder.contadorCategoria.setText("");  // el contador se puede llenar después si hace falta
        holder.itemView.setOnClickListener(v -> listener.onCategoriaClick(categoria));
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        TextView textoCategoria;
        TextView contadorCategoria;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            textoCategoria = itemView.findViewById(R.id.texto_categoria);
            contadorCategoria = itemView.findViewById(R.id.contador_categoria);
        }
    }
}
