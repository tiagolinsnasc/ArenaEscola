package br.com.suaescola.jogosinternos.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import br.com.suaescola.jogosinternos.util.PastaDadosUtil;

/**
 * Fábrica de conexões com o banco SQLite.
 *
 * IMPORTANTE: cada chamada a getConnection() devolve uma conexão NOVA.
 * Isso é proposital -- os DAOs abrem a conexão dentro de um
 * try-with-resources e a fecham ao final de cada operação; se
 * devolvêssemos sempre a mesma conexão (singleton), a primeira operação
 * fecharia essa conexão e todas as seguintes falhariam com
 * "database connection closed". Para SQLite (arquivo local), abrir/fechar
 * uma conexão por operação é barato e não é um problema de performance
 * nessa escala de uso.
 *
 * O arquivo do banco fica na pasta de dados do aplicativo (veja
 * PastaDadosUtil) -- visível, ao lado do próprio programa.
 */
public final class DatabaseConnection {

    private static final String ARQUIVO_BANCO = "jogosinternos.db";
    private static final String SCHEMA_RESOURCE =
            "/br/com/suaescola/jogosinternos/db/schema.sql";

    private static volatile boolean schemaInicializado = false;

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            Path pastaDados = PastaDadosUtil.pastaDados();
            Files.createDirectories(pastaDados);
            Path arquivoBanco = pastaDados.resolve(ARQUIVO_BANCO);

            String url = "jdbc:sqlite:" + arquivoBanco.toAbsolutePath();
            Connection conn = DriverManager.getConnection(url);

            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }

            garantirSchemaInicializado(conn);
            return conn;

        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Não foi possível conectar ao banco de dados", e);
        }
    }

    private static synchronized void garantirSchemaInicializado(Connection conn) throws SQLException {
        if (schemaInicializado) {
            return;
        }
        inicializarSchema(conn);
        schemaInicializado = true;
    }

    private static void inicializarSchema(Connection conn) throws SQLException {
        String sql = lerSchemaSql();
        try (Statement st = conn.createStatement()) {
            for (String comando : sql.split(";")) {
                String trimmed = comando.trim();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
        }
    }

    private static String lerSchemaSql() {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Arquivo de schema não encontrado: " + SCHEMA_RESOURCE);
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    // ignora linhas de comentario puras, mantém o restante
                    if (!linha.trim().startsWith("--")) {
                        sb.append(linha).append('\n');
                    }
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao ler o schema.sql", e);
        }
    }
}
