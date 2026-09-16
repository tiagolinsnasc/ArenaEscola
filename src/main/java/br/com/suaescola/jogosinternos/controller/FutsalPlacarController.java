package br.com.suaescola.jogosinternos.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.suaescola.jogosinternos.dao.ConfiguracaoDAO;
import br.com.suaescola.jogosinternos.dao.EventoDAO;
import br.com.suaescola.jogosinternos.dao.PartidaDAO;
import br.com.suaescola.jogosinternos.estado.EstadoPartida;
import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Evento;
import br.com.suaescola.jogosinternos.model.Jogador;
import br.com.suaescola.jogosinternos.model.Partida;
import br.com.suaescola.jogosinternos.model.StatusPartida;
import br.com.suaescola.jogosinternos.model.TipoEvento;
import br.com.suaescola.jogosinternos.util.DialogoUtil;
import br.com.suaescola.jogosinternos.util.LogotipoUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller da tela de placar do Futsal -- é a tela de CONTROLE, visível
 * só para quem está gerenciando a partida (tem os botões de gol/cartão).
 * Cada equipe tem seu próprio log de eventos, exibido logo abaixo dos
 * botões dessa equipe. Ela escreve num EstadoPartida compartilhado, que a
 * tela pública (telão) apenas lê via binding.
 */
public class FutsalPlacarController {

    private static final String CSS_PRINCIPAL = "/br/com/suaescola/jogosinternos/css/style.css";
    private static final int DURACAO_TEMPO_SEGUNDOS = 20 * 60; // 20 minutos por tempo

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
    private Button botaoIniciarPausar;
    @FXML
    private ListView<RegistroEvento> listaEventosA;
    @FXML
    private ListView<RegistroEvento> listaEventosB;

    private final EventoDAO eventoDAO = new EventoDAO();
    private final PartidaDAO partidaDAO = new PartidaDAO();
    private final ConfiguracaoDAO configuracaoDAO = new ConfiguracaoDAO();

    private final EstadoPartida estado = new EstadoPartida();

    private Stage stageControle;
    private Stage stagePublica;

    private Partida partida;
    private Equipe equipeA;
    private Equipe equipeB;
    private List<Jogador> jogadoresA;
    private List<Jogador> jogadoresB;

    private int placarA = 0;
    private int placarB = 0;
    private int segundosRestantes = DURACAO_TEMPO_SEGUNDOS;
    private int periodoAtual = 1; // 1 ou 2
    private boolean tocando = false;
    private Timeline cronometro;

    // acumula amarelos por jogador nesta partida, para o vermelho automático
    private final Map<Integer, Integer> cartoesAmarelosPorJogador = new HashMap<>();

    /** Representa uma linha do log: o Evento salvo no banco + o texto exibido. */
    private static class RegistroEvento {
        final Evento evento;
        final String texto;

        RegistroEvento(Evento evento, String texto) {
            this.evento = evento;
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }

    /** Chamado pelo NovaPartidaController logo após carregar este FXML. */
    public void inicializar(Partida partida, Equipe equipeA, Equipe equipeB,
                             List<Jogador> jogadoresA, List<Jogador> jogadoresB) {
        this.partida = partida;
        this.equipeA = equipeA;
        this.equipeB = equipeB;
        this.jogadoresA = jogadoresA;
        this.jogadoresB = jogadoresB;

        labelNomeA.setText(equipeA.getNome());
        labelNomeB.setText(equipeB.getNome());
        carregarEscudo(escudoA, equipeA.getEscudoPath());
        carregarEscudo(escudoB, equipeB.getEscudoPath());
        logoEscola.setImage(LogotipoUtil.carregarLogotipoEscola());

        estado.nomeEquipeAProperty().set(equipeA.getNome());
        estado.nomeEquipeBProperty().set(equipeB.getNome());
        estado.escudoPathAProperty().set(equipeA.getEscudoPath());
        estado.escudoPathBProperty().set(equipeB.getEscudoPath());

        atualizarPlacar();
        atualizarPeriodoLabel();
        atualizarTempoLabel();

        cronometro = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        cronometro.setCycleCount(Timeline.INDEFINITE);

        if (configuracaoDAO.getBoolean("abrir_tela_publica_automaticamente", true)) {
            onAbrirTelaPublica();
        }
    }

