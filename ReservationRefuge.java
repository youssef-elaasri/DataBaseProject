import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.time.format.DateTimeFormatter;

/**
 * Represents a reservation in a refuge, including nights and meals.
 */
public class ReservationRefuge {

    // Constants for connecting to the Oracle database
    static final String CONN_URL = "jdbc:oracle:thin:@oracle1.ensimag.fr:1521:oracle1";
    static final String USER = "lmimouna";
    static final String PASSWD = "lmimouna";

    private Connection conn;

    // Set of valid meals
    private final Set<String> Repas = new HashSet<>(Set.of("dejeuner", "diner", "souper", "casse-croute"));

    /**
     * Constructs a ReservationRefuge object and performs reservation actions.
     *
     * @param idUsr         The user's identifier.
     * @param emailRefuge   The refuge's email where the reservation is made.
     * @param nuitsReserves The number of nights reserved.
     * @param date          The date of the reservation.
     * @param repas         The meals reserved.
     */
    public ReservationRefuge(int idUsr, String emailRefuge, int nuitsReserves, String date, String... repas) {
        try {
            // Registering the Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

            // Establishing the connection
            conn = DriverManager.getConnection(CONN_URL, USER, PASSWD);

            // Number of reserved meals
            int nbrRepas = repas.length;

            // Verifying conditions before making the reservation
            if (verifyIdUsr(idUsr) &&
                    verifyRefuge(emailRefuge) &&
                    verifyDate(emailRefuge, date, nuitsReserves) &&
                    verifyNbrNuitsRepas(emailRefuge, nuitsReserves, nbrRepas) &&
                    verifyRepas(repas)) {

                // Calculate the total price of the reservation
                int prix = calculPrix(emailRefuge, nuitsReserves, repas);

                // Insert the reservation into the database
                insertQuery(nuitsReserves, nbrRepas, prix, emailRefuge, idUsr, date);
            }

            // Closing the connection
            conn.close();
            return;

        } catch (SQLException e) {
            System.err.println("failed");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Executes the insertion query into the ReservationRefuge table.
     *
     * @param nuitsReserves The number of nights reserved.
     * @param nbrRepas      The number of meals reserved.
     * @param prix          The total price of the reservation.
     * @param email         The refuge's email where the reservation is made.
     * @param idUsr         The user's identifier.
     * @param date          The date of the reservation.
     * @throws SQLException If a database access error occurs.
     */
    private void insertQuery(int nuitsReserves, int nbrRepas, int prix, String email, int idUsr, String date) throws SQLException {
        if (prix == -1) {
            return;
        }

        // Get the current time
        LocalTime currentTime = LocalTime.now();

        // Get the current hour
        int currentHour = currentTime.getHour();

        // Format the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.sql.Date sqlDate = java.sql.Date.valueOf(LocalDate.parse(date, formatter));

        // Prepare the SQL statement for inserting the reservation
        String prestmnt = "INSERT INTO ReservationRefuge (dateResRefuge, heureResRefuge," +
                "nbNuitResRefuge, nbRepasResRefuge, prixResRefuge, email, idUsr) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmnt = conn.prepareStatement(prestmnt);
        stmnt.setDate(1, sqlDate);
        stmnt.setInt(2, currentHour);
        stmnt.setInt(3, nuitsReserves);
        stmnt.setInt(4, nbrRepas);
        stmnt.setInt(5, prix);
        stmnt.setString(6, email);
        stmnt.setInt(7, idUsr);

        // Execute the insertion query
        stmnt.execute();
    }

    /**
     * Calculates the total price of the reservation.
     *
     * @param emailRefuge   The refuge's email where the reservation is made.
     * @param nuitsReserves The number of nights reserved.
     * @param repas         The meals reserved.
     * @return The total price of the reservation.
     * @throws SQLException If a database access error occurs.
     */
    private int calculPrix(String emailRefuge, int nuitsReserves, String... repas) throws SQLException {
        int prixReservationNuits = 0;
        int prixReservationRepas = 0;

        if (nuitsReserves > 0) {
            // Retrieve the price per night from the Refuge table
            String prestmnt = "SELECT prixNuitee FROM Refuge WHERE email = ?";
            PreparedStatement stmnt = conn.prepareStatement(prestmnt);
            stmnt.setString(1, emailRefuge);
            ResultSet resultSet = stmnt.executeQuery();
            if (resultSet.next()) {
                prixReservationNuits = nuitsReserves * resultSet.getInt(1);
                stmnt.close();
                resultSet.close();
            }

            // Retrieve the price per meal from the Propose table
            for (String leRepas : repas) {
                String prestmntRepas = "SELECT prix FROM Propose WHERE email = ? AND repas = ?";
                PreparedStatement stmntRepas = conn.prepareStatement(prestmntRepas);
                stmntRepas.setString(1, emailRefuge);
                stmntRepas.setString(2, leRepas);

                ResultSet resultSetRepas = stmntRepas.executeQuery();
                if (resultSetRepas.next()) {
                    prixReservationRepas += resultSetRepas.getInt(1);
                    stmntRepas.close();
                    resultSetRepas.close();
                } else {
                    System.out.println("Meal " + leRepas + " is not available");
                    return -1;
                }
            }
        }

        return prixReservationNuits + prixReservationRepas;
    }

    /**
     * Verifies the existence of the user in the CompteUtilisateur table.
     *
     * @param idUsr The user's identifier.
     * @return True if the user exists; otherwise, false.
     * @throws SQLException If a database access error occurs.
     */
    private Boolean verifyIdUsr(int idUsr) throws SQLException {
        String userExistenceStatement = "SELECT COUNT(*) FROM CompteUtilisateur WHERE idUsr = ?";
        PreparedStatement stmt = conn.prepareStatement((userExistenceStatement));
        stmt.setInt(1, idUsr);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            if (result.getInt(1) > 0) {
                stmt.close();
                result.close();
                return true;
            }
            System.out.println("cet ID " + idUsr + " N'existe pas");
        }

        stmt.close();
        result.close();

        return false;
    }

    /**
     * Verifies the existence of the refuge in the Refuge table.
     *
     * @param emailRefuge The refuge's email.
     * @return True if the refuge exists; otherwise, false.
     * @throws SQLException If a database access error occurs.
     */
    private Boolean verifyRefuge(String emailRefuge) throws SQLException {
        String refugeExistenceStatement = "SELECT COUNT(*) FROM Refuge WHERE email = ?";
        PreparedStatement stmt = conn.prepareStatement((refugeExistenceStatement));
        stmt.setString(1, emailRefuge);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            if (result.getInt(1) > 0) {
                stmt.close();
                result.close();
                return true;
            }
            System.out.println("Le refuge dont l'email est " + emailRefuge + " n'existe pas");
        }
        return false;
    }

