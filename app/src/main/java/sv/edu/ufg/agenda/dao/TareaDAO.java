package sv.edu.ufg.agenda.dao;

import java.util.List;
import sv.edu.ufg.agenda.modelo.Tarea;

public interface TareaDAO extends DAO<Tarea> {
    List<Tarea> listarPorMateria(int materiaId);

    /** Actividad "Búsqueda": tareas de una materia cuyo título contiene el texto. */
    List<Tarea> buscarPorTitulo(int materiaId, String texto);

    /** Actividad "Resumen por materia": cuántas tareas no completadas tiene la materia. */
    int contarPendientes(int materiaId);
}
