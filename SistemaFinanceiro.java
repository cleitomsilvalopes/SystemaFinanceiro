package API_Financeiro;

import java.sql.*;
import javax.swing.JOptionPane;

public class SistemaFinanceiro {

    private static final String URL_BANCO = "jdbc:sqlite:financeiro.db";
    private static int usuarioLogadoId = -1; // -1 significa que ninguém está logado
    private static String usuarioLogadoNome = "";

    static javax.swing.ImageIcon imagem = new javax.swing.ImageIcon("C:\\Users\\cleitom\\NickName\\Projeto_API_Financeiro\\API_Financeiro\\Banco-Itau-Logo-New.png");

    // Força o carregamento do Driver SQLite
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver SQLite não encontrado na biblioteca.");
        }
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL_BANCO);
    }

    // BANCO DE DADOS: Criação das tabelas solicitadas
    public static void inicializarBanco() {
        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            // Tabela Usuário
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "nome TEXT NOT NULL, " +
                         "email TEXT UNIQUE NOT NULL, " +
                         "senha TEXT NOT NULL);");

            // Tabela Receita (com usuario_id)
            stmt.execute("CREATE TABLE IF NOT EXISTS receitas (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "descricao TEXT NOT NULL, " +
                         "valor REAL NOT NULL, " +
                         "data TEXT NOT NULL, " +
                         "categoria TEXT NOT NULL, " +
                         "usuario_id INTEGER, " +
                         "FOREIGN KEY(usuario_id) REFERENCES usuarios(id));");

            // Tabela Despesa (com usuario_id)
            stmt.execute("CREATE TABLE IF NOT EXISTS despesas (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "descricao TEXT NOT NULL, " +
                         "valor REAL NOT NULL, " +
                         "data TEXT NOT NULL, " +
                         "categoria TEXT NOT NULL, " +
                         "usuario_id INTEGER, " +
                         "FOREIGN KEY(usuario_id) REFERENCES usuarios(id));");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao iniciar banco: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        inicializarBanco();

        while (true) {
            String[] opcoesDeslogado = {"Fazer Login", "Cadastrar Usuário", "Sair"};
            int escolha = JOptionPane.showOptionDialog(null, "=== SISTEMA FINANCEIRO ITAÚ ===\nEscolha uma opção:", "Boas-vindas",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, imagem, opcoesDeslogado, opcoesDeslogado[0]);

            if (escolha == 0) {
                fazerLogin();
                if (usuarioLogadoId != -1) {
                    menuPainelPrincipal();
                }
            } else if (escolha == 1) {
                cadastrarUsuario();
            } else {
                break; // Sair do programa
            }
        }
    }

    // CASO DE USO: Cadastrar Usuário
    public static void cadastrarUsuario() {
        String nome = JOptionPane.showInputDialog("Digite seu nome:");
        if (nome == null || nome.trim().isEmpty()) return;

        String email = JOptionPane.showInputDialog("Digite seu e-mail:");
        if (email == null || !email.contains("@")) {
            JOptionPane.showMessageDialog(null, "E-mail inválido.");
            return;
        }

        String senha = JOptionPane.showInputDialog("Crie uma senha:");
        if (senha == null || senha.isEmpty()) return;

        String sql = "INSERT INTO usuarios(nome, email, senha) VALUES(?, ?, ?)";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, email);
            pstmt.setString(3, senha);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar (E-mail já pode existir): " + e.getMessage());
        }
    }

    // CASO DE USO: Fazer Login
    public static void fazerLogin() {
        String email = JOptionPane.showInputDialog("Digite seu e-mail:");
        String senha = JOptionPane.showInputDialog("Digite sua senha:");

        String sql = "SELECT id, nome FROM usuarios WHERE email = ? AND senha = ?";
        try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, senha);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                usuarioLogadoId = rs.getInt("id");
                usuarioLogadoNome = rs.getString("nome");
                JOptionPane.showMessageDialog(null, "Bem-vindo(a), " + usuarioLogadoNome + "!");
            } else {
                JOptionPane.showMessageDialog(null, "Usuário ou senha incorretos! Cadastre-se caso não tenha conta.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // PAINEL LOGADO: Operações internas do Banco de Dados
    public static void menuPainelPrincipal() {
        String[] categorias = {"Alimentação", "Transporte", "Saúde", "Lazer", "Salário", "Outros"};

        while (true) {
            String[] operacoes = {
                "Cadastrar Receita", 
                "Cadastrar Despesa", 
                "Consultar Saldo", 
                "Gerar Relatório / Excluir", 
                "Pesquisar Lançamento", 
                "Sair"
            };

            int escolha = JOptionPane.showOptionDialog(null, "Olá, " + usuarioLogadoNome + "\nO que deseja gerenciar hoje?", "Painel Interno",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, imagem, operacoes, operacoes[0]);

            if (escolha == 5 || escolha == -1) {
                usuarioLogadoId = -1; // Limpa sessão
                break;
            }

            switch (escolha) {
                case 0: // CASO DE USO: Cadastrar Receita
                case 1: // CASO DE USO: Cadastrar Despesa
                    String tabela = (escolha == 0) ? "receitas" : "despesas";
                    String desc = JOptionPane.showInputDialog(null, "Descrição da " + tabela.substring(0, tabela.length()-1) + ":");
                    String valStr = JOptionPane.showInputDialog(null, "Valor (R$):");
                    String dataLanc = JOptionPane.showInputDialog(null, "Data (DD/MM/AAAA):");
                    String cat = (String) JOptionPane.showInputDialog(null, "Categoria:", "Categorias",
                            JOptionPane.QUESTION_MESSAGE, imagem, categorias, categorias[0]);

                    if (desc != null && valStr != null && dataLanc != null && cat != null) {
                        try {
                            double valorNum = Double.parseDouble(valStr.replace(",", "."));
                            String sqlInsert = "INSERT INTO " + tabela + "(descricao, valor, data, categoria, usuario_id) VALUES(?, ?, ?, ?, ?)";
                            try (Connection conn = conectar(); PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                                pstmt.setString(1, desc);
                                pstmt.setDouble(2, valorNum);
                                pstmt.setString(3, dataLanc);
                                pstmt.setString(4, cat);
                                pstmt.setInt(5, usuarioLogadoId);
                                pstmt.executeUpdate();
                                JOptionPane.showMessageDialog(null, "Lançamento efetuado!");
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(null, "Dados incorretos. Lançamento cancelado.");
                        }
                    }
                    break;

                case 2: // CASO DE USO: Consultar Saldo Disponível
                    double saldo = calcularSaldo();
                    JOptionPane.showMessageDialog(null, String.format("Seu Saldo Disponível atual:\nR$ %.2f", saldo), "Saldo Atual", JOptionPane.INFORMATION_MESSAGE, imagem);
                    break;

                case 3: // CASO DE USO: Gerar Relatório Simples + Desenvolvimento de Exclusão
                    gerarRelatorioEExclusao(null);
                    break;

                case 4: // CASO DE USO: Pesquisa de Lançamentos
                    String termo = JOptionPane.showInputDialog("Digite uma palavra-chave para pesquisar nas descrições:");
                    if (termo != null && !termo.trim().isEmpty()) {
                        gerarRelatorioEExclusao(termo);
                    }
                    break;
            }
        }
    }

    // DESENVOLVIMENTO: Consulta de Saldo Dinâmico
    public static double calcularSaldo() {
        double receitas = 0, despesas = 0;
        try (Connection conn = conectar()) {
            PreparedStatement p1 = conn.prepareStatement("SELECT SUM(valor) FROM receitas WHERE usuario_id = ?");
            p1.setInt(1, usuarioLogadoId);
            ResultSet r1 = p1.executeQuery();
            if (r1.next()) receitas = r1.getDouble(1);

            PreparedStatement p2 = conn.prepareStatement("SELECT SUM(valor) FROM despesas WHERE usuario_id = ?");
            p2.setInt(1, usuarioLogadoId);
            ResultSet r2 = p2.executeQuery();
            if (r2.next()) despesas = r2.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return receitas - despesas;
    }

    // DESENVOLVIMENTO: Relatório Simples, Pesquisa e Exclusão de Registros
    public static void gerarRelatorioEExclusao(String termoPesquisa) {
        StringBuilder relatorio = new StringBuilder("=== RELATÓRIO DE LANÇAMENTOS ===\n\nID | TIPO | DESCRIÇÃO | VALOR | DATA | CATEGORIA\n");
        String filtro = (termoPesquisa != null) ? " AND descricao LIKE ?" : "";

        try (Connection conn = conectar()) {
            // Varre Receitas
            PreparedStatement p1 = conn.prepareStatement("SELECT * FROM receitas WHERE usuario_id = ?" + filtro);
            p1.setInt(1, usuarioLogadoId);
            if (termoPesquisa != null) p1.setString(2, "%" + termoPesquisa + "%");
            ResultSet r1 = p1.executeQuery();
            while (r1.next()) {
                relatorio.append(String.format("R-%d | RECEITA | %s | R$ %.2f | %s | %s\n", 
                        r1.getInt("id"), r1.getString("descricao"), r1.getDouble("valor"), r1.getString("data"), r1.getString("categoria")));
            }

            // Varre Despesas
            PreparedStatement p2 = conn.prepareStatement("SELECT * FROM despesas WHERE usuario_id = ?" + filtro);
            p2.setInt(1, usuarioLogadoId);
            if (termoPesquisa != null) p2.setString(2, "%" + termoPesquisa + "%");
            ResultSet r2 = p2.executeQuery();
            while (r2.next()) {
                relatorio.append(String.format("D-%d | DESPESA | %s | R$ %.2f | %s | %s\n", 
                        r2.getInt("id"), r2.getString("descricao"), r2.getDouble("valor"), r2.getString("data"), r2.getString("categoria")));
            }

            relatorio.append(String.format("\nSaldo Disponível Geral: R$ %.2f\n", calcularSaldo()));
            relatorio.append("\nDeseja excluir algum registro? Digite o código correspondente (ex: R-2 ou D-5) ou deixe em branco para fechar:");

            String respostaExclusao = JOptionPane.showInputDialog(null, relatorio.toString(), "Relatório e Exclusão", JOptionPane.INFORMATION_MESSAGE);
            
            // LÓGICA DE EXCLUSÃO
            if (respostaExclusao != null && respostaExclusao.contains("-")) {
                String[] partes = respostaExclusao.toUpperCase().split("-");
                String tipo = partes[0].equals("R") ? "receitas" : "despesas";
                int idRegistro = Integer.parseInt(partes[1]);

                String sqlDelete = "DELETE FROM " + tipo + " WHERE id = ? AND usuario_id = ?";
                try (PreparedStatement pstmtDel = conn.prepareStatement(sqlDelete)) {
                    pstmtDel.setInt(1, idRegistro);
                    pstmtDel.setInt(2, usuarioLogadoId);
                    int linhasAfetadas = pstmtDel.executeUpdate();
                    if (linhasAfetadas > 0) {
                        JOptionPane.showMessageDialog(null, "Registro excluído com sucesso!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Registro não encontrado ou não pertence a você.");
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Operação de exibição/exclusão finalizada.");
        }
    }
} 

