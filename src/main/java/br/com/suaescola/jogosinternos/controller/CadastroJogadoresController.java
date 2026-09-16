package br.com.suaescola.jogosinternos.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import br.com.suaescola.jogosinternos.dao.EquipeDAO;
import br.com.suaescola.jogosinternos.dao.JogadorDAO;
import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Jogador;
import br.com.suaescola.jogosinternos.model.Modalidade;
import br.com.suaescola.jogosinternos.util.PastaDadosUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

/**
 * Controller da tela de Cadastro de Jogadores. O combo de filtro à esquerda
 * escolhe a equipe cuja lista de jogadores é exibida; o combo do formulário
 * define a equipe do jogador sendo salvo (normalmente a mesma, mas dá pra
 * trocar caso um jogador precise ser movido de equipe).
 */
public class CadastroJogadoresController {

    @FXML
    private ComboBox<Equipe> comboEquipeFiltro;
    @FXML
    private Label labelModalidadeFiltro;
    @FXML
    private TableView<Jogador> tabelaJogadores;
    @FXML
    private TableColumn<Jogador, String> colNumero;
    @FXML
    private TableColumn<Jogador, String> colNome;
    @FXML
    private TableColumn<Jogador, String> colPosicao;

    @FXML
    private ImageView previewFoto;
    @FXML
    private Label labelFoto;
    @FXML
    private TextField campoNome;
    @FXML
    private TextField campoNumero;
    @FXML
    private TextField campoPosicao;
    @FXML
    private ComboBox<Equipe> comboEquipeFormulario;
    @FXML
    private Label labelModalidadeFormulario;

    private final EquipeDAO equipeDAO = new EquipeDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();
    private final ObservableList<Jogador> jogadores = FXCollections.observableArrayList();

    private Jogador jogadorSelecionado;   // null = formulário representa um jogador novo
    private String caminhoFotoSelecionada;

