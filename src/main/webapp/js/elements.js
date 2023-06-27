function PlaylistList(table,rowTemplate){
    this.show = function(){
        table.hidden = false;
    }
    this.hide = function(){
        table.hidden = true;
    }
    this.refresh = function(){
        let self = this;
        makeCall("get","/PlaylistOnlineJS/user-playlists",null,(res)=>{
            if(res.status===200){
                //successful
                let playlists = JSON.parse(res.responseText);
                removeChildNodes(table);

                //filling new playlist list
                if(playlists.length!==0){
                    playlists.forEach(function(playlist){
                        let playlistRow =  rowTemplate.content.cloneNode(true);
                        let left = playlistRow.querySelector(".left");
                        let reorderLink = playlistRow.querySelector(".reorderLink");
                        left.textContent = playlist.name;
                        left.addEventListener("click",(evt)=>{
                            evt.preventDefault();
                            homePage.remainingSongs.saveInBuffer();
                            homePage.hide();
                            playlistPage.refresh(playlist.name,playlist.id);
                        });
                        reorderLink.addEventListener("click",(evt)=>{
                            evt.preventDefault();
                            homePage.hide();
                            orderPage.refresh(playlist.name,playlist.id);
                        });
                        let right = playlistRow.querySelector(".right");
                        right.textContent = playlist["date"];
                        table.appendChild(playlistRow)
                    });
                } else {
                    table.textContent = "no playlists";
                }

                //show
                self.show(playlists);
            } else {
                messageOverlay.showMessage("Unable to retrieve playlists from server")
            }
        })
    }
}

function SongForm(form){
    this.currentState = "closed";
    this.infoText = form.querySelector("#song-form-info-text");
    this.albums = undefined;
    this.existing = form.querySelector("#existing");
    this.toNewAlbum = form.querySelector("#toNewAlbum");
    this.toExistingAlbum = form.querySelector("#toExistingAlbums");
    this.submit = form.querySelector("#song-form-submit");
    this.newAlbumFormRows =[
        form.querySelector("#ar_name").parentElement.parentElement,
        form.querySelector("#a_name").parentElement.parentElement,
        form.querySelector("#a_date").parentElement.parentElement,
        form.querySelector("#a_cover").parentElement.parentElement.parentElement,
    ]
    this.existingAlbumFormRows=[
        form.querySelector("#a_ID").parentElement.parentElement,
    ]
    this.select = this.existingAlbumFormRows.at(0).querySelector("select");
    this.audioInput = form.querySelector("#s_file");

    //setting button functionalities
    this.toNewAlbum.querySelector(".button").addEventListener("click",(evt)=>{
        evt.preventDefault();
        this.currentState !== "new" ? this.show("new") : this.show("closed");
    });
    this.toExistingAlbum.querySelector(".button").addEventListener("click",(evt)=>{
        evt.preventDefault();
        this.currentState !== "existing" ? this.show("existing") : this.show("closed");
    });
    this.submit.addEventListener("click",(evt)=>{
        evt.preventDefault();
        let self = this;
        makeCall("post","/PlaylistOnlineJS/song",form,(req)=>{
            if (req.status===200){
                form.reset();

                //notifying the listeners to change color (reset doesn't)
                form.querySelector("#s_file").dispatchEvent(new Event("change"));
                form.querySelector("#a_cover").dispatchEvent(new Event("change"));

                //going to existing state with the album id returned by the server
                self.refresh(req.responseText,"existing");

                homePage.remainingSongs.refresh();

                //info message
                self.infoText.textContent = "song added!"
                window.setTimeout(function() {
                    self.infoText.textContent = " ";
                },2000);

            } else {
                self.infoText.textContent = req.responseText;
                window.setTimeout(function() {
                    self.infoText.textContent = " ";
                },2000)
            }
        });
    })
    //dynamic coloring of song file and cover buttons
    form.querySelector("#s_file").addEventListener("change",function(evt){
        if(evt.target.files[0]){
            evt.target.parentElement.style.backgroundColor =
                "var(--color-primary)"
        } else {
            evt.target.parentElement.style.backgroundColor =
                "var(--color-complementary)"
        }
    })
    form.querySelector("#a_cover").addEventListener("change",function(evt){
        if(evt.target.files[0]){
            evt.target.parentElement.style.backgroundColor =
                "var(--color-primary)"
        } else {
            evt.target.parentElement.style.backgroundColor =
                "var(--color-complementary)"
        }
    })

    this.show = function(choice,selectedID){
        let toNewAlbumButton = this.toNewAlbum.querySelector(".button");
        let toExistingAlbumButton = this.toExistingAlbum.querySelector(".button");
        switch (choice){
            case "closed" : {
                this.newAlbumFormRows.forEach(function (row) {row.hidden = true;})
                this.existingAlbumFormRows.forEach(function (row) {row.hidden = true;})
                this.audioInput.parentElement.parentElement.parentElement.hidden = true;
                toNewAlbumButton.style.backgroundColor = "var(--color-complementary)";
                toExistingAlbumButton.style.backgroundColor = "var(--color-complementary)";
                this.submit.hidden=true;
                this.currentState = "closed";
                this.existing.value = "none";
                break;
            }
            case "new" : {
                this.newAlbumFormRows.forEach(function (row) {row.hidden = false;})
                this.existingAlbumFormRows.forEach(function (row) {row.hidden = true;})
                toNewAlbumButton.style.backgroundColor = "var(--color-primary)";
                toExistingAlbumButton.style.backgroundColor = "var(--color-complementary)";
                this.audioInput.parentElement.parentElement.parentElement.hidden = false;
                this.submit.hidden=false;
                this.currentState = "new";
                this.existing.value = "false";
                break;
            }
            case "existing" : {
                this.newAlbumFormRows.forEach(function (row) {row.hidden = true;})
                this.existingAlbumFormRows.forEach(function (row) {row.hidden = false;})
                let options = this.select.getElementsByTagName("option");
                //selecting the requested option
                Array.from(options).forEach((option)=>{
                    if(option.value===selectedID){
                        option.selected=true;
                    }
                })
                toNewAlbumButton.style.backgroundColor = "var(--color-complementary)";
                toExistingAlbumButton.style.backgroundColor = "var(--color-primary)";
                this.audioInput.parentElement.parentElement.parentElement.hidden = false;
                this.submit.hidden=false;
                this.existing.value = "true";
                this.currentState = "existing";
                break;
            }
        }
        form.hidden = false;
    }

    this.hide = function(){
        form.hidden = true;
    }

    this.refresh = function(selectedID,state) {
        makeCall("get","/PlaylistOnlineJS/albums",null,(req)=> {
            if (req.status === 200) {
                this.albums = JSON.parse(req.responseText);

                //updating album list
                removeChildNodes(this.select);
                let self = this;
                this.albums.forEach(function (album) {
                    let option = document.createElement("option");
                    option.textContent = album.artist.name + " - " + album.name;
                    option.value = album.id;
                    self.select.appendChild(option);
                });

                if (state) {
                    this.show(state, selectedID);
                } else {
                    this.show(this.currentState, selectedID);
                }
            } else {
                messageOverlay.showMessage("Unable to retrieve user albums from server");
            }
        });
    }
}

