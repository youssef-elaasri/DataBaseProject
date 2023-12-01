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

    public ReservationFormation(Connection conn, int idUsr, Formation formation){
        this.conn = conn;
        this.idUsr = idUsr;
        this.annee = formation.getAnnee();
        this.rang = formation.getRang();
        try {
            this.InitResAttente();
            this.InitIdRes();
            if( verifyAdherent(idUsr) && verifyFormation(this.annee, this.rang) ) {
                if (verifyDisponibilite(this.annee, this.rang)) {
                    insertQuery(0, this.annee, this.rang, idUsr);
                } else {
                    this.reservFormationAttente.put(this.idRes,this.reservFormationAttente.size() + 1);
                    insertQuery(reservFormationAttente.size() , this.annee, this.rang, idUsr);
                    System.out.println("Oups, pas assez de place désolé. Rang en liste d'attente: " + reservFormationAttente.size());
                }
            }
            conn.close();
            return;
        }   catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
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
        System.out.println("La réservation a bien été prise en compte. idRes = " + this.idRes);
        stmnt.execute();
        stmnt.close();
        String priceStatement = "UPDATE Utilisateur SET SommeDue = SommeDue + ? WHERE idUSr = ?";
        PreparedStatement stmtPrice = conn.prepareStatement(priceStatement);
        stmtPrice.setInt(1, this.calculPrix(annee,rang));
        stmtPrice.setInt(2, idUsr);
        ResultSet resultSetPrice = stmtPrice.executeQuery();
        stmtPrice.close();
        resultSetPrice.close();
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
            return ret;
        }
        stmt.close();
        result.close();
        return false;
    }

}
