import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class ReservationRefInterf {
    private Connection conn;
    private Set<String> Repas = new HashSet<>(Set.of("dejeuner", "diner", "souper", "casse-croute"));

    public ReservationRefInterf (Connection conn, int idUsr,String  emailRefuge, int nuitsReserves, String ... repas  ){
        try {
            this.conn = conn;
            int nbrRepas = repas.length;

            if(     verifyIdUsr(idUsr) &&
                    verifyRefuge(emailRefuge) &&
                    verifyDate(emailRefuge) &&
                    verifyNbrNuitsRepas(emailRefuge, nuitsReserves, nbrRepas) &&
                    verifyRepas(repas) ) {

                int prix = calculPrix(emailRefuge, nuitsReserves,  repas);


                insertQuery(nuitsReserves, nbrRepas, prix, emailRefuge, idUsr);

            }

            return;




        }   catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }

    private void insertQuery(int nuitsReserves, int nbrRepas, int prix, String email, int idUsr) throws SQLException {
        if (prix == -1){
            return ;
        }

        java.sql.Date today = java.sql.Date.valueOf(LocalDate.now()) ;
        // Get the current time
        LocalTime currentTime = LocalTime.now();

        // Get the current hour
        int currentHour = currentTime.getHour();

        String prestmnt = "INSERT INTO ReservationRefuge (dateResRefuge, heureResRefuge," +
                "nbNuitResRefuge, nbRepasResRefuge, prixResRefuge, email, idUsr) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmnt = conn.prepareStatement(prestmnt);
        stmnt.setDate(1, today);
        stmnt.setInt(2, currentHour);
        stmnt.setInt(3, nuitsReserves);
        stmnt.setInt(4, nbrRepas);
        stmnt.setInt(5, prix);
        stmnt.setString(6, email);
        stmnt.setInt(7, idUsr);

        stmnt.execute();
        System.out.println("Votre réservation a bien été prise en compte. :)");




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
                    System.out.println(resultSetRepas.getInt(1));

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
            System.out.println("L'identifiant " + idUsr + " n'existe pas");
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
            System.out.println("Le refuge dont l'email est " + emailRefuge + " n'existe pas");

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

    private Boolean verifyDate(String  emailRefuge) throws SQLException {
        LocalDate today = LocalDate.now();

        String preStmntDate = "SELECT dateOuverture, dateFermeture FROM Refuge WHERE email = ?";
        PreparedStatement stmntDate = conn.prepareStatement(preStmntDate);
        stmntDate.setString(1, emailRefuge);

        ResultSet result = stmntDate.executeQuery();
        if(result.next()){

            LocalDate ouverture = result.getDate(1).toLocalDate();
            LocalDate fermeture = result.getDate(2).toLocalDate();
            stmntDate.close();
            result.close();
            return today.isAfter(ouverture) && today.isBefore(fermeture); //TODO no such example
        }

        return false;
    }
    private Boolean verifyRepas(String ... repas){
        for (String unRepas : repas){
            if(!Repas.contains(unRepas)){
                System.out.println(unRepas + " n'est pas un repas valide");
                return false;
            }
        }
        return true;
    }
}