    @FXML
    private void initialize() {
        List<Equipe> equipes = equipeDAO.listarTodas();
        ObservableList<Equipe> listaEquipes = FXCollections.observableArrayList(equipes);
        comboEquipeFiltro.setItems(listaEquipes);
        comboEquipeFormulario.setItems(listaEquipes);

        colNumero.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().getNumero() != null ? dados.getValue().getNumero().toString() : ""));
        colNome.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getNome()));
        colPosicao.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().getPosicao() != null ? dados.getValue().getPosicao() : ""));

        tabelaJogadores.setItems(jogadores);
        tabelaJogadores.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencherFormulario(novo));

        comboEquipeFiltro.valueProperty().addListener((obs, antiga, nova) -> {
            carregarJogadores(nova);
            limparFormulario();
            comboEquipeFormulario.setValue(nova);
            atualizarLabelModalidade(labelModalidadeFiltro, nova);
        });

        comboEquipeFormulario.valueProperty().addListener((obs, antiga, nova) ->
                atualizarLabelModalidade(labelModalidadeFormulario, nova));

        limparFormulario();

        if (!equipes.isEmpty()) {
            comboEquipeFiltro.setValue(equipes.get(0));
        }
    }

    private void carregarJogadores(Equipe equipe) {
        jogadores.setAll(equipe != null ? jogadorDAO.listarPorEquipe(equipe.getId()) : List.of());
    }

    private void atualizarLabelModalidade(Label label, Equipe equipe) {
        label.setText(equipe != null ? "Modalidade: " + textoModalidade(equipe.getModalidade()) : "");
    }

    private String textoModalidade(Modalidade modalidade) {
        return switch (modalidade) {
            case FUTSAL -> "Futsal";
            case VOLEI -> "Vôlei";
        };
    }

    private void preencherFormulario(Jogador jogador) {
        jogadorSelecionado = jogador;

        if (jogador == null) {
            campoNome.clear();
            campoNumero.clear();
            campoPosicao.clear();
            caminhoFotoSelecionada = null;
            comboEquipeFormulario.setValue(comboEquipeFiltro.getValue());
            atualizarPreview();
            return;
        }

        campoNome.setText(jogador.getNome());
        campoNumero.setText(jogador.getNumero() != null ? jogador.getNumero().toString() : "");
        campoPosicao.setText(jogador.getPosicao());
        caminhoFotoSelecionada = jogador.getFotoPath();
        comboEquipeFormulario.setValue(equipeDAO.buscarPorId(jogador.getEquipeId()));
        atualizarPreview();
    }

    @FXML
    private void onNovo() {
        if (comboEquipeFiltro.getValue() == null) {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setTitle("Selecione uma equipe");
            alerta.setHeaderText(null);
            alerta.setContentText("Escolha a equipe à esquerda antes de cadastrar um jogador.");
            alerta.showAndWait();
            return;
        }
        tabelaJogadores.getSelectionModel().clearSelection();
        limparFormulario();
    }

    private void limparFormulario() {
        preencherFormulario(null);
    }

    @FXML
    private void onEscolherFoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar foto do jogador");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));

        File arquivo = chooser.showOpenDialog(previewFoto.getScene().getWindow());
        if (arquivo == null) {
            return;
        }

        try {
            caminhoFotoSelecionada = copiarParaPastaDeDados(arquivo);
            atualizarPreview();
        } catch (IOException e) {
            mostrarErro("Não foi possível copiar a foto selecionada.", e);
        }
    }

    private String copiarParaPastaDeDados(File origem) throws IOException {
        Path pastaFotos = PastaDadosUtil.subpasta("jogadores");

        String nomeOriginal = origem.getName();
        int pontoIndex = nomeOriginal.lastIndexOf('.');
        String extensao = pontoIndex >= 0 ? nomeOriginal.substring(pontoIndex) : "";

        Path destino = pastaFotos.resolve(UUID.randomUUID() + extensao);
        Files.copy(origem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toAbsolutePath().toString();
    }

    private void atualizarPreview() {
        if (caminhoFotoSelecionada == null) {
            previewFoto.setImage(null);
            labelFoto.setText("Nenhuma foto selecionada");
            return;
        }

        File arquivo = new File(caminhoFotoSelecionada);
        if (arquivo.exists()) {
            previewFoto.setImage(new Image(arquivo.toURI().toString()));
            labelFoto.setText(arquivo.getName());
        } else {
            previewFoto.setImage(null);
            labelFoto.setText("Foto não encontrada");
        }
    }

    @FXML
    private void onSalvar() {
        String nome = campoNome.getText() == null ? "" : campoNome.getText().trim();
        String posicao = campoPosicao.getText() == null ? "" : campoPosicao.getText().trim();
        Equipe equipe = comboEquipeFormulario.getValue();

        if (nome.isEmpty() || equipe == null) {
            avisar("Informe o nome do jogador e a equipe.");
            return;
        }

        Integer numero = null;
        String textoNumero = campoNumero.getText() == null ? "" : campoNumero.getText().trim();
        if (!textoNumero.isEmpty()) {
            try {
                numero = Integer.valueOf(textoNumero);
            } catch (NumberFormatException e) {
                avisar("O número da camisa deve ser um valor numérico.");
                return;
            }
        }

        if (jogadorSelecionado == null) {
            jogadorDAO.salvar(new Jogador(nome, numero, posicao.isEmpty() ? null : posicao,
                    caminhoFotoSelecionada, equipe.getId()));
        } else {
            jogadorSelecionado.setNome(nome);
            jogadorSelecionado.setNumero(numero);
            jogadorSelecionado.setPosicao(posicao.isEmpty() ? null : posicao);
            jogadorSelecionado.setFotoPath(caminhoFotoSelecionada);
            jogadorSelecionado.setEquipeId(equipe.getId());
            jogadorDAO.atualizar(jogadorSelecionado);
        }

        // se o jogador foi salvo numa equipe diferente da que está filtrando agora, atualiza o filtro
        comboEquipeFiltro.setValue(equipe);
        carregarJogadores(equipe);
        tabelaJogadores.getSelectionModel().clearSelection();
        limparFormulario();
    }

    @FXML
    private void onExcluir() {
        Jogador selecionado = tabelaJogadores.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            return;
        }

        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Excluir jogador");
        confirmacao.setHeaderText("Excluir \"" + selecionado.getNome() + "\"?");
        confirmacao.setContentText(
                "Essa ação não pode ser desfeita. Jogadores com eventos em alguma partida não podem ser excluídos.");

        confirmacao.showAndWait().filter(botao -> botao == ButtonType.OK).ifPresent(botao -> {
            try {
                jogadorDAO.excluir(selecionado.getId());
                carregarJogadores(comboEquipeFiltro.getValue());
                limparFormulario();
            } catch (RuntimeException e) {
                mostrarErro("Não foi possível excluir o jogador.", e);
            }
        });
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
