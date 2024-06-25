import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static Connection connection = null;
    private static Scanner scanner = null;
    private static final String url = "jdbc:postgresql://localhost:5432/hospitalmanagementsystem";
    private static final String id = "postgres";
    private static final String password = "root";

    public static void main(String[] args) {
        mainMenu();
    }

    /*
     * Displays the main menu for the Hospital Management System and handles user
     * input.
     * This method sets up the JDBC connection to the PostgreSQL database,
     * initializes the necessary objects for managing patients, doctors, and
     * invoices, and
     * enters a loop to display the menu and process user choices.
     */

    public static void mainMenu() {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {

            connection = DriverManager.getConnection(url, id, password);
            scanner = new Scanner(System.in);

            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);

            Invoice invoice = new Invoice(connection, scanner);
            boolean loop = true;

            try {

                while (loop) {
                    System.out.println("\n+" + "-".repeat(28) + "+");
                    System.out.println("| HOSPITAL MANAGEMENT SYSTEM |");
                    System.out.println("+" + "-".repeat(28) + "+");
                    System.out.println(" 1. Add Patient");
                    System.out.println(" 2. View Patient");
                    System.out.println(" 3. Discharge Patient");
                    System.out.println(" 4. View Doctor");
                    System.out.println(" 5. Book Appointment");
                    System.out.println(" 6. View Appointment");
                    System.out.println(" 7. Cancel Appointment");
                    System.out.println(" 8. Fund Settement");
                    System.out.println(" 9. Exit");
                    System.out.print("\nEnter Your Choice : ");

                    int choice = scanner.nextInt();
                    System.out.println("\n");

                    switch (choice) {
                        case 1:
                            patient.addPatient();
                            System.out.println();
                            break;
                        case 2:
                            patient.viewPatients();
                            System.out.println();
                            break;
                        case 3:
                            patient.dischargePatient();
                            System.out.println();
                            break;
                        case 4:
                            doctor.viewDoctorDetails();
                            System.out.println();
                            break;
                        case 5:
                            bookAppointments(patient, doctor);
                            System.out.println();
                            break;
                        case 6:
                            viewAppointments();
                            System.out.println();
                            break;
                        case 7:
                            cancelAppointment();
                            System.out.println();
                            break;
                        case 8:
                            invoice.makePayment();
                            System.out.println();
                            break;
                        case 9:
                            System.out.println("---------------------------------");
                            System.out.println(" Thankyou for using our service. ");
                            System.out.println("---------------------------------\n");
                            loop = false;
                            break;
                        default:
                            System.out.println("Please enter valid option!!\n");
                            break;
                    }

                }
            } catch (InputMismatchException f) {
                System.out.println("\nInvalid input. Please choose a valid option.\n");
                mainMenu();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Books an appointment for a patient with a doctor on a specified date.
     *
     * @param patient The patient object for whom the appointment is being booked.
     * @param doctor  The doctor object with whom the appointment is being booked.
     */

    public static void bookAppointments(Patient patient, Doctor doctor) {

        int patientID = -1;
        int doctorID = -1;
        String date = null;

        // Input Patient ID
        while (patientID == -1) {
            try {
                System.out.print("Enter Patient's ID :  ");
                patientID = scanner.nextInt();

                if (patientID <= 0 || (!patient.getPatientByID(patientID))) {
                    System.out.println("\n------------------------------------");
                    System.out.println("Patient id cannot be verified.");
                    System.out.println("No entries found for patient id " + patientID);
                    System.out.println("------------------------------------\n");
                    patientID = -1;
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid Patient ID.\n");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        // Input Doctor ID
        while (doctorID == -1) {
            try {
                System.out.print("Enter Doctor's ID :  ");
                doctorID = scanner.nextInt();
                if (doctorID <= 0 || (!doctor.getDoctorById(doctorID))) {
                    System.out.println("\n------------------------------------");
                    System.out.println("Doctor id cannot be verified.");
                    System.out.println("No entries found for doctor id " + patientID);
                    System.out.println("------------------------------------\n");
                    doctorID = -1; // Reset doctorID to continue the loop
                }
            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid Doctor ID.\n");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        // Input Appointment Date
        while (date == null) {
            try {
                System.out.print("Enter appointment date (YYYY-MM-DD) :  ");
                date = scanner.next();
                DateTimeFormatter ex = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                ex.parse(date);

            } catch (DateTimeParseException e) {
                System.out.println("\n-----------------------------------------");
                System.out.println("Invalid input. \nPlease enter the date in YYYY-MM-DD format.");
                System.out.println("-------------------------------------------\n");
                scanner.nextLine();
                date = null;
            }
        }

        String cmd = "INSERT INTO APPOINTMENTS(PatientID, DoctorID,AppointmentDate)" +
                "VALUES ( ?, ?, ?)";
        try {
            PreparedStatement makeAppointmentStatement = connection.prepareStatement(cmd);

            if (checkDoctorAvailablity(doctorID, date)) {
                makeAppointmentStatement.setInt(1, patientID);
                makeAppointmentStatement.setInt(2, doctorID);
                makeAppointmentStatement.setDate(3, parseDate(date));
                makeAppointmentStatement.executeUpdate();
                System.out.println("\n" + "----------------------");
                System.out.println(" Appointment Booked!!");
                System.out.println("----------------------");
            } else {
                System.out.println("\n" + "-------------------------------------");
                System.out.println(" Failed to make appointment.");
                System.out.println(" Doctor not available on this date.");
                System.out.println("-------------------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException f) {
            f.printStackTrace();
        }

    }

    /**
     * Checks the availability of a doctor on a specific date.
     *
     * @param doctorID        The ID of the doctor whose availability is being
     *                        checked.
     * @param appointmentDate The date for which the doctor's availability is being
     *                        checked.
     * @return true if the doctor is available on the given date, false otherwise.
     */

    public static boolean checkDoctorAvailablity(int doctorID, String appointmentDate) {

        String checkAvailabilityCmd = "SELECT * FROM APPOINTMENTS WHERE DoctorID = ? AND AppointmentDate = ? ";

        try {

            PreparedStatement checkAvailabilitystatement = connection.prepareStatement(checkAvailabilityCmd);

            checkAvailabilitystatement.setInt(1, doctorID);
            checkAvailabilitystatement.setDate(2, parseDate(appointmentDate));

            ResultSet resultSet = checkAvailabilitystatement.executeQuery();

            if (resultSet.next()) {
                return false;
            }

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }

        return true;

    }

    /*
     * Displays a list of all scheduled appointments from the database.
     * This method retrieves appointment details including appointment ID,
     * patient name, doctor name, and appointment date by joining the
     * APPOINTMENTS, PATIENTS, and DOCTORS tables. The retrieved data
     * is then formatted and printed in a tabular format.
     *
     */
    private static void viewAppointments() {
        String checkAppointmentCmd = "SELECT " +
                "APPOINTMENTS.ID AS AppointmentID, " +
                "PATIENTS.Name AS PatientName, " +
                "DOCTORS.Name AS DoctorName, " +
                "APPOINTMENTS.AppointmentDate " +

                "FROM " +
                "    APPOINTMENTS " +
                "JOIN " +
                "    PATIENTS ON APPOINTMENTS.PatientID = PATIENTS.ID " +
                "JOIN " +
                "    DOCTORS ON APPOINTMENTS.DoctorID = DOCTORS.ID ";

        try {
            PreparedStatement statement = connection.prepareStatement(checkAppointmentCmd);
            ResultSet result = statement.executeQuery();
            System.out.println("Appointments :");
            System.out.println("+----------+-------------------------+-------------------------+--------------------+");
            System.out.println("|  ID      | Patient Name            | Doctor Name             | Appointment Date   |");
            System.out.println("+----------+-------------------------+-------------------------+--------------------+");

            while (result.next()) {
                String appointmentID = result.getString("AppointmentID");
                String patientName = result.getString("PatientName");
                String doctorName = result.getString("DoctorName");
                String appointmentDate = result.getString("AppointmentDate");

                System.out.printf("| %-9s| %-24s| %-24s| %-19s|\n", appointmentID, patientName, doctorName,
                        appointmentDate);
                System.out.println(
                        "+----------+-------------------------+-------------------------+--------------------+");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /*
     * Cancels an appointment and deletes it from the database. If the appointment
     * is associated
     * with an invoice, it also handles the payment refund by deleting the
     * corresponding invoice.
     *
     * This method prompts the user for confirmation before proceeding with the
     * deletion.
     * It checks the number of appointments linked to the invoice and performs the
     * deletion
     * based on this count
     */
    public static void cancelAppointment() {

        int appointmentId = -1;

        while (appointmentId == -1) {

            try {

                System.out.print("Enter Appointment ID : ");
                appointmentId = scanner.nextInt();

            } catch (InputMismatchException e) {
                System.out.println("\nInvalid input. Please enter a valid appointment id.\n");
                scanner.nextLine();
                appointmentId = -1;
            }
        }
        if (Invoice.checkForAppointments(appointmentId) == 0) {
            System.out.println("\n---------------------------------------");
            System.out.println("Appointment id cannot be verified.");
            System.out.println("No entries found for appointment id " + appointmentId);
            System.out.println("-----------------------------------------\n");
            cancelAppointment();
        }

        int count = Invoice.checkForAppointments(appointmentId);
        String delFromAppointmentStr = "DELETE FROM APPOINTMENTS WHERE ID = ? ";
        String delFromInvoiceStr = "DELETE FROM INVOICE WHERE APPOINTMENTID = ? ";

        System.out.print("Are you sure to proceed ? (y/n) : ");
        String input = scanner.next();

        if (input.trim().toUpperCase().equals("Y")) {

            if (count == 2) {
                try {
                    PreparedStatement delFromInvoiceStatement = connection.prepareStatement(delFromInvoiceStr);
                    delFromInvoiceStatement.setInt(1, appointmentId);
                    delFromInvoiceStatement.executeUpdate();

                    System.out.println("\n---------------------------------------------");
                    System.out.println(" Appointment cancelled and payment refunded.");
                    System.out.println("---------------------------------------------");
                    return;

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (count == 1) {
                try {
                    PreparedStatement delFromAppointmentStatement = connection.prepareStatement(delFromAppointmentStr);
                    delFromAppointmentStatement.setInt(1, appointmentId);
                    delFromAppointmentStatement.executeUpdate();

                    System.out.println("\n------------------------");
                    System.out.println(" Appointment cancelled .");
                    System.out.println("------------------------");
                    return;

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println();
    }

    /**
     * Parses a string representation of a date into a java.sql.Date object.
     *
     * @param dateString The date string in "yyyy-MM-dd" format.
     * @return A java.sql.Date object representing the parsed date.
     * @throws ParseException If the input string cannot be parsed into a date.
     */

    // Note : MySQL doesn't require parsing String to Date, but PostgreSQL does.
    private static Date parseDate(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate = dateFormat.parse(dateString);
        return new Date(parsedDate.getTime());
    }
}