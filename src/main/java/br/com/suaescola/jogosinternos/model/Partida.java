package br.com.suaescola.jogosinternos.model;

import java.time.LocalDateTime;

/**
 * Representa uma partida entre duas equipes da mesma modalidade.
 * O placar (placarA/placarB) é mantido aqui como um cache do resultado --
 * a fonte de verdade real são os eventos (tabela evento), mas manter o
 * placar na própria partida evita ter que recalcular a cada consulta de
 * histórico.
 *
 * campeonatoId e fase são opcionais (null): uma partida avulsa (fora de
 * campeonato) não tem nenhum dos dois. Quando pertence a um campeonato,
 * "fase" identifica a rodada/etapa (ex.: "Pontos Corridos", "Quartas de
 * Final", "Semifinal", "Final").
 */
public class Partida {

    private int id;
    private Modalidade modalidade;
    private int equipeAId;
    private int equipeBId;
    private LocalDateTime dataHora;
    private int placarA;
    private int placarB;
    private StatusPartida status;
    private Integer campeonatoId;
    private String fase;

    public Partida() {
    }

    public Partida(int id, Modalidade modalidade, int equipeAId, int equipeBId,
                    LocalDateTime dataHora, int placarA, int placarB, StatusPartida status,
                    Integer campeonatoId, String fase) {
        this.id = id;
        this.modalidade = modalidade;
        this.equipeAId = equipeAId;
        this.equipeBId = equipeBId;
        this.dataHora = dataHora;
        this.placarA = placarA;
        this.placarB = placarB;
        this.status = status;
        this.campeonatoId = campeonatoId;
        this.fase = fase;
    }

    public static Partida novaPartida(Modalidade modalidade, int equipeAId, int equipeBId) {
        Partida p = new Partida();
        p.modalidade = modalidade;
        p.equipeAId = equipeAId;
        p.equipeBId = equipeBId;
        p.dataHora = LocalDateTime.now();
        p.placarA = 0;
        p.placarB = 0;
        p.status = StatusPartida.EM_ANDAMENTO;
        return p;
    }

    public static Partida novaPartidaDeCampeonato(Modalidade modalidade, int equipeAId, int equipeBId,
                                                   int campeonatoId, String fase) {
        Partida p = novaPartida(modalidade, equipeAId, equipeBId);
        p.campeonatoId = campeonatoId;
        p.fase = fase;
        return p;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public int getEquipeAId() {
        return equipeAId;
    }

    public void setEquipeAId(int equipeAId) {
        this.equipeAId = equipeAId;
    }

    public int getEquipeBId() {
        return equipeBId;
    }

    public void setEquipeBId(int equipeBId) {
        this.equipeBId = equipeBId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public int getPlacarA() {
        return placarA;
    }

    public void setPlacarA(int placarA) {
        this.placarA = placarA;
    }

    public int getPlacarB() {
        return placarB;
    }

    public void setPlacarB(int placarB) {
        this.placarB = placarB;
    }

    public StatusPartida getStatus() {
        return status;
    }

    public void setStatus(StatusPartida status) {
        this.status = status;
    }

    public Integer getCampeonatoId() {
        return campeonatoId;
    }

    public void setCampeonatoId(Integer campeonatoId) {
        this.campeonatoId = campeonatoId;
    }

    public String getFase() {
        return fase;
    }

    public void setFase(String fase) {
        this.fase = fase;
    }
}
