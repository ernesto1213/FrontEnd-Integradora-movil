package com.example.frontendintegradora;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.ViewHolder> {

    private ArrayList<Usuario> lista;
    private OnUpdateListener listener;

    public interface OnUpdateListener {
        void onUpdate(Usuario usuario);
    }

    public UsuariosAdapter(ArrayList<Usuario> lista, OnUpdateListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario u = lista.get(position);
        holder.txtId.setText(String.valueOf(u.getId()));
        holder.txtName.setText(u.getName());
        holder.txtEmail.setText(u.getEmail());
        holder.txtPass.setText(u.getPassword());
        holder.txtRango.setText(u.getRango());

        holder.btnGuardar.setOnClickListener(v -> {
            u.setName(holder.txtName.getText().toString());
            u.setEmail(holder.txtEmail.getText().toString());
            u.setPassword(holder.txtPass.getText().toString());
            u.setRango(holder.txtRango.getText().toString());
            listener.onUpdate(u);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        EditText txtName, txtEmail, txtPass, txtRango;
        TextView txtId;
        Button btnGuardar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtId);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtPass = itemView.findViewById(R.id.txtPass);
            txtRango = itemView.findViewById(R.id.txtRango);
            btnGuardar = itemView.findViewById(R.id.btnGuardar);
        }
    }
}
