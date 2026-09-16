package br.com.suaescola.jogosinternos.estado;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Estado "ao vivo" de uma partida em andamento. A tela de controle (com os
 * botões de gol/cartão, visível só para quem está gerenciando) escreve
 * aqui; a tela pública (telão) só lê, através de binding -- assim ela
 * atualiza sozinha, sem precisar consultar o banco a cada evento.
 */
public class EstadoPartida {

    private final StringProperty nomeEquipeA = new SimpleStringProperty("");
    private final StringProperty nomeEquipeB = new SimpleStringProperty("");
    private final StringProperty escudoPathA = new SimpleStringProperty();
    private final StringProperty escudoPathB = new SimpleStringProperty();
    private final IntegerProperty placarA = new SimpleIntegerProperty(0);
    private final IntegerProperty placarB = new SimpleIntegerProperty(0);
    private final StringProperty periodoAtual = new SimpleStringProperty("");
    private final StringProperty tempoFormatado = new SimpleStringProperty("00:00");
    private final ObservableList<String> eventosEquipeA = FXCollections.observableArrayList();
    private final ObservableList<String> eventosEquipeB = FXCollections.observableArrayList();

    public StringProperty nomeEquipeAProperty() {
        return nomeEquipeA;
    }

    public StringProperty nomeEquipeBProperty() {
        return nomeEquipeB;
    }

    public StringProperty escudoPathAProperty() {
        return escudoPathA;
    }

    public StringProperty escudoPathBProperty() {
        return escudoPathB;
    }

    public IntegerProperty placarAProperty() {
        return placarA;
    }

    public IntegerProperty placarBProperty() {
        return placarB;
    }

    public StringProperty periodoAtualProperty() {
        return periodoAtual;
    }

    public StringProperty tempoFormatadoProperty() {
        return tempoFormatado;
    }

    /** Log de eventos da equipe A, mais recente no índice 0. Compartilhada por referência com a tela pública. */
    public ObservableList<String> getEventosEquipeA() {
        return eventosEquipeA;
    }

    /** Log de eventos da equipe B, mais recente no índice 0. Compartilhada por referência com a tela pública. */
    public ObservableList<String> getEventosEquipeB() {
        return eventosEquipeB;
    }
}
