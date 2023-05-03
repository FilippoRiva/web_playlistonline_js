package com.playonline.dao;

import com.playonline.beans.Artist;

import java.sql.*;

public class ArtistDAO {
   PreparedStatement addArtistStatement;
   PreparedStatement getArtistByUsernameStatement;
    public ArtistDAO(Connection connection) throws SQLException {
        String addArtistQuery = "INSERT INTO artist(ar_name) VALUES(?)";
        String getArtistByUsername = "SELECT  ar_ID, ar_name " +
                "FROM song JOIN album ON s_album_ID=a_ID " +
                "JOIN artist ON a_artist_ID=ar_ID " +
                "WHERE s_owner=?" +
                "GROUP BY ar_ID";
        addArtistStatement = connection.prepareStatement(addArtistQuery,Statement.RETURN_GENERATED_KEYS);
        getArtistByUsernameStatement = connection.prepareStatement(getArtistByUsername);
    }


    public Artist addArtist(String username, String arName) throws SQLException {
        //existing artist
        getArtistByUsernameStatement.setString(1,username);
        ResultSet result = getArtistByUsernameStatement.executeQuery();
        while(result.next()){
            if(result.getString("ar_name").equals(arName)) {
                Artist artist = new Artist();
                artist.setName(result.getString("ar_name"));
                artist.setID(result.getInt("ar_ID"));
                return artist;
            }
        }
        //new artist
        addArtistStatement.setString(1,arName);
        addArtistStatement.executeUpdate();
        result = addArtistStatement.getGeneratedKeys();
        result.next();
        int key = result.getInt(1);
        Artist artist = new Artist();
        artist.setName(arName);
        artist.setID(key);
        return artist;
    }
}
