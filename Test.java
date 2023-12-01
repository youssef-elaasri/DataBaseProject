import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Test {
    public static void main(String[] args) throws SQLException {
        Tablesquery tablesquery = new Tablesquery();
        boolean signIn = tablesquery.verifyPassword("meryemelaasri@gmail.com","ProjethBDD123");
        System.out.println(signIn);
        tablesquery.showRefuge(true);
        tablesquery.showRefuge(false);
        tablesquery.closeConnection();
    }
}
/* test for Res Formation
    public static void main(String[] args) throws SQLException {
        Formation Formation1 = new Formation(2023,1);
        Formation Formation2 = new Formation(2023,2);
        Formation Formation3 = new Formation(2023,3);
        new ReservationFormation(3,Formation2);
        new ReservationFormation(3,Formation3);
        new ReservationFormation(4,Formation2);
        new ReservationFormation(4,Formation3); //liste d'attente : 1
        ReservationFormationX Res1 = new ReservationFormationX(2, 3, Formation3);
        Res1.AnnulationResFormation(3,Formation3.getAnnee(),Formation3.getRang());
    }
}*/
