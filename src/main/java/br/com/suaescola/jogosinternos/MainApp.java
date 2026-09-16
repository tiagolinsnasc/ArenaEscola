package br.com.suaescola.jogosinternos;

import java.io.IOException;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicacao.
 *
 * Carrega a tela principal (MainView.fxml) dentro de uma unica Stage.
 * As telas de placar (futsal/volei) e a tela publica para o telao serao
 * abertas a partir daqui, cada uma em sua propria Stage, mais adiante.
 * TODO: Alterar CSS
 */
public class MainApp extends Application {

    private static final String FXML_PRINCIPAL =
            "/br/com/suaescola/jogosinternos/fxml/MainView.fxml";
    private static final String CSS_PRINCIPAL =
            "/br/com/suaescola/jogosinternos/css/style.css";

    @Override
    public void start(Stage stagePrincipal) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource(FXML_PRINCIPAL),
                        "FXML principal nao encontrado: " + FXML_PRINCIPAL));

        Parent root = loader.load();

        Scene scene = new Scene(root, 1024, 700);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(CSS_PRINCIPAL),
                        "CSS principal nao encontrado: " + CSS_PRINCIPAL).toExternalForm());

        stagePrincipal.setTitle("Jogos Internos - Gestao de Partidas");
        stagePrincipal.setScene(scene);
        stagePrincipal.setMinWidth(900);
        stagePrincipal.setMinHeight(600);
        stagePrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
