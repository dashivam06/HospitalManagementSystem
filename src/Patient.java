import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Patient {
    
    private Connection connection;
    private Scanner scanner;

    public Patient(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    /**
     * Adds a new patient to the database with the provided name, age, and gender.
     * Prompts the user for input and executes an SQL INSERT command to store the
     * patient's details.
     * Prints a success message upon successful insertion or an error message if
     * insertion fails.
     */
    public void addPatient() {

        System.out.println();

        System.out.print("Enter Patient Name: ");
        String name = scanner.next();

        System.out.print("Enter Patient Age: ");
        int age = scanner.nextInt();

        System.out.print("Enter Patient Gender: ");
        String gender = scanner.next();

        try {
            String addPatientCmd = "INSERT INTO PATIENTS(Name, Age , Gender)" +
                    "VALUES(? , ? , ? );";

            PreparedStatement addPatientQuery = connection.prepareStatement(addPatientCmd);

            addPatientQuery.setString(1, name);
            addPatientQuery.setInt(2, age);
            addPatientQuery.setString(3, gender);

            int affectedTable = addPatientQuery.executeUpdate();

            if (affectedTable > 0) {
                System.out.println("\n" + "--".repeat(14));
                System.out.println(" Patient Added Successfully.");
                System.out.println("--".repeat(14));

            } else {
                System.out.println("Failed to add Patient!!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /**
     * Retrieves and displays details of all patients stored in the database.
     * Executes an SQL SELECT command to fetch patient records and prints them in a
     * formatted table.
     * Handles SQLException by printing the stack trace if an error occurs during
     * database access.
     */
    public void viewPatients() {

        try {
            String showPatientsCmd = "SELECT * FROM PATIENTS";

            Statement statement = connection.createStatement();

            ResultSet patientsRows = statement.executeQuery(showPatientsCmd);

            System.out.println("Patients : ");
            System.out.println("+---------------+--------------------+----------+--------------+");
            System.out.println("| Patient ID    | Name               | Age      | Gender       |");
            System.out.println("+---------------+--------------------+----------+--------------+");

            while (patientsRows.next()) {
                int id = patientsRows.getInt("ID");
                String name = patientsRows.getString("Name");
                int age = patientsRows.getInt("Age");
                String gender = patientsRows.getString("Gender");
                System.out.printf("| %-13s | %-18s | %-8s | %-12s |\n", id, name, age, gender);
                System.out.println("+---------------+--------------------+----------+--------------+");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    /**
     * Retrieves a patient's details from the database based on the provided ID.
     *
     * @param id The ID of the patient to retrieve.
     * @return true if a patient with the given ID exists in the database, false
     *         otherwise.
     *         Prints a stack trace if an SQL exception occurs during database
     *         access.
     */

    public boolean getPatientByID(int id) {
        String patientEnquiryCMD = "SELECT * FROM PATIENTS WHERE ID =  ? ";

        try {
            PreparedStatement enquiryStatement = connection.prepareStatement(patientEnquiryCMD);
            enquiryStatement.setInt(1, id);

            ResultSet affectedRows = enquiryStatement.executeQuery();

            if (affectedRows.next()) {
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
