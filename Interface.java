import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Interface {
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";
    private Connection conn;

    /*
     * Etabissement de la connexion
     */
    public Interface() {
        try {
            // Enregistrement du driver Oracle
            System.out.print("Loading Oracle driver... ");
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            System.out.println("loaded");

            // Etablissement de la connection

            System.out.print("Connecting to the database... ");
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);
            //conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            System.out.println("connected");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Récuperer la connexion
     */
    public Connection getConnection(){
        return this.conn;
    }

    /*
    * Afficher les options
    */
    private static void printOptions(){
        System.out.println("Veuillez choisir l'une des options suivantes:");
        System.out.println("1 : Afficher nos magnifiques refuges");
        System.out.println("2 : Afficher nos formations polyvalentes");
        System.out.println("3 : Afficher nos matériels de qualité");
        System.out.println("4 : Faire une réservation de refuge ou annulation");
        System.out.println("5 : Faire une réservation de formation ou annulation");
        System.out.println("6 : Faire une location de matériel ou retourner un matériel");
        System.out.println("7 : Afficher vos messages");
        System.out.println("8 : Supprimer votre compte");
        System.out.println("9 : Se déconnecter");
        System.out.println("");
    }

    /*
     * Récupérer l'email de l'utilisateur
     */
    private static String emailUtilisateur(Scanner scan){
        System.out.println("Veuillez entrez votre e-mail:");
        return scan.nextLine();
    }

    /*
     * Récupérer le mot de passe de l'utilisateur
     */
    private static String pwdUtilisateur(Scanner scan){
        System.out.println("Veuillez entrez votre mot de passe:");
        return scan.nextLine();
    }

    /*
     * Récupérer la marque du lot de matériel
     */
    private static String getMarque(Scanner scan){
        System.out.println("marque :");
        return scan.nextLine();
    }

    /*
     * Récupérer le modèle du lot de matériel
     */
    private static String getModele(Scanner scan){
        System.out.println("modele :");
        return scan.nextLine();
    }

    /*
     * Récupérer l'année du lot de matériel
     */
    private static String getAnnee(Scanner scan){
        System.out.println("annee :");
        return scan.nextLine();
    }

    private static boolean autreOptions(Scanner scan) {
        System.out.println("");
        System.out.println("1 : Choisir une autre option");
        System.out.println("2 : Se déconnecter");
        boolean choix4 = true;
        while (choix4) {
            String option2 = scan.nextLine();
            if (option2.equals("1")) { //Choisir une autre option
                printOptions();
                break;
            } else if (option2.equals("2")) { //Quitter
                System.out.println("A la prochaine.");
                choix4 = false;
                return false;
            }
        }
        return true;
    }
    public static void main(String[] args) throws SQLException {
        // Se connecter à oracle
        Interface inter = new Interface();
        Scanner scan = new Scanner(System.in);


        // Connexion de l'utilisateur
        String email = emailUtilisateur(scan);
        String pwd = pwdUtilisateur(scan);

        TablesQueryIntef query = new TablesQueryIntef(inter.getConnection());
        if (query.verifyPassword(email, pwd)) {
            System.out.println("Vous êtes connectés :)");

            // Afficher à l'utilisateur les options
            printOptions();

            boolean connecte = true;
            while (connecte) {
                String choix = scan.nextLine();
                switch (choix) {
                    case "1": //Afficher les refuges
                        System.out.println("");
                        System.out.println("1 : Par dates");
                        System.out.println("2 : Par disponibilités");
                        System.out.println("");
                        boolean c = true;
                        while (c) {
                            String option = scan.nextLine();
                            if (option.equals("1")) {
                                query.showRefuge(true);
                                c = false;
                            } else if (option.equals("2")) {
                                query.showRefuge(false);
                                c = false;
                            } else System.out.println("Choisissez 1 ou 2");
                        }

                        connecte = autreOptions(scan);
                        break;

                    case "2": //Afficher les formations
                        query.showCourses();
                        connecte = autreOptions(scan);
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
                                query.showMaterielCat(cat);
                                s = false;
                            } else if (option.equals("2")) {
                                System.out.println("");
                                System.out.println("Veuillez entrez l'activité qui vous intéresse:");
                                String act = scan.nextLine();
                                query.showMaterielAct(act);
                                s = false;
                            } else System.out.println("Choisissez 1 ou 2");
                        }
                        connecte = autreOptions(scan);
                        break;

                    case "4": //Réservation/Annulation Refuge
                        System.out.println("");
                        System.out.println("1 : Réservation");
                        System.out.println("2 : Annulation");
                        System.out.println("");
                        String optionRef = scan.nextLine();
                        boolean boucleRef = true;
                        while (boucleRef){
                            if (optionRef.equals("1")) { //Réservation
                                System.out.println("Veuillez entrez l'email du refuge que vous voulez:");
                                String emailRef = scan.nextLine();
                                System.out.println("Veuillez entrez le nombre de nuitées:");
                                String nuits = scan.nextLine();
                                ArrayList<String> listeDeRepas = new ArrayList<>();
                                System.out.println("Veuillez ajoutez le ou les repas que vous voulez.");
                                System.out.println("Si vouz voulez plus d'un repas, tapez Entrer après chaque repas.");
                                System.out.println("si vous ne voulez pas de repas appuyez sur Entrer.");
                                while (true) {
                                    String repas = scan.nextLine();
                                    if (repas.equals("")) {
                                        break;
                                    }
                                    listeDeRepas.add(repas);
                                }
                                System.out.println("Veuillez entrez la date de réservation sous la forme YYYY-MM-DD:");
                                String date = scan.nextLine();
                                new ReservationRefuge(query.getidusr(email), emailRef, Integer.valueOf(nuits), date, listeDeRepas.toArray(new String[0]));
                                boucleRef = false;
                            } else if (optionRef.equals("2")) {//Annulation
                                System.out.println("Veuillez indiquer l'id de la réservation");
                                String idResREF = scan.nextLine();
                                new ReservationRefuge(Integer.valueOf(idResREF));
                            } else System.out.println("Choisissez 1 ou 2");
                        }
                        connecte = autreOptions(scan);
                        break;

                    case "5": //Réservation/Annulation formation
                        System.out.println("");
                        System.out.println("1 : Réservation");
                        System.out.println("2 : Annulation");
                        System.out.println("");
                        String optionForm = scan.nextLine();
                        boolean boucle = true;
                        while (boucle){
                            if (optionForm.equals("1")) {
                                System.out.println("");
                                System.out.println("Veuillez entrer la formation que vous voulez réserver.");
                                System.out.println("Pour cela, veuillez indiquer l'année:");
                                String anneeForm = scan.nextLine();
                                System.out.println("Veuillez indiquer le rang de la formation:");
                                String rang = scan.nextLine();
                                Formation form = new Formation(Integer.valueOf(anneeForm),Integer.valueOf(rang));
                                new ReservationFormation(inter.getConnection(), query.getidusr(email),form);
                                boucle = false;
                            } else if (optionForm.equals("2")) {
                                System.out.println("");
                                System.out.println("Veuillez entrer la formation que vous voulez annuler.");
                                System.out.println("Veuillez indiquer l'ID de la réservation formation:");
                                String idRes = scan.nextLine();
                                ReservationFormationX reserv = new ReservationFormationX(inter.getConnection(),Integer.valueOf(idRes), query.getidusr(email));
                                reserv.AnnulationResFormation(query.getidusr(email));
                                boucle = false;
                            } else System.out.println("Choisissez 1 ou 2");
                        }
                        connecte = autreOptions(scan);
                        break;

                    case "6": // Location ou Retour du matériel
                        System.out.println("");
                        System.out.println("1 : Location de matériel");
                        System.out.println("2 : Retour de matériel ");
                        System.out.println("");
                        boolean end = true;
                        while (end) {
                            String option = scan.nextLine();
                            // L'utilisateur choisit 1 pour louer un matériel
                            if (option.equals("1")) {
                                System.out.println("");
                                //initialisation des lot à réserver
                                HashMap<Lot, Integer> piecesReservees = new HashMap<Lot, Integer>();
                                System.out.println("Combien de lots vous intéressent ?");
                                String nbLot = scan.nextLine();
                                for (int i = 0; i < Integer.valueOf(nbLot); i++) {

                                    if (i == 0) System.out.println("Le premier de quel lot?");
                                    else System.out.println("Le " + (i + 1) + "ème de quel lot?");

                                    String marque = getMarque(scan);
                                    String modele = getModele(scan);
                                    String annee = getAnnee(scan);
                                    Lot lot = new Lot(modele, marque, Integer.valueOf(annee));

                                    System.out.println("Combien de pièces de ce lot ?");
                                    String nbPieces = scan.nextLine();
                                    piecesReservees.put(lot, Integer.valueOf(nbPieces));
                                }
                                System.out.println("Veuillez entrer la date de récupération:");
                                String dateRecup = scan.nextLine();
                                System.out.println("Veuillez entrer la date de retour:");
                                String dateRetour = scan.nextLine();

                                //La location:
                                new LocationMatInterf(inter.getConnection(), query.getidusr(email), piecesReservees, dateRecup, dateRetour);
                                end = false;

                            } 
                            // L'utilisateur choisit 2 pour retourner un matériel
                            else if (option.equals("2")) {
                                System.out.println("Avez-vous abîmé/perdu des pièces? Répondre par oui ou non");
                                String reponse = scan.nextLine();
                                boolean cassee = reponse.equals("oui");
                                while (cassee) {
                                    System.out.println("");
                                    System.out.println("Dequel lot?");
                                    String marque = getMarque(scan);
                                    String modele = getModele(scan);
                                    String annee = getAnnee(scan);

                                    Lot lot = new Lot(modele, marque, Integer.valueOf(annee));
                                    System.out.println("Combien de pièce de ce lot?");
                                    String nbPieces = scan.nextLine();
                                    new RetourMatInterf(inter.getConnection(), Integer.valueOf(nbPieces), lot, query.getidusr(email));
                                    System.out.println("D'autres lots? ");
                                    reponse = scan.nextLine();
                                    cassee = reponse.equals("oui");
                                }  
                                end = false;
                                if (reponse.equals("non")){
                                    System.out.println("");
                                    System.out.println("Merci pour votre réponse!");
                                }
                                System.out.println("");
                                System.out.println("Choisissez 1 ou 2");
                            } else {
                                System.out.println("Choisissez 1 ou 2");
                            }
                        }
                        connecte = autreOptions(scan);
                        break;

                    case "7": //Afficher les messages
                        String checkStatement = "Select message FROM Message WHERE idUsr = ?";
                        PreparedStatement checkPrep = inter.getConnection().prepareStatement(checkStatement);
                        checkPrep.setInt(1,query.getidusr(email));
                        ResultSet checkRes = checkPrep.executeQuery();
                        TablesQueryIntef.getTableData(checkRes);
                        String delStatement = "DELETE FROM Message WHERE idUsr = ?"; //on vide la messagerie après la lecture.
                        PreparedStatement delPrep = inter.getConnection().prepareStatement(delStatement);
                        delPrep.setInt(1,query.getidusr(email));
                        delPrep.executeQuery();
                        connecte = autreOptions(scan);
                        break;

                    case "8": //Supprimer compte
                        query.deleteAll(email);
                        System.out.println("Votre compte a été supprimé avec succès.");
                        System.out.println("A la prochaine.");
                        connecte = false;
                        break;
                    case "9": //
                        System.out.println("A la prochaine.");
                        connecte = false;
                        break;
                    default:
                        System.out.println("Veuillez choisir l'une des options proposées.");
                }
            }
        }

    }
}
