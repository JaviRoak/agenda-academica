package sv.edu.ufg.agenda.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import sv.edu.ufg.agenda.AgendaApp;
import sv.edu.ufg.agenda.R;
import sv.edu.ufg.agenda.dao.DAOFactory;
import sv.edu.ufg.agenda.dao.MateriaDAO;
import sv.edu.ufg.agenda.dao.TareaDAO;
import sv.edu.ufg.agenda.modelo.Materia;
import sv.edu.ufg.agenda.modelo.Tarea;

public class TareasActivity extends AppCompatActivity implements TareaAdapter.Acciones {

    private MateriaDAO materiaDAO;
    private TareaDAO tareaDAO;
    private int materiaId;
    private TareaAdapter adapter;
    private TextView tvEncabezado;
    private EditText etTitulo;
    private EditText etFecha;
    private EditText etBuscar;
    private Spinner spPrioridad;
    private Button btnGuardar;
    private Button btnCancelar;
    private Tarea enEdicion; // null = se está agregando una tarea nueva

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tareas);

        DAOFactory daos = ((AgendaApp) getApplication()).daos();
        materiaDAO = daos.materias();
        tareaDAO = daos.tareas();

        // Entrada: si la materia no existe, se cierra la pantalla
        materiaId = getIntent().getIntExtra("materiaId", -1);
        Materia materia = materiaDAO.buscar(materiaId);
        if (materia == null) {
            finish();
            return;
        }
        setTitle(materia.getNombre());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvEncabezado = findViewById(R.id.tvEncabezado);
        etTitulo = findViewById(R.id.etTitulo);
        etFecha = findViewById(R.id.etFecha);
        etBuscar = findViewById(R.id.etBuscar);
        spPrioridad = findViewById(R.id.spPrioridad);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        ArrayAdapter<CharSequence> prioridades = ArrayAdapter.createFromResource(
                this, R.array.prioridades, android.R.layout.simple_spinner_item);
        prioridades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPrioridad.setAdapter(prioridades);
        spPrioridad.setSelection(1); // Media por defecto

        adapter = new TareaAdapter(this);
        RecyclerView lista = findViewById(R.id.rvTareas);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        btnGuardar.setOnClickListener(v -> guardar());
        btnCancelar.setOnClickListener(v -> limpiarFormulario());
        etBuscar.addTextChangedListener(new TextoCambiado(this::refrescar));

        refrescar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void guardar() {
        String titulo = etTitulo.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String prioridad = spPrioridad.getSelectedItem().toString();

        if (titulo.isEmpty()) { // Regla 1
            etTitulo.setError("El título es obligatorio");
            return;
        }
        if (!fechaValida(fecha)) { // Regla 4
            etFecha.setError("Usa una fecha válida con formato aaaa-mm-dd");
            return;
        }
        if (materiaDAO.buscar(materiaId) == null) { // Regla 2
            Toast.makeText(this, "La materia ya no existe", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (enEdicion == null) {
            tareaDAO.insertar(new Tarea(0, materiaId, titulo, fecha, prioridad));
            Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show();
        } else {
            enEdicion.setTitulo(titulo);
            enEdicion.setFechaEntrega(fecha);
            enEdicion.setPrioridad(prioridad);
            tareaDAO.actualizar(enEdicion);
            Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show();
        }
        limpiarFormulario();
        refrescar();
    }

    private void limpiarFormulario() {
        enEdicion = null;
        etTitulo.setText("");
        etFecha.setText("");
        etTitulo.setError(null);
        etFecha.setError(null);
        spPrioridad.setSelection(1);
        btnGuardar.setText("Agregar tarea");
        btnCancelar.setVisibility(View.GONE);
    }

    private void refrescar() {
        // Encabezado: siempre sobre todas las tareas de la materia, aunque haya un filtro activo
        List<Tarea> todas = tareaDAO.listarPorMateria(materiaId);
        int completadas = 0;
        for (Tarea t : todas) {
            if (t.isCompletada()) completadas++;
        }
        Materia materia = materiaDAO.buscar(materiaId);
        String nombre = (materia != null) ? materia.getNombre() : "";
        tvEncabezado.setText(nombre + " · " + completadas + " de " + todas.size() + " completadas");

        // Lista: filtrada por la búsqueda y ordenada por fecha (aaaa-mm-dd se ordena igual como texto)
        String texto = etBuscar.getText().toString().trim();
        List<Tarea> visibles = texto.isEmpty() ? todas : tareaDAO.buscarPorTitulo(materiaId, texto);
        Collections.sort(visibles, (a, b) -> a.getFechaEntrega().compareTo(b.getFechaEntrega()));
        adapter.mostrar(visibles);
    }

    private boolean fechaValida(String texto) {
        if (!texto.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false; // evita que se acepten textos como 2026-1-5 o 2026-10-20abc
        }
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formato.setLenient(false); // rechaza fechas imposibles como 2026-02-30
        try {
            formato.parse(texto);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    // ---------- Acciones de la lista ----------

    @Override
    public void editar(Tarea tarea) {
        enEdicion = tarea;
        etTitulo.setText(tarea.getTitulo());
        etFecha.setText(tarea.getFechaEntrega());
        String[] opciones = getResources().getStringArray(R.array.prioridades);
        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i].equals(tarea.getPrioridad())) {
                spPrioridad.setSelection(i);
            }
        }
        btnGuardar.setText("Actualizar tarea");
        btnCancelar.setVisibility(View.VISIBLE);
    }

    @Override
    public void cambiarCompletada(Tarea tarea, boolean completada) {
        tarea.setCompletada(completada);
        tareaDAO.actualizar(tarea);
        if (enEdicion != null && enEdicion.getId() == tarea.getId()) {
            enEdicion.setCompletada(completada); // que guardar() no revierta la marca
        }
        refrescar();
    }

    @Override
    public void eliminar(Tarea tarea) {
        tareaDAO.eliminar(tarea.getId());
        if (enEdicion != null && enEdicion.getId() == tarea.getId()) {
            limpiarFormulario();
        }
        refrescar();
    }
}
