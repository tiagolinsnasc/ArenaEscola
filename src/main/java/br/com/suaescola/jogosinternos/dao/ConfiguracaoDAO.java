package br.com.suaescola.jogosinternos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Acesso à tabela de configurações (chave/valor), usada pela tela de
 * Configurações -- ex.: "indicar_autor_gol" -> "true"/"false".
 */
public class ConfiguracaoDAO {

    public Map<String, String> listarTodas() {
        String sql = "SELECT chave, valor FROM configuracao";
        Map<String, String> mapa = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                mapa.put(rs.getString("chave"), rs.getString("valor"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar configurações", e);
        }
        return mapa;
    }

    public boolean getBoolean(String chave, boolean valorPadrao) {
        String valor = getString(chave, null);
        return valor != null ? Boolean.parseBoolean(valor) : valorPadrao;
    }

    public String getString(String chave, String valorPadrao) {
        String sql = "SELECT valor FROM configuracao WHERE chave = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, chave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("valor") : valorPadrao;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar configuração: " + chave, e);
        }
    }

    public void salvar(String chave, String valor) {
        String sql = "INSERT INTO configuracao (chave, valor) VALUES (?, ?) "
                + "ON CONFLICT(chave) DO UPDATE SET valor = excluded.valor";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, chave);
            ps.setString(2, valor);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar configuração: " + chave, e);
        }
    }
}
