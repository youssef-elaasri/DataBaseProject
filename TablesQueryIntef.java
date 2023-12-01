import java.sql.*;
import java.util.Date;
import java.util.Objects;



public class TablesQueryIntef {
    
    private Connection conn;

    public TablesQueryIntef(Connection conn){
        this.conn = conn;
    }


    public void showCourses()  {
        try {
            String pre_stmt = "select distinct nomformation, apa.typeactivite, datedemarrage, dureeformation, nbplacesformation " +
                    "from formation f " +
                    "join a_pour_activite apa " +
                    "on f.annee = apa.annee and f.rang = apa.rang " +
                    "order by datedemarrage, nomformation ";
            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            ResultSet resultSet = stmt.executeQuery();
            getTableData(resultSet);
            stmt.close();
            resultSet.close();
        } catch (SQLException e) {
            System.err.println("An error occurred while executing the SQL query: ");
        }
    }

    public void showMaterielCat(String categorie)  {
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

    public void showMaterielAct(String activity) {
        try {
            String pre_stmt = "SELECT DISTINCT lm.modele, lm.marque, lm.annee " +
                    "FROM lotmateriel lm  " +
                    "JOIN utilise u ON lm.modele = u.modele AND lm.marque = u.marque AND lm.annee = u.annee " +
                    "WHERE u.typeactivite = ? AND lm.nbpieces > 0 " +
                    "MINUS " +
                    "SELECT DISTINCT lm.modele, lm.marque, lm.annee " +
                    "FROM lotmateriel lm " +
                    "JOIN a_pour_dateperemption apd ON lm.modele = apd.modele AND lm.marque = apd.marque AND lm.annee = apd.annee " +
                    "where apd.dateperemption < SYSDATE ";
            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            stmt.setString(1, activity);
            ResultSet resultSet = stmt.executeQuery();
            getTableData(resultSet);
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
    public void showRefuge(boolean option) {
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

    public void showAll(String whatToShow) {
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
    public void deleteAll(String EmailUsrString) throws SQLException {
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

            //add newIdUsr to adherent
            pre_stmt = "insert into adherent values(?) ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, newIdUsr);
            stmt.executeUpdate();
            stmt.close();

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

            //delete idusr from agherent
            pre_stmt = "delete adherent where idusr = ? ";
            stmt = conn.prepareStatement(pre_stmt);
            stmt.setInt(1, idusr);
            stmt.executeUpdate();
            stmt.close();

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
        int columnCount = rsetmd.getColumnCount();
    
        // Affichage des noms de colonnes avec une largeur fixe
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("%-30s", rsetmd.getColumnName(i));
        }
        System.out.println();
    
        // Affichage des lignes de données avec une largeur fixe
        while (resultSet.next()) {
            for (int j = 1; j <= columnCount; j++) {
                System.out.printf("%-30s", resultSet.getString(j));
            }
            System.out.println();
        }
    }

    /*
     * Fonction qui vérifie l'existence de l'email et du mdp
     *
     * */
    public boolean verifyPassword(String email, String password) throws SQLException {
        String pre_stmt = "select pwdusr from utilisateur where emailusr = ?";
        PreparedStatement stmt = conn.prepareStatement(pre_stmt);
        stmt.setString(1, email);
        ResultSet resultSet = stmt.executeQuery();
        if (resultSet.next()) {
            String addedPassword = resultSet.getString(1);
            stmt.close();
            resultSet.close();
            if (!addedPassword.equals( password)){
                System.out.println("Mot de passe incorrect!");
                return false;
            }
            return true;

        }
        else {
            System.out.println("Cet e-mail n'existe pas!");
            stmt.close();
            resultSet.close();
            return false;
        }
    }


    public int getidusr(String EmailUsrString) {
        try {
            // select the idusr
            conn.setAutoCommit(false);
            String pre_stmt = "select idusr from utilisateur where emailusr = ? ";
            PreparedStatement stmt = conn.prepareStatement(pre_stmt);
            stmt.setString(1, EmailUsrString);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                int idusr = resultSet.getInt(1);
                stmt.close();
                resultSet.close();
                return idusr;
            } else {
                System.out.println("there is no such email in our database");
                stmt.close();
                resultSet.close();
            }
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
        return -1;
    }




}
