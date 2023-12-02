import java.sql.*;
import java.util.Date;
import java.util.Objects;

public class Tablesquery {

    private static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";

    private static final String USER = "lmimouna";
    private static final String PASSWD = "lmimouna";

    private Connection conn;

    public Tablesquery() {
        try {
            // Enregistrement du driver Oracle

            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection

            System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            System.out.println("connected");
        } catch (SQLException e) {
            System.err.println("An error occurred while connecting to the database: ");
        }
    }

    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // Méthode pour vérifier le mot de passe d'un utilisateur en fonction de son email
    boolean verifyPassword(String email, String password) throws SQLException {
        try {
            // Requête SQL pour récupérer le mot de passe de l'utilisateur à partir de son email
            String pre_stmt = "select pwdusr from utilisateur where emailusr = ?";
            // Préparation de la requête SQL avec un PreparedStatement pour éviter les attaques par injection SQL
            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            // Paramètre la valeur de l'email dans la requête SQL
            stmt.setString(1, email);
            // Exécute la requête SQL et récupère le résultat dans un ResultSet
            ResultSet resultSet = stmt.executeQuery();
            // Vérifie si l'email existe dans la base de données
            if (resultSet.next()) {
                // Récupère le mot de passe stocké dans la base de données
                String addedPassword = resultSet.getString(1);
                // Ferme la connexion et le ResultSet
                stmt.close();
                resultSet.close();
                // Compare le mot de passe fourni avec celui stocké dans la base de données
                return addedPassword.equals(password);
            } else {
                // Affiche un message si l'email n'est pas trouvé
                System.out.println("Email non trouvé");
                // Ferme la connexion et le ResultSet
                stmt.close();
                resultSet.close();
                // Renvoie false car l'email n'est pas trouvé
                return false;
            }
        } catch (SQLException e) {
            // Gestion des erreurs SQL
            System.err.println("\"Une erreur s'est produite lors de l'exécution de la requête SQL");
        }
        return false;
    }


