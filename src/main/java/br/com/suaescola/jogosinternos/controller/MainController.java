package br.com.suaescola.jogosinternos.controller;

import java.io.IOException;
import java.util.Objects;

import br.com.suaescola.jogosinternos.util.LogotipoUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Controller da tela inicial (dashboard).
 */
public class MainController {

    private static final String CSS_PRINCIPAL =
            "/br/com/suaescola/jogosinternos/css/style.css";

    @FXML
    private ImageView logoEscola;

    @FXML
    private void initialize() {
        logoEscola.setImage(LogotipoUtil.carregarLogotipoEscola());
    }

    @FXML
    private void abrirCadastroEquipes() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/CadastroEquipesView.fxml", "Cadastro de Equipes");
    }

    @FXML
    private void abrirCadastroJogadores() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/CadastroJogadoresView.fxml", "Cadastro de Jogadores");
    }

    @FXML
    private void abrirImportacaoLote() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/ImportacaoLoteView.fxml", "Importação em Lote");
    }

    @FXML
    private void abrirNovaPartida() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/NovaPartidaView.fxml", "Nova Partida");
    }

    @FXML
    private void abrirHistorico() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/HistoricoPartidasView.fxml", "Histórico de Partidas");
    }

    @FXML
    private void abrirRelatorios() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/HistoricoPartidasView.fxml", "Relatórios e Súmulas");
    }

    @FXML
    private void abrirEstatisticas() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/EstatisticasView.fxml", "Estatísticas Consolidadas");
    }

    @FXML
    private void abrirConfiguracoes() {
        abrirJanela("/br/com/suaescola/jogosinternos/fxml/ConfiguracoesView.fxml", "Configurações");
    }

    @FXML
    private void sair() {
        System.exit(0);
    }

    @FXML
    private void abrirSobre() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Sobre");
        alert.setHeaderText("Jogos Internos - Gestão de Partidas");
        alert.setContentText(
                "Versão 1.0\n\n"
                + "Desenvolvido por Thiago Lins do Nascimento.");
        alert.showAndWait();
    }

    /** Carrega um FXML em uma nova janela independente (não-modal). */
    private void abrirJanela(String caminhoFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(caminhoFxml),
                            "FXML não encontrado: " + caminhoFxml));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(CSS_PRINCIPAL)).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro ao abrir tela");
            alert.setHeaderText(titulo);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}

