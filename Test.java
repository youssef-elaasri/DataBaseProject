import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Test {
    public static void main(String[] args) throws SQLException {
        Tablesquery tablesquery = new Tablesquery();
        boolean signIn = tablesquery.verifyPassword("meryemelaasri@gmail.com","ProjethBDD123");
        System.out.println(signIn);
        tablesquery.showCourses();
        tablesquery.closeConnection();
    }
}


