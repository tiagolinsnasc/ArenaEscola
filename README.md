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

## Gerando um pacote pronto para rodar no Windows (sem instalar Java, sem precisar de um Windows)

O `jpackage` (a ferramenta que gera o `.exe` com o runtime Java embutido)
só funciona rodando no mesmo sistema operacional do pacote final -- ou
seja, teria que rodar num Windows de verdade. Como você não tem uma
máquina Windows, a solução é usar o **GitHub Actions**: o GitHub te
empresta, de graça, uma máquina Windows na nuvem só pra rodar esse build.
Já deixei um workflow pronto em `.github/workflows/build-windows.yml`.

### Passo a passo

1. Crie uma conta gratuita em [github.com](https://github.com), se ainda não tiver.
2. Crie um repositório novo (pode ser privado) e suba este projeto nele
   (pelo Eclipse: botão direito no projeto > Team > Share Project > Git,
   ou usando o GitHub Desktop, que tem interface gráfica e não exige
   linha de comando).
3. No repositório, no GitHub, vá na aba **Actions**.
4. Clique no workflow **"Build Windows Package"** na lista à esquerda.
5. Clique no botão **"Run workflow"** (canto direito) > **Run workflow**
   de novo para confirmar.
6. Aguarde alguns minutos (aparece uma bolinha amarela girando, depois
   um check verde quando terminar).
7. Clique na execução concluída > role até **Artifacts**, no fim da
   página > baixe **JogosInternos-Windows** (vem como um `.zip`).
8. Dentro do zip está a pasta `JogosInternos/` com o `JogosInternos.exe`
   já pronto -- é isso que você distribui. Quem for rodar não precisa
   instalar Java nem JavaFX.

Cada vez que quiser gerar uma versão nova (depois de alguma mudança),
suba o código atualizado pro GitHub e repita os passos 4 a 8.

**Sobre o ícone:** como no passo anterior, o `pom.xml` espera um arquivo
`icone-app.ico` (formato Windows, não `.png`) na raiz do projeto. Se
esse arquivo não existir, o jpackage usa um ícone padrão -- não trava o
build, só fica sem o ícone customizado.

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

Regra geral: tudo que está dentro de `src/main/resources` vira a raiz
do classpath. Então `src/main/resources/icone-app.png` é chamado no
código como `/icone-app.png`; se estivesse em
`src/main/resources/images/icone-app.png`, seria `/images/icone-app.png`.

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
