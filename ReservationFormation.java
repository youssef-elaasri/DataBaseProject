import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;

public class ReservationFormation {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";

    private Connection conn;

    private int idUsr;
    private int annee;
    private int rang;
    private int idRes;
    private HashMap<Integer,Integer> reservFormationAttente;

    public ReservationFormation(int idUsr, Formation formation){
        this.idUsr = idUsr;
        this.annee = formation.getAnnee();
        this.rang = formation.getRang();
        try {
            // Enregistrement du driver Oracle
            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection
            System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            System.out.println("connected");
            this.InitResAttente();
            this.InitIdRes();
            if( verifyAdherent(idUsr) && verifyFormation(this.annee, this.rang) ) {
                if (verifyDisponibilite(this.annee, this.rang)) {
                    insertQuery(0, this.annee, this.rang, idUsr);
                    //somme due for adhérent ? Update it with calculPrix.
                } else {
                    for (HashMap.Entry<Integer, Integer> entry : this.reservFormationAttente.entrySet()) {
                        int valeurRangAttente = entry.getValue();
                        System.out.println("valeurRangAttente :" + valeurRangAttente);
                    }
                    System.out.println("we insert " + this.annee + "--" + this.rang);
                    this.reservFormationAttente.put(this.idRes,this.reservFormationAttente.size() + 1);
                    for (HashMap.Entry<Integer, Integer> entry : this.reservFormationAttente.entrySet()) {
                        int valeurRangAttente = entry.getValue();
                        System.out.println("valeurRangAttente :" + valeurRangAttente);
                    }
                    insertQuery(reservFormationAttente.size() , this.annee, this.rang, idUsr);
                    System.out.println("Pas assez de place désolé. Rang en liste d'attente: " + reservFormationAttente.size());
                    //when he is off the 'liste d'attente' update the sommedue also.
                }
            }
            conn.close();
            return;
        }   catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }
    private int getIdRes(){
        return this.idRes;
    }
    private int CalculNbRes() throws SQLException {
        String nbResStatement = "SELECT COUNT(*) FROM ReservationFormation WHERE annee = ? AND rang = ? AND rangAttente = 0";
        PreparedStatement stmnt = conn.prepareStatement(nbResStatement);
        stmnt.setInt(1, this.annee);
        stmnt.setInt(2, this.rang);
        ResultSet result = stmnt.executeQuery();
        int res = 0;
        if(result.next()) {
            res = result.getInt(1);
        }
        stmnt.close();
        result.close();
        System.out.println("NbRes : " + res);
        return res;
    }

    private void InitIdRes() throws SQLException {
        String maxIdStatement = "SELECT MAX(idReservationFormation) FROM ReservationFormation";
        PreparedStatement stmnt = conn.prepareStatement(maxIdStatement);
        ResultSet result = stmnt.executeQuery();

        int maxId = 0;

        if (result.next()) {
            maxId = result.getInt(1);
        }

        System.out.println("Max idReservationFormation: " + maxId);

        stmnt.close();
        result.close();
        this.idRes = maxId + 1;
    }

    private void InitResAttente() throws SQLException {
        this.reservFormationAttente = new HashMap<Integer, Integer>();
        String nbResStatement = "SELECT * FROM ReservationFormation WHERE annee = ? AND rang = ? AND rangAttente >= 1";
        PreparedStatement stmnt = conn.prepareStatement(nbResStatement);
        stmnt.setInt(1, this.annee);
        stmnt.setInt(2, this.rang);
        ResultSet result = stmnt.executeQuery();

        while (result.next()) {
            int retrievedIdRes = result.getInt("idReservationFormation");
            int retrievedRangAttente = result.getInt("rangAttente");

            this.reservFormationAttente.put(retrievedIdRes, retrievedRangAttente);
        }
        stmnt.close();
        result.close();
        System.out.println("En Attente = " + this.reservFormationAttente.size());
    }
    private int CalculNbRes(int idUsr) throws SQLException {
        String nbResStatement = "SELECT COUNT(*) FROM ReservationFormation WHERE annee = ? AND rang = ? AND rangAttente = 0 AND idUsr = ?";
        PreparedStatement stmnt = conn.prepareStatement(nbResStatement);
        stmnt.setInt(1, this.annee);
        stmnt.setInt(2, this.rang);
        stmnt.setInt(3, idUsr);
        ResultSet result = stmnt.executeQuery();
        int res = 0;
        if(result.next()) {
            res = result.getInt(1);
        }
        stmnt.close();
        result.close();
        System.out.println(res);
        return res;
    }
    public void AnnulationResFormation() throws SQLException {
        // Enregistrement du driver Oracle
        System.out.print("Loading Oracle driver... ");
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        System.out.println("loaded");

        // Etablissement de la connection
        System.out.print("Connecting to the database... ");
        conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
        System.out.println("connected");
        InitResAttente();
        int nbRes = this.CalculNbRes(this.idUsr);
        if (nbRes > 0) {
            String deleteStatement = "DELETE FROM ReservationFormation WHERE idReservationFormation = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteStatement);
            stmt.setInt(1, this.idRes);
            ResultSet resultSet = stmt.executeQuery();
            stmt.close();
            resultSet.close();
            this.updateReservations();
        } else {
            System.out.println("L'annulation de cette réservation n'est pas possible.");
        }
        conn.close();
    }

