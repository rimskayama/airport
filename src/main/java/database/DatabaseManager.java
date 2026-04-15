package database;

import entity.Fare;
import entity.Passenger;
import entity.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // путь к файлу базы данных
    private static final String DB_URL = "jdbc:sqlite:airport.db";
    private static Connection connection;

    // соединение с БД
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
            } catch (ClassNotFoundException e) {
                throw new SQLException("Драйвер SQLite не найден", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
            }
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createFaresTable(conn);
            createPassengersTable(conn);
            createTicketsTable(conn);
            System.out.println("База данных успешно инициализирована");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }

    private static void createFaresTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS fares (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                from_location TEXT NOT NULL,
                to_location TEXT NOT NULL,
                price REAL NOT NULL,
                class_choice INTEGER NOT NULL
            )
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void createPassengersTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS passengers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                passport_id TEXT UNIQUE NOT NULL,
                birth_date TEXT NOT NULL
            )
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void createTicketsTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS tickets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                passenger_id INTEGER NOT NULL,
                fare_id INTEGER NOT NULL,
                price REAL NOT NULL,
                purchase_date TEXT NOT NULL,
                ticket_number TEXT UNIQUE NOT NULL,
                FOREIGN KEY (passenger_id) REFERENCES passengers(id) ON DELETE CASCADE,
                FOREIGN KEY (fare_id) REFERENCES fares(id) ON DELETE CASCADE
            )
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С ТАРИФАМИ ============

    public static void saveFare(Fare fare) throws SQLException {
        String sql = "INSERT INTO fares (from_location, to_location, price, class_choice) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, fare.fromLocation());
            pstmt.setString(2, fare.toLocation());
            pstmt.setDouble(3, fare.getPrice());
            pstmt.setInt(4, fare.getClassChoice());
            pstmt.executeUpdate();

            // Получаем сгенерированный ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                fare.setId(rs.getInt(1));
            }
        }
    }

    public static List<Fare> loadAllFares() throws SQLException {
        List<Fare> fares = new ArrayList<>();
        String sql = "SELECT * FROM fares ORDER BY id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Fare fare = new Fare(
                        rs.getString("from_location"),
                        rs.getString("to_location"),
                        rs.getDouble("price"),
                        rs.getInt("class_choice")
                );
                fare.setId(rs.getInt("id"));
                fares.add(fare);
            }
        }
        return fares;
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С ПАССАЖИРАМИ ============

    public static int savePassenger(Passenger passenger) throws SQLException {
        String checkSql = "SELECT id FROM passengers WHERE passport_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, passenger.getPassportId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        String insertSql = "INSERT INTO passengers (name, passport_id, birth_date) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, passenger.getName());
            pstmt.setString(2, passenger.getPassportId());
            pstmt.setString(3, passenger.getBirthDate());
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Не удалось получить ID пассажира");
            }
        }
    }

    public static List<Passenger> loadAllPassengers() throws SQLException {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT DISTINCT id, name, passport_id, birth_date FROM passengers ORDER BY id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Passenger passenger = new Passenger(
                        rs.getString("name"),
                        rs.getString("passport_id"),
                        rs.getString("birth_date")
                );
                passenger.setId(rs.getInt("id"));
                passengers.add(passenger);
            }
        }
        return passengers;
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С БИЛЕТАМИ ============

    public static void saveTicket(Ticket ticket, int passengerId, int fareId) throws SQLException {
        String sql = "INSERT INTO tickets (passenger_id, fare_id, price, purchase_date, ticket_number) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, passengerId);
            pstmt.setInt(2, fareId);
            pstmt.setDouble(3, ticket.price());
            pstmt.setString(4, ticket.getPurchaseDate());
            pstmt.setString(5, ticket.getTicketNumber());
            pstmt.executeUpdate();
        }
    }

    public static List<Ticket> loadAllTickets() throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = """
            SELECT t.ticket_number, t.price, t.purchase_date,
                   p.name, p.passport_id, p.birth_date,
                   f.from_location, f.to_location, f.class_choice
            FROM tickets t
            JOIN passengers p ON t.passenger_id = p.id
            JOIN fares f ON t.fare_id = f.id
            ORDER BY t.purchase_date DESC
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Passenger passenger = new Passenger(
                        rs.getString("name"),
                        rs.getString("passport_id"),
                        rs.getString("birth_date")
                );

                Fare fare = new Fare(
                        rs.getString("from_location"),
                        rs.getString("to_location"),
                        rs.getDouble("price"),
                        rs.getInt("class_choice")
                );

                Ticket ticket = new Ticket(
                        rs.getString("ticket_number"),
                        passenger,
                        fare,
                        rs.getString("purchase_date")
                );
                tickets.add(ticket);
            }
        }
        return tickets;
    }

    public static double calculateTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(price) as total FROM tickets";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }
}