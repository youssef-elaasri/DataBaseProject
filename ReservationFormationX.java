import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class ReservationFormationX {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";

    private Connection conn;
    private int idRes;
    private int annee;
    private int rang;
    private HashMap<Integer,Integer> reservFormationAttente;

    public ReservationFormationX(Connection conn, int idRes, int idUsr){
        this.conn = conn;
        this.idRes = idRes;

        try {
            String resStatement = "SELECT * FROM ReservationFormation WHERE idReservationFormation = ?";
            PreparedStatement resstmnt = conn.prepareStatement(resStatement);
            resstmnt.setInt(1, this.idRes);
            ResultSet resultRes = resstmnt.executeQuery();

            if (resultRes.next()) {
                int annee = resultRes.getInt("annee");
                int rang = resultRes.getInt("rang");
                int rangAttente = resultRes.getInt("rangAttente");
                this.annee = annee;
                this.rang = rang;
                if (verifyAdherent(idUsr)) {
                    this.InitResAttente(annee, rang, rangAttente);
                }
            }
            resstmnt.close();
            resultRes.close();
            return;
        }   catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }

    private void InitResAttente(int annee, int rang, int rangAttente) throws SQLException {
        this.reservFormationAttente = new HashMap<Integer, Integer>();
        String initResStatement = "SELECT * FROM ReservationFormation WHERE annee = ? AND rang = ? AND rangAttente >= ?";
        PreparedStatement initResstmnt = conn.prepareStatement(initResStatement);
        initResstmnt.setInt(1, annee);
        initResstmnt.setInt(2, rang);
        initResstmnt.setInt(3, rangAttente);
        ResultSet result = initResstmnt.executeQuery();

        while (result.next()) {
            int retrievedIdRes = result.getInt("idReservationFormation");
            int retrievedRangAttente = result.getInt("rangAttente");
            this.reservFormationAttente.put(retrievedIdRes, retrievedRangAttente);
        }
        initResstmnt.close();
        result.close();
    }
    private int CalculNbRes() throws SQLException {
        String nbResStatement = "SELECT COUNT(*) FROM ReservationFormation WHERE idReservationFormation = ?";
        PreparedStatement stmnt = conn.prepareStatement(nbResStatement);
        stmnt.setInt(1, this.idRes);
        ResultSet result = stmnt.executeQuery();
        int res = 0;
        if(result.next()) {
            res = result.getInt(1);
        }
        stmnt.close();
        result.close();
        return res;
    }
    public void AnnulationResFormation(int idUsr) throws SQLException {
        int nbRes = this.CalculNbRes();
        if (nbRes > 0) {
            String delStatement = "DELETE FROM Message WHERE idReservationFormation = ?"; //faut supprimer les messages qui concernent cette réservation.
            PreparedStatement delPrep = conn.prepareStatement(delStatement);
            delPrep.setInt(1,this.idRes);
            delPrep.executeQuery();
            String deleteStatement = "DELETE FROM ReservationFormation WHERE idReservationFormation = ?";
            PreparedStatement stmt = conn.prepareStatement(deleteStatement);
            stmt.setInt(1, this.idRes);
            ResultSet resultSet = stmt.executeQuery();
            stmt.close();
            resultSet.close();
            String priceStatement = "UPDATE Utilisateur SET SommeDue = SommeDue - ? WHERE idUSr = ?";
            PreparedStatement stmtPrice = conn.prepareStatement(priceStatement);
            stmtPrice.setInt(1, this.calculPrix(annee,rang));
            stmtPrice.setInt(2, idUsr);
            ResultSet resultSetPrice = stmtPrice.executeQuery();
            stmtPrice.close();
            resultSetPrice.close();
            this.updateReservations(idUsr);
            System.out.println("L'annulation a été bien prise en compte.");
        } else {
            System.out.println("L'annulation de cette réservation n'est pas possible.");
        }
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

    private void updateReservations(int idUsr) throws SQLException {
        Iterator<Map.Entry<Integer, Integer>> iterator = this.reservFormationAttente.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int idResAttente = entry.getKey();
            int valeurRangAttente = entry.getValue();
            valeurRangAttente--;
            this.reservFormationAttente.put(idResAttente, valeurRangAttente);
            String updtStatement = "UPDATE ReservationFormation SET rangAttente = ? WHERE idReservationFormation = ?";
            PreparedStatement stmt = conn.prepareStatement((updtStatement));
            stmt.setInt(1, valeurRangAttente);
            stmt.setInt(2, idResAttente);
            ResultSet resultSet = stmt.executeQuery();
            String message = "idRes = " + idResAttente + " - Vous êtes passé en position : " + valeurRangAttente + " dans la liste d'attente, merci pour votre patience.";
            if (valeurRangAttente == 0) {
                message = "idRes = " + idResAttente + " - Vous êtes passé en liste principale, merci pour votre patience.";
                iterator.remove();
            }
            stmt.close();
            resultSet.close();
            String pregetUsrstmnt = "Select idUsr From ReservationFormation Where idReservationFormation = ?";
            PreparedStatement getUsrstmnt = conn.prepareStatement(pregetUsrstmnt);
            getUsrstmnt.setInt(1, idResAttente);
            ResultSet getUsrResult = getUsrstmnt.executeQuery();
            if (getUsrResult.next()) {
                String preInsertstmnt = "INSERT INTO Message(message, idUsr, idReservationFormation) VALUES (?, ?, ?)";
                PreparedStatement stmntInsert = conn.prepareStatement(preInsertstmnt);
                stmntInsert.setString(1, message);
                stmntInsert.setInt(2, getUsrResult.getInt(1));
                stmntInsert.setInt(3, idResAttente);
                stmntInsert.execute();
                stmntInsert.close();
            }
            getUsrstmnt.close();
            getUsrResult.close();
        }
    }
}