    private void carregarEscudo(ImageView imageView, String caminho) {
        if (caminho != null && new File(caminho).exists()) {
            imageView.setImage(new Image(new File(caminho).toURI().toString()));
        }
    }

    /**
     * Chamado por quem abriu esta tela (NovaPartidaController), logo após
     * criar a Stage. Pede confirmação antes de fechar (evita perder o
     * progresso do jogo por engano) e, se confirmado, fecha o telão junto
     * -- não pode ficar um telão órfão, sem ninguém controlando a partida.
     */
    public void definirStage(Stage stage) {
        this.stageControle = stage;
        stage.setOnCloseRequest(evento -> {
            boolean confirmar = DialogoUtil.confirmar("Fechar tela de controle",
                    "Isso encerra o acompanhamento desta partida"
                    + (stagePublica != null ? " e fecha o telão também" : "") + ".\n"
                    + "O que já foi registrado continua salvo, mas você não vai mais conseguir lançar "
                    + "pontos/cartões nesta partida por aqui.\n\nDeseja realmente fechar?");
            if (!confirmar) {
                evento.consume();
                return;
            }
            fecharTelaPublicaSeAberta();
        });
    }

    private void fecharTelaPublicaSeAberta() {
        if (stagePublica != null) {
            stagePublica.close();
            stagePublica = null;
        }
    }

    @FXML
    private void onAbrirTelaPublica() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/br/com/suaescola/jogosinternos/fxml/TelaPublicaView.fxml")));
            Parent root = loader.load();

            TelaPublicaController controller = loader.getController();
            controller.inicializar(estado);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource(CSS_PRINCIPAL)).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Telão - " + equipeA.getNome() + " x " + equipeB.getNome());
            stage.setScene(scene);
            stage.setOnCloseRequest(evento -> {
                boolean confirmar = DialogoUtil.confirmar("Fechar tela pública",
                        "A tela de controle continua aberta, mas a torcida deixa de ver o placar.\n\n"
                        + "Deseja realmente fechar o telão?");
                if (!confirmar) {
                    evento.consume();
                    return;
                }
                stagePublica = null;
            });

