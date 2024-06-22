import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class HospitalManagementSystem {

    private static Connection connection = null;
    private static Scanner scanner = null;
    private static final String url = "jdbc:postgresql://localhost:5432/hospitalmanagementsystem";
    private static final String id = "postgres";
    private static final String password = "root";

    public static void main(String[] args) {

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

            while (true) {
                System.out.println("\n+" + "-".repeat(28) + "+");
                System.out.println("| HOSPITAL MANAGEMENT SYSTEM |");
                System.out.println("+" + "-".repeat(28) + "+");
                System.out.println(" 1. Add Patient");
                System.out.println(" 2. View Patient");
                System.out.println(" 3. View Doctor");
                System.out.println(" 4. Book Appointment");
                System.out.println(" 5. Exit");
                System.out.print("\nEnter Your Choice : ");

                int choice = scanner.nextInt();
                System.out.println();

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
                        doctor.viewDoctorDetails();
                        System.out.println();
                        break;
                    case 4:
                        bookAppointments(patient, doctor);
                        System.out.println();
                        break;
                    case 5:
                        break;
                    default:
                        System.out.println("Please enter valid option!!");
                        System.out.println();
                        break;
                }

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
        System.out.print("Enter Patient's ID :  ");
        int patientID = scanner.nextInt();
        System.out.print("Enter Doctors's ID :  ");
        int doctorID = scanner.nextInt();
        System.out.print("Enter appointment date (YYYY-MM-DD) :  ");
        String date = scanner.next();

        String cmd = "INSERT INTO APPOINTMENTS(PatientID, DoctorID,AppointmentDate)" +
                "VALUES ( ?, ?, ?)";
        try {
            PreparedStatement makeAppointmentStatement = connection.prepareStatement(cmd);

            if ((doctor.getDoctorById(doctorID)) && (patient.getPatientByID(patientID))) {
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

            } else {
                System.out.println("\n" + "--------------------------------------------------");
                System.out.println(" Provided patient id or doctor id might be wrong.");
                System.out.println("--------------------------------------------------");
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