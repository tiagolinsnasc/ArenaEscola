package br.com.suaescola.jogosinternos.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller da tela de placar do Vôlei -- tela de CONTROLE, visível só
 * para quem está gerenciando a partida.
 *
 * Regras aplicadas (padrão escolar, melhor de 3 sets):
 * - Cada set vai até 25 pontos (o 3º set, decisivo, vai até 15), sempre
 *   com diferença mínima de 2 pontos para fechar o set.
 * - A partida termina quando uma equipe vence 2 sets.
 * - Bloqueio conta como ponto (respeitando a configuração de indicar o
 *   autor do bloqueio).
 * - Cartão vermelho, na regra oficial do vôlei, dá ponto automático para
 *   a equipe adversária -- diferente do futsal, onde só reduz o time a
 *   um jogador a menos.
 * - Não há cronômetro de jogo regressivo (o vôlei não é cronometrado);
 *   o tempo mostrado é só um cronômetro corrido informativo.
 */
public class VoleiPlacarController {

    private static final String CSS_PRINCIPAL = "/br/com/suaescola/jogosinternos/css/style.css";
    private static final int PONTOS_SET_NORMAL = 25;
    private static final int PONTOS_SET_DECISIVO = 15;
    private static final int DIFERENCA_MINIMA = 2;
    private static final int SETS_PARA_VENCER = 2;
    private static final int NUMERO_SET_DECISIVO = 3;

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
    private Label labelSets;
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

    private int pontosSetA = 0;
    private int pontosSetB = 0;
    private int setsGanhosA = 0;
    private int setsGanhosB = 0;
    private int numeroSetAtual = 1;

    private int segundosDecorridos = 0;
    private boolean tocando = false;
    private Timeline cronometro;

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
        atualizarLabelSets();
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

