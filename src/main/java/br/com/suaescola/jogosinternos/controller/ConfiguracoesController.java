package br.com.suaescola.jogosinternos.controller;

import br.com.suaescola.jogosinternos.dao.ConfiguracaoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;

/**
 * Tela de Configurações: liga/desliga os toggles guardados na tabela
 * configuracao (chave/valor) e usados pelas telas de placar.
 */
public class ConfiguracoesController {

    @FXML
    private CheckBox checkIndicarGol;
    @FXML
    private CheckBox checkIndicarBloqueio;
    @FXML
    private CheckBox checkExigirCartao;
    @FXML
    private CheckBox checkAbrirTelaPublica;

    private final ConfiguracaoDAO configuracaoDAO = new ConfiguracaoDAO();

    @FXML
    private void initialize() {
        checkIndicarGol.setSelected(configuracaoDAO.getBoolean("indicar_autor_gol", true));
        checkIndicarBloqueio.setSelected(configuracaoDAO.getBoolean("indicar_autor_bloqueio", true));
        checkExigirCartao.setSelected(configuracaoDAO.getBoolean("exigir_jogador_cartao", true));
        checkAbrirTelaPublica.setSelected(configuracaoDAO.getBoolean("abrir_tela_publica_automaticamente", true));
    }

    @FXML
    private void onSalvar() {
        configuracaoDAO.salvar("indicar_autor_gol", String.valueOf(checkIndicarGol.isSelected()));
        configuracaoDAO.salvar("indicar_autor_bloqueio", String.valueOf(checkIndicarBloqueio.isSelected()));
        configuracaoDAO.salvar("exigir_jogador_cartao", String.valueOf(checkExigirCartao.isSelected()));
        configuracaoDAO.salvar("abrir_tela_publica_automaticamente", String.valueOf(checkAbrirTelaPublica.isSelected()));

        Alert alerta = new Alert(AlertType.INFORMATION);
        alerta.setTitle("Configurações salvas");
        alerta.setHeaderText(null);
        alerta.setContentText("Vale a partir da próxima partida iniciada.");
        alerta.showAndWait();
    }
}
