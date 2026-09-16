package br.com.suaescola.jogosinternos.model;

import java.time.LocalDateTime;

/**
 * Representa um evento lançado durante a partida: gol, ponto, bloqueio,
 * cartão amarelo ou vermelho. Todo evento pertence a uma equipe; o jogador
 * é opcional (por exemplo, se a configuração "indicar quem fez o gol"
 * estiver desativada).
 */
public class Evento {

    private int id;
    private int partidaId;
    private Integer jogadorId; // pode ser nulo, dependendo da configuração
    private int equipeId;
    private TipoEvento tipo;
    private String periodo;     // ex.: "1T", "2T" (futsal) ou "SET1", "SET2" (vôlei)
    private Integer minutoSegundos; // tempo decorrido em segundos no momento do evento (futsal)
    private LocalDateTime timestamp;

    public Evento() {
    }

    public Evento(int id, int partidaId, Integer jogadorId, int equipeId, TipoEvento tipo,
                   String periodo, Integer minutoSegundos, LocalDateTime timestamp) {
        this.id = id;
        this.partidaId = partidaId;
        this.jogadorId = jogadorId;
        this.equipeId = equipeId;
        this.tipo = tipo;
        this.periodo = periodo;
        this.minutoSegundos = minutoSegundos;
        this.timestamp = timestamp;
    }

    public static Evento novoEvento(int partidaId, Integer jogadorId, int equipeId,
                                     TipoEvento tipo, String periodo, Integer minutoSegundos) {
        Evento e = new Evento();
        e.partidaId = partidaId;
        e.jogadorId = jogadorId;
        e.equipeId = equipeId;
        e.tipo = tipo;
        e.periodo = periodo;
        e.minutoSegundos = minutoSegundos;
        e.timestamp = LocalDateTime.now();
        return e;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(int partidaId) {
        this.partidaId = partidaId;
    }

    public Integer getJogadorId() {
        return jogadorId;
    }

    public void setJogadorId(Integer jogadorId) {
        this.jogadorId = jogadorId;
    }

    public int getEquipeId() {
        return equipeId;
    }

    public void setEquipeId(int equipeId) {
        this.equipeId = equipeId;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Integer getMinutoSegundos() {
        return minutoSegundos;
    }

    public void setMinutoSegundos(Integer minutoSegundos) {
        this.minutoSegundos = minutoSegundos;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
