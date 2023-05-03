package com.playonline.dao;

import com.playonline.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserDAO {
    private final PreparedStatement checkCredentialsStatement;
    private final PreparedStatement checkCookieStatement;
    private final PreparedStatement addUserStatement;
    private final PreparedStatement checkUserAvailableStatement;
    private final PreparedStatement validCookieStatement;
    public UserDAO(Connection connection) throws SQLException{
        String checkCredentialsQuery="SELECT u_password,u_cookie FROM user WHERE u_name=?";
        String userAddQuery = "INSERT INTO user(u_cookie,u_name,u_password) VALUES(?,?,?)";
        String checkUserAvailableQuery = "SELECT COUNT(*) FROM user WHERE u_name=?";

        String validCookieQuery = "SELECT COUNT(*) FROM user WHERE u_cookie=?";
        String checkCookieQuery = "SELECT u_name,u_password FROM user WHERE u_cookie=?";

        checkCredentialsStatement = connection.prepareStatement(checkCredentialsQuery);
        addUserStatement = connection.prepareStatement(userAddQuery);
        checkUserAvailableStatement= connection.prepareStatement(checkUserAvailableQuery);

        validCookieStatement = connection.prepareStatement(validCookieQuery);
        checkCookieStatement = connection.prepareStatement(checkCookieQuery);
    }

    public String getNewCookie() throws SQLException{
        ResultSet result;
        String newCookieVal;
        boolean alreadyExists;
        do {
            //this generates Strings of length 32
            UUID randomUUID = UUID.randomUUID();
            newCookieVal = randomUUID.toString();
            validCookieStatement.setString(1, newCookieVal);
            result = validCookieStatement.executeQuery();
            result.next();
            alreadyExists = result.getInt(1) >= 1;
        } while (alreadyExists);
        return newCookieVal;
    }

    public User checkCredentials(String username, String password) throws SQLException{
        ResultSet result;
        checkCredentialsStatement.setString(1,username);
        result = checkCredentialsStatement.executeQuery();
        if(result.next()){
            if(result.getString(1).compareTo(password)==0){
                User user = new User();
                user.setUsername(username);
                user.setCookie(result.getString(2));
                return user;
            }
        }
        return null;
    }

    public User checkCookie(String cookie) throws SQLException{
        checkCookieStatement.setString(1, cookie);
        ResultSet result = checkCookieStatement.executeQuery();
        if(result.next()){
            User user = new User();
            user.setUsername(result.getString(1));
            user.setCookie(cookie);
            return user;
        } else {
            return null;
        }
    }
    
    public void addUser(String username,String password, String cookie) throws SQLException{
        addUserStatement.setString(1,cookie);
        addUserStatement.setString(2,username);
        addUserStatement.setString(3,password);
        addUserStatement.executeUpdate();
    }

    public boolean isUsernameAvailable(String username) throws SQLException{
        checkUserAvailableStatement.setString(1,username);
        ResultSet result = checkUserAvailableStatement.executeQuery();
        if(result.next()){
            return result.getInt(1)==0;
        } else return false;
    }
}
