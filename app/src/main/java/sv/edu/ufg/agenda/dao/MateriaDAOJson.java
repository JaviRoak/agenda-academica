package sv.edu.ufg.agenda.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import sv.edu.ufg.agenda.modelo.Materia;

public class MateriaDAOJson extends JsonDAOBase<Materia> implements MateriaDAO {

    public MateriaDAOJson(File archivo) {
        super(archivo, Materia.class);
    }

    @Override
    public List<Materia> buscarPorNombre(String texto) {
        String buscado = texto.trim().toLowerCase();
        List<Materia> resultado = new ArrayList<>();
        for (Materia m : cargar()) {
            if (m.getNombre().toLowerCase().contains(buscado)) {
                resultado.add(m);
            }
        }
        return resultado;
    }
}