    /** Pede confirmação antes de fechar (evita perder o progresso do jogo por engano). */
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
        segundosDecorridos++;
        atualizarTempoLabel();
    }

    @FXML
    private void onIniciarPausar() {
        if (tocando) {
            cronometro.pause();
            tocando = false;
            botaoIniciarPausar.setText("Iniciar");
        } else {
            cronometro.play();
            tocando = true;
            botaoIniciarPausar.setText("Pausar");
        }
    }

    private void atualizarTempoLabel() {
        int minutos = segundosDecorridos / 60;
        int segundos = segundosDecorridos % 60;
        String texto = String.format("%02d:%02d", minutos, segundos);
        labelTempo.setText(texto);
        estado.tempoFormatadoProperty().set(texto);
    }

    private void atualizarLabelSets() {
        String texto = "Set " + numeroSetAtual + "  —  Sets: " + setsGanhosA + " x " + setsGanhosB;
        labelSets.setText(texto);
        estado.periodoAtualProperty().set(texto);
    }

    private void atualizarPlacar() {
        labelPlacar.setText(pontosSetA + " x " + pontosSetB);
        estado.placarAProperty().set(pontosSetA);
        estado.placarBProperty().set(pontosSetB);
    }

    @FXML
    private void onPontoA() {
        registrarPonto(equipeA, jogadoresA, true, TipoEvento.PONTO, "indicar_autor_gol", "🏐 Ponto");
    }

    @FXML
    private void onPontoB() {
        registrarPonto(equipeB, jogadoresB, false, TipoEvento.PONTO, "indicar_autor_gol", "🏐 Ponto");
    }

    @FXML
    private void onBloqueioA() {
        registrarPonto(equipeA, jogadoresA, true, TipoEvento.BLOQUEIO, "indicar_autor_bloqueio", "🖐 Bloqueio");
    }

    @FXML
    private void onBloqueioB() {
        registrarPonto(equipeB, jogadoresB, false, TipoEvento.BLOQUEIO, "indicar_autor_bloqueio", "🖐 Bloqueio");
    }

    private void registrarPonto(Equipe equipe, List<Jogador> jogadores, boolean ehEquipeA,
                                 TipoEvento tipo, String chaveConfiguracao, String rotulo) {
        Jogador jogador = null;
        if (configuracaoDAO.getBoolean(chaveConfiguracao, true) && !jogadores.isEmpty()) {
            jogador = escolherJogador(jogadores, "Quem fez o " + (tipo == TipoEvento.BLOQUEIO ? "bloqueio" : "ponto") + "?");
            if (jogador == null) {
                return; // cancelado
            }
        }

        somarPonto(ehEquipeA);

        Evento evento = Evento.novoEvento(partida.getId(), jogador != null ? jogador.getId() : null,
                equipe.getId(), tipo, "Set " + numeroSetAtual, segundosDecorridos);
        eventoDAO.salvar(evento);

        registrarLog(evento, rotulo + (jogador != null ? " de " + jogador.getNome() : "")
                + " — " + pontosSetA + " x " + pontosSetB, ehEquipeA);

        verificarFimDeSet();
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
            return;
        }

        Evento evento = Evento.novoEvento(partida.getId(), jogador.getId(), equipe.getId(),
                tipoCartao, "Set " + numeroSetAtual, segundosDecorridos);
        eventoDAO.salvar(evento);

        if (tipoCartao == TipoEvento.CARTAO_AMARELO) {
            registrarLog(evento, "🟨 " + jogador.getNome() + " (advertência)", ehEquipeA);
            return;
        }

        // regra do vôlei: cartão vermelho dá ponto automático para a equipe adversária
        registrarLog(evento, "🟥 " + jogador.getNome() + " — ponto para o adversário", ehEquipeA);
        somarPonto(!ehEquipeA);

        Evento pontoAdversario = Evento.novoEvento(partida.getId(), null,
                ehEquipeA ? equipeB.getId() : equipeA.getId(), TipoEvento.PONTO,
                "Set " + numeroSetAtual, segundosDecorridos);
        eventoDAO.salvar(pontoAdversario);
        registrarLog(pontoAdversario, "🏐 Ponto (cartão vermelho adversário) — " + pontosSetA + " x " + pontosSetB,
                !ehEquipeA);

        verificarFimDeSet();
    }

    private void somarPonto(boolean equipeA) {
        if (equipeA) {
            pontosSetA++;
        } else {
            pontosSetB++;
        }
        atualizarPlacar();
    }

    private void verificarFimDeSet() {
        int metaSet = numeroSetAtual == NUMERO_SET_DECISIVO ? PONTOS_SET_DECISIVO : PONTOS_SET_NORMAL;
        boolean aVenceu = pontosSetA >= metaSet && (pontosSetA - pontosSetB) >= DIFERENCA_MINIMA;
        boolean bVenceu = pontosSetB >= metaSet && (pontosSetB - pontosSetA) >= DIFERENCA_MINIMA;

        if (!aVenceu && !bVenceu) {
            return;
        }

        if (aVenceu) {
            setsGanhosA++;
        } else {
            setsGanhosB++;
        }

        registrarLogGeral("Fim do Set " + numeroSetAtual + ": " + pontosSetA + " x " + pontosSetB
                + " — Sets agora " + setsGanhosA + " x " + setsGanhosB);

        pontosSetA = 0;
        pontosSetB = 0;
        numeroSetAtual++;
        atualizarPlacar();
        atualizarLabelSets();

        if (setsGanhosA >= SETS_PARA_VENCER || setsGanhosB >= SETS_PARA_VENCER) {
            String vencedora = setsGanhosA > setsGanhosB ? equipeA.getNome() : equipeB.getNome();
            Alert alerta = new Alert(AlertType.INFORMATION);
            alerta.setTitle("Partida decidida");
            alerta.setHeaderText(vencedora + " venceu por " + Math.max(setsGanhosA, setsGanhosB)
                    + " sets a " + Math.min(setsGanhosA, setsGanhosB) + "!");
            alerta.setContentText("Clique em \"Encerrar Partida\" quando quiser finalizar.");
            alerta.showAndWait();
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

    private void registrarLog(Evento evento, String mensagem, boolean ehEquipeA) {
        if (ehEquipeA) {
            listaEventosA.getItems().add(0, new RegistroEvento(evento, mensagem));
            estado.getEventosEquipeA().add(0, mensagem);
        } else {
            listaEventosB.getItems().add(0, new RegistroEvento(evento, mensagem));
            estado.getEventosEquipeB().add(0, mensagem);
        }
    }

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

        boolean confirmar = DialogoUtil.confirmar("Remover evento", selecionado.texto
                + "\n\nIsso não recalcula sets já fechados automaticamente -- use com cuidado perto do fim de um set."
                + "\n\nDeseja remover mesmo assim?");
        if (!confirmar) {
            return;
        }

        reverterEvento(selecionado.evento);
        eventoDAO.excluir(selecionado.evento.getId());

        int indice = lista.getItems().indexOf(selecionado);
        lista.getItems().remove(selecionado);
        if (indice >= 0 && indice < logCompartilhado.size()) {
            logCompartilhado.remove(indice);
        }

        atualizarPlacar();
    }

    private void reverterEvento(Evento evento) {
        if (evento.getTipo() == TipoEvento.PONTO || evento.getTipo() == TipoEvento.BLOQUEIO) {
            if (evento.getEquipeId() == equipeA.getId()) {
                pontosSetA = Math.max(0, pontosSetA - 1);
            } else {
                pontosSetB = Math.max(0, pontosSetB - 1);
            }
        }
        // cartões não alteram diretamente o placar armazenado (o ponto do vermelho é um evento PONTO à parte,
        // removível separadamente)
    }

    @FXML
    private void onEncerrarPartida() {
        boolean confirmar = DialogoUtil.confirmar("Encerrar partida",
                "Encerrar " + equipeA.getNome() + " " + setsGanhosA + " x " + setsGanhosB + " " + equipeB.getNome()
                + " (sets)? Essa ação não pode ser desfeita.");
        if (!confirmar) {
            return;
        }

        if (tocando) {
            cronometro.pause();
        }
        partida.setPlacarA(setsGanhosA);
        partida.setPlacarB(setsGanhosB);
        partida.setStatus(StatusPartida.ENCERRADA);
        partidaDAO.atualizar(partida);

        Alert fim = new Alert(AlertType.INFORMATION);
        fim.setTitle("Partida encerrada");
        fim.setHeaderText("Resultado final (sets): " + equipeA.getNome() + " " + setsGanhosA
                + " x " + setsGanhosB + " " + equipeB.getNome());
        fim.setContentText("A geração da súmula em PDF será implementada na etapa de Relatórios.");
        fim.showAndWait();

        fecharTelaPublicaSeAberta();
        ((Stage) listaEventosA.getScene().getWindow()).close();
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
