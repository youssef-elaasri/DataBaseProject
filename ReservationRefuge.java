import java.sql.*;
import java.time.LocalDate;

public class ReservationRefuge {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";

    public ReservationRefuge(String idUsr,String  emailRefuge, int nuitsReserves, String ... repas  ){
        try {
            // Enregistrement du driver Oracle
            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection
            System.out.print("Connecting to the database... ");
            Connection conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            System.out.println("connected");

            if(!verifyIdUsr()|| !verifyRefuge() || !verifyNbrNuitsRepas() || !verifyRepas()){
                conn.close();
                return;
            }


        }   catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }

    private Boolean verifyIdUsr(){
        return false;
    }
    private Boolean verifyRefuge(){
        return false;
    }
    private Boolean verifyNbrNuitsRepas(){
        return false;
    }
    private Boolean verifyRepas(){
        return false;
    }
    public static void main(String[] args) {
        new ReservationRefuge("id", "mel", 5, "cheesecake");
    }



}
