# SystemaFinanceiro
Documentação de Software: Sistema Financeiro Itaú
1. Introdução
Este documento apresenta a especificação técnica e funcional do Sistema Financeiro Itaú, uma
aplicação desktop em linguagem Java desenvolvida com o objetivo de oferecer gerenciamento
autônomo e categorizado de finanças pessoais. A solução integra uma interface gráfica amigável
baseada na biblioteca Swing (via componentes JOptionPane) com um banco de dados relacional
embarcado e leve (SQLite).
O software opera sob um modelo multiusuário local, onde cada indivíduo possui sua própria carteira
isolada e protegida por credenciais de autenticação, mitigando erros clássicos de misturas de fluxos de
caixa corporativos ou familiares.
2. Objetivos
Objetivo Geral
Desenvolver um protótipo de sistema de controle financeiro seguro, modular e de fácil execução que
permita o registro e monitoramento contínuo de movimentações financeiras de crédito e débito
vinculadas a usuários cadastrados.
Objetivos Específicos
• 
• 
• 
Prover um mecanismo de persistência seguro utilizando banco de dados relacional local (SQLite)
sem necessidade de servidores complexos de infraestrutura.
Permitir o cálculo dinâmico de saldo disponível (Receitas menos Despesas) de forma isolada por
conta autenticada.
Disponibilizar filtros de consulta por palavras-chave e operações ágeis de exclusão de registros
incorretos através de identificadores curtos (Ex: R-1, D-3).
3. Requisitos Funcionais (RF)
Os requisitos funcionais descrevem as ações e comportamentos que o sistema deve executar a partir
das interações do usuário.
Código
Descrição do Requisito
Regra de Negócio Associada
RF-001
RF-002
Cadastrar Novo Usuário
Autenticação (Login)
O sistema deve coletar Nome, E-mail (obrigatório
conter caractere '@') e Senha. O e-mail deve atuar
como chave exclusiva (UNIQUE).
O usuário só poderá acessar o painel interno
inserindo combinações válidas de e-mail e senha
previamente cadastrados.
RF-003
Cadastrar Receita
Documentação de Sistema - API Financeiro Itaú
Permitir a inserção de entradas financeiras
fornecendo Descrição, Valor positivo, Data (DD/MM/
AAAA) e Categoria vinculada.
1
Código Descrição do Requisito Regra de Negócio Associada
RF-004 Cadastrar Despesa Permitir o registro de saídas financeiras fornecendo
Descrição, Valor, Data e Categoria, associando ao ID
do usuário ativo.
RF-005 Consultar Saldo Dinâmico Calcular e exibir em tempo de execução a soma de
todas as receitas do usuário subtraída do total de
suas despesas.
RF-006 Gerar Relatório de Lançamentos Exibir de forma unificada uma listagem contendo ID
gerado, Tipo (Receita/Despesa), Descrição, Valor,
Data e Categoria das movimentações.
RF-007 Pesquisar Lançamento Filtrar o relatório de lançamentos com base em um
termo de pesquisa ou palavra-chave digitada pelo
usuário nas descrições.
RF-008 Excluir Lançamentos Permitir a deleção física de um registro de receita ou
despesa inserindo o código identificador formatado
(Ex: R-[ID] ou D-[ID]).
4. Requisitos Não Funcionais (RNF)
Os requisitos não funcionais especificam critérios que qualificam os atributos globais de qualidade e
restrições do software.
Atributo Descrição da Restrição / Critério Técnico
RNF-001 
Persistência
O sistema deve operar com banco de dados em arquivo local financeiro.db
gerenciado pelo motor SQLite 3.x, dispensando instalações de SGBD externos.
RNF-002 
Tecnologia
A aplicação deve ser desenvolvida em Java SE utilizando a biblioteca nativa JDBC e
drivers estruturados para execução direta via Java Virtual Machine (JVM).
RNF-003 
Interface
A interface homem-máquina (IHM) deve ser construída por meio de diálogos modais
gráficos simplificados da classe javax.swing.JOptionPane.
RNF-004 
Portabilidade
Por utilizar SQLite e Java, a aplicação deve possuir portabilidade integral entre sistemas
operacionais Windows, Linux e macOS.
Documentação de Sistema - API Financeiro Itaú 2
5. Casos de Uso (UC)
Abaixo são detalhados os principais fluxos de interação dos atores com o sistema.
[UC-01] Cadastrar Usuário e Acessar o Sistema
• 
• 
• 
1. 
2. 
3. 
4. 
5. 
Ator Principal: Usuário Não Autenticado.
Fluxo Principal:
O ator seleciona a opção "Cadastrar Usuário" no menu inicial.
O sistema solicita sucessivamente o Nome, E-mail e Senha.
O usuário insere os dados. O sistema valida se o e-mail contém "@".
O sistema persiste as informações na tabela usuarios e exibe mensagem de sucesso.
O ator seleciona "Fazer Login", preenche suas credenciais e o sistema libera o acesso ao "Painel
Interno".
Fluxos de Exceção: E-mail inválido ou já cadastrado aborta a operação com alerta visual na tela.
[UC-02] Controle de Lançamentos e Exclusão
• 
• 
1. 
2. 
3. 
4. 
5. 
Ator Principal: Usuário Autenticado.
Fluxo Principal:
O ator seleciona "Cadastrar Despesa" ou "Cadastrar Receita".
O sistema abre caixas de entrada para Descrição, Valor, Data e fornece uma lista de seleção para
a Categoria (Alimentação, Transporte, Saúde, Lazer, Salário, Outros).
O sistema converte os valores para padrão decimal e injeta a referência do usuario_id logado.
O ator acessa "Gerar Relatório / Excluir" para conferir os dados inseridos.
Para corrigir um lançamento, o ator digita o código (ex: D-1) no campo inferior da janela de
relatório e confirma a exclusão.
6. Banco de Dados
O modelo físico adota uma arquitetura relacional de 1 para Muitos (1:N), onde um usuário pode ter
múltiplas receitas e despesas associadas. Toda a criação de tabelas ocorre de maneira automatizada
na inicialização do sistema (IF NOT EXISTS).
Dicionário de Dados / Estrutura das Tabelas
Tabela: usuarios
Coluna Tipo de Dados Restrições
id
INTEGER
Descrição
PRIMARY KEY AUTOINCREMENT Chave primária exclusiva do usuário.
nome
TEXT
NOT NULL
Nome completo ou apelido do usuário.
email
TEXT
UNIQUE NOT NULL
Documentação de Sistema - API Financeiro Itaú
E-mail de login (Não permite duplicidade).
3
Coluna Tipo de Dados Restrições Descrição
senha TEXT NOT NULL Senha de acesso em texto limpo.
Tabelas: receitas e despesas (Estruturas idênticas)
Coluna Tipo de
Dados
Restrições Descrição
id INTEGER PRIMARY KEY
AUTOINCREMENT
Chave primária do lançamento.
descricao TEXT NOT NULL Breve identificação da transação.
valor REAL NOT NULL Valor monetário da transação (Ponto
flutuante).
data TEXT NOT NULL Data do evento salva em formato String.
categoria TEXT NOT NULL Categoria associada ao plano de contas.
usuario_id INTEGER FOREIGN KEY Chave estrangeira apontando para 
usuarios(id).
Documentação de Sistema - API Financeiro Itaú 4
7. Protótipos (Visualização Conceitual das Interfaces)
Abaixo estão representados textualmente os layouts gerados pelas chamadas de diálogos do
JOptionPane implementados no código fonte.
Janela: Boas-vindas (Menu Deslogado)
[ Ícone Banco Itaú ]
=== SISTEMA FINANCEIRO ITAÚ ===
Escolha uma opção:
[ Fazer Login ] [ Cadastrar Usuário ] [ Sair ]
Janela: Painel Interno (Menu Logado)
[ Ícone Banco Itaú ]
Olá, [Nome do Usuário Logado]
O que deseja gerenciar hoje?
[ Cadastrar Receita ] [ Cadastrar Despesa ] [ Consultar Saldo ]
[ Gerar Relatório / Excluir ] [ Pesquisar Lançamento ] [ Sair ]
Janela: Relatório Geral e Comando de Deleção
=== RELATÓRIO DE LANÇAMENTOS ===
ID  | TIPO    | DESCRIÇÃO       | VALOR      | DATA       | CATEGORIA
R-1 | RECEITA | Salário Mensal  | R$ 5000,00 | 05/06/2026 | Salário
D-1 | DESPESA | Supermercado    | R$ 450,00  | 10/06/2026 | Alimentação
D-2 | DESPESA | Abastecimento   | R$ 120,00  | 12/06/2026 | Transporte
Saldo Disponível Geral: R$ 4430,00
Deseja excluir algum registro? Digite o código correspondente (ex: R-2 ou D-5) ou deixe em branco para fechar:
Input: [ D-1_ ]    [ OK ] [ Cancelar ]
8. Conclusão
O Sistema Financeiro Itaú cumpre satisfatoriamente as diretrizes de um software utilitário ágil e
funcional para controle de despesas e receitas. Ao acoplar regras de isolamento de dados por ID de
usuário e comandos práticos de exclusão indexada diretamente na tela de listagem, o sistema resolve
Documentação de Sistema - API Financeiro Itaú 5
dobras comuns de experiência do usuário em consoles de terminal tradicionais de forma compacta e
objetiva.
Como melhorias futuras para iterações do ciclo de vida deste software, recomenda-se a inserção de
criptografia de senhas (ex: BCrypt ou SHA-256) antes da persistência em banco de dados e a migração
da interface gráfica baseada em diálogos para telas completas utilizando JavaFX.
Documentação de Sistema - API Financeiro Itaú
6
