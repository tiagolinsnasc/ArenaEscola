package br.com.suaescola.jogosinternos.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Evento;
import br.com.suaescola.jogosinternos.model.Jogador;
import br.com.suaescola.jogosinternos.model.Modalidade;
import br.com.suaescola.jogosinternos.model.Partida;
import br.com.suaescola.jogosinternos.util.PastaDadosUtil;

/**
 * Gera o PDF da súmula de uma partida encerrada: uma capa com os dados de
 * identificação e espaço em branco para o preenchimento manual da súmula
 * oficial, seguida de um relatório automático com todos os eventos
 * registrados e um resumo de cartões.
 */
public class RelatorioPdfService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public File gerarSumula(Partida partida, Equipe equipeA, Equipe equipeB,
                             List<Evento> eventos, Map<Integer, Jogador> jogadoresPorId) throws IOException {

        Path pastaRelatorios = PastaDadosUtil.subpasta("relatorios");
        File arquivo = pastaRelatorios.resolve("sumula-partida-" + partida.getId() + ".pdf").toFile();

        try (PDDocument documento = new PDDocument()) {
            PDType1Font fonteTitulo = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fonteTexto = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            adicionarCapa(documento, fonteTitulo, fonteTexto, partida, equipeA, equipeB);
            adicionarRelatorioEventos(documento, fonteTitulo, fonteTexto, partida, equipeA, equipeB, eventos, jogadoresPorId);

            documento.save(arquivo);
        }

        return arquivo;
    }

    private void adicionarCapa(PDDocument documento, PDType1Font fonteTitulo, PDType1Font fonteTexto,
                                Partida partida, Equipe equipeA, Equipe equipeB) throws IOException {

        PDPage pagina = new PDPage(PDRectangle.A4);
        documento.addPage(pagina);

        float largura = PDRectangle.A4.getWidth();
        float margem = 50;
        float y = PDRectangle.A4.getHeight() - 80;

        try (PDPageContentStream cs = new PDPageContentStream(documento, pagina)) {
            y = desenharTexto(cs, fonteTitulo, 22, margem, y, "SÚMULA DA PARTIDA");
            y -= 36;
            y = desenharTexto(cs, fonteTexto, 13, margem, y, "Modalidade: " + textoModalidade(partida.getModalidade()));
            y -= 20;
            y = desenharTexto(cs, fonteTexto, 13, margem, y, "Data: " + partida.getDataHora().format(FORMATO_DATA));
            y -= 36;
            y = desenharTexto(cs, fonteTitulo, 17, margem, y,
                    equipeA.getNome() + "        " + partida.getPlacarA() + " x " + partida.getPlacarB()
                    + "        " + equipeB.getNome());
            y -= 50;
            y = desenharTexto(cs, fonteTexto, 12, margem, y, "Preencha abaixo a súmula oficial da partida:");
            y -= 30;

            cs.setLineWidth(0.5f);
            for (int i = 0; i < 20; i++) {
                cs.moveTo(margem, y);
                cs.lineTo(largura - margem, y);
                cs.stroke();
                y -= 26;
            }

            y -= 20;
            desenharTexto(cs, fonteTexto, 11, margem, y, "Assinatura do(a) responsável: ______________________________________");
        }
    }

    private void adicionarRelatorioEventos(PDDocument documento, PDType1Font fonteTitulo, PDType1Font fonteTexto,
                                            Partida partida, Equipe equipeA, Equipe equipeB,
                                            List<Evento> eventos, Map<Integer, Jogador> jogadoresPorId) throws IOException {

        try (EscritorPdf escritor = new EscritorPdf(documento, fonteTitulo, fonteTexto)) {
            escritor.titulo("EVENTOS DA PARTIDA");
            escritor.texto(equipeA.getNome() + "  " + partida.getPlacarA() + " x " + partida.getPlacarB()
                    + "  " + equipeB.getNome());
            escritor.pular(10);

            if (eventos.isEmpty()) {
                escritor.texto("Nenhum evento registrado.");
            } else {
                for (Evento evento : eventos) {
                    escritor.texto(formatarEvento(evento, equipeA, equipeB, jogadoresPorId));
                }
            }

            escritor.pular(24);
            escritor.subtitulo("Resumo de cartões");

            Map<Integer, int[]> contagemCartoes = new LinkedHashMap<>();
            for (Evento evento : eventos) {
                if (evento.getJogadorId() == null) {
                    continue;
                }
                if (evento.getTipo() == br.com.suaescola.jogosinternos.model.TipoEvento.CARTAO_AMARELO
                        || evento.getTipo() == br.com.suaescola.jogosinternos.model.TipoEvento.CARTAO_VERMELHO) {
                    int[] contagem = contagemCartoes.computeIfAbsent(evento.getJogadorId(), id -> new int[2]);
                    if (evento.getTipo() == br.com.suaescola.jogosinternos.model.TipoEvento.CARTAO_AMARELO) {
                        contagem[0]++;
                    } else {
                        contagem[1]++;
                    }
                }
            }

            if (contagemCartoes.isEmpty()) {
                escritor.texto("Nenhum cartão registrado.");
            } else {
                for (Map.Entry<Integer, int[]> entrada : contagemCartoes.entrySet()) {
                    Jogador jogador = jogadoresPorId.get(entrada.getKey());
                    String nome = jogador != null ? jogador.getNome() : "Jogador #" + entrada.getKey();
                    escritor.texto(nome + ": " + entrada.getValue()[0] + " amarelo(s), "
                            + entrada.getValue()[1] + " vermelho(s)");
                }
            }
        }
    }

    private String formatarEvento(Evento evento, Equipe equipeA, Equipe equipeB, Map<Integer, Jogador> jogadoresPorId) {
        String nomeEquipe = evento.getEquipeId() == equipeA.getId() ? equipeA.getNome() : equipeB.getNome();
        Jogador jogador = evento.getJogadorId() != null ? jogadoresPorId.get(evento.getJogadorId()) : null;

        String tipoTexto = switch (evento.getTipo()) {
            case GOL -> "Gol";
            case PONTO -> "Ponto";
            case BLOQUEIO -> "Bloqueio";
            case CARTAO_AMARELO -> "Cartão amarelo";
            case CARTAO_VERMELHO -> "Cartão vermelho";
        };

        String periodo = evento.getPeriodo() != null ? " (" + evento.getPeriodo() + ")" : "";
        return tipoTexto + " — " + nomeEquipe + (jogador != null ? " — " + jogador.getNome() : "") + periodo;
    }

    private String textoModalidade(Modalidade modalidade) {
        return modalidade == Modalidade.FUTSAL ? "Futsal" : "Vôlei";
    }

    /** Escreve texto absoluto na posição dada e retorna o mesmo y (não avança -- quem chama controla o cursor). */
    private float desenharTexto(PDPageContentStream cs, PDType1Font fonte, float tamanho,
                                 float x, float y, String texto) throws IOException {
        cs.beginText();
        cs.setFont(fonte, tamanho);
        cs.newLineAtOffset(x, y);
        cs.showText(texto);
        cs.endText();
        return y;
    }

    /**
     * Escreve texto em fluxo (título/subtítulo/parágrafo), quebrando de página
     * automaticamente quando o espaço na página atual acaba.
     */
    private static class EscritorPdf implements AutoCloseable {

        private static final float MARGEM = 50;
        private static final float MARGEM_INFERIOR = 60;

        private final PDDocument documento;
        private final PDType1Font fonteTitulo;
        private final PDType1Font fonteTexto;

        private PDPageContentStream cs;
        private float y;

        EscritorPdf(PDDocument documento, PDType1Font fonteTitulo, PDType1Font fonteTexto) throws IOException {
            this.documento = documento;
            this.fonteTitulo = fonteTitulo;
            this.fonteTexto = fonteTexto;
            novaPagina();
        }

        private void novaPagina() throws IOException {
            if (cs != null) {
                cs.close();
            }
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            cs = new PDPageContentStream(documento, pagina);
            y = PDRectangle.A4.getHeight() - 60;
        }

        private void garantirEspaco(float necessario) throws IOException {
            if (y - necessario < MARGEM_INFERIOR) {
                novaPagina();
            }
        }

        void titulo(String texto) throws IOException {
            garantirEspaco(30);
            escrever(fonteTitulo, 16, texto);
            y -= 24;
        }

        void subtitulo(String texto) throws IOException {
            garantirEspaco(24);
            escrever(fonteTitulo, 13, texto);
            y -= 18;
        }

        void texto(String texto) throws IOException {
            garantirEspaco(16);
            escrever(fonteTexto, 11, texto);
            y -= 16;
        }

        void pular(float espaco) {
            y -= espaco;
        }

        private void escrever(PDType1Font fonte, float tamanho, String texto) throws IOException {
            cs.beginText();
            cs.setFont(fonte, tamanho);
            cs.newLineAtOffset(MARGEM, y);
            cs.showText(texto);
            cs.endText();
        }

        @Override
        public void close() throws IOException {
            if (cs != null) {
                cs.close();
            }
        }
    }
}
