import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
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
        
        String name = "";
        int age = -1;
        String gender = "";

        // Input Patient Name
        while (name.isEmpty()) {
            try {
                System.out.print("Enter Patient Name: ");
                name = scanner.next();
                
                
                if (name.trim().isEmpty() || (containsNumber(name))) {
                    System.out.println("\nInvalid input. Please enter a valid name.\n");
                    name = "";
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid name.\n");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        // Input Patient Age
        while (age == -1) {
            try {
                System.out.print("Enter Patient Age: ");
                age = scanner.nextInt();
                if (age <= 0) {
                    System.out.println("\nInvalid age. Please enter a positive integer.\n");
                    age = -1; // Reset age to continue the loop
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid age.\n");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        // Input Patient Gender
        while (gender.isEmpty()) {
            try {
                System.out.print("Enter Patient Gender: ");
                gender = scanner.next();

                if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female") && !gender.equalsIgnoreCase("Other")) {
                    System.out.println("\nInvalid input. Please enter 'Male', 'Female', or 'Other'.\n");
                    gender = "";
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid gender.\n");
                scanner.nextLine(); // Clear the invalid input
            }
        }







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
                System.out.println("\n" + "--".repeat(14));
                System.out.println(" Failed to add patient. ");
                System.out.println("--".repeat(14));
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

    public void dischargePatient() {

        int patientID = -1;

        // Input Patient ID
        while (patientID == -1) {
            try {
                System.out.print("Enter Patient's ID :  ");
                patientID = scanner.nextInt();

                if (patientID <= 0 || (!getPatientByID(patientID))) {
                    System.out.println("\n-----------------------------");
                    System.out.println("Patient ID " + patientID + " does not exist.");
                    System.out.println("-----------------------------\n");
                    patientID = -1;
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid Patient ID.\n");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        String deleteAppointmentCmd = "DELETE FROM APPOINTMENTS WHERE PatientID = ?";
        String deletePatientCmd = "DELETE FROM PATIENTS WHERE ID = ?";
        String patientCheck = "SELECT * FROM PATIENTS WHERE ID = ? ";

        System.out.println();
        System.out.print("Are you sure to proceed ? (y/n) : ");
        String input = scanner.next();

        if (input.toUpperCase().equals("Y")) {

            try {
                PreparedStatement deleteAppointmentStmt = connection.prepareStatement(deleteAppointmentCmd);
                PreparedStatement deletePatientStmt = connection.prepareStatement(deletePatientCmd);
                PreparedStatement patientCheckStmt = connection.prepareStatement(patientCheck);

                connection.setAutoCommit(false); // Start transaction


                patientCheckStmt.setInt(1, patientID);
                ResultSet rs = patientCheckStmt.executeQuery();

                if (rs.next()) {
                    // if Patient exists, proceed with deletions
                    deleteAppointmentStmt.setInt(1, patientID);
                    deleteAppointmentStmt.addBatch();

                    deletePatientStmt.setInt(1, patientID);
                    deletePatientStmt.addBatch();

                    // Execute batch
                    deleteAppointmentStmt.executeBatch();
                    deletePatientStmt.executeBatch();

                    connection.commit();

                    System.out.println("\n-------------------------------");
                    System.out.println(" Patient Discharge Successful.");
                    System.out.println("-------------------------------\n");

                } 

            } catch (SQLException e) {
                try {
                    connection.rollback();
                    // Rollback transaction on error
                    System.out.println("\n---------------------------");
                    System.out.println(" Patient Discharge Failed.");
                    System.out.println("---------------------------\n");
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public static boolean containsNumber(String input)
    {

        int len = input.length();

        for(int i = 0 ; i < len; i ++)
        {
            char character = input.charAt(i);
            if(Character.isDigit(character))
            {
                
                return true;
            }
        }
        return false;
    }    


}
