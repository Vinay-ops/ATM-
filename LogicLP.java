import java.sql.*;

public class LogicLP {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/vinay";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vinay";

    // Method to check PIN
    public static boolean checkPin(int pin) {
        boolean ok = false;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM account WHERE pin = ?")) {

            stmt.setInt(1, pin);
            try (ResultSet rs = stmt.executeQuery()) {
                ok = rs.next();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return ok;
    }


}
