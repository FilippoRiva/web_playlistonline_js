package com.playonline.dao;

import com.playonline.beans.Album;
import com.playonline.beans.Artist;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlbumDAO {
    PreparedStatement addAlbumStatement;
    PreparedStatement getAlbumByIDStatement;
    PreparedStatement getUserAlbumsStatement;
    String folder;
    public AlbumDAO(Connection connection, String folder) throws SQLException{
        this.folder = folder;
        String addAlbumQuery = "INSERT INTO album(a_name,a_artist_ID,a_date) VALUES (?,?,?)";
        String getAlbumByIDQuery = "SELECT a_date, a_name, ar_name, ar_ID FROM album JOIN artist ON a_artist_ID=ar_ID " +
                "JOIN song ON s_album_ID=a_ID JOIN user ON u_name=? WHERE a_ID=? ";
        String getUserAlbums = "SELECT a_ID, a_name, a_date, ar_ID, ar_name " +
                "FROM song JOIN album ON s_album_ID=a_ID JOIN artist ON a_artist_ID=ar_ID " +
                "WHERE s_owner = ? " +
                "GROUP BY a_ID, ar_name, a_name "+
                "ORDER BY ar_name, a_name ";
        addAlbumStatement = connection.prepareStatement(addAlbumQuery, Statement.RETURN_GENERATED_KEYS);
        getAlbumByIDStatement = connection.prepareStatement(getAlbumByIDQuery);
        getUserAlbumsStatement = connection.prepareStatement(getUserAlbums);
    }

    public List<Album> getUserAlbums(String username) throws SQLException {
        getUserAlbumsStatement.setString(1,username);
        ResultSet result = getUserAlbumsStatement.executeQuery();
        List<Album> albums = new ArrayList<>();
        while(result.next()){
            Album album = new Album();
            album.setId(result.getInt("a_ID"));
            album.setName(result.getString("a_name"));
            album.setDate(new Date(result.getDate("a_date").getTime()));
            Artist artist = new Artist();
            artist.setID(result.getInt("ar_ID"));
            artist.setName(result.getString("ar_name"));
            album.setArtist(artist);
            albums.add(album);
        }
        return albums;
    }

    public Album getAlbum(String username, int albumID) throws SQLException, InvalidParameterException{
        getAlbumByIDStatement.setString(1,username);
        getAlbumByIDStatement.setInt(2,albumID);
        ResultSet result = getAlbumByIDStatement.executeQuery();
        if(!result.next()) {
            throw new InvalidParameterException();
        }
        // java bean creation
        Album album = new Album();
        album.setName(result.getString("a_name"));
        Artist artist = new Artist();
        artist.setName(result.getString("ar_name"));
        artist.setID(result.getInt("ar_ID"));
        album.setArtist(artist);
        album.setDate(new Date(result.getDate("a_date").getTime()));
        album.setId(albumID);
        return album;
    }

    public Album addAlbum(String albumName, Artist artist, Date date, Part cover,String username) throws SQLException{
        // db insert
        addAlbumStatement.setString(1,albumName);
        addAlbumStatement.setInt(2,artist.getID());
        addAlbumStatement.setDate(3,new java.sql.Date(date.getTime()));
        addAlbumStatement.executeUpdate();
        ResultSet keys = addAlbumStatement.getGeneratedKeys();
        keys.next();
        int currentID = keys.getInt(1);
        // path generation
        String dir = folder+username+"/"+artist.getID()+"/"+currentID+"/";
        String filePath = dir+"cover";
        // saving cover image
        try {
            File f = new File(dir);
            if(!f.exists() && !f.mkdirs()){
                throw new SQLException("unable to create dirs");
            }
            f = new File(filePath);
            cover.write(f.getAbsolutePath());
        } catch (IOException e) {
            throw new SQLException(e);
        }
        // java bean creation
        Album album = new Album();
        album.setName(albumName);
        album.setArtist(artist);
        album.setDate(date);
        album.setId(currentID);
        return album;
    }

    public void deleteAlbumCover(String username, Album album) {
        if(album==null){
            return;
        }
        int artistID = album.getArtist().getID();
        int albumID = album.getId();
        String dir = folder+username+"/"+artistID+"/"+albumID+"/";
        File f = new File(dir);
        File[] files = f.listFiles();
        if(files == null || files.length<=1) {
            f.delete();
        } else {
            for(File file : files){
                if(file.getName().substring(0,file.getName().lastIndexOf(".")-1).equals("cover")){
                    file.delete();
                    return;
                }
            }
        }
    }
}
