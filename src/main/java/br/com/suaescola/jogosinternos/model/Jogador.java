package br.com.suaescola.jogosinternos.model;

/**
 * Representa um jogador vinculado a uma equipe.
 */
public class Jogador {

    private int id;
    private String nome;
    private Integer numero;   // número da camisa, pode ser nulo
    private String posicao;   // opcional
    private String fotoPath;  // opcional
    private int equipeId;

    public Jogador() {
    }

    public Jogador(int id, String nome, Integer numero, String posicao, String fotoPath, int equipeId) {
        this.id = id;
        this.nome = nome;
        this.numero = numero;
        this.posicao = posicao;
        this.fotoPath = fotoPath;
        this.equipeId = equipeId;
    }

    public Jogador(String nome, Integer numero, String posicao, String fotoPath, int equipeId) {
        this(0, nome, numero, posicao, fotoPath, equipeId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getPosicao() {
        return posicao;
    }

    public void setPosicao(String posicao) {
        this.posicao = posicao;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public int getEquipeId() {
        return equipeId;
    }

    public void setEquipeId(int equipeId) {
        this.equipeId = equipeId;
    }

    @Override
    public String toString() {
        // usado diretamente em ComboBox/ListView: "10 - Fulano"
        return (numero != null ? numero + " - " : "") + nome;
    }
}
