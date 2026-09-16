package br.com.suaescola.jogosinternos.model;

/**
 * Representa uma equipe cadastrada (de futsal ou de vôlei).
 */
public class Equipe {

    private int id;
    private String nome;
    private Modalidade modalidade;
    private String escudoPath; // caminho do arquivo de imagem do escudo, pode ser nulo

    public Equipe() {
    }

    public Equipe(int id, String nome, Modalidade modalidade, String escudoPath) {
        this.id = id;
        this.nome = nome;
        this.modalidade = modalidade;
        this.escudoPath = escudoPath;
    }

    public Equipe(String nome, Modalidade modalidade, String escudoPath) {
        this(0, nome, modalidade, escudoPath);
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

    public Modalidade getModalidade() {
        return modalidade;
    }

    public void setModalidade(Modalidade modalidade) {
        this.modalidade = modalidade;
    }

    public String getEscudoPath() {
        return escudoPath;
    }

    public void setEscudoPath(String escudoPath) {
        this.escudoPath = escudoPath;
    }

    @Override
    public String toString() {
        // usado diretamente em ComboBox/ListView
        return nome;
    }
}