function RemainingSongs(form,templateArtist,templateAlbum,templateCheckbox){
    this.form = form;
    this.submit = form.querySelector("#remaining-songs-submit");
    this.titleRow = form.firstElementChild;
    this.homePageArtistsBuffer = undefined;
    this.show = function(useBuffer) {
        if(this.homePageArtistsBuffer!== undefined && useBuffer) {
            this.form.replaceChild(this.homePageArtistsBuffer.cloneNode(true),this.form.querySelector(".custom-table"))
            //removing listeners to submit
            let submitCopy = this.submit.cloneNode(true);
            this.submit.parentElement.replaceChild(submitCopy, this.submit);
            this.submit = submitCopy;
            this.submit.addEventListener("click",this.createNewPlaylist);
            this.submit.value = "Create";
            this.titleRow.hidden = false;
            this.submit.hidden = false;
        }
        form.hidden = false;
    }
    this.hide = function() {
        form.hidden = true;
    }

    this.createNewPlaylist = function (evt){
        evt.preventDefault();
        makeCall("post","/PlaylistOnlineJS/playlist-create",form,(req)=>{
            if(req.status===200){
                form.reset();
                homePage.playlistList.refresh();
                homePage.remainingSongs.refresh();
            } else {
                messageOverlay.showMessage("Unable to create playlist");
            }
        });
    }

    this.refresh = function(homePageBuffering = false,playlistID,playlistName) {
        //removing listeners to submit
        let submitCopy = this.submit.cloneNode(true);
        this.submit.parentElement.replaceChild(submitCopy, this.submit);
        this.submit = submitCopy;
        if(playlistID){
            this.submit.value = "Add"
            // submit for adding songs
            form.querySelector("#p_ID").value = playlistID;
            this.submit.addEventListener("click",(evt)=>{
                evt.preventDefault();
                makeCall("post","/PlaylistOnlineJS/playlist",form,(req)=>{
                    if(req.status===200){
                        playlistPage.refresh(playlistName,playlistID);
                    } else {
                        messageOverlay.showMessage("Unable to create playlist");
                    }
                });
            })
            //getting remaining artists and showing them
            makeCall("get","/PlaylistOnlineJS/remaining-artists?p_ID="+playlistID,null,(req)=>{
                if(req.status===200){
                    this.updateArtistList(JSON.parse(req.responseText),false);
                    this.titleRow.hidden = true;
                    this.show();
                } else {
                    messageOverlay.showMessage("Unable to retrieve remaining artists from server");
                }
            })
        } else {
            this.submit.value = "Create"
            // submit for creating new playlists
            this.submit.addEventListener("click",this.createNewPlaylist);
            //getting all user artists and showing them
            makeCall("get","/PlaylistOnlineJS/artists",null,(req)=>{
                if(req.status===200){
                    this.updateArtistList(JSON.parse(req.responseText),homePageBuffering)
                    this.titleRow.hidden = false;
                    this.show();
                } else {
                    messageOverlay.showMessage("Unable to retrieve artists from server");
                }
            });
        }
    }

    //compiling artist list
    this.updateArtistList = function(artistsList,homePageBuffering){
        let table = form.querySelector(".custom-table");
        removeChildNodes(table);
        if(artistsList){
            artistsList.forEach(function (artist){
                let artistNode = templateArtist.content.cloneNode(true);
                artistNode.firstElementChild.firstElementChild.textContent = artist.name;
                let artistTable = artistNode.firstElementChild.lastElementChild.firstElementChild;
                artist.albums.forEach(function (album){
                    let albumNode = templateAlbum.content.cloneNode(true);
                    albumNode.firstElementChild.firstElementChild.textContent = album.name;
                    let albumCheckBoxContainer = albumNode.firstElementChild.lastElementChild.firstElementChild.firstElementChild.firstElementChild.firstElementChild;
                    album["songs"].forEach(function (song) {
                        let checkbox = templateCheckbox.content.cloneNode(true);
                        checkbox.firstElementChild.firstElementChild.value = song.id;
                        checkbox.firstElementChild.lastElementChild.textContent = song.title;
                        albumCheckBoxContainer.appendChild(checkbox);
                    })
                    artistTable.appendChild(albumNode);
                })
                table.appendChild(artistNode);
            });
            if(artistsList.length === 0) {
                let emptyText = document.createElement("span");
                emptyText.textContent = "no songs";
                table.appendChild(emptyText);
                this.submit.hidden = true;
            } else {
                this.submit.hidden = false;
            }
        }
        if(homePageBuffering) this.saveInBuffer();
    }

    this.saveInBuffer = function(){
        let table = form.querySelector(".custom-table");
        this.homePageArtistsBuffer = table.cloneNode(true);
    }
}

