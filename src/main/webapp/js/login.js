let state = "login";
let topButton = window.document.getElementById("topButton");
let errorMessage = window.document.getElementById("errorMessage");
let passwordCopyLabel = window.document.getElementById("passwordCopyLabel");
let passwordCopyInput = window.document.getElementById("passwordCopyInput");
let form = window.document.getElementsByTagName("form").item(0);
let submitButton = form.querySelector("input[type=submit]");
let header = window.document.getElementsByTagName("h1").item(0);

let pageSwitch = function (evt) {
   evt.preventDefault();
   if(state === "login"){
      state = "registration";
      topButton.querySelector("div").textContent="Login";
      header.textContent = "Registration";
      submitButton.textContent = "Register"
      passwordCopyLabel.hidden = false;
      passwordCopyInput.hidden = false;
   } else {
      state = "login";
      topButton.querySelector("div").textContent="Register";
      header.textContent = "Login";
      submitButton.textContent = "Login"
      passwordCopyLabel.hidden = true;
      passwordCopyInput.hidden = true;
   }
}

window.onload = function () {

   makeCall("post","/PlaylistOnlineJS/login",form,(req)=>{ //checking cookie
      let message = req.responseText;
      if(req.status === 200){
         window.sessionStorage.setItem("username",message); //storing username
         window.location.href = "/PlaylistOnlineJS/home";
      }
   });

   topButton.addEventListener("click",evt=>{pageSwitch(evt)},false);

   submitButton.addEventListener("click",evt => {
      evt.preventDefault();
      if(state==="login"){ //login
         makeCall("post","/PlaylistOnlineJS/login",form,(req)=>{
            let message = req.responseText;
            if(req.status === 200){
               window.sessionStorage.setItem("username",message);
               window.location.href = "/PlaylistOnlineJS/home";
            } else {
               errorMessage.textContent = message
               window.setTimeout(()=>{
                  errorMessage.textContent = " ";
               },2000)
            }
         })
      } else { //registration
         makeCall("post","/PlaylistOnlineJS/registration",form,(req)=>{
            if(req.readyState === 4){
               let message = req.responseText;
               if(req.status === 200){ //200 is the
                  window.location.href = req.responseURL;
               } else {
                  errorMessage.textContent = message
                  window.setTimeout(()=>{
                     errorMessage.textContent = " ";
                  },2000)
               }
            }
         })
      }
   });
}