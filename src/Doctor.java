import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Doctor
{
    Connection connection;

    public Doctor(Connection connection) 
    {
        this.connection = connection;
    }


    public boolean getDoctorById(int id)
    {
        try
        {
            String cmd = "SELECT * FROM DOCTORS WHERE ID = ? ";

            PreparedStatement statement = connection.prepareStatement(cmd);
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next())
            {
                return true;
            }

        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void viewDoctorDetails()
    {
        String cmd = "SELECT * FROM DOCTORS";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(cmd);

            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("Doctors : ");
            System.out.println("+---------------+--------------------+--------------------+");
            System.out.println("| Doctor ID     | Name               | Specialization     |");
            System.out.println("+---------------+--------------------+--------------------+");

            while(resultSet.next())
            {
                int id = resultSet.getInt("ID");
                String name = resultSet.getString("Name");
                String specialization = resultSet.getString("Specialization");
                System.out.printf("| %-13s | %-18s | %-18s |\n", id,name,specialization);
                System.out.println("+---------------+--------------------+--------------------+");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        


    }

    
        
}
