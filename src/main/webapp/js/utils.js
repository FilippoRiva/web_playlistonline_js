function makeCall(method, url, formElement, callback, json) {
    let req = new XMLHttpRequest();
    req.onreadystatechange = function () {
        if (req.readyState === 4) {
            callback(req);
        }
    };
    req.open(method, url);
    if (json) {
        req.send(json);
    } else if (formElement === null) {
        req.send();
    } else {
        req.send(new FormData(formElement));
    }
}

function removeChildNodes(parent) {
    while (parent.firstChild) { //children are a live collection
        parent.removeChild(parent.firstChild);
    }
}

function removeListeners(node) {
    return replaceNode(node);
}

function replaceNode(node) {
    let nodeClone = node.cloneNode(true);
    node.parentElement.replaceChild(nodeClone, node);
    return nodeClone;
}

function reduceSongList(songList) { //used to generate a JSON of IDs ignoring other values
    let result = [];
    for (let songIndex in songList) {
        let song = songList.at(songIndex)
        let songCopy = {
            id: song.id
        }
        result = result.concat(songCopy);
    }
    return result;
}