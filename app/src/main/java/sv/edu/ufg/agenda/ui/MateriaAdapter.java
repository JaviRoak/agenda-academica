package sv.edu.ufg.agenda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sv.edu.ufg.agenda.R;
import sv.edu.ufg.agenda.modelo.Materia;

public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.Fila> {

    public interface Acciones {
        void editar(Materia materia);
        void verTareas(Materia materia);
        void eliminar(Materia materia);
    }

    private final List<Materia> datos = new ArrayList<>();
    private final Map<Integer, Integer> pendientes = new HashMap<>();
    private final Acciones acciones;

    public MateriaAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    /** El adapter solo muestra: recibe ya calculadas las tareas pendientes de cada materia (id -> cantidad). */
    public void mostrar(List<Materia> nuevos, Map<Integer, Integer> pendientesPorMateria) {
        datos.clear();
        datos.addAll(nuevos);
        pendientes.clear();
        pendientes.putAll(pendientesPorMateria);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View vista = LayoutInflater.from(padre.getContext())
                .inflate(R.layout.item_materia, padre, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        Materia m = datos.get(posicion);
        String docente = (m.getDocente() == null || m.getDocente().isEmpty()) ? "Sin docente" : m.getDocente();
        fila.tvTexto.setText(m.getNombre() + "\n" + docente);

        Integer n = pendientes.get(m.getId());
        int cantidad = (n != null) ? n : 0;
        fila.tvPendientes.setText(cantidad == 1 ? "1 tarea pendiente" : cantidad + " tareas pendientes");

        fila.itemView.setOnClickListener(v -> acciones.editar(m));
        fila.btnTareas.setOnClickListener(v -> acciones.verTareas(m));
        fila.btnEliminar.setOnClickListener(v -> acciones.eliminar(m));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView tvTexto;
        final TextView tvPendientes;
        final Button btnTareas;
        final Button btnEliminar;

        Fila(@NonNull View vista) {
            super(vista);
            tvTexto = vista.findViewById(R.id.tvTexto);
            tvPendientes = vista.findViewById(R.id.tvPendientes);
            btnTareas = vista.findViewById(R.id.btnTareas);
            btnEliminar = vista.findViewById(R.id.btnEliminar);
        }
    }
}
