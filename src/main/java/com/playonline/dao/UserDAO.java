package com.playonline.dao;

import com.playonline.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;

    }

    public String getNewCookie() throws SQLException {
        ResultSet result;
        String newCookieVal;
        String validCookieQuery = "SELECT COUNT(*) FROM user WHERE u_cookie=?";
        boolean alreadyExists;
        try (PreparedStatement validCookieStatement = connection.prepareStatement(validCookieQuery)) {
            do {
                //this generates random Strings of length 32
                UUID randomUUID = UUID.randomUUID();
                newCookieVal = randomUUID.toString();
                validCookieStatement.setString(1, newCookieVal);
                result = validCookieStatement.executeQuery();
                result.next();
                alreadyExists = result.getInt(1) >= 1;
            } while (alreadyExists);
        }
        return newCookieVal;
    }

    public User checkCredentials(String username, String password) throws SQLException {
        String checkCredentialsQuery = "SELECT u_password,u_cookie FROM user WHERE u_name=?";
        try (PreparedStatement checkCredentialsStatement = connection.prepareStatement(checkCredentialsQuery)) {
            ResultSet result;
            checkCredentialsStatement.setString(1, username);
            result = checkCredentialsStatement.executeQuery();
            if (result.next()) {
                if (result.getString(1).compareTo(password) == 0) {
                    User user = new User();
                    user.setUsername(username);
                    user.setCookie(result.getString(2));
                    return user;
                }
            }
            return null;
        }
    }

    public User checkCookie(String cookie) throws SQLException {
        String checkCookieQuery = "SELECT u_name,u_password FROM user WHERE u_cookie=?";
        try (PreparedStatement checkCookieStatement = connection.prepareStatement(checkCookieQuery)) {
            checkCookieStatement.setString(1, cookie);
            ResultSet result = checkCookieStatement.executeQuery();
            if (result.next()) {
                User user = new User();
                user.setUsername(result.getString(1));
                user.setCookie(cookie);
                return user;
            } else {
                return null;
            }
        }
    }

    public void addUser(String username, String password, String cookie) throws SQLException {
        String userAddQuery = "INSERT INTO user(u_cookie,u_name,u_password) VALUES(?,?,?)";
        try (PreparedStatement addUserStatement = connection.prepareStatement(userAddQuery)) {
            addUserStatement.setString(1, cookie);
            addUserStatement.setString(2, username);
            addUserStatement.setString(3, password);
            addUserStatement.executeUpdate();
        }
    }

    public boolean isUsernameAvailable(String username) throws SQLException {
        String checkUserAvailableQuery = "SELECT COUNT(*) FROM user WHERE u_name=?";
        try (PreparedStatement checkUserAvailableStatement = connection.prepareStatement(checkUserAvailableQuery)) {
            checkUserAvailableStatement.setString(1, username);
            ResultSet result = checkUserAvailableStatement.executeQuery();
            if (result.next()) {
                return result.getInt(1) == 0;
            } else return false;
        }
    }
}
