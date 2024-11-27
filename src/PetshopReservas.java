import java.sql.*;
import java.util.Scanner;

public class PetshopReservas {

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/petshop_reservas";
        String user = "postgres";
        String password = "postgres";
        return DriverManager.getConnection(url, user, password);
    }

    // Verificar se já existe uma reserva para o mesmo espaço, data e hora
    public static boolean verificarConflitoReserva(Connection conn, int idEspaco, String dataReserva, String horaReserva) throws SQLException {

        java.sql.Date dataReservaSql = java.sql.Date.valueOf(dataReserva);

        java.sql.Time horaReservaSql = java.sql.Time.valueOf(horaReserva);

        String sql = "SELECT * FROM Solicitacoes WHERE id_espaco = ? AND data_reserva = ? AND hora_reserva = ? AND status = 'Aprovada'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idEspaco);
            pstmt.setDate(2, dataReservaSql);
            pstmt.setTime(3, horaReservaSql);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Solicitar reserva de um espaço físico
    public static void fazerSolicitacao(Connection conn, int idUsuario, int idEspaco, String dataReserva, String horaReserva) throws SQLException {
        java.sql.Date dataReservaSql = java.sql.Date.valueOf(dataReserva);
        java.sql.Time horaReservaSql = java.sql.Time.valueOf(horaReserva);

        if (verificarConflitoReserva(conn, idEspaco, dataReserva, horaReserva)) {
            System.out.println("Erro: O espaço já está reservado nesse horário.");
            return;
        }

        String sql = "INSERT INTO Solicitacoes (id_usuario, id_espaco, data_reserva, hora_reserva, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            pstmt.setInt(2, idEspaco);
            pstmt.setDate(3, dataReservaSql); // Passando a data convertida
            pstmt.setTime(4, horaReservaSql); // Passando a hora convertida como java.sql.Time
            pstmt.setString(5, "Pendente");
            pstmt.executeUpdate();
            System.out.println("Solicitação inserida com sucesso!");
        }
    }

    // Aprovar ou rejeitar solicitação
    public static void gerenciarSolicitacao(Connection conn, int idSolicitacao, String status, String justificativa, int idGestor) throws SQLException {
        String sqlUpdate = "UPDATE Solicitacoes SET status = ? WHERE id_solicitacao = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, idSolicitacao);
            pstmt.executeUpdate();
            System.out.println("Solicitação " + status + " com sucesso!");

            String sqlHistorico = "INSERT INTO Historico_Solicitacoes (id_solicitacao, status, justificativa, id_gestor) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtHistorico = conn.prepareStatement(sqlHistorico)) {
                pstmtHistorico.setInt(1, idSolicitacao);
                pstmtHistorico.setString(2, status);
                pstmtHistorico.setString(3, justificativa);
                pstmtHistorico.setInt(4, idGestor);
                pstmtHistorico.executeUpdate();
                System.out.println("Histórico atualizado.");
            }
        }
    }

    // Exibir o histórico de solicitações
    public static void exibirHistoricoSolicitacoes(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Historico_Solicitacoes";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int idHistorico = rs.getInt("id_historico");
                int idSolicitacao = rs.getInt("id_solicitacao");
                String status = rs.getString("status");
                String justificativa = rs.getString("justificativa");
                String dataAvaliacao = rs.getString("data_avaliacao");
                System.out.println("ID Histórico: " + idHistorico + ", ID Solicitação: " + idSolicitacao + ", Status: " + status + ", Justificativa: " + justificativa + ", Data Avaliação: " + dataAvaliacao);
            }
        }
    }

    // Exibir todas as solicitações
    public static void exibirSolicitacoes(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Solicitacoes";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int idSolicitacao = rs.getInt("id_solicitacao");
                int idUsuario = rs.getInt("id_usuario");
                int idEspaco = rs.getInt("id_espaco");
                String dataReserva = rs.getString("data_reserva");
                String horaReserva = rs.getString("hora_reserva");
                String status = rs.getString("status");
                System.out.println("ID Solicitação: " + idSolicitacao + ", ID Usuário: " + idUsuario + ", ID Espaço: " + idEspaco + ", Data Reserva: " + dataReserva + ", Hora Reserva: " + horaReserva + ", Status: " + status);
            }
        }
    }

    // Menu do sistema
    public static void menu() {
        System.out.println("Sistema de Reservas de Espaços Físicos do Petshop");
        System.out.println("1. Solicitar Reserva");
        System.out.println("2. Gerenciar Solicitações");
        System.out.println("3. Exibir Histórico de Solicitações");
        System.out.println("4. Exibir Todas as Solicitações");
        System.out.println("0. Sair");
    }

    // Função principal
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = getConnection()) {
            System.out.println("Conexão com o banco de dados estabelecida!");

            boolean continuar = true;
            while (continuar) {
                menu();
                int opcao = sc.nextInt();
                sc.nextLine();
                switch (opcao) {
                    case 1: {
                        System.out.print("Digite seu ID de Usuário (Cliente): ");
                        int idUsuario = sc.nextInt();
                        System.out.print("Digite o ID do espaço desejado: ");
                        int idEspaco = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Digite a data da reserva (yyyy-mm-dd): ");
                        String dataReserva = sc.nextLine();
                        System.out.print("Digite a hora da reserva (hh:mm:ss): ");
                        String horaReserva = sc.nextLine();
                        fazerSolicitacao(conn, idUsuario, idEspaco, dataReserva, horaReserva);
                        break;
                    }
                    case 2: {
                        System.out.print("Digite o ID da solicitação para gerenciar: ");
                        int idSolicitacao = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Digite o status (Aprovada/Rejeitada): ");
                        String status = sc.nextLine();
                        System.out.print("Digite a justificativa (se rejeitada): ");
                        String justificativa = sc.nextLine();
                        System.out.print("Digite o ID do Gestor: ");
                        int idGestor = sc.nextInt();
                        gerenciarSolicitacao(conn, idSolicitacao, status, justificativa, idGestor);
                        break;
                    }
                    case 3: {
                        exibirHistoricoSolicitacoes(conn);
                        break;
                    }
                    case 4: {
                        exibirSolicitacoes(conn);
                        break;
                    }
                    case 0: {
                        continuar = false;
                        break;
                    }
                    default:
                        System.out.println("Opção inválida!");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
