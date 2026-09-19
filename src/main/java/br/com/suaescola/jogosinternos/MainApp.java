package br.com.suaescola.jogosinternos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicacao.
 *
 * Carrega a tela principal (MainView.fxml) dentro de uma unica Stage.
 * As telas de placar (futsal/volei) e a tela publica para o telao serao
 * abertas a partir daqui, cada uma em sua propria Stage, mais adiante.
 */
public class MainApp extends Application {

    private static final String FXML_PRINCIPAL =
            "/br/com/suaescola/jogosinternos/fxml/MainView.fxml";
    private static final String CSS_PRINCIPAL =
            "/br/com/suaescola/jogosinternos/css/style.css";

    // Ícone FIXO do aplicativo (não muda por escola) -- fica empacotado dentro
    // do .jar, em src/main/resources/br/com/suaescola/jogosinternos/images/.
    // Coloque o arquivo icone-app.png nessa pasta antes de rodar/empacotar.
    private static final String ICONE_APP =
            "/icone.png";

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

        // getResourceAsStream lê de DENTRO do jar (classpath) -- certo paraum
        // ícone fixo do software. Se o arquivo ainda não foi colocado na pasta
        // de recursos, apenas não define o ícone (não quebra o programa).
        try (InputStream fluxoIcone = getClass().getResourceAsStream(ICONE_APP)) {
            if (fluxoIcone != null) {
                stagePrincipal.getIcons().add(new Image(fluxoIcone));
            }
        }

        stagePrincipal.setTitle("Jogos Internos - Gestao de Partidas - Arena Escola");
        stagePrincipal.setScene(scene);
        stagePrincipal.setMinWidth(900);
        stagePrincipal.setMinHeight(600);
        stagePrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
