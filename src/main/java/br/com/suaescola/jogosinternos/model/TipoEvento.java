package br.com.suaescola.jogosinternos.model;

/**
 * Tipos de evento que podem ser lançados durante uma partida.
 * Nem todo tipo se aplica a toda modalidade:
 * - GOL: futsal
 * - PONTO, BLOQUEIO: vôlei
 * - CARTAO_AMARELO, CARTAO_VERMELHO: ambas
 */
public enum TipoEvento {
    GOL,
    PONTO,
    BLOQUEIO,
    CARTAO_AMARELO,
    CARTAO_VERMELHO
}
