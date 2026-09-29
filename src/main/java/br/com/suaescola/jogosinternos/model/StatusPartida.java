package br.com.suaescola.jogosinternos.model;

/**
 * Situação atual de uma partida.
 * ENCERRADA_WO: partida decidida por WO (vitória por ausência do
 * adversário) -- não chegou a ser jogada, então não tem eventos.
 */
public enum StatusPartida {
    EM_ANDAMENTO,
    ENCERRADA,
    ENCERRADA_WO
}
