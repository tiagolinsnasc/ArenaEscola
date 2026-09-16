package br.com.suaescola.jogosinternos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.com.suaescola.jogosinternos.model.Jogador;

public class JogadorDAO {

    public Jogador salvar(Jogador jogador) {
        if (jogador.getId() > 0) {
            atualizar(jogador);
            return jogador;
        }
        String sql = "INSERT INTO jogador (nome, numero, posicao, foto_path, equipe_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencher(ps, jogador);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    jogador.setId(rs.getInt(1));
                }
            }
            return jogador;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar jogador", e);
        }
    }

    public void atualizar(Jogador jogador) {
        String sql = "UPDATE jogador SET nome = ?, numero = ?, posicao = ?, foto_path = ?, equipe_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencher(ps, jogador);
            ps.setInt(6, jogador.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar jogador", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM jogador WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao excluir jogador (verifique se ele não está vinculado a eventos de alguma partida)", e);
        }
    }

    public List<Jogador> listarPorEquipe(int equipeId) {
        String sql = "SELECT * FROM jogador WHERE equipe_id = ? ORDER BY numero, nome";
        List<Jogador> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, equipeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar jogadores da equipe", e);
        }
        return lista;
    }

    public List<Jogador> listarTodos() {
        String sql = "SELECT * FROM jogador ORDER BY nome";
        List<Jogador> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar jogadores", e);
        }
        return lista;
    }

    private void preencher(PreparedStatement ps, Jogador jogador) throws SQLException {
        ps.setString(1, jogador.getNome());
        if (jogador.getNumero() != null) {
            ps.setInt(2, jogador.getNumero());
        } else {
            ps.setNull(2, Types.INTEGER);
        }
        ps.setString(3, jogador.getPosicao());
        ps.setString(4, jogador.getFotoPath());
        ps.setInt(5, jogador.getEquipeId());
    }

    private Jogador mapear(ResultSet rs) throws SQLException {
        int numero = rs.getInt("numero");
        Integer numeroObj = rs.wasNull() ? null : numero;

        return new Jogador(
                rs.getInt("id"),
                rs.getString("nome"),
                numeroObj,
                rs.getString("posicao"),
                rs.getString("foto_path"),
                rs.getInt("equipe_id")
        );
    }
}
