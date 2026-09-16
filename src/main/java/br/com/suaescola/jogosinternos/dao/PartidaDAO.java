package br.com.suaescola.jogosinternos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.suaescola.jogosinternos.model.Modalidade;
import br.com.suaescola.jogosinternos.model.Partida;
import br.com.suaescola.jogosinternos.model.StatusPartida;

public class PartidaDAO {

    public Partida salvar(Partida partida) {
        if (partida.getId() > 0) {
            atualizar(partida);
            return partida;
        }
        String sql = "INSERT INTO partida (modalidade, equipe_a_id, equipe_b_id, data_hora, placar_a, placar_b, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencher(ps, partida);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    partida.setId(rs.getInt(1));
                }
            }
            return partida;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar partida", e);
        }
    }

    /** Atualiza placar/status -- chamado a cada evento lançado e ao encerrar a partida. */
    public void atualizar(Partida partida) {
        String sql = "UPDATE partida SET modalidade = ?, equipe_a_id = ?, equipe_b_id = ?, data_hora = ?, "
                + "placar_a = ?, placar_b = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencher(ps, partida);
            ps.setInt(8, partida.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar partida", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM partida WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir partida", e);
        }
    }

    public Partida buscarPorId(int id) {
        String sql = "SELECT * FROM partida WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar partida", e);
        }
    }

    public List<Partida> listarTodas() {
        String sql = "SELECT * FROM partida ORDER BY data_hora DESC";
        List<Partida> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar partidas", e);
        }
        return lista;
    }

    public List<Partida> listarEmAndamento() {
        String sql = "SELECT * FROM partida WHERE status = 'EM_ANDAMENTO' ORDER BY data_hora DESC";
        List<Partida> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar partidas em andamento", e);
        }
        return lista;
    }

    private void preencher(PreparedStatement ps, Partida partida) throws SQLException {
        ps.setString(1, partida.getModalidade().name());
        ps.setInt(2, partida.getEquipeAId());
        ps.setInt(3, partida.getEquipeBId());
        ps.setString(4, partida.getDataHora().toString());
        ps.setInt(5, partida.getPlacarA());
        ps.setInt(6, partida.getPlacarB());
        ps.setString(7, partida.getStatus().name());
    }

    private Partida mapear(ResultSet rs) throws SQLException {
        return new Partida(
                rs.getInt("id"),
                Modalidade.valueOf(rs.getString("modalidade")),
                rs.getInt("equipe_a_id"),
                rs.getInt("equipe_b_id"),
                LocalDateTime.parse(rs.getString("data_hora")),
                rs.getInt("placar_a"),
                rs.getInt("placar_b"),
                StatusPartida.valueOf(rs.getString("status"))
        );
    }
}
