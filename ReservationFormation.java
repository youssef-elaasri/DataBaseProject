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
    private HashMap<Formation,Integer> reservationsFormation;
    private HashMap<Formation,Integer> reservFormationAttente;

    public ReservationFormation(int idUsr, int  anneeFormation, int rangFormation, HashMap<Formation,Integer> reservationsFormation, HashMap<Formation,Integer> reservFormationAttente){
        this.idUsr = idUsr;
        this.annee = anneeFormation;
        this.rang = rangFormation;
        this.reservationsFormation = reservationsFormation;
        this.reservFormationAttente = reservFormationAttente;
        try {
            // Enregistrement du driver Oracle
            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection
            System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            System.out.println("connected");

            if( verifyAdherent(idUsr) && verifyFormation(anneeFormation, rangFormation) ) {
                if (verifyDisponibilite(anneeFormation, rangFormation, reservationsFormation, reservFormationAttente)) {
                    insertQuery(0, anneeFormation, rangFormation, idUsr);
                    //somme due for adhérent ? Update it with calculPrix.
                } else {
                    insertQuery(reservFormationAttente.size(), anneeFormation, rangFormation, idUsr);
                    System.out.println("Pas assez de place désolé. Rang en liste d'attente: " + reservFormationAttente.size());
                }
            }
            conn.close();
            return;
        }   catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
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
        Formation key = new Formation(this.annee, this.rang);
        int currentValue = this.reservationsFormation.getOrDefault(key, 0);

        if (currentValue > 0) {
            this.reservationsFormation.put(key, currentValue - 1);
            this.updateReservations();
        } else {
            System.out.println("L'annulation de cette réservation n'est pas possible.");
        }
        conn.close();
    }

    private void insertQuery(int rangAttente, int anneeFormation, int rangFormation, int idUsr) throws SQLException {
        String prestmnt = "INSERT INTO ReservationFormation(rangAttente, annee," +
                "rang, idUsr) VALUES (?, ?, ?, ?)";
        PreparedStatement stmnt = conn.prepareStatement(prestmnt);
        stmnt.setInt(1, rangAttente);
        stmnt.setInt(2, anneeFormation);
        stmnt.setInt(3, rangFormation);
        stmnt.setInt(4, idUsr);
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
        String formationExistenceStatement = "SELECT COUNT(*) FROM Refuge WHERE annee = ? AND rang = ?";
        PreparedStatement stmnt = conn.prepareStatement(formationExistenceStatement);
        stmnt.setInt(1, anneeFormation);
        stmnt.setInt(2, rangFormation);
        ResultSet result = stmnt.executeQuery();
        if(result.next()) {
            if (result.getInt(1) > 0) {
                LocalDate today = LocalDate.now();
                String preStmntDate = "SELECT dateDemarrage FROM Formation WHERE annee = ? AND rang = ?";
                PreparedStatement stmt = conn.prepareStatement(preStmntDate);
                stmt.setInt(1, anneeFormation);
                stmt.setInt(2, rangFormation);
                ResultSet resultSet = stmt.executeQuery();
                if (result.next()) {
                    LocalDate demarrage = resultSet.getDate(1).toLocalDate();
                    stmnt.close();
                    result.close();
                    stmt.close();
                    resultSet.close();
                    if (today.isBefore(demarrage)) { //TODO no such example
                        return true;
                    }
                }
                stmt.close();
                resultSet.close();
            }
        }
        stmnt.close();
        result.close();
        System.out.println("La formation dont l'annee est " + anneeFormation + " et le rang est " + rangFormation + " n'est pas disponible.");
        return false;
    }

    private Boolean verifyDisponibilite(int anneeFormation, int rangFormation, HashMap<Formation, Integer> reservationsFormation, HashMap<Formation, Integer> reservFormationAttente) throws SQLException {
        String preStmntDate = "SELECT nbPlacesFormation FROM Refuge WHERE annee = ? AND rang = ?";
        PreparedStatement stmt = conn.prepareStatement(preStmntDate);
        stmt.setInt(1, anneeFormation);
        stmt.setInt(2, rangFormation);
        ResultSet result = stmt.executeQuery();
        if(result.next()){
            int nbPlacesFormation = result.getInt(1);
            stmt.close();
            result.close();
            return reservationsFormation.get(new Formation(anneeFormation, rangFormation)) < nbPlacesFormation;
        }
        stmt.close();
        result.close();
        reservFormationAttente.put(new Formation(anneeFormation, rangFormation),1);
        return false;
    }

    private void updateReservations() throws SQLException {
        for (HashMap.Entry<Formation, Integer> entry : this.reservFormationAttente.entrySet()) {
            Formation formation = entry.getKey();
            int valeurRangAttente = entry.getValue();

            valeurRangAttente--;
            this.reservFormationAttente.put(formation, valeurRangAttente);
            String adherentExistenceStatement = "UPDATE Formation SET rangAttente = rangAttente - 1 WHERE annee = ? AND rang = ?";
            PreparedStatement stmt = conn.prepareStatement((adherentExistenceStatement));
            stmt.setInt(1, this.annee);
            stmt.setInt(2, this.rang);
            ResultSet resultSet = stmt.executeQuery();
            if (valeurRangAttente == 0) {
                System.out.println(this.idUsr + " vous êtes passez en liste principale, merci pour votre patience.");
            }
            stmt.close();
            resultSet.close();
        }
    }
}
