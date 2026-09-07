package com.labolilla.argentumtv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class SeccionAdapter extends RecyclerView.Adapter<SeccionAdapter.SeccionViewHolder> {

    private final Context context;
    private final Map<String, List<JSONObject>> secciones;  // categoría -> canales
    private final List<String> ordenCategorias;
    private final CanalAdapter.OnCanalClickListener listener;

    public SeccionAdapter(Context context, Map<String, List<JSONObject>> secciones,
                          List<String> ordenCategorias, CanalAdapter.OnCanalClickListener listener) {
        this.context = context;
        this.secciones = secciones;
        this.ordenCategorias = ordenCategorias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seccion, parent, false);
        return new SeccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeccionViewHolder holder, int position) {
        String categoria = ordenCategorias.get(position);
        List<JSONObject> canales = secciones.get(categoria);

        holder.tituloSeccion.setText(categoria);

        CanalAdapter adapter = new CanalAdapter(context, canales, listener);
        holder.recyclerSeccion.setLayoutManager(new LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerSeccion.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return ordenCategorias.size();
    }

    public static class SeccionViewHolder extends RecyclerView.ViewHolder {
        TextView tituloSeccion;
        RecyclerView recyclerSeccion;

        public SeccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tituloSeccion = itemView.findViewById(R.id.titulo_seccion);
            recyclerSeccion = itemView.findViewById(R.id.recycler_seccion);
        }
    }
}
