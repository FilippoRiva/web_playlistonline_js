package com.playonline.dao;

import com.playonline.beans.*;
import com.playonline.utils.GenreEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {
    PreparedStatement getUserPlaylistsStatement;
    PreparedStatement isPlaylistUserCoupleValidStatement;
    PreparedStatement getPlaylistInfoStatement;
    PreparedStatement addPlaylistStatement;
    PreparedStatement addSongToPlaylistStatement;
    PreparedStatement checkPlaylistExistenceStatement;
    PreparedStatement getRemainingUserArtistsStatement;
    PreparedStatement getUserArtistsStatement;
    public PlaylistDAO(Connection connection) throws SQLException{
        String getUserPlaylistsQuery = "SELECT p_title,p_id,p_date FROM playlist WHERE p_owner=? ORDER BY p_date";
        String isPlaylistUserValid = "SELECT COUNT(*) FROM playlist WHERE p_owner=? AND p_ID=?";
        String getPlaylistInfoQuery ="SELECT p_title,p_date,s.s_ID,a_ID,s_title,s_genre,a_name,ar_name, ar_ID ,a_date " +
                "FROM playlist " +
                "LEFT JOIN playlistsong p on playlist.p_ID = p.p_ID " +
                "JOIN song s on s.s_ID = p.s_ID " +
                "JOIN album a on a.a_ID = s.s_album_ID " +
                "JOIN artist a2 on a.a_artist_ID = a2.ar_ID " +
                "WHERE playlist.p_ID=? AND playlist.p_owner=?" +
                "ORDER BY a_date DESC, ps_position ASC";
        String addPlaylistQuery = "INSERT INTO playlist(p_owner,p_title,p_date) VALUES (?,?,?)";
        String addSongQuery = "INSERT INTO playlistsong(s_ID,p_ID) VALUES(?,?)";
        String checkPlaylistExistenceQuery = "SELECT p_title, p_date, p_ID FROM playlist WHERE p_ID=? and p_owner=?";
        String getRemainingArtistsQuery =
                "SELECT s_ID, s_title, s_genre, a_ID, a_name, a_date, ar_name, ar_ID " +
                        "FROM song " +
                        "JOIN album a on a.a_ID = song.s_album_ID " +
                        "JOIN artist a2 on a2.ar_ID = a.a_artist_ID " +
                        "WHERE s_owner = ? and s_ID not in " +
                        "(SELECT s_ID " +
                        "FROM playlistsong " +
                        "JOIN playlist p on p.p_ID = playlistsong.p_ID " +
                        "WHERE p.p_owner=? and p.p_ID = ?)";
        String getUserSongs =
                "SELECT ar_name, ar_ID, a_name, a_date, a_ID, s_title, s_genre, s_ID " +
                        "FROM artist " +
                        "JOIN album a on artist.ar_ID = a.a_artist_ID " +
                        "JOIN song s on a.a_ID = s.s_album_ID " +
                        "WHERE s_owner= ? " +
                        "ORDER BY ar_name, a_name, s_title";
        getUserPlaylistsStatement = connection.prepareStatement(getUserPlaylistsQuery);
        isPlaylistUserCoupleValidStatement = connection.prepareStatement(isPlaylistUserValid);
        getPlaylistInfoStatement = connection.prepareStatement(getPlaylistInfoQuery);
        addPlaylistStatement = connection.prepareStatement(addPlaylistQuery,Statement.RETURN_GENERATED_KEYS);
        addSongToPlaylistStatement = connection.prepareStatement(addSongQuery);
        checkPlaylistExistenceStatement = connection.prepareStatement(checkPlaylistExistenceQuery);
        getRemainingUserArtistsStatement = connection.prepareStatement(getRemainingArtistsQuery);
        getUserArtistsStatement = connection.prepareStatement(getUserSongs);
    }

    public List<Playlist> getUserPlaylists(String username) throws SQLException{
        List<Playlist> result = new ArrayList<>();
        ResultSet queryResult;
        getUserPlaylistsStatement.setString(1,username);
        queryResult = getUserPlaylistsStatement.executeQuery();
        while(queryResult.next()){
            Playlist playlist = new Playlist();
            playlist.setName(queryResult.getString(1));
            playlist.setId(queryResult.getInt(2));
            playlist.setDate(queryResult.getDate(3));
            result.add(playlist);
        }
        return result;
    }

    public int createPlaylist(String username, String playlistName) throws SQLException {
        addPlaylistStatement.setString(1,username);
        addPlaylistStatement.setString(2,playlistName);
        addPlaylistStatement.setDate(3,new Date(System.currentTimeMillis()));
        addPlaylistStatement.executeUpdate();
        ResultSet result = addPlaylistStatement.getGeneratedKeys();
        result.next();
        return result.getInt(1);
    }

    public void addSongToPlaylist(int pId, int sId) throws SQLException {
        addSongToPlaylistStatement.setInt(1,sId);
        addSongToPlaylistStatement.setInt(2,pId);
        addSongToPlaylistStatement.executeUpdate();
    }

    public Playlist getPlaylist(int playlistID,String owner) throws SQLException{
        Playlist playlist = new Playlist();
        ResultSet queryResult;
        getPlaylistInfoStatement.setInt(1,playlistID);
        getPlaylistInfoStatement.setString(2,owner);
        queryResult = getPlaylistInfoStatement.executeQuery();
        if(!queryResult.next()){ //empty playlist
            checkPlaylistExistenceStatement.setInt(1,playlistID);
            checkPlaylistExistenceStatement.setString(2,owner);
            queryResult = checkPlaylistExistenceStatement.executeQuery();
            if(!queryResult.next()) return null;
            playlist.setName(queryResult.getString("p_title"));
            playlist.setId(queryResult.getInt("p_ID"));
            playlist.setDate(new Date(queryResult.getDate("p_date").getTime()));
            return playlist;
        }
        playlist.setName(queryResult.getString("p_title"));
        playlist.setId(playlistID);
        playlist.setDate(new Date(queryResult.getDate("p_date").getTime()));
        List<Song> songs = new ArrayList<>();
        do{
            Song song = new Song();
            song.setId(queryResult.getInt("s_ID"));
            song.setTitle(queryResult.getString("s_title"));
            GenreEnum genreEnum = GenreEnum.valueOf(queryResult.getString("s_genre").toUpperCase());
            Genre genre = new Genre();
            genre.setName(genreEnum.getName());
            song.setGenre(genre);
            Album album = new Album();
            album.setId(queryResult.getInt("a_ID"));
            album.setName(queryResult.getString("a_name"));
            album.setDate(new Date(queryResult.getDate("a_date").getTime()));
            Artist artist = new Artist();
            artist.setName(queryResult.getString("ar_name"));
            artist.setID(queryResult.getInt("ar_ID"));
            album.setArtist(artist);
            song.setAlbum(album);
            songs.add(song);
        }while(queryResult.next());
        playlist.setSongs(songs);
        return playlist;
    }

    public List<Artist> getArtists(String username) throws SQLException {
        getUserArtistsStatement.setString(1, username);
        return getArtists(getUserArtistsStatement);
    }

    public List<Artist> getRemainingArtists(int playlistID, String username) throws SQLException {
        getRemainingUserArtistsStatement.setString(1,username);
        getRemainingUserArtistsStatement.setString(2,username);
        getRemainingUserArtistsStatement.setInt(3,playlistID);
        return getArtists(getRemainingUserArtistsStatement);
    }

    private List<Artist> getArtists(PreparedStatement getArtistsStatement) throws SQLException {
        ResultSet result = getArtistsStatement.executeQuery();
        List<Artist> artists = new ArrayList<>();
        Artist currentArtist = new Artist();
        Album currentAlbum = new Album();
        List<Album> currentAlbums = new ArrayList<>();
        Song currentSong;
        List<Song> currentSongs = new ArrayList<>();
        if(!result.next()) return artists;
        do {
            //artist changes
            if(currentArtist.getName()==null || currentArtist.getID()!=result.getInt("ar_ID")) {
                if(currentArtist.getName()!=null) {
                    currentArtist.setAlbums(currentAlbums);
                    currentAlbums = new ArrayList<>();
                    artists.add(currentArtist);
                }
                currentArtist = new Artist();
                currentArtist.setID(result.getInt("ar_ID"));
                currentArtist.setName(result.getString("ar_name"));
            }
            //album changes
            if(currentAlbum.getName()==null || currentAlbum.getId()!= result.getInt("a_ID")){
                currentAlbum.setSongs(currentSongs);
                currentSongs = new ArrayList<>();
                currentAlbum= new Album();
                currentAlbum.setId(result.getInt("a_ID"));
                currentAlbum.setDate(new Date(result.getDate("a_date").getTime()));
                currentAlbum.setName(result.getString("a_name"));
                currentAlbums.add(currentAlbum);
            }
            //new song
            currentSong = new Song();
            currentSong.setTitle(result.getString("s_title"));
            currentSong.setId(result.getInt("s_ID"));
            Genre genre = new Genre();
            genre.setName(result.getString("s_genre"));
            currentSong.setGenre(genre);
            currentSongs.add(currentSong);
        } while(result.next());
        currentAlbum.setSongs(currentSongs);
        currentArtist.setAlbums(currentAlbums);
        artists.add(currentArtist);
        return artists;
    }

}
