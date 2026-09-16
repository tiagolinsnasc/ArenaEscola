package br.com.suaescola.jogosinternos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.suaescola.jogosinternos.model.Equipe;
import br.com.suaescola.jogosinternos.model.Modalidade;

public class EquipeDAO {

    public Equipe salvar(Equipe equipe) {
        if (equipe.getId() > 0) {
            atualizar(equipe);
            return equipe;
        }
        String sql = "INSERT INTO equipe (nome, modalidade, escudo_path) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, equipe.getNome());
            ps.setString(2, equipe.getModalidade().name());
            ps.setString(3, equipe.getEscudoPath());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    equipe.setId(rs.getInt(1));
                }
            }
            return equipe;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar equipe", e);
        }
    }

    public void atualizar(Equipe equipe) {
        String sql = "UPDATE equipe SET nome = ?, modalidade = ?, escudo_path = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, equipe.getNome());
            ps.setString(2, equipe.getModalidade().name());
            ps.setString(3, equipe.getEscudoPath());
            ps.setInt(4, equipe.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar equipe", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM equipe WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao excluir equipe (verifique se ela não está vinculada a jogadores ou partidas)", e);
        }
    }

    public Equipe buscarPorId(int id) {
        String sql = "SELECT * FROM equipe WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar equipe", e);
        }
    }

    public List<Equipe> listarTodas() {
        String sql = "SELECT * FROM equipe ORDER BY nome";
        List<Equipe> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar equipes", e);
        }
        return lista;
    }

    public List<Equipe> listarPorModalidade(Modalidade modalidade) {
        String sql = "SELECT * FROM equipe WHERE modalidade = ? ORDER BY nome";
        List<Equipe> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, modalidade.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar equipes por modalidade", e);
        }
        return lista;
    }

    private Equipe mapear(ResultSet rs) throws SQLException {
        return new Equipe(
                rs.getInt("id"),
                rs.getString("nome"),
                Modalidade.valueOf(rs.getString("modalidade")),
                rs.getString("escudo_path")
        );
    }
}
