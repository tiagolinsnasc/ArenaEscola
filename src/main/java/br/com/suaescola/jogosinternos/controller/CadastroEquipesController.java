package br.com.suaescola.jogosinternos.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import br.com.suaescola.jogosinternos.dao.EquipeDAO;
import br.com.suaescola.jogosinternos.model.Equipe;
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
 * Controller da tela de Cadastro de Equipes: lista à esquerda, formulário à
 * direita. Selecionar uma linha da tabela carrega os dados no formulário
 * para edição; "Nova Equipe" limpa o formulário para um cadastro novo.
 */
public class CadastroEquipesController {

    @FXML
    private TableView<Equipe> tabelaEquipes;
    @FXML
    private TableColumn<Equipe, String> colNome;
    @FXML
    private TableColumn<Equipe, String> colModalidade;
    @FXML
    private TextField campoNome;
    @FXML
    private ComboBox<Modalidade> comboModalidade;
    @FXML
    private ImageView previewEscudo;
    @FXML
    private Label labelEscudo;

    private final EquipeDAO equipeDAO = new EquipeDAO();
    private final ObservableList<Equipe> equipes = FXCollections.observableArrayList();

    private Equipe equipeSelecionada;       // null = formulário representa uma equipe nova
    private String caminhoEscudoSelecionado; // já copiado para a pasta de dados do app

    @FXML
    private void initialize() {
        comboModalidade.setItems(FXCollections.observableArrayList(Modalidade.values()));

        colNome.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getNome()));
        colModalidade.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getModalidade().name()));

        tabelaEquipes.setItems(equipes);
        tabelaEquipes.getSelectionModel().selectedItemProperty()
                .addListener((obs, antiga, nova) -> preencherFormulario(nova));

        carregarEquipes();
        limparFormulario();
    }

    private void carregarEquipes() {
        equipes.setAll(equipeDAO.listarTodas());
    }

    private void preencherFormulario(Equipe equipe) {
        equipeSelecionada = equipe;

        if (equipe == null) {
            campoNome.clear();
            comboModalidade.setValue(null);
            caminhoEscudoSelecionado = null;
            atualizarPreview();
            return;
        }

        campoNome.setText(equipe.getNome());
        comboModalidade.setValue(equipe.getModalidade());
        caminhoEscudoSelecionado = equipe.getEscudoPath();
        atualizarPreview();
    }

    @FXML
    private void onNovo() {
        tabelaEquipes.getSelectionModel().clearSelection();
        limparFormulario();
    }

    private void limparFormulario() {
        preencherFormulario(null);
    }

    @FXML
    private void onEscolherImagem() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar escudo da equipe");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));

        File arquivo = chooser.showOpenDialog(previewEscudo.getScene().getWindow());
        if (arquivo == null) {
            return;
        }

        try {
            caminhoEscudoSelecionado = copiarParaPastaDeDados(arquivo);
            atualizarPreview();
        } catch (IOException e) {
            mostrarErro("Não foi possível copiar a imagem selecionada.", e);
        }
    }

    /** Copia o arquivo escolhido para ~/.jogosinternos/escudos, com um nome único. */
    private String copiarParaPastaDeDados(File origem) throws IOException {
        Path pastaEscudos = PastaDadosUtil.subpasta("escudos");

        String nomeOriginal = origem.getName();
        int pontoIndex = nomeOriginal.lastIndexOf('.');
        String extensao = pontoIndex >= 0 ? nomeOriginal.substring(pontoIndex) : "";

        Path destino = pastaEscudos.resolve(UUID.randomUUID() + extensao);
        Files.copy(origem.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toAbsolutePath().toString();
    }

    private void atualizarPreview() {
        if (caminhoEscudoSelecionado == null) {
            previewEscudo.setImage(null);
            labelEscudo.setText("Nenhuma imagem selecionada");
            return;
        }

        File arquivo = new File(caminhoEscudoSelecionado);
        if (arquivo.exists()) {
            previewEscudo.setImage(new Image(arquivo.toURI().toString()));
            labelEscudo.setText(arquivo.getName());
        } else {
            previewEscudo.setImage(null);
            labelEscudo.setText("Imagem não encontrada");
        }
    }

    @FXML
    private void onSalvar() {
        String nome = campoNome.getText() == null ? "" : campoNome.getText().trim();
        Modalidade modalidade = comboModalidade.getValue();

        if (nome.isEmpty() || modalidade == null) {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setTitle("Dados incompletos");
            alerta.setHeaderText(null);
            alerta.setContentText("Informe o nome e a modalidade da equipe.");
            alerta.showAndWait();
            return;
        }

        if (equipeSelecionada == null) {
            equipeDAO.salvar(new Equipe(nome, modalidade, caminhoEscudoSelecionado));
        } else {
            equipeSelecionada.setNome(nome);
            equipeSelecionada.setModalidade(modalidade);
            equipeSelecionada.setEscudoPath(caminhoEscudoSelecionado);
            equipeDAO.atualizar(equipeSelecionada);
        }

        carregarEquipes();
        tabelaEquipes.getSelectionModel().clearSelection();
        limparFormulario();
    }

    @FXML
    private void onExcluir() {
        Equipe selecionada = tabelaEquipes.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            return;
        }

        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Excluir equipe");
        confirmacao.setHeaderText("Excluir \"" + selecionada.getNome() + "\"?");
        confirmacao.setContentText(
                "Essa ação não pode ser desfeita. Equipes com jogadores ou partidas vinculadas não podem ser excluídas.");

        confirmacao.showAndWait().filter(botao -> botao == ButtonType.OK).ifPresent(botao -> {
            try {
                equipeDAO.excluir(selecionada.getId());
                carregarEquipes();
                limparFormulario();
            } catch (RuntimeException e) {
                mostrarErro("Não foi possível excluir a equipe.", e);
            }
        });
    }

    private void mostrarErro(String mensagem, Exception causa) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(mensagem);
        alerta.setContentText(causa.getMessage());
        alerta.showAndWait();
    }
}