function SongNavigator(songNavigation,templateSongNavInfo) {
    this.prevButton = songNavigation.querySelector(".back");
    this.nextButton = songNavigation.querySelector(".next");
    this.songList = undefined;
    this.currentSongsDiv = songNavigation.querySelector(".current-songs");
    this.currentPage = 1;

    this.show = function() {
        if(this.songList) this.changePage(1);
        songNavigation.style.display="flex";
    }
    this.hide = function() {
        songNavigation.style.display="none";
    }
    this.refresh = function(playlistName, playlistID) {
        makeCall("get","/PlaylistOnlineJS/playlist?id="+playlistID,null,(req)=>{
            if(req.status===200){
                let playlist = JSON.parse(req.responseText);
                this.songList = playlist["songs"];
                removeChildNodes(this.currentSongsDiv);
                if(this.songList.length === 0){
                    let emptyText = document.createElement("span");
                    emptyText.textContent = "no songs";
                    this.currentSongsDiv.appendChild(emptyText);
                    this.show();
                    return;
                }
                this.changePage(this.currentPage);
            } else {
                messageOverlay.showMessage("Unable to retrieve playlist songs from server");
            }
        });
    }
    this.changePage = function(pageNumber){
        if((pageNumber-1)*5>this.songList.length) return;
        this.currentPage = pageNumber;
        let array = Array.from(this.songList);
        let firstIndex = (this.currentPage-1)*5;
        let lastIndex = this.currentPage*5;

        this.nextButton = removeListeners(this.nextButton);
        this.prevButton = removeListeners(this.prevButton);
        if(array.length>lastIndex){
            this.nextButton.textContent = ">";
            this.nextButton.addEventListener("click",()=>{
                this.changePage(++this.currentPage);
            });
        } else {
            this.nextButton.textContent = "";
        }
        if(firstIndex>0){
            this.prevButton.textContent = "<";
            this.prevButton.addEventListener("click",()=>{
                if(this.currentPage>1){
                    this.changePage(--this.currentPage);
                }
            });
        } else {
            this.prevButton.textContent = "";
        }
        removeChildNodes(this.currentSongsDiv);
        for(let i = firstIndex;i<lastIndex;i++){
            let song = array.at(i);
            if(song === undefined) break;
            let newNode = templateSongNavInfo.content.cloneNode(true);
            let link = newNode.firstElementChild;
            let cover = newNode.firstElementChild.firstElementChild.firstElementChild;
            let songTitle = cover.nextElementSibling;
            let artistName = songTitle.nextElementSibling;
            link.addEventListener("click",(evt)=>{
                evt.preventDefault()
                playlistPage.hide();
                songPage.refresh(song.id,song.title);
            })
            cover.firstElementChild.src = "/PlaylistOnlineJS/cover?arID="+song.album.artist["ID"]+"&aID="+song.album.id;
            songTitle.textContent = song.title;
            artistName.textContent = song.album.artist.name;
            this.currentSongsDiv.appendChild(newNode);
        }
    }
}

