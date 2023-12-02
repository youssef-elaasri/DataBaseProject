import java.awt.desktop.SystemSleepEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.time.format.DateTimeFormatter;


public class ReservationRefuge {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";

    private Connection conn;

    private Set<String> Repas = new HashSet<>(Set.of("dejeuner", "diner", "souper", "casse-croute"));

    public ReservationRefuge(int idUsr,String  emailRefuge, int nuitsReserves, String date, String ... repas ){
        try {
            // Enregistrement du driver Oracle
            //System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            //System.out.println("loaded");

            // Etablissement de la connection
            //System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            //System.out.println("connected");
            int nbrRepas = repas.length;
            System.out.println("1");
            if(!verifyIdUsr(idUsr)){
                System.out.println("L'identifiant " + idUsr + " n'existe pas");
                return;
            }
            System.out.println("2");
            if(!verifyRefuge(emailRefuge)){
                System.out.println("Le refuge dont l'email est " + emailRefuge + " n'existe pas");
                return;
            }
            System.out.println("3");
            if(!verifyDate(emailRefuge, date, nuitsReserves)){
                System.err.println("Le refuge sera fermé à cette date :(");
                return;
            }
            System.out.println("4");
            if(!verifyNbrNuitsRepas(emailRefuge, nuitsReserves, nbrRepas)){
                return;
            }
            System.out.println("5");
            if(!verifyRepas(repas)){
                System.out.println("Un repas n'est pas valide!");
                return;
            }
            System.out.println("6");
            int prix = calculPrix(emailRefuge, nuitsReserves,  repas);
            insertQuery(nuitsReserves, nbrRepas, prix, emailRefuge, idUsr, date);

            conn.close();
            return;


        }   catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }

