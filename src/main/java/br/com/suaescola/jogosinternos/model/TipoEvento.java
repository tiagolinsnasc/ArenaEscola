package br.com.suaescola.jogosinternos.model;

/**
 * Tipos de evento que podem ser lançados durante uma partida.
 * Nem todo tipo se aplica a toda modalidade:
 * - GOL, GOL_CONTRA: futsal
 * - PONTO, BLOQUEIO: vôlei
 * - CARTAO_AMARELO, CARTAO_VERMELHO: ambas
 *
 * Em GOL_CONTRA, o jogadorId é de quem marcou contra (equipe que errou),
 * mas o equipeId do evento é da equipe BENEFICIADA (que ganha o ponto) --
 * assim o placar geral continua sendo calculado por equipeId, igual aos
 * outros tipos de evento.
 */
public enum TipoEvento {
    GOL,
    GOL_CONTRA,
    PONTO,
    BLOQUEIO,
    CARTAO_AMARELO,
    CARTAO_VERMELHO
}
