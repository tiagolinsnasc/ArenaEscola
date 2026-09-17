# Jogos Internos - Gestão de Partidas

Projeto base Java + JavaFX + Maven + SQLite para gerenciar as partidas de
futsal e vôlei dos jogos internos escolares.

## Requisitos

- JDK 21 instalado (OpenJDK 21)
- Eclipse com o plugin m2e (Maven) — já vem por padrão no Eclipse IDE for
  Java Developers
- Conexão à internet na primeira execução, para o Maven baixar as
  dependências

## Como importar no Eclipse

1. Descompacte este zip em uma pasta (fora de qualquer workspace ainda
   aberto, se preferir organizar depois).
2. No Eclipse: **File > Import... > Maven > Existing Maven Projects**.
3. Em "Root Directory", aponte para a pasta `jogos-internos` (onde está o
   `pom.xml`) e clique em **Finish**.
4. Aguarde o Eclipse baixar as dependências (acompanhe pela aba
   **Progress**). Isso pode demorar um pouco na primeira vez.
5. Clique com o botão direito no projeto > **Maven > Update Project**
   (Alt+F5) se algo parecer fora do lugar.

## Como rodar

Pelo terminal, dentro da pasta do projeto:

```
mvn clean javafx:run
```

Ou, dentro do Eclipse, rode a classe `MainApp`
(`src/main/java/br/com/suaescola/jogosinternos/MainApp.java`) como
**Java Application** — caso o Eclipse reclame do módulo do JavaFX ao
rodar direto (sem o plugin), prefira sempre `mvn javafx:run`, que já
configura o module-path automaticamente.

## Estrutura do projeto

```
src/main/java/br/com/suaescola/jogosinternos/
├── MainApp.java              # classe principal (Application)
└── controller/
    └── MainController.java   # controller da tela inicial

src/main/resources/br/com/suaescola/jogosinternos/
├── fxml/
│   └── MainView.fxml         # layout da tela inicial (menu + dashboard)
├── css/
│   └── style.css             # estilo visual
└── images/                   # (vazio por enquanto — escudos das equipes, ícones)
```

Pacotes que ainda serão adicionados nas próximas etapas: `model`, `dao`,
`service` e `report`.

## Ícone do aplicativo

Diferente do logotipo da escola (que fica em `dados/`, pois muda de
instalação para instalação), o ícone do próprio aplicativo é fixo e fica
empacotado dentro do `.jar`. Para definir/trocar esse ícone, coloque um
arquivo `icone-app.png` em:

```
src/main/resources/br/com/suaescola/jogosinternos/images/icone-app.png
```

e reconstrua o projeto. Se o arquivo não existir, o programa continua
funcionando normalmente, só usa o ícone padrão do Java/JavaFX.

## Onde os dados ficam salvos

O aplicativo cria uma pasta `dados/` ao lado de onde ele é executado
(a pasta de trabalho da aplicação -- geralmente a raiz do projeto, se
você rodar com `mvn javafx:run` pelo Eclipse). Essa pasta é visível
(não oculta) e contém:

```
dados/
├── jogosinternos.db     # banco SQLite
├── escudos/              # escudos das equipes
├── jogadores/             # fotos dos jogadores
└── logo-escola.png        # logotipo da escola (você coloca manualmente aqui)
```

Para exibir o logotipo da escola nas telas de placar, basta colocar um
arquivo `logo-escola.png` (ou `.jpg`/`.jpeg`) dentro dessa pasta `dados/`.

## Estado atual

A tela inicial já funciona: menu no topo e um painel com atalhos para as
principais funções. Cada atalho hoje só mostra um aviso "em construção" —
as telas reais (cadastro de equipes, jogadores, placar, relatórios) serão
implementadas nas próximas etapas.
