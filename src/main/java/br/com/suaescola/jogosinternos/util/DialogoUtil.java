package br.com.suaescola.jogosinternos.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public final class DialogoUtil {

    private DialogoUtil() {
    }

    /** Mostra um diálogo de confirmação (OK/Cancelar) e retorna true se o usuário confirmou. */
    public static boolean confirmar(String titulo, String mensagem) {
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        return alerta.showAndWait().filter(botao -> botao == ButtonType.OK).isPresent();
    }
}
