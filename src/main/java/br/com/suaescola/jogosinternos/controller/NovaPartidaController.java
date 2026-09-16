package br.com.suaescola.jogosinternos.controller;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.controlsfx.control.CheckListView;

import br.com.suaescola.jogosinternos.dao.EquipeDAO;
import br.com.suaescola.jogosinternos.dao.JogadorDAO;
import br.com.suaescola.jogosinternos.dao.PartidaDAO;
import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Jogador;
import br.com.suaescola.jogosinternos.model.Modalidade;
import br.com.suaescola.jogosinternos.model.Partida;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

/**
 * Controller da tela de Nova Partida: escolhe modalidade, as duas equipes
 * (já filtradas pela modalidade escolhida) e os titulares de cada uma.
 * Ao iniciar, cria a partida no banco e abre a tela de placar
 * correspondente à modalidade.
 *
 * A seleção de titulares ainda não é persistida no banco -- não existe
 * uma tabela de "escalação" no schema atual. Por enquanto ela serve para
 * o operador organizar quem está em quadra antes de começar.
 */
public class NovaPartidaController {

    private static final String CSS_PRINCIPAL = "/br/com/suaescola/jogosinternos/css/style.css";

    @FXML
    private ComboBox<Modalidade> comboModalidade;
    @FXML
    private ComboBox<Equipe> comboEquipeA;
    @FXML
    private ComboBox<Equipe> comboEquipeB;
    @FXML
    private CheckListView<Jogador> listaJogadoresA;
    @FXML
    private CheckListView<Jogador> listaJogadoresB;

    private final EquipeDAO equipeDAO = new EquipeDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();
    private final PartidaDAO partidaDAO = new PartidaDAO();

    @FXML
    private void initialize() {
        comboModalidade.setItems(FXCollections.observableArrayList(Modalidade.values()));

        comboModalidade.valueProperty().addListener((obs, antiga, nova) -> {
            ObservableList<Equipe> equipes = nova != null
                    ? FXCollections.observableArrayList(equipeDAO.listarPorModalidade(nova))
                    : FXCollections.observableArrayList();
            comboEquipeA.setItems(equipes);
            comboEquipeB.setItems(equipes);
            comboEquipeA.setValue(null);
            comboEquipeB.setValue(null);
            listaJogadoresA.getItems().clear();
            listaJogadoresB.getItems().clear();
        });

        comboEquipeA.valueProperty().addListener((obs, antiga, nova) ->
                carregarJogadores(listaJogadoresA, nova));
        comboEquipeB.valueProperty().addListener((obs, antiga, nova) ->
                carregarJogadores(listaJogadoresB, nova));
    }

    private void carregarJogadores(CheckListView<Jogador> lista, Equipe equipe) {
        List<Jogador> jogadores = equipe != null ? jogadorDAO.listarPorEquipe(equipe.getId()) : List.of();
        lista.setItems(FXCollections.observableArrayList(jogadores));
    }

    @FXML
    private void onIniciarPartida(ActionEvent event) {
        Modalidade modalidade = comboModalidade.getValue();
        Equipe equipeA = comboEquipeA.getValue();
        Equipe equipeB = comboEquipeB.getValue();

        if (modalidade == null || equipeA == null || equipeB == null) {
            avisar("Escolha a modalidade e as duas equipes antes de iniciar a partida.");
            return;
        }
        if (equipeA.getId() == equipeB.getId()) {
            avisar("Escolha duas equipes diferentes.");
            return;
        }

        Partida partida = Partida.novaPartida(modalidade, equipeA.getId(), equipeB.getId());
        partidaDAO.salvar(partida);

        if (modalidade == Modalidade.FUTSAL) {
            abrirPlacarFutsal(partida, equipeA, equipeB);
            fecharJanelaAtual(event);
        } else {
            abrirPlacarVolei(partida, equipeA, equipeB);
            fecharJanelaAtual(event);
        }
    }

    private void abrirPlacarFutsal(Partida partida, Equipe equipeA, Equipe equipeB) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/br/com/suaescola/jogosinternos/fxml/FutsalPlacarView.fxml")));
            Parent root = loader.load();

            FutsalPlacarController controller = loader.getController();
            controller.inicializar(partida, equipeA, equipeB,
                    jogadorDAO.listarPorEquipe(equipeA.getId()),
                    jogadorDAO.listarPorEquipe(equipeB.getId()));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(CSS_PRINCIPAL)).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Placar - Futsal - " + equipeA.getNome() + " x " + equipeB.getNome());
            stage.setScene(scene);
            controller.definirStage(stage);
            stage.show();
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir a tela de placar.", e);
        }
    }

    private void abrirPlacarVolei(Partida partida, Equipe equipeA, Equipe equipeB) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/br/com/suaescola/jogosinternos/fxml/VoleiPlacarView.fxml")));
            Parent root = loader.load();

            VoleiPlacarController controller = loader.getController();
            controller.inicializar(partida, equipeA, equipeB,
                    jogadorDAO.listarPorEquipe(equipeA.getId()),
                    jogadorDAO.listarPorEquipe(equipeB.getId()));

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(CSS_PRINCIPAL)).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Placar - Vôlei - " + equipeA.getNome() + " x " + equipeB.getNome());
            stage.setScene(scene);
            controller.definirStage(stage);
            stage.show();
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir a tela de placar.", e);
        }
    }

    private void fecharJanelaAtual(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    private void avisar(String mensagem) {
        Alert alerta = new Alert(AlertType.WARNING);
        alerta.setTitle("Dados incompletos");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErro(String mensagem, Exception causa) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(mensagem);
        alerta.setContentText(causa.getMessage());
        alerta.showAndWait();
    }
}

