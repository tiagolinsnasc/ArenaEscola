package br.com.suaescola.jogosinternos.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pasta onde o aplicativo guarda seus dados: banco SQLite, escudos, fotos
 * de jogadores e o logotipo da escola.
 *
 * Fica ao lado do próprio programa (pasta de trabalho da aplicação), não
 * na pasta pessoal do usuário, e não é oculta -- assim dá pra localizar,
 * copiar ou fazer backup facilmente.
 */
public final class PastaDadosUtil {

    private static final String NOME_PASTA = "dados";

    private PastaDadosUtil() {
    }

    /** Pasta raiz de dados do aplicativo (ex.: banco de dados, logotipo). */
    public static Path pastaDados() {
        return Path.of(System.getProperty("user.dir"), NOME_PASTA);
    }

    /** Uma subpasta dentro da pasta de dados (ex.: "escudos", "jogadores"), já garantida existir. */
    public static Path subpasta(String nome) {
        Path caminho = pastaDados().resolve(nome);
        try {
            Files.createDirectories(caminho);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar a pasta de dados: " + caminho, e);
        }
        return caminho;
    }
}
