package com.playonline.dao;

import com.playonline.beans.Album;
import com.playonline.beans.Artist;
import com.playonline.beans.Genre;
import com.playonline.beans.Song;
import com.playonline.utils.GenreEnum;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.sql.*;

public class SongDAO {

    private final Connection connection;
    private final String folder;
    public SongDAO(Connection connection, String folder){
        this.folder = folder;
        this.connection = connection;
    }

    public void addSong(String owner, String title, GenreEnum genre, Part song, Album album) throws SQLException{
        String addSongQuery =
                "INSERT INTO song (s_title, s_album_ID, s_genre, s_owner) VALUES (?,?,?,?)";
        try(PreparedStatement addSongStatement = connection.prepareStatement(addSongQuery,Statement.RETURN_GENERATED_KEYS)){
            addSongStatement.setString(1,title);
            addSongStatement.setInt(2,album.getId());
            addSongStatement.setString(3, genre.name());
            addSongStatement.setString(4,owner);
            addSongStatement.executeUpdate();
            ResultSet generatedKey = addSongStatement.getGeneratedKeys();
            generatedKey.next();
            int currentID = generatedKey.getInt(1);
            String dir = folder+owner+"/"+album.getArtist().getID()+"/"+album.getId()+"/";
            String filepath = dir + currentID;
            try {
                File f = new File(dir);
                if(!f.exists() && !f.mkdirs()){
                    throw new SQLException("unable to create song dirs");
                }
                f = new File(filepath);
                song.write(f.getAbsolutePath());
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
    }


    public Song getSongByID(int id, String username) throws SQLException {
        String getSongByIDQuery =
                "SELECT s_ID, s_title, s_genre, a_ID, a_name, a_date, ar_ID ,ar_name " +
                        "FROM song " +
                        "JOIN album a on a.a_ID = song.s_album_ID " +
                        "JOIN artist a2 on a2.ar_ID = a.a_artist_ID " +
                        "WHERE s_ID=? and s_owner=? ";
        try(PreparedStatement getSongByIDStatement = connection.prepareStatement(getSongByIDQuery)){
            getSongByIDStatement.setInt(1,id);
            getSongByIDStatement.setString(2,username);
            ResultSet result = getSongByIDStatement.executeQuery();
            if(!result.next()) return null;
            Song song = new Song();
            song.setId(result.getInt("s_ID"));
            song.setTitle(result.getString("s_title"));
            Genre genre = new Genre();
            genre.setName(result.getString("s_genre"));
            song.setGenre(genre);
            Album album = new Album();
            album.setDate(result.getDate("a_date"));
            album.setName(result.getString("a_name"));
            album.setId(result.getInt("a_ID"));
            Artist artist = new Artist();
            artist.setID(result.getInt("ar_ID"));
            artist.setName(result.getString("ar_name"));
            album.setArtist(artist);
            song.setAlbum(album);
            return song;
        }
    }

    public boolean isOwner(String username, int id) throws SQLException {
        String checkSongOwnershipQuery = "SELECT COUNT(*) FROM song WHERE s_ID = ? AND s_owner = ?";
        try(PreparedStatement checkSongOwnershipStatement = connection.prepareStatement(checkSongOwnershipQuery)){
            checkSongOwnershipStatement.setInt(1,id);
            checkSongOwnershipStatement.setString(2,username);
            ResultSet result = checkSongOwnershipStatement.executeQuery();
            if(!result.next()) return false;
            return result.getInt(1) == 1;
        }
    }
}
