function HomePage(playlistList, songForm, remainingSongs) {
    this.playlistList = playlistList;
    this.songForm = songForm;
    this.remainingSongs = remainingSongs;
    this.show = function () {
        //removing listeners to top right button
        let buttonCopy = topRightButton.cloneNode(true);
        topRightButton.parentElement.replaceChild(buttonCopy, topRightButton);
        topRightButton = buttonCopy;

        //adding logout functionality
        topRightButton.addEventListener("click", (evt) => {
            evt.preventDefault();
            makeCall("post", "/PlaylistOnlineJS/logout", null, (req) => {
                if (req.status === 200) {
                    window.location.href = req.responseURL;
                } else {
                    messageOverlay.showMessage("Unable to Logout");
                }
            })
        }, false);

        //showing top button
        topRightButton.firstElementChild.textContent = "logout";
        topRightButton.hidden = false;

        //setting headers
        firstH1.textContent = username + "'s Playlists"
        secondH1.textContent = "Add Songs";
        thirdH1.textContent = "Create Playlist";

        //showing elements
        this.playlistList.show();
        this.songForm.show();
        this.remainingSongs.show(true);
    }
    this.hide = function () {
        topRightButton.hidden = true;

        //hiding headers
        firstH1.textContent = "";
        secondH1.textContent = "";
        thirdH1.textContent = "";

        //hiding elements
        this.playlistList.hide();
        this.songForm.hide();
        this.remainingSongs.hide();
    }
    this.refresh = function () {
        //reloading elements
        this.playlistList.refresh();
        this.songForm.refresh();
        this.remainingSongs.refresh(true);
        this.show();
    }
}

function PlaylistPage(songNavigator, remainingSongs) {

    this.playlistName = undefined;
    this.songNavigator = songNavigator;
    this.remainingSongs = remainingSongs;
    this.show = function (playlistName) {

        if (playlistName) this.playlistName = playlistName;

        //removing listeners to top right button
        let buttonCopy = topRightButton.cloneNode(true);
        topRightButton.parentElement.replaceChild(buttonCopy, topRightButton);
        topRightButton = buttonCopy;

        //go back to homepage
        topRightButton.addEventListener("click", (evt) => {
            evt.preventDefault();
            playlistPage.hide();
            homePage.show(true);
        }, false);

        //showing top button
        topRightButton.hidden = false;
        topRightButton.firstElementChild.textContent = "home";

        if (playlistName !== undefined) firstH1.textContent = playlistName;
        this.songNavigator.show();
        firstH1.textContent = this.playlistName;
        secondH1.textContent = "Add Songs";
        remainingSongs.show();
    }

    this.hide = function () {
        topRightButton.hidden = true;
        firstH1.textContent = "";
        songNavigator.hide();
        secondH1.textContent = "";
        remainingSongs.hide();
    }

    this.refresh = function (playlistName, playlistID) {
        songNavigator.refresh(playlistName, playlistID);
        remainingSongs.refresh(false, playlistID, playlistName);
        this.show(playlistName, playlistID);
    }

}

function SongPage(songPlayer) {
    this.songContainer = songPlayer;
    this.show = function () {
        topRightButton.hidden = false;
    }
    this.hide = function () {
        this.songContainer.hide();
        topRightButton.hidden = true;

        songPlayer.hide()
    }
    this.refresh = function (songId) {
        topRightButton = removeListeners(topRightButton);
        topRightButton.firstElementChild.textContent = "back to playlist";
        topRightButton.addEventListener("click", (evt) => {
            evt.preventDefault();
            songPage.hide();
            playlistPage.show();
            songPlayer.stop();
        })
        this.songContainer.refresh(songId);
        this.show();
    }
}

function OrderPage(dragAndDropTable) {
    this.show = function () {
        dragAndDropTable.show();
        topRightButton.hidden = false;
    }
    this.hide = function () {
        dragAndDropTable.hide();
        firstH1.textContent = "";
        topRightButton.hidden = true;
    }
    this.refresh = function (playlistName, playlistId) {
        topRightButton = removeListeners(topRightButton);
        topRightButton.firstElementChild.textContent = "home";
        topRightButton.addEventListener("click", (evt) => {
            evt.preventDefault();
            orderPage.hide();
            homePage.show();
        });
        firstH1.textContent = "Reorder : " + playlistName;
        dragAndDropTable.refresh(playlistId);
        this.show();
    }
}

function MessageOverlay(exceptionDiv) {
    exceptionDiv.hidden = true;
    this.showMessage = function (message) {
        exceptionDiv.innerText = message;
        exceptionDiv.hidden = false;
        window.setTimeout(() => {
            exceptionDiv.hidden = true;
        }, 5000);
    }
}
