package br.com.suaescola.jogosinternos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.suaescola.jogosinternos.model.Evento;
import br.com.suaescola.jogosinternos.model.TipoEvento;

public class EventoDAO {

    public Evento salvar(Evento evento) {
        String sql = "INSERT INTO evento (partida_id, jogador_id, equipe_id, tipo, periodo, minuto_segundos, timestamp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, evento.getPartidaId());
            if (evento.getJogadorId() != null) {
                ps.setInt(2, evento.getJogadorId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, evento.getEquipeId());
            ps.setString(4, evento.getTipo().name());
            ps.setString(5, evento.getPeriodo());
            if (evento.getMinutoSegundos() != null) {
                ps.setInt(6, evento.getMinutoSegundos());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, evento.getTimestamp().toString());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    evento.setId(rs.getInt(1));
                }
            }
            return evento;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar evento", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM evento WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir evento", e);
        }
    }

    /** Todos os eventos de uma partida, em ordem cronológica -- usado no placar e na súmula. */
    public List<Evento> listarPorPartida(int partidaId) {
        String sql = "SELECT * FROM evento WHERE partida_id = ? ORDER BY timestamp";
        List<Evento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, partidaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar eventos da partida", e);
        }
        return lista;
    }

    /** Todos os eventos de um determinado tipo em todas as partidas -- usado nas estatísticas consolidadas. */
    public List<Evento> listarPorTipo(TipoEvento tipo) {
        String sql = "SELECT * FROM evento WHERE tipo = ? ORDER BY timestamp";
        List<Evento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar eventos por tipo", e);
        }
        return lista;
    }

    private Evento mapear(ResultSet rs) throws SQLException {
        int jogadorId = rs.getInt("jogador_id");
        Integer jogadorIdObj = rs.wasNull() ? null : jogadorId;

        int minutoSegundos = rs.getInt("minuto_segundos");
        Integer minutoSegundosObj = rs.wasNull() ? null : minutoSegundos;

        return new Evento(
                rs.getInt("id"),
                rs.getInt("partida_id"),
                jogadorIdObj,
                rs.getInt("equipe_id"),
                TipoEvento.valueOf(rs.getString("tipo")),
                rs.getString("periodo"),
                minutoSegundosObj,
                LocalDateTime.parse(rs.getString("timestamp"))
        );
    }
}
