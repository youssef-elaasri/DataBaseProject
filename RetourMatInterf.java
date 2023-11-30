import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RetourMatInterf {
    private Connection conn;

    public RetourMatInterf(Connection conn, int nbPiecesCasseesPerdues, Lot materiel, int idUser){
        this.conn = conn;
        try {
            piecesCassees(nbPiecesCasseesPerdues, materiel, idUser);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void piecesCassees(int nbPiecesCasseesPerdues, Lot materiel, int idUser) throws SQLException{
        try {
            conn.setAutoCommit(false);
            //Récupérer l'identifiant de la location à partir de l'identifiant d'utilisateur
            String preStmnt_ =  "Select idLocationMateriel From LocationMateriel " +
                                "WHERE idUsr = ? ";
            PreparedStatement stmt_ = conn.prepareStatement(preStmnt_);
            stmt_.setInt(1, idUser);
            ResultSet resultSet_ = stmt_.executeQuery();
            if (resultSet_.next()){
                int idLoc = resultSet_.getInt(1); 
                stmt_.close();
                resultSet_.close();

                //Mise à jour de la table
                String preStmnt =   "UPDATE ReservationPieces " +
                                    "SET nbPiecesCasseesPerdues = ? " +
                                    "WHERE marque = ? AND modele = ? AND annee = ? AND idLocationMateriel = ? ";
                PreparedStatement stmt = conn.prepareStatement(preStmnt);
                stmt.setInt(1, nbPiecesCasseesPerdues);
                stmt.setString(2, materiel.marque);
                stmt.setString(3, materiel.modele);
                stmt.setInt(4, materiel.annee);
                stmt.setInt(5, idLoc);
                stmt.executeUpdate();
                stmt.close();
            } else {
                System.out.println("Utilisateur non trouvé. ");
            }
        }catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("An error occurred while executing the SQL query 1.");
                System.err.println(e.getMessage());
            } catch (SQLException ex) {
                System.err.println("An error occurred while executing the SQL query 2.");
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("An error occurred while executing the SQL query 3.");
            }
        }
    }
}
