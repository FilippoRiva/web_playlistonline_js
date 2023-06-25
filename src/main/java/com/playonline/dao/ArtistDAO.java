package com.playonline.dao;

import com.playonline.beans.Artist;

import java.sql.*;

public class ArtistDAO {
    private final Connection connection;
    public ArtistDAO(Connection connection){
        this.connection = connection;
    }


    public Artist addArtist(String username, String arName) throws SQLException {
        //existing artist
        String getArtistByUsername = "SELECT  ar_ID, ar_name " +
                "FROM song JOIN album ON s_album_ID=a_ID " +
                "JOIN artist ON a_artist_ID=ar_ID " +
                "WHERE s_owner=?" +
                "GROUP BY ar_ID";
        try(PreparedStatement getArtistByUsernameStatement = connection.prepareStatement(getArtistByUsername)){
            getArtistByUsernameStatement.setString(1,username);
            ResultSet result = getArtistByUsernameStatement.executeQuery();
            while(result.next()){
                if(result.getString("ar_name").equals(arName)) {
                    Artist artist = new Artist();
                    artist.setName(result.getString("ar_name"));
                    artist.setID(result.getInt("ar_ID"));
                    getArtistByUsernameStatement.close();
                    return artist;
                }
            }
        }
        //new artist
        String addArtistQuery = "INSERT INTO artist(ar_name) VALUES(?)";
        try(PreparedStatement addArtistStatement = connection.prepareStatement(addArtistQuery,Statement.RETURN_GENERATED_KEYS)){
            addArtistStatement.setString(1,arName);
            addArtistStatement.executeUpdate();
            ResultSet result = addArtistStatement.getGeneratedKeys();
            result.next();
            int key = result.getInt(1);
            Artist artist = new Artist();
            artist.setName(arName);
            artist.setID(key);
            return artist;
        }
    }
}
