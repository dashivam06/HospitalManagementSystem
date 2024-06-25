import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Invoice {
    private static Connection connection;
    private static Scanner scanner;
    static int qty = 1;
    static double discountPercentage = 5;

    public Invoice(Connection con, Scanner scan) {
        connection = con;
        scanner = scan;
    }

    public  void makePayment() {
        String cmd = "INSERT INTO INVOICE (AppointmentID, PaymentMethod, Amount, Discount)" +
                "VALUES( ?, ? , ? , ? )";

        System.out.println();

        System.out.print("Enter Appointment ID : ");

        int appointmentID =  -1;

        while(appointmentID ==  -1){
        try{
            appointmentID = scanner.nextInt();
        }
        catch(InputMismatchException f )
        {
            System.out.println("\nInvalid input. Please enter a valid Doctor ID.\n");
            appointmentID = -1;
        }
    }

        if (checkForAppointments(appointmentID) == 2) {
            System.out.println("\n---------------------------------------------");
            System.out.println(" Payment already made for Appointment ID : " + appointmentID);
            System.out.println("---------------------------------------------");
            return;
        } else if (checkForAppointments(appointmentID) == 0) {
            System.out.println("\n---------------------------------------------");
            System.out.println(
                    " Please provide a valid appointment id.\n Appointment Id ( " + appointmentID + " ) not found.");
            System.out.println("---------------------------------------------");
            return;
        } else if (checkForAppointments(appointmentID) == 1) {

            String paymentMode = null ;
            while(paymentMode == null)
            {
                try{
            System.out.print("Enter PaymentMethod : ");
            paymentMode = scanner.next();
                }catch(InputMismatchException f)
                {
                    System.out.println("\nInvalid input. Please enter a valid payment mode.\n");
                    paymentMode = null;
                }
            }
            List<Double> paymentDetail = getTotalPaymentAmt(appointmentID, discountPercentage, qty);
            System.out.println(paymentDetail);
            double amount = paymentDetail.get(3);
            double discount = paymentDetail.get(2);

            try {
                PreparedStatement makePaymentStatement = connection.prepareStatement(cmd);

                makePaymentStatement.setInt(1, appointmentID);
                makePaymentStatement.setString(2, paymentMode);
                makePaymentStatement.setDouble(3, amount);
                makePaymentStatement.setDouble(4, discount);

                makePaymentStatement.executeUpdate();

                System.out.println();

                extractData(appointmentID);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static List<Double> getTotalPaymentAmt(int appointmentid, double discountPercentage, int qty) {
        Integer PHYSICIAN_FEES = 3000;
        Integer SURGEON_FEES = 8000;
        Integer ORTHODONTISTS_FEES = 5000;
        String cmd = "SELECT DOCTORS.Specialization " +
                "FROM APPOINTMENTS " +
                "JOIN DOCTORS ON APPOINTMENTS.DoctorID = DOCTORS.ID " +
                "WHERE APPOINTMENTS.ID = ? ";
        double totalPayment = 0;
        double rate = 0;
        double discount = 0;

        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(cmd);
            preparedStatement.setInt(1, appointmentid);
            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {

                String specialization = result.getString("Specialization");

                if (specialization.equals("Physician")) {
                    rate = PHYSICIAN_FEES;
                } else if (specialization.equals("Surgeon")) {
                    rate = SURGEON_FEES;
                } else if (specialization.equals("Orthodontists")) {
                    rate = ORTHODONTISTS_FEES;
                } else {
                    System.out.println("Specialization not registered.");
                }
            } else {
                System.out.println("No column retrived");
            }

            totalPayment = rate * qty;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        discount = discountPercentage * totalPayment / 100;

        totalPayment = totalPayment - discount;

        ArrayList<Double> output = new ArrayList<>();
        output.add(rate);
        output.add((double) qty);
        output.add(discount);
        output.add(totalPayment);


        return output;
    }

    public static ResultSet getDetails(int appointmentID) {
        String detailsCmd = """
                SELECT
                    PATIENTS.ID AS PatientID,
                    PATIENTS.Name AS PatientName,
                    PATIENTS.Age,
                    PATIENTS.Gender,
                    INVOICE.InvoiceID,
                    INVOICE.PaymentMethod,
                    INVOICE.TransactionDate,
                    DOCTORS.Name AS DoctorName,
                    DOCTORS.Specialization,
                    APPOINTMENTS.ID AS AppointmentID,
                    APPOINTMENTS.AppointmentDate,
                    INVOICE.Amount,
                    Invoice.Discount
                FROM
                    PATIENTS PATIENTS
                JOIN
                    APPOINTMENTS APPOINTMENTS ON PATIENTS.ID = APPOINTMENTS.PatientID
                JOIN
                    DOCTORS DOCTORS ON APPOINTMENTS.DoctorID = DOCTORS.ID
                JOIN
                    INVOICE INVOICE ON APPOINTMENTS.ID = INVOICE.AppointmentID
                WHERE
                    APPOINTMENTS.ID = ?
                """;

        ResultSet resultSet = null;

        try {
            PreparedStatement statement = connection.prepareStatement(detailsCmd);
            statement.setInt(1, appointmentID);
            resultSet = statement.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;

    }

    public static void printInvoice(String invoiceID, String paymentMode, String patientID,
            String patientName, String patientAge, String patientGender, String doctorName, String specialization,
            String appointmentDate, String transactionDate, List<Double> paymentDetail) {


        Double totalAmount = paymentDetail.get(3);
        Double rate = paymentDetail.get(0);
        String amtInString = String.valueOf(rate);
        String sumTotalinString = String.valueOf(qty * rate);
        String qtyString = String.valueOf(qty);
        String discountInString = String.valueOf(discountPercentage * (rate / 100));       
        String numInWords = NumberToWords.convert(totalAmount.intValue());
        double totalAmtAfterDis = Double.parseDouble(sumTotalinString) - Double.parseDouble(discountInString);
        String totalAmtAfterDisinStr = String.valueOf(totalAmtAfterDis);
        String testName = " 1 : 1  Session";


        System.out.println(
                "+----------------------------------------------------------------------------------------------------+");
        System.out.println(
                "|                                                                                                    |");
        System.out.println(
                "|                                        KATHMANDU NEURO HOSPITAL                                    |");
        System.out.println(
                "|                                    Jawalakhel, Lalitpur, Kathmandu                                 | ");
        System.out.println("| INVOICE ID : " + formatData(invoiceID, 4)
                + "                                                                                  |");
        System.out.println(
                "|                                                                                                    |");
        System.out.println(
                "| Tel  : 01-5432431                          BILL INVOICE                              PAN 592931047 |");
        System.out.println(
                "|____________________________________________________________________________________________________|");
        System.out.println(
                "|                                                                                                    | ");
        System.out.println("| Patient ID : " + formatData(patientID, 4)
                + "                                                                                  | ");
        System.out.println("| Name : " + formatData(patientName, 19)
                + "                                          Transaction Date : " + formatData(transactionDate, 10)
                + "  | ");
        System.out.println("| Age/Sex : " + formatData(patientAge, 2) + "Y/" + formatData(patientGender, 6)
                + "                                                Appointment Date : "
                + formatData(appointmentDate, 10) + "  | ");
        System.out.println("| Designated Doctor : Dr." + formatData(doctorName + " ( " + specialization + " )", 15)
                + "                                                     | ");
        System.out.println(
                "|                                                                                                    | ");
        System.out.println(
                "|----------------------------------------------------------------------------------------------------|");
        System.out.println(
                "|  SN    Test                                        Rate            Qty            Amount           | ");
        System.out.println(
                "|----------------------------------------------------------------------------------------------------|");
        System.out.println("|  1     " + formatData(testName, 22) + "                    " + formatData(amtInString, 9)
                + "          " + formatData(qtyString, 3) + "           " + formatData(sumTotalinString, 9)
                + "        |");
        System.out.println(
                "|                                                                                                    | ");
        System.out.println(
                "|----------------------------------------------------------------------------------------------------|");
        System.out.println("|                                                                  Gross Amount :  "
                + formatData(amtInString, 9) + "         | ");
        System.out.println("| Payment Mode : " + formatData(paymentMode, 10)
                + "                                            Discount :  " + formatData(discountInString, 8)
                + "          | ");
        System.out.println("|                                                                  Total Amount :  "
                + formatData(totalAmtAfterDisinStr, 9) + "         | ");
        System.out.println(
                "| In Words,                                                                                          | ");
        System.out.println("| " + formatData(numInWords + " Only.", 60) + "                                       | ");
        System.out.println(
                "|                                                                                                    | ");
        System.out.println(
                "+----------------------------------------------------------------------------------------------------+");

    }

    public static int checkForAppointments(int appointmentId) {
        String checkAppointmentCmd = "SELECT * FROM INVOICE WHERE APPOINTMENTID = ? ";
        String checkInvoiceCmd = "SELECT * FROM APPOINTMENTS WHERE ID = ? ";
        int count = 0;
        boolean appointmentExists = false;

        // Check if the appointment exists
        try {
            PreparedStatement appointmentStatement = connection.prepareStatement(checkInvoiceCmd);
            appointmentStatement.setInt(1, appointmentId);
            ResultSet appointmentResultSet = appointmentStatement.executeQuery();

            if (appointmentResultSet.next()) {
                appointmentExists = true;
                count = 1;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Check if the appointment exists in the invoice and is paid
        if (appointmentExists) {

            try {
                PreparedStatement invoiceStatement = connection.prepareStatement(checkAppointmentCmd);
                invoiceStatement.setInt(1, appointmentId);
                ResultSet invoiceResultSet = invoiceStatement.executeQuery();

                if (invoiceResultSet.next()) {
                    count = 2;
                }
            } catch (SQLException f) {
                f.printStackTrace();
            }
        }
        return count;
    }

    public static void extractData(int appointmentid) {
        try {
            ResultSet result = getDetails(appointmentid);

            if (result.next()) {

                String patientID = result.getString("PatientID");
                String patientName = result.getString("PatientName");
                String patientAge = result.getString("Age");
                String patientGender = result.getString("Gender");

                String doctorName = result.getString("DoctorName");
                String doctorSpecialization = result.getString("Specialization");

                String appointmentDate = result.getString("AppointmentDate");

                String invoiceID = result.getString("InvoiceID");
                String paymentMethod = result.getString("PaymentMethod");
                List<Double> paymentDetail = getTotalPaymentAmt(appointmentid, discountPercentage, qty);

                String transactionDate = result.getString("TransactionDate");

                printInvoice(invoiceID, paymentMethod, patientID, patientName, patientAge, patientGender,
                        doctorName, doctorSpecialization, appointmentDate, transactionDate, paymentDetail);
            } else {
                System.out.println("No entries found for appointment id : " + appointmentid);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static String formatData(String inputString, int desiredLength) {
        int length = inputString.trim().length();

        while (length <= desiredLength - 1) {
            inputString += " ";
            length = inputString.length();
        }

        return inputString;
    }

}
