package br.com.suaescola.jogosinternos;

/**
 * Classe de entrada separada da MainApp (que estende javafx.application.Application).
 *
 * Isso é necessário porque, ao rodar um jar "fat/uber" com 'java -jar', o
 * runtime do JavaFX recusa iniciar se a classe informada no manifesto
 * (Main-Class) for diretamente uma subclasse de Application -- ele exige
 * que os módulos JavaFX estejam no module-path, o que não é o caso aqui
 * (aplicação non-modular, tudo no classpath). Apontar o Main-Class do jar
 * para esta classe, que apenas repassa para MainApp.main(), contorna essa
 * checagem.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
