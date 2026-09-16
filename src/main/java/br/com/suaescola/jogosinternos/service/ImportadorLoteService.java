package br.com.suaescola.jogosinternos.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.suaescola.jogosinternos.dao.EquipeDAO;
import br.com.suaescola.jogosinternos.dao.JogadorDAO;
import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Jogador;
import br.com.suaescola.jogosinternos.model.Modalidade;

/**
 * Importa equipes e jogadores em lote a partir de um texto no padrão:
 *
 * <pre>
 * FUTSAL: Turma 9A
 * 10 - João Silva - Goleiro
 * 7 - Pedro Santos
 *
 * VOLEI: Turma 8B
 * 5 - Maria Souza - Levantadora
 * </pre>
 *
 * Uma linha "MODALIDADE: Nome da equipe" inicia uma equipe (reaproveita se
 * já existir uma com o mesmo nome e modalidade). As linhas seguintes, até
 * a próxima linha de equipe, são jogadores no formato
 * "número - nome - posição" -- número e posição são opcionais.
 */
public class ImportadorLoteService {

    private static final Pattern PADRAO_EQUIPE =
            Pattern.compile("^(FUTSAL|V[OÔ]LEI)\\s*:\\s*(.+)$",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    // exige espaço dos dois lados do hífen, para não quebrar nomes compostos como "Ana-Paula"
    private static final Pattern SEPARADOR_CAMPOS = Pattern.compile("\\s+-\\s+");

    private final EquipeDAO equipeDAO = new EquipeDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();

    public String importar(String texto) {
        StringBuilder log = new StringBuilder();
        Equipe equipeAtual = null;

        for (String linhaBruta : texto.split("\\R")) {
            String linha = linhaBruta.trim();
            if (linha.isEmpty()) {
                continue;
            }

            Matcher matcherEquipe = PADRAO_EQUIPE.matcher(linha);
            if (matcherEquipe.matches()) {
                Modalidade modalidade = matcherEquipe.group(1).toUpperCase().startsWith("FUTSAL")
                        ? Modalidade.FUTSAL : Modalidade.VOLEI;
                String nomeEquipe = matcherEquipe.group(2).trim();
                equipeAtual = buscarOuCriarEquipe(nomeEquipe, modalidade, log);
                continue;
            }

            if (equipeAtual == null) {
                log.append("⚠ Linha ignorada (nenhuma equipe definida ainda): \"").append(linha).append("\"\n");
                continue;
            }

            processarLinhaJogador(linha, equipeAtual, log);
        }

        return log.toString();
    }

    private Equipe buscarOuCriarEquipe(String nome, Modalidade modalidade, StringBuilder log) {
        List<Equipe> existentes = equipeDAO.listarPorModalidade(modalidade);
        for (Equipe equipe : existentes) {
            if (equipe.getNome().equalsIgnoreCase(nome)) {
                log.append("• Equipe \"").append(nome).append("\" (").append(textoModalidade(modalidade))
                        .append(") já existia — jogadores serão adicionados a ela.\n");
                return equipe;
            }
        }

        Equipe nova = new Equipe(nome, modalidade, null);
        equipeDAO.salvar(nova);
        log.append("✓ Equipe \"").append(nome).append("\" (").append(textoModalidade(modalidade))
                .append(") criada.\n");
        return nova;
    }

    private void processarLinhaJogador(String linha, Equipe equipe, StringBuilder log) {
        String[] partes = SEPARADOR_CAMPOS.split(linha);

        Integer numero = null;
        int indiceNome = 0;
        if (partes.length > 0 && partes[0].matches("\\d+")) {
            numero = Integer.valueOf(partes[0]);
            indiceNome = 1;
        }

        if (indiceNome >= partes.length || partes[indiceNome].isBlank()) {
            log.append("  ⚠ Linha de jogador inválida: \"").append(linha).append("\"\n");
            return;
        }

        String nome = partes[indiceNome].trim();
        String posicao = partes.length > indiceNome + 1 ? partes[indiceNome + 1].trim() : null;

        if (numero != null && jaExisteNumero(equipe.getId(), numero)) {
            log.append("  ⚠ Jogador \"").append(nome).append("\" ignorado: já existe alguém com o número ")
                    .append(numero).append(" nessa equipe.\n");
            return;
        }

        jogadorDAO.salvar(new Jogador(nome, numero, posicao, null, equipe.getId()));
        log.append("  ✓ Jogador \"").append(nome).append("\"")
                .append(numero != null ? " (nº " + numero + ")" : "")
                .append(" adicionado.\n");
    }

    private boolean jaExisteNumero(int equipeId, int numero) {
        return jogadorDAO.listarPorEquipe(equipeId).stream()
                .anyMatch(j -> j.getNumero() != null && j.getNumero() == numero);
    }

    private String textoModalidade(Modalidade modalidade) {
        return switch (modalidade) {
            case FUTSAL -> "Futsal";
            case VOLEI -> "Vôlei";
        };
    }
}
