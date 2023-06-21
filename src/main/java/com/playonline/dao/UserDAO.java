package com.playonline.dao;

import com.playonline.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserDAO {
    private final Connection connection;
    public UserDAO(Connection connection){
        this.connection = connection;

    }

    public String getNewCookie() throws SQLException{
        String validCookieQuery = "SELECT COUNT(*) FROM user WHERE u_cookie=?";
        PreparedStatement validCookieStatement = connection.prepareStatement(validCookieQuery);

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
        validCookieStatement.close();
        return newCookieVal;
    }

    public User checkCredentials(String username, String password) throws SQLException{
        String checkCredentialsQuery="SELECT u_password,u_cookie FROM user WHERE u_name=?";
        PreparedStatement checkCredentialsStatement = connection.prepareStatement(checkCredentialsQuery);

        ResultSet result;
        checkCredentialsStatement.setString(1,username);
        result = checkCredentialsStatement.executeQuery();
        if(result.next()){
            if(result.getString(1).compareTo(password)==0){
                User user = new User();
                user.setUsername(username);
                user.setCookie(result.getString(2));
                checkCredentialsStatement.close();
                return user;
            }
        }
        checkCredentialsStatement.close();
        return null;
    }

    public User checkCookie(String cookie) throws SQLException{
        String checkCookieQuery = "SELECT u_name,u_password FROM user WHERE u_cookie=?";
        PreparedStatement checkCookieStatement = connection.prepareStatement(checkCookieQuery);

        checkCookieStatement.setString(1, cookie);
        ResultSet result = checkCookieStatement.executeQuery();
        if(result.next()){
            User user = new User();
            user.setUsername(result.getString(1));
            user.setCookie(cookie);
            checkCookieStatement.close();
            return user;
        } else {
            checkCookieStatement.close();
            return null;
        }
    }
    
    public void addUser(String username,String password, String cookie) throws SQLException{
        String userAddQuery = "INSERT INTO user(u_cookie,u_name,u_password) VALUES(?,?,?)";
        PreparedStatement addUserStatement = connection.prepareStatement(userAddQuery);

        addUserStatement.setString(1,cookie);
        addUserStatement.setString(2,username);
        addUserStatement.setString(3,password);
        addUserStatement.executeUpdate();

        addUserStatement.close();
    }

    public boolean isUsernameAvailable(String username) throws SQLException{
        String checkUserAvailableQuery = "SELECT COUNT(*) FROM user WHERE u_name=?";
        PreparedStatement checkUserAvailableStatement= connection.prepareStatement(checkUserAvailableQuery);
        checkUserAvailableStatement.setString(1,username);
        ResultSet result = checkUserAvailableStatement.executeQuery();
        if(result.next()){
            boolean res = result.getInt(1) == 0;
            checkUserAvailableStatement.close();
            return res;
        } else {
            checkUserAvailableStatement.close();
            return false;
        }
    }
}