    /**
     * Verifies the number of nights and meals to be reserved.
     *
     * @param emailRefuge    The refuge's email.
     * @param nuitsReserves  The number of nights reserved.
     * @param repasReserves  The number of meals reserved.
     * @return True if the reservation is valid; otherwise, false.
     * @throws SQLException If a database access error occurs.
     */
    private Boolean verifyNbrNuitsRepas(String emailRefuge, int nuitsReserves, int repasReserves) throws SQLException {
        if (nuitsReserves < 0 || nuitsReserves + repasReserves == 0) {
            System.out.println("Tu dois réserver au moins une place ou un repas!");
            return false;
        }

        // Check the total number of nights and meals already reserved
        String preStatementNuitsRepasReserve = "SELECT SUM(nbNuitResRefuge), SUM(nbRepasResRefuge) FROM ReservationRefuge WHERE email = ? ";
        PreparedStatement statementNuitsRepasReserve = conn.prepareStatement((preStatementNuitsRepasReserve));
        statementNuitsRepasReserve.setString(1, emailRefuge);
        ResultSet resultNuitsRepasReserve = statementNuitsRepasReserve.executeQuery();
        if (resultNuitsRepasReserve.next()) {
            int nuitsDejaReserve = resultNuitsRepasReserve.getInt(1);
            int repasDejaReserve = resultNuitsRepasReserve.getInt(2);

            statementNuitsRepasReserve.close();
            resultNuitsRepasReserve.close();

            // Check the available places in the refuge
            String refugeExistenceStatement = "SELECT nbPlacesDormir, nbPlacesRepas FROM Refuge WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement((refugeExistenceStatement));
            stmt.setString(1, emailRefuge);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                int nuitsDispo = result.getInt(1);
                int repasDispo = result.getInt(2);
                stmt.close();
                result.close();

                if ((nuitsReserves + nuitsDejaReserve > nuitsDispo)) {
                    System.out.println("Il n'y a que " + (nuitsDispo - nuitsDejaReserve) + " places valable pour l'instant");
                    return false;
                }
                if ((repasReserves + repasDejaReserve > repasDispo)) {
                    System.out.println("Il n'y a que " + (repasDispo - repasDejaReserve) + " repas disponible pour le moment");
                    return false;
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Verifies the validity of the reservation date.
     *
     * @param emailRefuge The refuge's email.
     * @param date        The date of the reservation.
     * @param nbrNuits    The number of nights reserved.
     * @return True if the date is valid; otherwise, false.
     * @throws SQLException If a database access error occurs.
     */
    private Boolean verifyDate(String emailRefuge, String date, int nbrNuits) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);

        // Retrieve the opening and closing dates of the refuge
        String preStmntDate = "SELECT dateOuverture, dateFermeture FROM Refuge WHERE email = ?";
        PreparedStatement stmntDate = conn.prepareStatement(preStmntDate);
        stmntDate.setString(1, emailRefuge);

        ResultSet result = stmntDate.executeQuery();
        if (result.next()) {
            LocalDate ouverture = result.getDate(1).toLocalDate();
            LocalDate fermeture = result.getDate(2).toLocalDate();
            if (fermeture.isBefore(ouverture)) fermeture = fermeture.plusYears(1);

            // Check if the reservation date is within the valid range
            stmntDate.close();
            result.close();
            return localDate.isAfter(ouverture) && localDate.plusDays(nbrNuits).isBefore(fermeture);
        }

        return false;
    }

    /**
     * Verifies the validity of the reserved meals.
     *
     * @param repas The meals to be reserved.
     * @return True if the meals are valid; otherwise, false.
     */
    private Boolean verifyRepas(String... repas) {
        for (String unRepas : repas) {
            if (!Repas.contains(unRepas)) {
                System.out.println("Le repas " + unRepas + " n'est pas valide");
                return false;
            }
        }
        return true;
    }
}
