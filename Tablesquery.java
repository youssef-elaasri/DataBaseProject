import java.sql.*;
import java.util.Objects;

public class Tablesquery {

    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";

    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";

    private final Connection conn;

    public Tablesquery() {
        try {
            // Enregistrement du driver Oracle

            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection

            System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            System.out.println("connected");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    boolean verifyPassword(String email, String password) throws SQLException {
        String pre_stmt = "select pwdusr from utilisateur where emailusr = ?";
        PreparedStatement stmt = conn.prepareStatement(pre_stmt);
        stmt.setString(1, email);
        ResultSet resultSet = stmt.executeQuery();
        if (resultSet.next()) {
            String addedPassword = resultSet.getString(1);
            stmt.close();
            resultSet.close();
            return addedPassword.equals( password);
        }
        else {
            System.out.println("email not found");
            stmt.close();
            resultSet.close();
            return false;
        }
    }

    void showCourses() throws SQLException {
        String pre_stmt = "select * from formation order by datedemarrage, nomformation ";
        PreparedStatement stmt = conn.prepareStatement(pre_stmt);
        ResultSet resultSet = stmt.executeQuery();
        getTableData(resultSet);
        stmt.close();
        resultSet.close();
    }


    void showMateriel(String categorie) throws SQLException {
        String pre_stmt = "select * from lotmateriel where categorie = ?";
        PreparedStatement stmt = conn.prepareStatement(pre_stmt);
        stmt.setString(1,categorie);
        ResultSet resultSet = stmt.executeQuery();
        getTableData(resultSet);
        stmt.close();
        resultSet.close();
        pre_stmt = "select souscategorie from a_comme_sous_categorie where categorie = ?";
        stmt = conn.prepareStatement(pre_stmt);
        resultSet = stmt.executeQuery();
        if (!resultSet.next()) {
            stmt.close();
            resultSet.close();
            return;
        }
        while (resultSet.next()) {
            showMateriel(resultSet.getString(1));
        }
        stmt.close();
        resultSet.close();
    }

    static void getTableData(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsetmd = resultSet.getMetaData();
        int lenghtColum = rsetmd.getColumnCount();
        while (resultSet.next()) {
            for (int j = 1; j <= lenghtColum; j++) {
                System.out.print(resultSet.getString(j) + "\t");
            }
            System.out.println();
        }
    }

}
