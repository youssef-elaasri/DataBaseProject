import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;


public class Interface {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";
    private Connection conn;


    public Interface() {
        try {
            // Enregistrement du driver Oracle
            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection

            System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            System.out.println("connected");


            Scanner scan = new Scanner(System.in);


            // Connexion
            System.out.println("Veuillez entrez votre e-mail:");
            String email = scan.nextLine();
            System.out.println("Veuillez entrez votre mot de passe:");
            String pwd = scan.nextLine();
            if (verifyPassword(email,pwd)){
                System.out.println("Vous êtes connectés :)");

                // Demander à l'utilisateur
                System.out.println("Veuillez choisir l'une des options suivantes:");
                System.out.println("1 : Afficher nos magnifiques refuges");
                System.out.println("2 : Afficher nos formations polyvalentes");
                System.out.println("3 : Afficher nos matériels de qualité");
                System.out.println("4 : Faire une réservation de refuge");
                System.out.println("5 : Faire une réservation de formation");
                System.out.println("6 : Faire une location de matériel");
                System.out.println("7 : Supprimer votre compte");
                System.out.println("8 : Se déconnecter");
                System.out.println("");
                boolean fin = true;
                while (fin){
                String choix = scan.nextLine();
                switch (choix) {
                    case "1":
                        System.out.println("");
                        System.out.println("1 : Par dates");
                        System.out.println("2 : Par disponibilités");
                        System.out.println("");
                        boolean c = true;
                        while (c) {
                            String option = scan.nextLine();
                            if (option.equals("1")) {
                                showRefuge(true);
                                c = false;
                            } else if (option.equals("2")) {
                                showRefuge(false);
                                c = false;
                            } else System.out.println("Choisissez 1 ou 2");
                        }
                        break;
                    case "2":
                        showCourses();
                        break;
                    case "3":
                        boolean s = true;
                        while (s) {
                            System.out.println("");
                            System.out.println("1 : Par catégories");
                            System.out.println("2 : Par activités");
                            String option = scan.nextLine();
                            if (option.equals("1")) {
                                System.out.println("");
                                System.out.println("Veuillez entrez la catégorie qui vous intéresse:");
                                String cat = scan.nextLine();
                                showMaterielCat(cat);
                                s = false;
                            } else if (option.equals("2")) {
                                System.out.println("");
                                System.out.println("Veuillez entrez l'activité qui vous intéresse:");
                                String act = scan.nextLine();
                                showMaterielAct(act);
                                s = false;
                            } else System.out.println("Choisissez 1 ou 2");
                        }
                        System.out.println("1 : Choisir une autre option");
                        System.out.println("2 : Quitter");
                        boolean ss = true;
                        while (ss){
                            String option2 = scan.nextLine();
                            if (option2.equals("1")){
                                choix = scan.nextLine();
                                break;
                            } else if (option2.equals("2")){
                                System.out.println("A la prochaine.");
                                ss = false;
                                fin = false;
                            }
                        }

                        break;
                    case "4":
                        System.out.println("Veuillez entrez l'email du refuge que vous voulez:");
                        String emailRef = scan.nextLine();
                        System.out.println("Veuillez entrez le nombre de nuitées:");
                        String nuits = scan.nextLine();

                        ArrayList<String> listeDeRepas = new ArrayList<>();

                        while(true){
                            System.out.println("Veuillez ajoutez un repas que vous voulez, si vous ne voulez pas de repas appuyez sur Entrer:");
                            String repas = scan.nextLine();
                            if(repas.equals("")){
                                break;
                            }
                            listeDeRepas.add(repas);
                        }



                        System.out.println("Veuillez entrez la date de réservation sous la forme YYYY-MM-DD:");
                        String date = scan.nextLine();
                        new ReservationRefuge (getidusr(email),emailRef, Integer.valueOf(nuits),date,listeDeRepas.toArray(new String[0]));
                        break;
                    case "5":
                        showCourses();
                        break;
                    case "6":
                        showCourses();
                        break;
                    case "7":
                        deleteAll(email);
                        System.out.println("Votre compte a été supprimé avec succès.");
                        System.out.println("A la prochaine.");
                        fin = false;
                        break;
                    case "8":
                        System.out.println("A la prochaine.");
                        fin = false;
                        break;
                    default:
                        System.out.println("Veuillez choisir l'une des options proposées.");
                }
                }
            }




        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /*
    * Fonction qui vérifie l'existence de l'email et du mdp
    *
    * */
    boolean verifyPassword(String email, String password) throws SQLException {
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
    void showCourses()  {
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


    void showMaterielAct(String activity) {
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

    void deleteAll(String EmailUsrString) throws SQLException {
        try {
            // select the idusr
            String pre_stmt = "select idusr from utilisateur where emailusr = ? ";
            conn.setAutoCommit(false);
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

            //delete idusr from compteutilisateur
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



    public static void main(String[] args){
        Interface inter = new Interface();
    }

    private int getidusr(String EmailUsrString) {
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
            }
            else {
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
