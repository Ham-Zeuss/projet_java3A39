package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    private String url="jdbc:mysql://localhost:3306/main";
    private String username="root";
    private String password="";
    private Connection connection;
    private static DataSource instance;


    private DataSource(){
        try {
            connection= DriverManager.getConnection(url,username,password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataSource getInstance(){
        if(instance==null)
            instance=new DataSource();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