    public ReservationRefuge(int iDRefuge){
        try {
            // Registering the Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

            // Establishing the connection
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);

            if(verifyIdRefuge(iDRefuge)){
                String sectionGeo = deleteQuery(iDRefuge);
                suggestRefuge(sectionGeo);
            }
        } catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }

    }


    private Boolean verifyIdRefuge(int iDRefuge) throws SQLException {

        String refugeExistenceStatement = "SELECT COUNT(*) FROM RESERVATIONREFUGE WHERE IDRESREFUGE = ?";
        PreparedStatement stmt = conn.prepareStatement((refugeExistenceStatement));
        stmt.setInt(1, iDRefuge);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            if (result.getInt(1) > 0) {
                stmt.close();
                result.close();
                return true;
            }
            System.out.println("cet ID " + iDRefuge + " N'existe pas");
        }

        stmt.close();
        result.close();

        return false;
    }


    private String deleteQuery(int iDRefuge) throws SQLException {
        try {
            String sectionGeoExistenceStatement = "SELECT secteurGeo from (RESERVATIONREFUGE join Refuge R on R.email = RESERVATIONREFUGE.email) where ReservationRefuge.idResRefuge = ?";
            PreparedStatement stmt = conn.prepareStatement((sectionGeoExistenceStatement));
            stmt.setInt(1, iDRefuge);
            ResultSet result = stmt.executeQuery();
            //stmt.close();
            if (result.next()) {
                String sectionGeo = result.getString(1);
                String prestmnt = "DELETE FROM RESERVATIONREFUGE where idResRefuge = ?";
                PreparedStatement stmt2 = conn.prepareStatement(prestmnt);
                stmt2.setInt(1, iDRefuge);
                stmt2.execute();
                stmt2.close();

                return sectionGeo;
            }
            System.err.println("failed");
            return "ERROR";
        }catch(SQLException e){
            System.err.println("failed");
            return "ERROR";
        }
    }

    private void suggestRefuge(String sectionGeo) throws SQLException {
        if(sectionGeo.equals("ERROR")){
            System.out.println("there is an error somewhere");
        }
        System.out.println(sectionGeo);
        String prestmt = "SELECT nomRefuge FROM Refuge where secteurGeo = ?";
        PreparedStatement stmt = conn.prepareStatement(prestmt);
        stmt.setString(1,sectionGeo);
        ResultSet result = stmt.executeQuery();
        System.out.print("Tu peux aussi aller aux refuges suivants: ");
        while(result.next()){

            System.out.print(result.getString(1) + " ");
        }
        System.out.print("\n");
    }


    private void insertQuery(int nuitsReserves, int nbrRepas, int prix, String email, int idUsr, String date) throws SQLException {
        try {
            conn.setAutoCommit(false);
            if (prix == -1) {
                return;
            }

            // Get the current time
            LocalTime currentTime = LocalTime.now();

            // Get the current hour
            int currentHour = currentTime.getHour();

            // Format the date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.parse(date, formatter));

            // Prepare the SQL statement for inserting the reservation
            String prestmnt = "INSERT INTO ReservationRefuge (dateResRefuge, heureResRefuge," +
                    "nbNuitResRefuge, nbRepasResRefuge, prixResRefuge, email, idUsr) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmnt = conn.prepareStatement(prestmnt);
            stmnt.setDate(1, sqlDate);
            stmnt.setInt(2, currentHour);
            stmnt.setInt(3, nuitsReserves);
            stmnt.setInt(4, nbrRepas);
            stmnt.setInt(5, prix);
            stmnt.setString(6, email);
            stmnt.setInt(7, idUsr);

            // Execute the insertion query
            stmnt.execute();

            prestmnt = "UPDATE Utilisateur SET SommeDue = SommeDue + ? WHERE idUSr = ?";
            stmnt = conn.prepareStatement(prestmnt);
            stmnt.setInt(1, prix);
            stmnt.setInt(2, idUsr);

            stmnt.execute();

        } catch (SQLException e) {
            System.out.println("Failed");
            conn.rollback();
        } finally {
            conn.setAutoCommit(true);
        }
    }
    private int calculPrix(String  emailRefuge, int nuitsReserves, String ... repas) throws SQLException {
        int prixReservationNuits = 0;
        int prixReservationRepas = 0;

        if(nuitsReserves > 0) {
            String prestmnt = "SELECT prixNuitee FROM Refuge WHERE email = ?";
            PreparedStatement stmnt = conn.prepareStatement(prestmnt);
            stmnt.setString(1, emailRefuge);
            ResultSet resultSet = stmnt.executeQuery();
            if (resultSet.next()) {
                prixReservationNuits = nuitsReserves * resultSet.getInt(1);
                stmnt.close();
                resultSet.close();
            }

            for(String leRepas: repas){
                String prestmntRepas = "SELECT prix FROM Propose WHERE email = ? AND repas = ?";
                PreparedStatement stmntRepas = conn.prepareStatement(prestmntRepas);
                stmntRepas.setString(1, emailRefuge);
                stmntRepas.setString(2, leRepas);

                ResultSet resultSetRepas = stmntRepas.executeQuery();
                if (resultSetRepas.next()) {
                    //System.out.println(resultSetRepas.getInt(1));

                    prixReservationRepas += resultSetRepas.getInt(1);
                    stmntRepas.close();
                    resultSetRepas.close();
                } else {

                    System.out.println("Le repas " + leRepas + " n'est pas disponible");
                    return -1;
                }
            }
        }

        return prixReservationNuits + prixReservationRepas;

    }

    private Boolean verifyIdUsr(int idUsr) throws SQLException {
        String userExistenceStatement = "SELECT COUNT(*) FROM CompteUtilisateur WHERE idUsr = ?";
        PreparedStatement stmt = conn.prepareStatement((userExistenceStatement));
        stmt.setInt(1, idUsr);
        ResultSet result = stmt.executeQuery();
        if(result.next()){
            if(result.getInt(1) > 0) {
                stmt.close();
                result.close();
                return true;
            }
        }

        stmt.close();
        result.close();

        return false;
    }
    private Boolean verifyRefuge(String  emailRefuge) throws SQLException {
        String refugeExistenceStatement = "SELECT COUNT(*) FROM Refuge WHERE email = ?";
        PreparedStatement stmt = conn.prepareStatement((refugeExistenceStatement));
        stmt.setString(1, emailRefuge);
        ResultSet result = stmt.executeQuery();
        if(result.next()){
            if(result.getInt(1) > 0) {
                stmt.close();
                result.close();
                return true;
            }
        }
        return false;
    }
    private Boolean verifyNbrNuitsRepas(String emailRefuge,int nuitsReserves, int repasReserves) throws SQLException {
        if( nuitsReserves < 0 || nuitsReserves + repasReserves == 0 ) {
            System.out.println("Il faut reserver une nuit ou/et un repas!");
            return false;
        }


        String preStatementNuitsRepasReserve = "SELECT SUM(nbNuitResRefuge), SUM(nbRepasResRefuge) FROM ReservationRefuge WHERE email = ? ";
        PreparedStatement statementNuitsRepasReserve = conn.prepareStatement((preStatementNuitsRepasReserve));
        statementNuitsRepasReserve.setString(1, emailRefuge);
        ResultSet resultNuitsRepasReserve = statementNuitsRepasReserve.executeQuery();
        if(resultNuitsRepasReserve.next()){
            int nuitsDejaReserve = resultNuitsRepasReserve.getInt(1);
            int repasDejaReserve = resultNuitsRepasReserve.getInt(2);

            statementNuitsRepasReserve.close();
            resultNuitsRepasReserve.close();

            String refugeExistenceStatement = "SELECT nbPlacesDormir, nbPlacesRepas FROM Refuge WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement((refugeExistenceStatement));
            stmt.setString(1, emailRefuge);
            ResultSet result = stmt.executeQuery();
            if(result.next()){
                int nuitsDispo = result.getInt(1);
                int repasDispo = result.getInt(2);
                stmt.close();
                result.close();
                //System.out.println("J'ai " + nuitsDejaReserve + " nuits deja rederve et tu veut reserver " + nuitsReserves + " nuits, sachant qu'il y a " + nuitsDispo +" de nuit dispo");
                if((nuitsReserves + nuitsDejaReserve > nuitsDispo)){
                    System.out.println("Il ne reste que " + (nuitsDispo - nuitsDejaReserve) + " places dans le refuge" );
                    return false;
                }
                if((repasReserves + repasDejaReserve > repasDispo)){
                    System.out.println("Il ne reste que " + (repasDispo - repasDejaReserve) + " repas dans le refuge" );
                    return false;
                }
                return true;
            }

        }

        return false;
    }

    private Boolean verifyDate(String  emailRefuge, String date, int nuits) throws SQLException {
        //LocalDate datee = date.toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate localDate = LocalDate.parse(date, formatter);

        String preStmntDate = "SELECT dateOuverture, dateFermeture FROM Refuge WHERE email = ?";
        PreparedStatement stmntDate = conn.prepareStatement(preStmntDate);
        stmntDate.setString(1, emailRefuge);


        ResultSet result = stmntDate.executeQuery();
        if(result.next()){

            LocalDate ouverture = result.getDate(1).toLocalDate();
            LocalDate fermeture = result.getDate(2).toLocalDate();
            if(fermeture.isBefore(ouverture)) fermeture = fermeture.plusYears(1);
            stmntDate.close();
            result.close();
            return localDate.isAfter(ouverture) && localDate.plusDays(nuits).isBefore(fermeture); //TODO no such example
        }

        return false;
    }
    private Boolean verifyRepas(String ... repas){
        for (String unRepas : repas){
            if(!Repas.contains(unRepas)){
                return false;
            }
        }
        return true;
    }
}
