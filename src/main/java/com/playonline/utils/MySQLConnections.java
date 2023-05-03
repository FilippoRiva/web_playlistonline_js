package com.playonline.utils;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnections {
    public static Connection getConnection(ServletContext context){
        String driver = context.getInitParameter("dbDriver");
        String url = context.getInitParameter("dbUrl");
        String user = context.getInitParameter("dbUser");
        String password = context.getInitParameter("dbPassword");
        try {
            Class.forName(driver); //class instantiation given a name
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("There's no JDBC driver class in this application. " +
                    "Please install the correct library or correct the web.xml dbDriver " +
                    "context parameter");
        }
        Connection connection;
        try {
            connection = DriverManager.getConnection(url,user,password);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to "+ url);
        }
        return connection;
    }
}