function SongPlayer(songContainer) {
    this.img = songContainer.querySelector(".album-cover");
    this.songTitle = songContainer.querySelector(".song-title");
    this.artistAlbum = songContainer.querySelector(".artist-album");
    this.releaseDateGenre = songContainer.querySelector(".releaseDate-genre");
    this.audio = songContainer.querySelector("#audio")
    this.show = function () {
        songContainer.style.display = "flex";
    }
    this.hide = function () {
        songContainer.style.display = "none";
    }
    this.refresh = function (songId) {
        makeCall("get","/PlaylistOnlineJS/song?id="+songId,null,(req)=>{
            if(req.status===200){
                let song = JSON.parse(req.responseText);
                this.songTitle.textContent = song.title;
                this.artistAlbum.textContent = song.album.artist.name + " - " + song.album.name;
                this.releaseDateGenre.textContent = song.album["date"] + " - " + song["genre"]["name"];
                let source = document.createElement("source");
                source.src = "/PlaylistOnlineJS/songfile?id="+songId+"&arID="+song.album.artist["ID"]+"&aID="+song.album.id;
                removeChildNodes(this.audio);
                this.audio = replaceNode(this.audio);
                this.audio.appendChild(source);
                let imgSrc = document.createElement("img");
                imgSrc.src = "/PlaylistOnlineJS/cover?arID="+song.album.artist["ID"]+"&aID="+song.album.id;
                removeChildNodes(this.img);
                this.img.appendChild(imgSrc);
                this.show();
            } else {
                messageOverlay.showMessage("Unable to retrieve song info");
            }
        });
    }
}

function DragAndDropTable(table,orderTableRowTemplate){
    this.movingRowIndex = -1;
    this.dropRowIndex = -1;
    this.playlistSongs = undefined;
    this.confirm = table.lastElementChild.firstElementChild.firstElementChild.firstElementChild;
    this.show = function(){
        table.hidden = false;
    }
    this.hide = function(){
        table.hidden = true;
    }
    this.refresh = function(playlistID){
        //getting new rows and updating the table
        makeCall("get","/PlaylistOnlineJS/playlist?id="+playlistID,null,(req)=>{
            if(req.status === 200){
                this.playlistSongs = Array.from(JSON.parse(req.responseText)["songs"]);
                this.updateList(playlistID);
                this.show();
            } else {
                messageOverlay.showMessage("Unable to get playlist info");
            }
        });
    }

    this.updateList = function(playlistID){
        let tbody = table.firstElementChild.nextElementSibling;

        //removing old rows
        removeChildNodes(tbody);

        for(let i = 1 ; i<=this.playlistSongs.length ; i++){
            let song = this.playlistSongs.at(i-1);
            let row = orderTableRowTemplate.content.cloneNode(true);
            let position = row.querySelector("#position");
            let artist = row.querySelector("#artist");
            let album = row.querySelector("#album");
            let title = row.querySelector("#title");
            position.textContent = i;
            artist.textContent = song.album.artist.name;
            album.textContent = song.album.name;
            title.textContent = song.title;
            position.ondragstart = function() {
                return false;
            }
            row.firstElementChild.addEventListener("dragstart",()=>{
                this.movingRowIndex = i-1;
            });
            row.firstElementChild.addEventListener("dragenter",()=>{
                this.dropRowIndex = i-1;
                this.moveDraggedRow();
                this.movingRowIndex = i-1;
                this.updateList(playlistID);
            })
            tbody.appendChild(row);
        }

        this.confirm = removeListeners(this.confirm);
        this.confirm.addEventListener("click",(evt)=>{
            evt.preventDefault();
            makeCall("post","/PlaylistOnlineJS/reorder?id="+playlistID,null,(req)=>{
                if(req.status===200){
                    orderPage.hide();
                    homePage.show();
                } else {
                    messageOverlay.showMessage("Unable to reorder playlist")
                }
            },JSON.stringify(reduceSongList(this.playlistSongs)));
        });
    }

    this.moveDraggedRow = function(){
        let elementToMove = this.playlistSongs.splice(this.movingRowIndex,1);
        this.playlistSongs.splice(this.dropRowIndex,0,elementToMove.at(0));
    }
}
