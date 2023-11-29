import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class LocationMateriel {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";

    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";

    private Connection conn;

    public LocationMateriel(int idAdherent, 
                            HashMap<Lot,Integer> nbPiecesReservees,
                            java.sql.Date dateRecuperation,
                            java.sql.Date dateRetour,
                            int sommeRemboursee){
        try {
            // Enregistrement du driver Oracle

            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection

            System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            System.out.println("connected");

            if (
                verifIdAdherent(idAdherent) &&
                verifDateRecup(dateRecuperation) &&
                verifDateRetour(dateRecuperation,dateRetour) &&
                verifdisponibilite(dateRecuperation, dateRetour,nbPiecesReservees)
            ){
                System.out.println("DKHELTI????????");
                insertQuery(idAdherent,nbPiecesReservees,dateRetour,dateRecuperation,sommeRemboursee);
            }

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
    public void insertQuery(int idAdherent,
                            HashMap<Lot,Integer> nbPiecesReservees,
                            java.sql.Date dateRecuperation,
                            java.sql.Date dateRetour ,
                            int sommeRemboursee) throws SQLException {

        try{
            String prestmnt = "INSERT INTO LocationMateriel(dateRecup, dateRetour," +
                    "SommeDue, SommeRemboursee, idUsr) " +
                    "VALUES (?, ?, ?, ?, ?)";
            conn.setAutoCommit(false);
            PreparedStatement stmnt = conn.prepareStatement(prestmnt);
            stmnt.setDate(1, dateRecuperation);
            stmnt.setDate(2, dateRetour);
            stmnt.setInt(3, 0);
            stmnt.setInt(4, sommeRemboursee);
            stmnt.setInt(5, idAdherent);
            stmnt.execute();
            stmnt.close();



            prestmnt = "SELECT MAX(idLocationMateriel) FROM LocationMateriel ";
            PreparedStatement stmt = conn.prepareStatement(prestmnt);
            ResultSet res = stmt.executeQuery();
            res.next();
            int id = res.getInt(1);

            for (Lot lot: nbPiecesReservees.keySet()) {
                prestmnt = "INSERT INTO ReservationPieces(nbPiecesReservees,nbPiecesCasseesPerdues," +
                        "marque,modele,annee,IdLocationMateriel) " +
                        "VALUES (?, ?, ?, ?, ?,?)";
                conn.setAutoCommit(false);
                PreparedStatement stmnt2 = conn.prepareStatement(prestmnt);
                stmnt2.setInt(1, nbPiecesReservees.get(lot));
                stmnt2.setInt(2, 0);
                stmnt2.setString(3, lot.marque);
                stmnt2.setString(4, lot.modele);
                stmnt2.setInt(5, lot.annee);
                stmnt2.setInt(6, id);
                stmnt2.execute();
                stmnt2.close();
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("An error occurred while executing the SQL query, please try again later");
            } catch (SQLException ex) {
                System.err.println("An error occurred while executing the SQL query");
            }
        }finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("An error occurred while executing the SQL query");
            }

    }




    private boolean verifIdAdherent(int idAdherent) throws SQLException {
        String adherentExistenceStatement = "SELECT * FROM Adherent WHERE idUsr = ?";
        PreparedStatement stmt = conn.prepareStatement((adherentExistenceStatement));
        stmt.setInt(1, idAdherent);
        ResultSet result = stmt.executeQuery();
        if(result.next()){
            stmt.close();
            result.close();
            return true;
        }
        System.out.println("L'identifiant " + idAdherent + " n'existe pas");
        stmt.close();
        result.close();
        return false;
    }

    private boolean verifDateRecup(java.sql.Date dateRecup) {
        LocalDate today = LocalDate.now();
        LocalDate dateRecupLocal = dateRecup.toLocalDate();
        
        return dateRecupLocal.isAfter(today);
    }

    private boolean verifDateRetour(java.sql.Date dateRecup, java.sql.Date dateRetour) {
        LocalDate localDateRecup = dateRecup.toLocalDate();
        LocalDate localDateRetour = dateRetour.toLocalDate();


        if (!localDateRetour.isAfter(localDateRecup)) {
            return false;
        }

        long difference = ChronoUnit.DAYS.between(localDateRecup, localDateRetour);
        return difference <= 14;
    }


    private boolean verifdisponibilite(java.sql.Date dateRecup, java.sql.Date dateRetour,HashMap<Lot,Integer> nbPiecesReservees) throws SQLException {
        String prestmnt = "SELECT lm.modele,lm.marque,lm.annee,(lm.nbPieces - rp.nbPiecesReservees - rp.nbPiecesCasseesPerdues) " +
                "FROM ReservationPieces rp " +
                "JOIN  LotMateriel lm " +
                "ON rp.marque = lm.marque and rp.modele = lm.modele and rp.annee = lm.annee " +
                "JOIN LocationMateriel locm " +
                "ON rp.idLocationMateriel = locm.idLocationMateriel " +
                "WHERE locm.dateRetour >= ? and locm.dateRecup <= ? " +
                "UNION " +
                "SELECT lm.modele,lm.marque,lm.annee,lm.nbPieces " +
                "FROM LotMateriel lm " +
                "WHERE (lm.modele, lm.marque, lm.annee) NOT IN ( SELECT rp.modele, rp.marque, rp.annee FROM ReservationPieces rp)";
        PreparedStatement stmt = conn.prepareStatement(prestmnt);
        stmt.setDate(1, dateRecup);
        stmt.setDate(2, dateRetour);
        ResultSet resultSet = stmt.executeQuery();
        ResultSetMetaData rsetmd = resultSet.getMetaData();
        while (resultSet.next()){
            String modele = resultSet.getString(1);
            String marque = resultSet.getString(2);
            int annee = resultSet.getInt(3);
            int dispo = resultSet.getInt(4);
            Lot lot = new Lot(modele,marque,annee);
            if (nbPiecesReservees.containsKey(lot)){
                if (nbPiecesReservees.get(lot) > dispo) {
                    System.out.println("Réservation refusée, pas assez de pièces disponibles.");
                    stmt.close();
                    resultSet.close();
                    return false;
                }
            }
        }
        stmt.close();
        resultSet.close();
        return true;
    }


    public static void main(String[] args) {

        java.sql.Date dateRecup = java.sql.Date.valueOf("2024-01-01");
        java.sql.Date dateRetour = java.sql.Date.valueOf("2024-01-15");
        HashMap<Lot, Integer>piecesRes = new HashMap<Lot, Integer>();
        Lot lot1 = new Lot("modele 2", "C", 2019);
        piecesRes.put(lot1, 100);
        Lot lot2 = new Lot("modele 1", "D", 2021);
        piecesRes.put(lot2, 6);
        Lot lot3 = new Lot("modele 2", "D", 2022);
        piecesRes.put(lot3, 7);
        new LocationMateriel( 1, piecesRes, dateRecup, dateRetour, 20);
    }
}
