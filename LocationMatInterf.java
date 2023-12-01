import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;


public class LocationMatInterf {
    private Connection conn;


    public LocationMatInterf(Connection conn,
                            int idAdherent,
                            HashMap<Lot, Integer> nbPiecesReservees,
                            String dateRecuperation,
                            String dateRetour) {
        this.conn = conn;
        try {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            java.sql.Date dateRecuperation_ = java.sql.Date.valueOf(LocalDate.parse(dateRecuperation, formatter)) ;
            java.sql.Date dateRetour_ = java.sql.Date.valueOf(LocalDate.parse(dateRetour, formatter)) ;

            insertQuery(idAdherent, nbPiecesReservees, dateRecuperation_, dateRetour_);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void insertQuery(int idAdherent,
                            HashMap<Lot, Integer> nbPiecesReservees,
                            java.sql.Date dateRecuperation,
                            java.sql.Date dateRetour) throws SQLException {

        try {
            if (!verifIdAdherent(idAdherent) ) {
                System.out.println("Id Adhérent invalide!");
                System.out.println("L'identifiant " + idAdherent + " n'existe pas");
                return;
            }
            if (!verifDateRecup(dateRecuperation)) {
                System.out.println("Date de récupération invalide!");
                return;
            }
            if (!verifDateRetour(dateRecuperation, dateRetour)) {
                System.out.println("La durée de location ne doit pas dépasser 14 jours!");
                return;
            }
            if (!verifdisponibilite(dateRecuperation, dateRetour, nbPiecesReservees)) {
                System.out.println("Réservation refusée, pas assez de pièces disponibles.");
                return;
            }
            String prestmnt = "INSERT INTO LocationMateriel(dateRecup, dateRetour," +
                    "idUsr) " +
                    "VALUES (?, ?, ?)";
            conn.setAutoCommit(false);
            PreparedStatement stmnt = conn.prepareStatement(prestmnt);
            stmnt.setDate(1, dateRecuperation);
            stmnt.setDate(2, dateRetour);
            stmnt.setInt(3, idAdherent);
            stmnt.execute();
            stmnt.close();

            prestmnt = "SELECT MAX(idLocationMateriel) FROM LocationMateriel ";
            PreparedStatement stmt = conn.prepareStatement(prestmnt);
            ResultSet res = stmt.executeQuery();
            res.next();
            int id = res.getInt(1);

            for (Lot lot : nbPiecesReservees.keySet()) {
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
            System.out.println("Votre location a été bien prise en compte :) .");
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("An error occurred while executing the SQL query, please try again later");
            } catch (SQLException ex) {
                System.err.println("An error occurred while executing the SQL query");
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("An error occurred while executing the SQL query");
            }

        }
    }

    private int calculPrix(){

    }


    private boolean verifIdAdherent ( int idAdherent) throws SQLException {
        String adherentExistenceStatement = "SELECT * FROM Adherent WHERE idUsr = ?";
        PreparedStatement stmt = conn.prepareStatement((adherentExistenceStatement));
        stmt.setInt(1, idAdherent);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            stmt.close();
            result.close();
            return true;
        }
        stmt.close();
        result.close();
        return false;
    }

    private boolean verifDateRecup (java.sql.Date dateRecup){
        LocalDate today = LocalDate.now();
        LocalDate dateRecupLocal = dateRecup.toLocalDate();

        return dateRecupLocal.isAfter(today);
    }

    private boolean verifDateRetour (java.sql.Date dateRecup, java.sql.Date dateRetour){
        LocalDate localDateRecup = dateRecup.toLocalDate();
        LocalDate localDateRetour = dateRetour.toLocalDate();

        if (!localDateRetour.isAfter(localDateRecup)) {
            return false;
        }

        long difference = ChronoUnit.DAYS.between(localDateRecup, localDateRetour);
        return difference <= 14;
    }

    private boolean verifdisponibilite (java.sql.Date dateRecup, java.sql.Date
    dateRetour, HashMap < Lot, Integer > nbPiecesReservees) throws SQLException {
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
        while (resultSet.next()) {
            String modele = resultSet.getString(1);
            String marque = resultSet.getString(2);
            int annee = resultSet.getInt(3);
            int dispo = resultSet.getInt(4);
            Lot lot = new Lot(modele, marque, annee);
            if (nbPiecesReservees.containsKey(lot)) {
                if (nbPiecesReservees.get(lot) > dispo) {
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


}