    private void insertQuery(int rangAttente, int anneeFormation, int rangFormation, int idUsr) throws SQLException {
        String prestmnt = "INSERT INTO ReservationFormation(idReservationFormation, rangAttente, annee," +
                "rang, idUsr) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmnt = conn.prepareStatement(prestmnt);
        stmnt.setInt(1, this.idRes);
        stmnt.setInt(2, rangAttente);
        stmnt.setInt(3, anneeFormation);
        stmnt.setInt(4, rangFormation);
        stmnt.setInt(5, idUsr);
        System.out.println("before inserting : idRes = " + this.idRes);
        stmnt.execute();
    }
    private int calculPrix(int anneeFormation, int rangFormation) throws SQLException {
        int prixReservation = 0;

        String prestmnt = "SELECT prixFormation FROM Formation WHERE annee = ? AND rang = ?";
        PreparedStatement stmnt = conn.prepareStatement(prestmnt);
        stmnt.setInt(1, anneeFormation);
        stmnt.setInt(2, rangFormation);
        ResultSet resultSet = stmnt.executeQuery();
        if (resultSet.next()) {
            prixReservation = resultSet.getInt(1);
            stmnt.close();
            resultSet.close();
        }
        return prixReservation;
    }

    private boolean verifyAdherent(int idUsr) throws SQLException {
        String adherentExistenceStatement = "SELECT * FROM Adherent WHERE idUsr = ?";
        PreparedStatement stmt = conn.prepareStatement((adherentExistenceStatement));
        stmt.setInt(1, idUsr);
        ResultSet resultSet = stmt.executeQuery();
        if(resultSet.next()){
            stmt.close();
            resultSet.close();
            return true;
        }
        System.out.println("Vous n'êtes pas un adhérent.");
        stmt.close();
        resultSet.close();
        return false;
    }
    private Boolean verifyFormation(int anneeFormation, int rangFormation) throws SQLException {
        String formationExistenceStatement = "SELECT COUNT(*) FROM Formation WHERE annee = ? AND rang = ?";
        PreparedStatement stmnt = conn.prepareStatement(formationExistenceStatement);
        stmnt.setInt(1, anneeFormation);
        stmnt.setInt(2, rangFormation);
        ResultSet result = stmnt.executeQuery();
        if(result.next()) {
            if (result.getInt(1) > 0) {
                stmnt.close();
                result.close();
                return true;
            }
        }
        stmnt.close();
        result.close();
        System.out.println("La formation dont l'annee est " + anneeFormation + " et le rang est " + rangFormation + " n'est pas disponible.");
        return false;
    }

    private Boolean verifyDisponibilite(int anneeFormation, int rangFormation) throws SQLException {
        String preStmntDate = "SELECT nbPlacesFormation FROM Formation WHERE annee = ? AND rang = ?";
        PreparedStatement stmt = conn.prepareStatement(preStmntDate);
        stmt.setInt(1, anneeFormation);
        stmt.setInt(2, rangFormation);
        ResultSet result = stmt.executeQuery();
        int nbRes = this.CalculNbRes();
        if(result.next()){
            int nbPlacesFormation = result.getInt(1);
            stmt.close();
            result.close();
            boolean ret = nbRes < nbPlacesFormation;
            System.out.println(nbRes + " // " + nbPlacesFormation);
            System.out.println(ret);
            return ret;
        }
        stmt.close();
        result.close();
        return false;
    }

private void updateReservations() throws SQLException {
        System.out.println("Updating " + this.reservFormationAttente.size());
        for (HashMap.Entry<Integer, Integer> entry : this.reservFormationAttente.entrySet()) {
            String deleteStatement = "SELECT * FROM ReservationFormation WHERE idReservationFormation = ?";
            PreparedStatement stmtAttente = conn.prepareStatement(deleteStatement);
            stmtAttente.setInt(1, this.idRes);
            ResultSet resultSetAttente = stmtAttente.executeQuery();
            int anneeAttente = resultSetAttente.getInt("aneee");
            int rangAttente = resultSetAttente.getInt("rang");
            stmtAttente.close();
            resultSetAttente.close();
            if( anneeAttente == this .annee && rangAttente == this.rang) {
                int idRes = entry.getKey();
                int valeurRangAttente = entry.getValue();
                System.out.println("RangAttente = " + valeurRangAttente);
                valeurRangAttente--;
                this.reservFormationAttente.put(idRes, valeurRangAttente);
                String updtStatement = "UPDATE ReservationFormation SET rangAttente = rangAttente - 1 WHERE annee = ? AND rang = ?";
                PreparedStatement stmt = conn.prepareStatement((updtStatement));
                stmt.setInt(1, this.annee);
                stmt.setInt(2, this.rang);
                ResultSet resultSet = stmt.executeQuery();
                if (valeurRangAttente == 0) {
                    System.out.println("idUsr = " + this.idUsr + " vous êtes passez en liste principale, merci pour votre patience.");
                    this.reservFormationAttente.remove(idRes);
                }
                stmt.close();
                resultSet.close();
            }
        }
    }
}
