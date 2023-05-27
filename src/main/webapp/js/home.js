let topRightButton = window.document.getElementById("top-right-button");
let firstH1 = window.document.getElementById("firstH1");
let secondH1 = window.document.getElementById("secondH1");
let thirdH1 = window.document.getElementById("thirdH1");
let username = window.sessionStorage.getItem("username");
let homePage;
let playlistPage;
let songPage;

window.onload = function () {
    //HTMl elements
    let songContainer = window.document.getElementById("song-container");
    let playlistTable = window.document.getElementById("playlist-table");
    let templatePlaylistRow = window.document.getElementById("temp-playlistRow");
    let songNavigationElem = window.document.getElementById("song-navigation");
    let templateSongNavInfo = window.document.getElementById("song-nav-info");
    let songFormElem = window.document.getElementById("song-form");
    let remainingSongsForm = window.document.getElementById("remaining-songs-form")
    let templateCustomTableArtist = window.document.getElementById("custom-table-artist-template");
    let templateCustomTableAlbum = window.document.getElementById("custom-table-album-template");
    let templateCustomTableCheckbox = window.document.getElementById("custom-table-checkbox");

    //My Elements
    let playlistList = new PlaylistList(playlistTable,templatePlaylistRow);
    let songForm = new SongForm(songFormElem);
    let remainingSongs = new RemainingSongs(remainingSongsForm,templateCustomTableArtist,
        templateCustomTableAlbum,templateCustomTableCheckbox);
    let songNavigation = new SongNavigator(songNavigationElem,templateSongNavInfo);
    let songPlayer = new SongPlayer(songContainer);

    //logout button
    homePage = new HomePage(playlistList,songForm,remainingSongs);
    homePage.refresh();
    playlistPage = new PlaylistPage(songNavigation,remainingSongs)
    songPage = new SongPage(songPlayer)
}