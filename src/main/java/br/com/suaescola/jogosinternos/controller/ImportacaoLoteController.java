package br.com.suaescola.jogosinternos.controller;

import br.com.suaescola.jogosinternos.service.ImportadorLoteService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ImportacaoLoteController {

    @FXML
    private TextArea campoTexto;
    @FXML
    private TextArea areaResultado;

    private final ImportadorLoteService importadorLoteService = new ImportadorLoteService();

    @FXML
    private void onProcessar() {
        String texto = campoTexto.getText();
        if (texto == null || texto.isBlank()) {
            areaResultado.setText("Cole o texto no padrão indicado antes de processar.");
            return;
        }

        String log = importadorLoteService.importar(texto);
        areaResultado.setText(log.isBlank() ? "Nada foi importado -- confira o padrão do texto." : log);
    }
}
