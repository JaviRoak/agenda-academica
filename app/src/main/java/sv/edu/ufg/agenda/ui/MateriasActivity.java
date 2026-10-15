package sv.edu.ufg.agenda.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sv.edu.ufg.agenda.AgendaApp;
import sv.edu.ufg.agenda.R;
import sv.edu.ufg.agenda.dao.DAOFactory;
import sv.edu.ufg.agenda.dao.MateriaDAO;
import sv.edu.ufg.agenda.dao.TareaDAO;
import sv.edu.ufg.agenda.modelo.Materia;

public class MateriasActivity extends AppCompatActivity implements MateriaAdapter.Acciones {

    private MateriaDAO materiaDAO;
    private TareaDAO tareaDAO;
    private MateriaAdapter adapter;
    private EditText etNombre;
    private EditText etDocente;
    private EditText etBuscar;
    private Button btnGuardar;
    private Button btnCancelar;
    private Materia enEdicion; // null = se está agregando una materia nueva

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materias);

        DAOFactory daos = ((AgendaApp) getApplication()).daos();
        materiaDAO = daos.materias();
        tareaDAO = daos.tareas();

        etNombre = findViewById(R.id.etNombre);
        etDocente = findViewById(R.id.etDocente);
        etBuscar = findViewById(R.id.etBuscar);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        adapter = new MateriaAdapter(this);
        RecyclerView lista = findViewById(R.id.rvMaterias);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        btnGuardar.setOnClickListener(v -> guardar());
        btnCancelar.setOnClickListener(v -> limpiarFormulario());
        etBuscar.addTextChangedListener(new TextoCambiado(this::refrescar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refrescar(); // al volver de TareasActivity, los pendientes pudieron cambiar
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String docente = etDocente.getText().toString().trim();
        if (nombre.isEmpty()) { // Regla 1
            etNombre.setError("El nombre es obligatorio");
            return;
        }
        if (enEdicion == null) {
            materiaDAO.insertar(new Materia(0, nombre, docente));
            Toast.makeText(this, "Materia agregada", Toast.LENGTH_SHORT).show();
        } else {
            enEdicion.setNombre(nombre);
            enEdicion.setDocente(docente);
            materiaDAO.actualizar(enEdicion);
            Toast.makeText(this, "Materia actualizada", Toast.LENGTH_SHORT).show();
        }
        limpiarFormulario();
        refrescar();
    }

    private void limpiarFormulario() {
        enEdicion = null;
        etNombre.setText("");
        etDocente.setText("");
        etNombre.setError(null);
        btnGuardar.setText("Agregar materia");
        btnCancelar.setVisibility(View.GONE);
    }

    private void refrescar() {
        String texto = etBuscar.getText().toString().trim();
        List<Materia> materias = texto.isEmpty() ? materiaDAO.listar() : materiaDAO.buscarPorNombre(texto);

        // Resumen por materia: el conteo lo resuelve la capa de datos (TareaDAO.contarPendientes)
        Map<Integer, Integer> pendientes = new HashMap<>();
        for (Materia m : materias) {
            pendientes.put(m.getId(), tareaDAO.contarPendientes(m.getId()));
        }
        adapter.mostrar(materias, pendientes);
    }

    @Override
    public void editar(Materia materia) {
        enEdicion = materia;
        etNombre.setText(materia.getNombre());
        etDocente.setText(materia.getDocente());
        btnGuardar.setText("Actualizar materia");
        btnCancelar.setVisibility(View.VISIBLE);
    }

    @Override
    public void verTareas(Materia materia) {
        Intent intento = new Intent(this, TareasActivity.class);
        intento.putExtra("materiaId", materia.getId());
        startActivity(intento);
    }

    @Override
    public void eliminar(Materia materia) {
        if (!tareaDAO.listarPorMateria(materia.getId()).isEmpty()) { // Regla 3
            Toast.makeText(this, "No se puede eliminar: la materia tiene tareas",
                    Toast.LENGTH_LONG).show();
            return;
        }
        materiaDAO.eliminar(materia.getId());
        if (enEdicion != null && enEdicion.getId() == materia.getId()) {
            limpiarFormulario();
        }
        refrescar();
    }
}
