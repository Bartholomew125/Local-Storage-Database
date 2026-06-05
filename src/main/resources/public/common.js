// In common.js, loaded by every page
async function loadComponent(id, url) {
    const res = await fetch(url);
    document.getElementById(id).innerHTML = await res.text();
}

loadComponent("navbar", "/components/navbar.html");
loadComponent("usericon", "/components/usericon.html");
