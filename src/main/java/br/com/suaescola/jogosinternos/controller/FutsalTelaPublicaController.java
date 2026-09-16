package br.com.suaescola.jogosinternos.controller;

import java.io.File;

import br.com.suaescola.jogosinternos.estado.EstadoPartida;
import br.com.suaescola.jogosinternos.util.LogotipoUtil;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller da tela pública (telão): somente leitura, sem nenhum botão de
 * controle. Cada equipe tem seu próprio log de eventos, exibido logo
 * abaixo do escudo e nome dela. Todo o conteúdo é ligado (bind) ao
 * EstadoPartida compartilhado com a tela de controle -- atualiza sozinho,
 * sem consultar o banco.
 */
public class FutsalTelaPublicaController {

    @FXML
    private ImageView escudoA;
    @FXML
    private ImageView escudoB;
    @FXML
    private ImageView logoEscola;
    @FXML
    private Label labelNomeA;
    @FXML
    private Label labelNomeB;
    @FXML
    private Label labelPlacar;
    @FXML
    private Label labelPeriodo;
    @FXML
    private Label labelTempo;
    @FXML
    private ListView<String> listaEventosA;
    @FXML
    private ListView<String> listaEventosB;

    public void inicializar(EstadoPartida estado) {
        labelNomeA.textProperty().bind(estado.nomeEquipeAProperty());
        labelNomeB.textProperty().bind(estado.nomeEquipeBProperty());
        labelPeriodo.textProperty().bind(estado.periodoAtualProperty());
        labelTempo.textProperty().bind(estado.tempoFormatadoProperty());
        labelPlacar.textProperty().bind(Bindings.concat(
                estado.placarAProperty().asString(), " x ", estado.placarBProperty().asString()));

        listaEventosA.setItems(estado.getEventosEquipeA());
        listaEventosB.setItems(estado.getEventosEquipeB());

        logoEscola.setImage(LogotipoUtil.carregarLogotipoEscola());

        carregarEscudo(escudoA, estado.escudoPathAProperty().get());
        carregarEscudo(escudoB, estado.escudoPathBProperty().get());
        estado.escudoPathAProperty().addListener((obs, antigo, novo) -> carregarEscudo(escudoA, novo));
        estado.escudoPathBProperty().addListener((obs, antigo, novo) -> carregarEscudo(escudoB, novo));
    }

    private void carregarEscudo(ImageView imageView, String caminho) {
        if (caminho != null && new File(caminho).exists()) {
            imageView.setImage(new Image(new File(caminho).toURI().toString()));
        }
    }
}
