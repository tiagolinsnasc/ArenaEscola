package br.com.suaescola.jogosinternos.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.suaescola.jogosinternos.dao.EquipeDAO;
import br.com.suaescola.jogosinternos.dao.EventoDAO;
import br.com.suaescola.jogosinternos.dao.JogadorDAO;
import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Evento;
import br.com.suaescola.jogosinternos.model.Jogador;
import br.com.suaescola.jogosinternos.model.Modalidade;
import br.com.suaescola.jogosinternos.model.TipoEvento;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Consolida, a partir de todos os eventos já registrados no banco: o
 * ranking de maiores pontuadores/goleadores e o ranking de cartões,
 * filtráveis por modalidade.
 */
public class EstatisticasController {

    @FXML
    private ComboBox<String> comboModalidade;
    @FXML
    private TableView<LinhaPontuador> tabelaPontuadores;
    @FXML
    private TableColumn<LinhaPontuador, String> colPontuadorJogador;
    @FXML
    private TableColumn<LinhaPontuador, String> colPontuadorEquipe;
    @FXML
    private TableColumn<LinhaPontuador, String> colPontuadorQuantidade;
    @FXML
    private TableView<LinhaCartoes> tabelaCartoes;
    @FXML
    private TableColumn<LinhaCartoes, String> colCartaoJogador;
    @FXML
    private TableColumn<LinhaCartoes, String> colCartaoEquipe;
    @FXML
    private TableColumn<LinhaCartoes, String> colCartaoAmarelos;
    @FXML
    private TableColumn<LinhaCartoes, String> colCartaoVermelhos;

    private final EventoDAO eventoDAO = new EventoDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();
    private final EquipeDAO equipeDAO = new EquipeDAO();

    private static final String OPCAO_TODAS = "Todas as modalidades";
    private static final String OPCAO_FUTSAL = "Futsal";
    private static final String OPCAO_VOLEI = "Vôlei";

    private record LinhaPontuador(String jogador, String equipe, int quantidade) {
    }

    private record LinhaCartoes(String jogador, String equipe, int amarelos, int vermelhos) {
    }

    @FXML
    private void initialize() {
        comboModalidade.setItems(FXCollections.observableArrayList(OPCAO_TODAS, OPCAO_FUTSAL, OPCAO_VOLEI));
        comboModalidade.setValue(OPCAO_TODAS);
        comboModalidade.valueProperty().addListener((obs, antiga, nova) -> atualizar());

        colPontuadorJogador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().jogador()));
        colPontuadorEquipe.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().equipe()));
        colPontuadorQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().quantidade())));

        colCartaoJogador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().jogador()));
        colCartaoEquipe.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().equipe()));
        colCartaoAmarelos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().amarelos())));
        colCartaoVermelhos.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().vermelhos())));

        atualizar();
    }

    private void atualizar() {
        Modalidade filtro = modalidadeSelecionada();

        Map<Integer, Equipe> equipesPorId = new HashMap<>();
        for (Equipe equipe : equipeDAO.listarTodas()) {
            equipesPorId.put(equipe.getId(), equipe);
        }
        Map<Integer, Jogador> jogadoresPorId = new HashMap<>();
        for (Jogador jogador : jogadorDAO.listarTodos()) {
            jogadoresPorId.put(jogador.getId(), jogador);
        }

        atualizarPontuadores(filtro, equipesPorId, jogadoresPorId);
        atualizarCartoes(filtro, equipesPorId, jogadoresPorId);
    }

    private Modalidade modalidadeSelecionada() {
        String selecao = comboModalidade.getValue();
        if (OPCAO_FUTSAL.equals(selecao)) {
            return Modalidade.FUTSAL;
        }
        if (OPCAO_VOLEI.equals(selecao)) {
            return Modalidade.VOLEI;
        }
        return null; // todas
    }

    private void atualizarPontuadores(Modalidade filtro, Map<Integer, Equipe> equipesPorId, Map<Integer, Jogador> jogadoresPorId) {
        List<Evento> eventosDePontuacao = new ArrayList<>();
        eventosDePontuacao.addAll(eventoDAO.listarPorTipo(TipoEvento.GOL));
        eventosDePontuacao.addAll(eventoDAO.listarPorTipo(TipoEvento.PONTO));
        eventosDePontuacao.addAll(eventoDAO.listarPorTipo(TipoEvento.BLOQUEIO));

        Map<Integer, Integer> contagem = new HashMap<>();
        for (Evento evento : eventosDePontuacao) {
            Jogador jogador = jogadorValido(evento, filtro, equipesPorId, jogadoresPorId);
            if (jogador == null) {
                continue;
            }
            contagem.merge(jogador.getId(), 1, Integer::sum);
        }

        List<LinhaPontuador> linhas = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entrada : contagem.entrySet()) {
            Jogador jogador = jogadoresPorId.get(entrada.getKey());
            Equipe equipe = equipesPorId.get(jogador.getEquipeId());
            linhas.add(new LinhaPontuador(jogador.getNome(), equipe != null ? equipe.getNome() : "-", entrada.getValue()));
        }
        linhas.sort(Comparator.comparingInt(LinhaPontuador::quantidade).reversed());

        tabelaPontuadores.setItems(FXCollections.observableArrayList(linhas));
    }

    private void atualizarCartoes(Modalidade filtro, Map<Integer, Equipe> equipesPorId, Map<Integer, Jogador> jogadoresPorId) {
        Map<Integer, int[]> contagem = new HashMap<>(); // jogadorId -> [amarelos, vermelhos]

        for (Evento evento : eventoDAO.listarPorTipo(TipoEvento.CARTAO_AMARELO)) {
            Jogador jogador = jogadorValido(evento, filtro, equipesPorId, jogadoresPorId);
            if (jogador != null) {
                contagem.computeIfAbsent(jogador.getId(), id -> new int[2])[0]++;
            }
        }
        for (Evento evento : eventoDAO.listarPorTipo(TipoEvento.CARTAO_VERMELHO)) {
            Jogador jogador = jogadorValido(evento, filtro, equipesPorId, jogadoresPorId);
            if (jogador != null) {
                contagem.computeIfAbsent(jogador.getId(), id -> new int[2])[1]++;
            }
        }

        List<LinhaCartoes> linhas = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entrada : contagem.entrySet()) {
            Jogador jogador = jogadoresPorId.get(entrada.getKey());
            Equipe equipe = equipesPorId.get(jogador.getEquipeId());
            linhas.add(new LinhaCartoes(jogador.getNome(), equipe != null ? equipe.getNome() : "-",
                    entrada.getValue()[0], entrada.getValue()[1]));
        }
        linhas.sort(Comparator.comparingInt((LinhaCartoes l) -> l.amarelos() + l.vermelhos()).reversed());

        tabelaCartoes.setItems(FXCollections.observableArrayList(linhas));
    }

    /** Retorna o jogador do evento, ou null se o evento não tiver jogador ou não passar no filtro de modalidade. */
    private Jogador jogadorValido(Evento evento, Modalidade filtro, Map<Integer, Equipe> equipesPorId, Map<Integer, Jogador> jogadoresPorId) {
        if (evento.getJogadorId() == null) {
            return null;
        }
        Jogador jogador = jogadoresPorId.get(evento.getJogadorId());
        if (jogador == null) {
            return null;
        }
        if (filtro == null) {
            return jogador;
        }
        Equipe equipe = equipesPorId.get(jogador.getEquipeId());
        return (equipe != null && equipe.getModalidade() == filtro) ? jogador : null;
    }
}