            List<Screen> telas = Screen.getScreens();
            if (telas.size() > 1) {
                // se houver um segundo monitor/projetor, abre a tela pública nele, em tela cheia
                Rectangle2D area = telas.get(1).getVisualBounds();
                stage.setX(area.getMinX());
                stage.setY(area.getMinY());
                stage.setWidth(area.getWidth());
                stage.setHeight(area.getHeight());
            }
            stage.show();
            stagePublica = stage;
        } catch (IOException e) {
            mostrarErro("Não foi possível abrir a tela pública.", e);
        }
    }

    private void tick() {
        if (segundosRestantes > 0) {
            segundosRestantes--;
            atualizarTempoLabel();
        }
        if (segundosRestantes == 0) {
            pausar();
            Alert alerta = new Alert(AlertType.INFORMATION);
            alerta.setTitle("Fim do período");
            alerta.setHeaderText(periodoAtual == 1 ? "Fim do 1º tempo" : "Fim do 2º tempo");
            alerta.setContentText(periodoAtual == 1
                    ? "Clique em \"Avançar período\" para iniciar o 2º tempo."
                    : "A partida pode ser encerrada.");
            alerta.showAndWait();
        }
    }

    @FXML
    private void onIniciarPausar() {
        if (tocando) {
            pausar();
        } else {
            cronometro.play();
            tocando = true;
            botaoIniciarPausar.setText("Pausar");
        }
    }

    private void pausar() {
        cronometro.pause();
        tocando = false;
        botaoIniciarPausar.setText("Iniciar");
    }

    @FXML
    private void onAvancarPeriodo() {
        if (periodoAtual >= 2) {
            avisar("A partida já está no 2º tempo.");
            return;
        }
        pausar();
        periodoAtual = 2;
        segundosRestantes = DURACAO_TEMPO_SEGUNDOS;
        atualizarPeriodoLabel();
        atualizarTempoLabel();
        registrarLogGeral("Início do 2º tempo.");
    }

    private void atualizarTempoLabel() {
        int minutos = segundosRestantes / 60;
        int segundos = segundosRestantes % 60;
        String texto = String.format("%02d:%02d", minutos, segundos);
        labelTempo.setText(texto);
        estado.tempoFormatadoProperty().set(texto);
    }

    private void atualizarPeriodoLabel() {
        String texto = periodoAtual == 1 ? "1º Tempo" : "2º Tempo";
        labelPeriodo.setText(texto);
        estado.periodoAtualProperty().set(texto);
    }

    private void atualizarPlacar() {
        labelPlacar.setText(placarA + " x " + placarB);
        estado.placarAProperty().set(placarA);
        estado.placarBProperty().set(placarB);
    }

    @FXML
    private void onGolA() {
        registrarGol(equipeA, jogadoresA, true);
    }

    @FXML
    private void onGolB() {
        registrarGol(equipeB, jogadoresB, false);
    }

    private void registrarGol(Equipe equipe, List<Jogador> jogadores, boolean equipeAMarcou) {
        Jogador jogador = null;
        if (configuracaoDAO.getBoolean("indicar_autor_gol", true) && !jogadores.isEmpty()) {
            jogador = escolherJogador(jogadores, "Quem fez o gol?");
            if (jogador == null) {
                return; // cancelado
            }
        }

        if (equipeAMarcou) {
            placarA++;
        } else {
            placarB++;
        }
        atualizarPlacar();

        Evento evento = Evento.novoEvento(partida.getId(), jogador != null ? jogador.getId() : null,
                equipe.getId(), TipoEvento.GOL, periodoTexto(), tempoDecorridoSegundos());
        eventoDAO.salvar(evento);

        registrarLog(evento, "⚽ Gol" + (jogador != null ? " de " + jogador.getNome() : "")
                + " — " + placarA + " x " + placarB, equipeAMarcou);
    }

    @FXML
    private void onAmareloA() {
        registrarCartao(equipeA, jogadoresA, TipoEvento.CARTAO_AMARELO, true);
    }

    @FXML
    private void onAmareloB() {
        registrarCartao(equipeB, jogadoresB, TipoEvento.CARTAO_AMARELO, false);
    }

    @FXML
    private void onVermelhoA() {
        registrarCartao(equipeA, jogadoresA, TipoEvento.CARTAO_VERMELHO, true);
    }

    @FXML
    private void onVermelhoB() {
        registrarCartao(equipeB, jogadoresB, TipoEvento.CARTAO_VERMELHO, false);
    }

    private void registrarCartao(Equipe equipe, List<Jogador> jogadores, TipoEvento tipoCartao, boolean ehEquipeA) {
        Jogador jogador = escolherJogador(jogadores, "Cartão para qual jogador?");
        if (jogador == null) {
            return; // cartão sempre precisa de um jogador, para o controle de acúmulo
        }

        Evento evento = Evento.novoEvento(partida.getId(), jogador.getId(), equipe.getId(),
                tipoCartao, periodoTexto(), tempoDecorridoSegundos());
        eventoDAO.salvar(evento);

        String simbolo = tipoCartao == TipoEvento.CARTAO_AMARELO ? "🟨" : "🟥";
        registrarLog(evento, simbolo + " " + jogador.getNome(), ehEquipeA);

        if (tipoCartao == TipoEvento.CARTAO_AMARELO) {
            int quantidade = cartoesAmarelosPorJogador.merge(jogador.getId(), 1, Integer::sum);
            if (quantidade >= 2) {
                Evento vermelhoAutomatico = Evento.novoEvento(partida.getId(), jogador.getId(), equipe.getId(),
                        TipoEvento.CARTAO_VERMELHO, periodoTexto(), tempoDecorridoSegundos());
                eventoDAO.salvar(vermelhoAutomatico);
                registrarLog(vermelhoAutomatico,
                        "🟥 " + jogador.getNome() + " (2º amarelo)", ehEquipeA);
            }
        }
    }

    private Jogador escolherJogador(List<Jogador> jogadores, String titulo) {
        if (jogadores == null || jogadores.isEmpty()) {
            return null;
        }
        ChoiceDialog<Jogador> dialogo = new ChoiceDialog<>(jogadores.get(0), jogadores);
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(null);
        dialogo.setContentText("Jogador:");
        return dialogo.showAndWait().orElse(null);
    }

    /** Adiciona a mensagem ao log da equipe correspondente (tela de controle e estado compartilhado). */
    private void registrarLog(Evento evento, String mensagem, boolean ehEquipeA) {
        if (ehEquipeA) {
            listaEventosA.getItems().add(0, new RegistroEvento(evento, mensagem));
            estado.getEventosEquipeA().add(0, mensagem);
        } else {
            listaEventosB.getItems().add(0, new RegistroEvento(evento, mensagem));
            estado.getEventosEquipeB().add(0, mensagem);
        }
    }

    /** Mensagens que não pertencem a nenhuma equipe (ex.: início do 2º tempo) vão para os dois logs. */
    private void registrarLogGeral(String mensagem) {
        listaEventosA.getItems().add(0, new RegistroEvento(null, mensagem));
        listaEventosB.getItems().add(0, new RegistroEvento(null, mensagem));
        estado.getEventosEquipeA().add(0, mensagem);
        estado.getEventosEquipeB().add(0, mensagem);
    }

    @FXML
    private void onRemoverEventoA() {
        removerSelecionado(listaEventosA, estado.getEventosEquipeA());
    }

    @FXML
    private void onRemoverEventoB() {
        removerSelecionado(listaEventosB, estado.getEventosEquipeB());
    }

    private void removerSelecionado(ListView<RegistroEvento> lista, ObservableList<String> logCompartilhado) {
        RegistroEvento selecionado = lista.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            avisar("Selecione um evento no log para remover.");
            return;
        }
        if (selecionado.evento == null) {
            avisar("Esse item não é um evento removível.");
            return;
        }

        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Remover evento");
        confirmacao.setHeaderText("Remover este evento?");
        confirmacao.setContentText(selecionado.texto
                + "\n\nSe esse cartão amarelo tiver gerado um vermelho automático, remova o vermelho separadamente.");

        confirmacao.showAndWait().filter(botao -> botao == ButtonType.OK).ifPresent(botao -> {
            reverterEvento(selecionado.evento);
            eventoDAO.excluir(selecionado.evento.getId());

            int indice = lista.getItems().indexOf(selecionado);
            lista.getItems().remove(selecionado);
            if (indice >= 0 && indice < logCompartilhado.size()) {
                logCompartilhado.remove(indice);
            }

            atualizarPlacar();
        });
    }

    private void reverterEvento(Evento evento) {
        switch (evento.getTipo()) {
            case GOL -> {
                if (evento.getEquipeId() == equipeA.getId()) {
                    placarA = Math.max(0, placarA - 1);
                } else {
                    placarB = Math.max(0, placarB - 1);
                }
            }
            case CARTAO_AMARELO -> {
                if (evento.getJogadorId() != null) {
                    cartoesAmarelosPorJogador.computeIfPresent(
                            evento.getJogadorId(), (id, quantidade) -> quantidade > 1 ? quantidade - 1 : null);
                }
            }
            default -> {
                // cartão vermelho: nada a reverter no placar
            }
        }
    }

    private String periodoTexto() {
        return periodoAtual == 1 ? "1T" : "2T";
    }

    private int tempoDecorridoSegundos() {
        return DURACAO_TEMPO_SEGUNDOS - segundosRestantes;
    }

    @FXML
    private void onEncerrarPartida() {
        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Encerrar partida");
        confirmacao.setHeaderText(
                "Encerrar " + equipeA.getNome() + " " + placarA + " x " + placarB + " " + equipeB.getNome() + "?");
        confirmacao.setContentText("Essa ação não pode ser desfeita.");

        confirmacao.showAndWait().filter(botao -> botao == ButtonType.OK).ifPresent(botao -> {
            pausar();
            partida.setPlacarA(placarA);
            partida.setPlacarB(placarB);
            partida.setStatus(StatusPartida.ENCERRADA);
            partidaDAO.atualizar(partida);

            Alert fim = new Alert(AlertType.INFORMATION);
            fim.setTitle("Partida encerrada");
            fim.setHeaderText(
                    "Resultado final: " + equipeA.getNome() + " " + placarA + " x " + placarB + " " + equipeB.getNome());
            fim.setContentText("A geração da súmula em PDF será implementada na etapa de Relatórios.");
            fim.showAndWait();

            fecharTelaPublicaSeAberta();
            ((Stage) listaEventosA.getScene().getWindow()).close();
        });
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
