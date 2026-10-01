-- Schema do banco Jogos Internos (SQLite)
-- Executado automaticamente pela DatabaseConnection na primeira vez que
-- o arquivo do banco é criado.

CREATE TABLE IF NOT EXISTS equipe (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    modalidade TEXT NOT NULL,
    escudo_path TEXT
);

CREATE TABLE IF NOT EXISTS jogador (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    numero INTEGER,
    posicao TEXT,
    foto_path TEXT,
    equipe_id INTEGER NOT NULL,
    FOREIGN KEY (equipe_id) REFERENCES equipe(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS partida (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    modalidade TEXT NOT NULL,
    equipe_a_id INTEGER NOT NULL,
    equipe_b_id INTEGER NOT NULL,
    data_hora TEXT NOT NULL,
    placar_a INTEGER NOT NULL DEFAULT 0,
    placar_b INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'EM_ANDAMENTO',
    campeonato_id INTEGER,
    fase TEXT,
    FOREIGN KEY (equipe_a_id) REFERENCES equipe(id),
    FOREIGN KEY (equipe_b_id) REFERENCES equipe(id),
    FOREIGN KEY (campeonato_id) REFERENCES campeonato(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS campeonato (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    modalidade TEXT NOT NULL,
    formato TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'EM_ANDAMENTO'
);

CREATE TABLE IF NOT EXISTS campeonato_equipe (
    campeonato_id INTEGER NOT NULL,
    equipe_id INTEGER NOT NULL,
    PRIMARY KEY (campeonato_id, equipe_id),
    FOREIGN KEY (campeonato_id) REFERENCES campeonato(id) ON DELETE CASCADE,
    FOREIGN KEY (equipe_id) REFERENCES equipe(id)
);

CREATE TABLE IF NOT EXISTS evento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    partida_id INTEGER NOT NULL,
    jogador_id INTEGER,
    equipe_id INTEGER NOT NULL,
    tipo TEXT NOT NULL,
    periodo TEXT,
    minuto_segundos INTEGER,
    timestamp TEXT NOT NULL,
    FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE CASCADE,
    FOREIGN KEY (jogador_id) REFERENCES jogador(id),
    FOREIGN KEY (equipe_id) REFERENCES equipe(id)
);

CREATE TABLE IF NOT EXISTS configuracao (
    chave TEXT PRIMARY KEY,
    valor TEXT NOT NULL
);

-- Migração leve para bancos criados ANTES da funcionalidade de campeonatos
-- (a tabela partida já existia sem essas colunas). Se a coluna já existir
-- (banco criado do zero com o schema novo), o erro "duplicate column" é
-- ignorado silenciosamente pela DatabaseConnection -- ver comentário lá.
ALTER TABLE partida ADD COLUMN campeonato_id INTEGER;
ALTER TABLE partida ADD COLUMN fase TEXT;

-- Configurações padrão (só insere se a chave ainda não existir)
INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('indicar_autor_gol', 'true');
INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('indicar_autor_bloqueio', 'true');
INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('exigir_jogador_cartao', 'true');
INSERT OR IGNORE INTO configuracao (chave, valor) VALUES ('abrir_tela_publica_automaticamente', 'true');
