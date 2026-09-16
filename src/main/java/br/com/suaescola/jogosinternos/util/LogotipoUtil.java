package br.com.suaescola.jogosinternos.util;

import java.io.File;

import javafx.scene.image.Image;

/**
 * Carrega o logotipo da escola, usado no canto das telas de placar.
 *
 * O arquivo é fixo e mantido manualmente pelo usuário na pasta de dados
 * do aplicativo (veja PastaDadosUtil), como
 * dados/logo-escola.(png|jpg|jpeg) -- não há tela de upload para isso;
 * basta colocar o arquivo com esse nome na pasta.
 */
public final class LogotipoUtil {

    private static final String NOME_BASE = "logo-escola";
    private static final String[] EXTENSOES = {"png", "jpg", "jpeg"};

    private LogotipoUtil() {
    }

    /** Retorna a imagem do logotipo, ou null se nenhum arquivo correspondente for encontrado. */
    public static Image carregarLogotipoEscola() {
        for (String extensao : EXTENSOES) {
            File arquivo = PastaDadosUtil.pastaDados().resolve(NOME_BASE + "." + extensao).toFile();
            if (arquivo.exists()) {
                return new Image(arquivo.toURI().toString());
            }
        }
        return null;
    }

    /** Caminho onde o usuário deve colocar o arquivo, para exibir em mensagens de ajuda. */
    public static String pastaEsperada() {
        return PastaDadosUtil.pastaDados().toString();
    }
}
