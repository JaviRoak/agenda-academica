package sv.edu.ufg.agenda.ui;

import android.text.Editable;
import android.text.TextWatcher;

/** TextWatcher reducido a un solo método, para los campos de búsqueda. */
class TextoCambiado implements TextWatcher {

    private final Runnable accion;

    TextoCambiado(Runnable accion) {
        this.accion = accion;
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override public void afterTextChanged(Editable s) { accion.run(); }
}
