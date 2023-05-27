function makeCall(method, url, formElement, callback) {
    let req = new XMLHttpRequest();
    req.onreadystatechange = function() {
        if(req.readyState===4){
            callback(req);
        }
    };
    req.open(method, url);
    if (formElement === null) {
        req.send();
    } else {
        req.send(new FormData(formElement));
    }
}

function removeChildNodes(parent) {
    while(parent.firstChild){ //live collection
        parent.removeChild(parent.firstChild);
    }
}

function logout() {
    makeCall("post", "/PlaylistOnlineJS/logout", null, (req) => {
        if(req.status === 200){ //ok status code(sent with temporary redirect
            window.location.href = req.responseURL;
        } else {
            console.log("logout failed");
        }
    })
}

function removeListeners(node){
    let nodeClone = node.cloneNode(true);
    node.parentElement.replaceChild(nodeClone,node);
    return nodeClone
}