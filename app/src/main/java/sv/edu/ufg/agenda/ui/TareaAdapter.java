package sv.edu.ufg.agenda.ui;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import sv.edu.ufg.agenda.R;
import sv.edu.ufg.agenda.modelo.Tarea;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.Fila> {

    public interface Acciones {
        void editar(Tarea tarea);
        void cambiarCompletada(Tarea tarea, boolean completada);
        void eliminar(Tarea tarea);
    }

    private final List<Tarea> datos = new ArrayList<>();
    private final Acciones acciones;

    public TareaAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    public void mostrar(List<Tarea> nuevas) {
        datos.clear();
        datos.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View vista = LayoutInflater.from(padre.getContext())
                .inflate(R.layout.item_tarea, padre, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Tarea t = datos.get(posicion);
        fila.tvTexto.setText(t.getTitulo() + "\nEntrega: " + t.getFechaEntrega()
                + " · Prioridad " + t.getPrioridad());

        if (t.isCompletada()) {
            fila.tvTexto.setPaintFlags(fila.tvTexto.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            fila.tvTexto.setPaintFlags(fila.tvTexto.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Se quita el listener antes de setChecked para que el reciclaje de filas no dispare cambios falsos
        fila.cbCompletada.setOnCheckedChangeListener(null);
        fila.cbCompletada.setChecked(t.isCompletada());
        fila.cbCompletada.setOnCheckedChangeListener((boton, marcada) -> acciones.cambiarCompletada(t, marcada));

        fila.itemView.setOnClickListener(v -> acciones.editar(t));
        fila.btnEliminar.setOnClickListener(v -> acciones.eliminar(t));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final CheckBox cbCompletada;
        final TextView tvTexto;
        final Button btnEliminar;

        Fila(@NonNull View vista) {
            super(vista);
            cbCompletada = vista.findViewById(R.id.cbCompletada);
            tvTexto = vista.findViewById(R.id.tvTexto);
            btnEliminar = vista.findViewById(R.id.btnEliminar);
        }
    }
}
