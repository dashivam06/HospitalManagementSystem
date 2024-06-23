import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Invoice {
    private Connection connection;
    private Scanner scanner;

    public Invoice(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void makePayment() {
        String cmd = "INSERT INTO INVOICE (AppointmentID, PaymentMethod, Amount)" +
                "VALUES( ?, ? , ? )";

        System.out.println();

        System.out.print("Enter Appointment ID : ");
        int appointmentID = scanner.nextInt();
        System.out.println("Enter PaymentMethod : ");
        String paymentMode = scanner.next();
        System.out.println("Enter the amount : ");
        double amount = scanner.nextDouble();

        try {
            PreparedStatement makePaymentStatement = connection.prepareStatement(cmd);

            makePaymentStatement.setInt(1, appointmentID);
            makePaymentStatement.setString(2, paymentMode);
            makePaymentStatement.setDouble(3, amount);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getTotalPaymentAmt(int id) {
        Integer PHYSICIAN_FEES = 2000;
        Integer SURGEON_FEES = 5000;
        Integer ORTHODONTISTS_FEES = 2000;
        String cmd = "SELECT Specialization FROM DOCTORS WHERE ID = ? ";
        int totalPayment = 0;

        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(cmd);
            preparedStatement.setInt(1, id);
            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {

                String specialization = result.getString("Specialization");

                switch (specialization) {
                    case "Physician":
                        totalPayment = PHYSICIAN_FEES;
                        break;
                    case "Surgeon":
                        totalPayment = SURGEON_FEES;
                        break;
                    case "Orthodontist":
                        totalPayment = ORTHODONTISTS_FEES;
                        break;
                    default:
                        System.out.println("Unknown Specialization");
                        break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalPayment;
    }



    public ResultSet getDetails( int appointmentID )
    {
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
            INVOICE.Amount
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

        ResultSet resultSet = null ;

        try {
            PreparedStatement statement = connection.prepareStatement(detailsCmd);
            statement.setInt(1, appointmentID);
            resultSet = statement.executeQuery();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
        
    }


    public static void printInvoice(String invoiceID, String paymentMode, double totalAmount , String patientID, String patientName, String patientAge,String patientGender, String doctorName, String testName, String specialization,String appointmentDate,String transactionDate,int qty,int discountPercentage)
    {
        String amtInString = String.valueOf(totalAmount);
        String sumTotalinString = String.valueOf(qty*totalAmount);
        String qtyString = String.valueOf(qty);
        String discountInString = String.valueOf(discountPercentage * totalAmount/100);
        NumberToWords toWords = new NumberToWords();
        String numInWords = NumberToWords.convert((int)totalAmount);
        double totalAmtAfterDis = totalAmount - Double.parseDouble(discountInString);
        String totalAmtAfterDisinStr = String.valueOf(totalAmtAfterDis);

        System.out.println("+----------------------------------------------------------------------------------------------------+");
        System.out.println("|                                                                                                    |");
        System.out.println("|                                        KATHMANDU NEURO HOSPITAL                                    |");
        System.out.println("|                                    Jawalakhel, Lalitpur, Kathmandu                                 | ");
        System.out.println("| INVOICE ID : "+formatData(invoiceID, 4)+"                                                                                  |");
        System.out.println("|                                                                                                    |");
        System.out.println("| Tel  : 01-5432431                          BILL INVOICE                              PAN 592931047 |");
        System.out.println("|____________________________________________________________________________________________________|");
        System.out.println("|                                                                                                    | ");
        System.out.println("| Patient ID : "+formatData(patientID, 4)+"                                                                                  | ");
        System.out.println("| Name : "+formatData(patientName, 19)+"                                          Transaction Date : "+formatData(transactionDate, 10)+"  | ");
        System.out.println("| Age/Sex : "+formatData(patientAge, 2)+"Y/"+formatData(patientGender, 6)+"                                                Appointment Date : "+formatData(appointmentDate, 10)+"  | ");
        System.out.println("| Designated Doctor : Dr."+formatData(doctorName + " ( " +specialization+" )", 15)+"                                                     | ");
        System.out.println("|                                                                                                    | ");
        System.out.println("|----------------------------------------------------------------------------------------------------|");
        System.out.println("|  SN Test                                           Rate            Qty            Amount           | ");
        System.out.println("|----------------------------------------------------------------------------------------------------|");
        System.out.println("|  1  "+formatData(testName, 22)+"                       "+formatData(amtInString, 9)+ "          "+formatData(qtyString, 3)+"           "+formatData(sumTotalinString, 9)+"        |");
        System.out.println("|                                                                                                    | ");
        System.out.println("|----------------------------------------------------------------------------------------------------|");
        System.out.println("|                                                                  Gross Amount :  "+formatData(amtInString, 9)+"         | ");
        System.out.println("| Payment Mode : "+formatData(paymentMode, 7)+"                                               Discount :  "+formatData(discountInString, 8)+"          | ");
        System.out.println("|                                                                  Total Amount :  "+formatData(totalAmtAfterDisinStr, 9)+"         | ");
        System.out.println("| In Words,                                                                                          | ");
        System.out.println("| "+formatData(numInWords, 60)+"                                       | ");
        System.out.println("|                                                                                                    | ");
        System.out.println("+----------------------------------------------------------------------------------------------------+");




    } 


    public void extractData()
    {
        try{
        ResultSet result = getDetails( 3 );
        
        if(result.next())
{
     
        String patientID = result.getString("PatientID");
        String patientName = result.getString("PatientName");
        String patientAge = result.getString("Age");
        String patientGender = result.getString("Gender");
        
        String doctorName = result.getString("DoctorName");
        String doctorSpecialization = result.getString("Specialization");

        String appointmentID = result.getString("AppointmentID");
        String appointmentDate = result.getString("AppointmentDate");

        String invoiceID = result.getString("InvoiceID");
        String paymentMethod = result.getString("PaymentMethod");

        String transactionDate = result.getString("TransactionDate");

        double amt = result.getDouble("Amount");


        printInvoice(invoiceID, paymentMethod, amt, patientID, patientName, patientAge,patientGender, doctorName, patientName,doctorSpecialization, appointmentDate,transactionDate,1,5);
}else{
    System.out.println("No entries found..");
}

        }catch(SQLException e)
        {
            e.printStackTrace();
        }

        
    }

    public static String formatData(String inputString , int desiredLength)
    {
        int length = inputString.trim().length();

        while(length<=desiredLength-1)
        {
            inputString += " ";
            length  = inputString.length();
        }

        return inputString;
    }
    

    
}
