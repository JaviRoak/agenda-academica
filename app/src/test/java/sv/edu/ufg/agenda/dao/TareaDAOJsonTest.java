package sv.edu.ufg.agenda.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sv.edu.ufg.agenda.modelo.Tarea;

/** Reto "prueba unitaria" (rol B): se ejecuta en la JVM, sin emulador. */
public class TareaDAOJsonTest {

    @Rule
    public TemporaryFolder carpeta = new TemporaryFolder();

    private File archivo;
    private TareaDAO dao;

    @Before
    public void preparar() {
        archivo = new File(carpeta.getRoot(), "datos/tareas.json");
        dao = new TareaDAOJson(archivo);
    }

    @Test
    public void insertarAsignaIdsConsecutivosYCreaElArchivo() {
        Tarea a = new Tarea(0, 1, "Guía 3", "2026-10-20");
        Tarea b = new Tarea(0, 1, "Parcial", "2026-10-27");
        dao.insertar(a);
        dao.insertar(b);

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertTrue(archivo.exists());
        assertEquals(2, dao.listar().size());
    }

    @Test
    public void listarPorMateriaSoloDevuelveLasDeEsaMateria() {
        dao.insertar(new Tarea(0, 1, "Guía 3", "2026-10-20"));
        dao.insertar(new Tarea(0, 2, "Laboratorio", "2026-10-22"));
        dao.insertar(new Tarea(0, 1, "Parcial", "2026-10-27"));

        List<Tarea> deLaMateria1 = dao.listarPorMateria(1);
        assertEquals(2, deLaMateria1.size());
        for (Tarea t : deLaMateria1) {
            assertEquals(1, t.getMateriaId());
        }
        assertEquals(1, dao.listarPorMateria(2).size());
        assertTrue(dao.listarPorMateria(99).isEmpty());
    }

    @Test
    public void eliminarQuitaLaTareaYPersisteElCambio() {
        dao.insertar(new Tarea(0, 1, "Guía 3", "2026-10-20"));
        dao.insertar(new Tarea(0, 1, "Parcial", "2026-10-27"));

        assertTrue(dao.eliminar(1));
        assertFalse(dao.eliminar(1)); // ya no existe

        TareaDAO otraInstancia = new TareaDAOJson(archivo); // lee de nuevo el archivo
        assertNull(otraInstancia.buscar(1));
        assertEquals(1, otraInstancia.listarPorMateria(1).size());
    }

    @Test
    public void actualizarCompletadaYContarPendientes() {
        dao.insertar(new Tarea(0, 1, "Guía 3", "2026-10-20"));
        dao.insertar(new Tarea(0, 1, "Parcial", "2026-10-27"));

        Tarea t = dao.buscar(1);
        t.setCompletada(true);
        assertTrue(dao.actualizar(t));

        assertEquals(1, dao.contarPendientes(1));
        assertTrue(new TareaDAOJson(archivo).buscar(1).isCompletada());
    }

    @Test
    public void buscarPorTituloIgnoraMayusculas() {
        dao.insertar(new Tarea(0, 1, "Guía práctica 3", "2026-10-20"));
        dao.insertar(new Tarea(0, 1, "Parcial", "2026-10-27"));

        assertEquals(1, dao.buscarPorTitulo(1, "GUÍA").size());
        assertEquals(0, dao.buscarPorTitulo(2, "guía").size());
    }

    @Test
    public void prioridadPorDefectoEsMedia() {
        dao.insertar(new Tarea(0, 1, "Sin prioridad", "2026-10-20"));
        assertEquals(Tarea.MEDIA, dao.buscar(1).getPrioridad());
    }
}
