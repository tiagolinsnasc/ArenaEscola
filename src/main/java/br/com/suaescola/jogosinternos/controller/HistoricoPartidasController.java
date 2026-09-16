package br.com.suaescola.jogosinternos.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.suaescola.jogosinternos.dao.EquipeDAO;
import br.com.suaescola.jogosinternos.dao.EventoDAO;
import br.com.suaescola.jogosinternos.dao.JogadorDAO;
import br.com.suaescola.jogosinternos.dao.PartidaDAO;
import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Evento;
import br.com.suaescola.jogosinternos.model.Jogador;
import br.com.suaescola.jogosinternos.model.Modalidade;
import br.com.suaescola.jogosinternos.model.Partida;
import br.com.suaescola.jogosinternos.model.StatusPartida;
import br.com.suaescola.jogosinternos.service.RelatorioPdfService;
import br.com.suaescola.jogosinternos.util.DialogoUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Lista todas as partidas registradas (em andamento ou encerradas),
 * permite gerar a súmula em PDF de uma partida selecionada e excluir
 * registros.
 */
public class HistoricoPartidasController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private TableView<Partida> tabelaPartidas;
    @FXML
    private TableColumn<Partida, String> colData;
    @FXML
    private TableColumn<Partida, String> colModalidade;
    @FXML
    private TableColumn<Partida, String> colEquipeA;
    @FXML
    private TableColumn<Partida, String> colPlacar;
    @FXML
    private TableColumn<Partida, String> colEquipeB;
    @FXML
    private TableColumn<Partida, String> colStatus;

    private final PartidaDAO partidaDAO = new PartidaDAO();
    private final EquipeDAO equipeDAO = new EquipeDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();
    private final EventoDAO eventoDAO = new EventoDAO();
    private final RelatorioPdfService relatorioPdfService = new RelatorioPdfService();

    private final Map<Integer, Equipe> cacheEquipes = new HashMap<>();
    private final ObservableList<Partida> partidas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colData.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getDataHora().format(FORMATO_DATA)));
        colModalidade.setCellValueFactory(dados -> new SimpleStringProperty(textoModalidade(dados.getValue().getModalidade())));
        colEquipeA.setCellValueFactory(dados -> new SimpleStringProperty(nomeEquipe(dados.getValue().getEquipeAId())));
        colPlacar.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().getPlacarA() + " x " + dados.getValue().getPlacarB()));
        colEquipeB.setCellValueFactory(dados -> new SimpleStringProperty(nomeEquipe(dados.getValue().getEquipeBId())));
        colStatus.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().getStatus() == StatusPartida.ENCERRADA ? "Encerrada" : "Em andamento"));

        tabelaPartidas.setItems(partidas);
        carregar();
    }

    private void carregar() {
        cacheEquipes.clear();
        for (Equipe equipe : equipeDAO.listarTodas()) {
            cacheEquipes.put(equipe.getId(), equipe);
        }
        partidas.setAll(partidaDAO.listarTodas());
    }

    private String nomeEquipe(int id) {
        Equipe equipe = cacheEquipes.get(id);
        return equipe != null ? equipe.getNome() : "Equipe #" + id;
    }

    private String textoModalidade(Modalidade modalidade) {
        return modalidade == Modalidade.FUTSAL ? "Futsal" : "Vôlei";
    }

    @FXML
    private void onGerarSumula() {
        Partida selecionada = tabelaPartidas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            avisar("Selecione uma partida na lista.");
            return;
        }

        Equipe equipeA = cacheEquipes.get(selecionada.getEquipeAId());
        Equipe equipeB = cacheEquipes.get(selecionada.getEquipeBId());
        if (equipeA == null || equipeB == null) {
            avisar("Não foi possível identificar as equipes dessa partida (podem ter sido excluídas).");
            return;
        }

        List<Evento> eventos = eventoDAO.listarPorPartida(selecionada.getId());

        Map<Integer, Jogador> jogadoresPorId = new HashMap<>();
        for (Jogador jogador : jogadorDAO.listarPorEquipe(equipeA.getId())) {
            jogadoresPorId.put(jogador.getId(), jogador);
        }
        for (Jogador jogador : jogadorDAO.listarPorEquipe(equipeB.getId())) {
            jogadoresPorId.put(jogador.getId(), jogador);
        }

        try {
            File arquivo = relatorioPdfService.gerarSumula(selecionada, equipeA, equipeB, eventos, jogadoresPorId);

            Alert alerta = new Alert(AlertType.INFORMATION);
            alerta.setTitle("Súmula gerada");
            alerta.setHeaderText("PDF gerado com sucesso");
            alerta.setContentText(arquivo.getAbsolutePath());
            alerta.showAndWait();

            abrirArquivo(arquivo);
        } catch (IOException e) {
            mostrarErro("Não foi possível gerar a súmula.", e);
        }
    }

    private void abrirArquivo(File arquivo) {
        // Desktop.open() usa AWT por baixo dos panos; chamá-lo na JavaFX Application
        // Thread pode travar o programa (conflito de toolkit nativo, comum no Linux).
        // Por isso roda numa thread separada.
        Thread thread = new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(arquivo);
                }
            } catch (IOException | UnsupportedOperationException ignorada) {
                // não é crítico -- o caminho do arquivo já foi mostrado no alerta acima
            }
        }, "abrir-pdf-sumula");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onExcluirPartida() {
        Partida selecionada = tabelaPartidas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            avisar("Selecione uma partida na lista.");
            return;
        }

        boolean confirmar = DialogoUtil.confirmar("Excluir partida",
                "Excluir " + nomeEquipe(selecionada.getEquipeAId()) + " x " + nomeEquipe(selecionada.getEquipeBId())
                + " e todos os eventos registrados nela? Essa ação não pode ser desfeita.");
        if (!confirmar) {
            return;
        }

        partidaDAO.excluir(selecionada.getId());
        carregar();
    }

    private void avisar(String mensagem) {
        Alert alerta = new Alert(AlertType.WARNING);
        alerta.setTitle("Aviso");
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