    void showMaterielCat(String categorie)  {
        try {
            String pre_stmt = "SELECT DISTINCT lm.modele, lm.marque, lm.annee, lm.categorie, lm.NBPIECES, coalesce(lm.NBPIECES -NBPIECESalouee, lm.NBPIECES ) as NBPIECESDISPO " +
                    "from lotmateriel lm " +
                    "left join (SELECT coalesce(SUM(NBPIECESRESERVEES),0) as NBPIECESalouee, modele as md ,marque mq ,annee an from reservationpieces group by marque,modele,annee) " +
                    "on md = lm.modele and an = lm.annee and mq = lm.marque " +
                    "where categorie = ? and coalesce(lm.NBPIECES -NBPIECESalouee, lm.NBPIECES ) > 0 " +
                    "MINUS " +
                    "(SELECT DISTINCT lm.modele, lm.marque, lm.annee, lm.categorie, lm.NBPIECES, NBPIECESDISPO " +
                    "FROM lotmateriel lm " +
                    "join (SELECT coalesce(SUM(NBPIECESRESERVEES),0) as NBPIECESDISPO, modele as md ,marque mq ,annee an from reservationpieces rp group by marque,modele,annee) " +
                    "on md = lm.modele and an = lm.annee and mq = lm.marque " +
                    "JOIN a_pour_dateperemption apd ON lm.modele = apd.modele AND lm.marque = apd.marque AND lm.annee = apd.annee " +
                    "where apd.dateperemption < SYSDATE)";

            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            stmt.setString(1, categorie);
            ResultSet resultSet = stmt.executeQuery();
            getTableData(resultSet);
            stmt.close();
            resultSet.close();
            pre_stmt = "select souscategorie from a_comme_sous_categorie where categorie = ?";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setString(1, categorie);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                showMaterielCat(resultSet.getString(1));
            }
            stmt.close();
            resultSet.close();
        } catch (SQLException e) {
            System.err.println("An error occurred while executing the SQL query: ");
        }
    }

    /*
    * @param option : takes true if we want to order the table by  refuge's name and dates
    *                 takes false if we want to order the table by  refuge's name and available places
    * */
    void showRefuge(boolean option) {
        try {
            String pre_stmt = "select nomrefuge, SECTEURGEO, NBPLACESREPAS, nbplacesdormir from refuge order by nomrefuge, ?";
            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            stmt.setString(1, option ? "dateouverture, datefermeture" : "nbplacesdormir");
            ResultSet resultSet = stmt.executeQuery();
            getTableData(resultSet);
            stmt.close();
            resultSet.close();
        } catch (SQLException e) {
            System.err.println("An error occurred while executing the SQL query: ");
        }
    }
    void showAll(String whatToShow) {
        try {
            String pre_stmt = "select * from " + whatToShow;
            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            ResultSet resultSet = stmt.executeQuery();
            getTableData(resultSet);
            stmt.close();
            resultSet.close();
        } catch (SQLException e) {
            System.err.println("An error occurred while executing the SQL query: ");
        }
    }

    void deleteAll(String EmailUsrString) throws SQLException {
        try {
            // select the idusr
            conn.setAutoCommit(false);
            String pre_stmt = "select idusr from utilisateur where emailusr = ? ";
            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            stmt.setString(1, EmailUsrString);
            ResultSet resultSet = stmt.executeQuery();
            int idusr;
            if (resultSet.next()) idusr = resultSet.getInt(1);
            else {
                System.out.println("there is no such email in our database");
                stmt.close();
                resultSet.close();
                return;
            }
            stmt.close();
            resultSet.close();

            // select the idusr
            conn.setAutoCommit(false);
            pre_stmt = "select * from adherent where IDUSR = ? ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, idusr);
            resultSet = stmt.executeQuery();
            boolean isAdherent = resultSet.next();
            stmt.close();
            resultSet.close();


            // select the new idusr
            pre_stmt = "select max(idusr) from compteutilisateur ";
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(pre_stmt);
            resultSet = stmt.executeQuery();
            int newIdUsr;
            resultSet.next();
            newIdUsr = resultSet.getInt(1) + 1;
            stmt.close();
            resultSet.close();

            //delete all the information of the user from utilisateur
            pre_stmt = "delete from utilisateur where emailusr = ? ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setString(1, EmailUsrString);
            stmt.executeUpdate();
            stmt.close();

            //add newIdUsr to compteutilisateur
            pre_stmt = "insert into compteutilisateur values(?) ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, newIdUsr);
            stmt.executeUpdate();
            stmt.close();

            //add newIdUsr to adherent if the user was an adherent
            if (isAdherent) {
                pre_stmt = "insert into adherent values(?) ";
                stmt = conn.prepareStatement(pre_stmt);
                stmt.setInt(1, newIdUsr);
                stmt.executeUpdate();
                stmt.close();
            }

            // change the idusr in reservationrefuge
            pre_stmt = "update reservationrefuge set idusr = ? where idusr = ? ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, newIdUsr);
            stmt.setInt(2, idusr);
            stmt.executeUpdate();
            stmt.close();

            // change the idusr in locationmateriel
            pre_stmt = "update locationmateriel set idusr = ? where idusr = ? ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, newIdUsr);
            stmt.setInt(2, idusr);
            stmt.executeUpdate();
            stmt.close();

            // change the idusr in reservationformation
            pre_stmt = "update reservationformation set idusr = ? where idusr = ? ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, newIdUsr);
            stmt.setInt(2, idusr);
            stmt.executeUpdate();
            stmt.close();

            //delete idusr from adherent
            if (isAdherent) {
                pre_stmt = "delete adherent where idusr = ? ";
                stmt = conn.prepareStatement(pre_stmt);
                stmt.setInt(1, idusr);
                stmt.executeUpdate();
                stmt.close();
            }

            //delete idusr from compteutilisateur
            pre_stmt = "delete compteutilisateur where idusr = ? ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, idusr);
            stmt.executeUpdate();
            stmt.close();

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


    static void getTableData(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsetmd = resultSet.getMetaData();
        int lenghtColum = rsetmd.getColumnCount();
        while (resultSet.next()) {
            for (int j = 1; j <= lenghtColum; j++) {
                System.out.print(resultSet.getString(j) + "\t");
            }
            System.out.println();
        }
    }

}
