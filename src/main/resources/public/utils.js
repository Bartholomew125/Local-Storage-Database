let page = 0;
let loading = false;
const NUM_COLUMNS = window.innerWidth/400;
const columns = [];
const column_heights = [];

window.addEventListener("DOMContentLoaded", (event) => {
    const usericon = document.getElementById("usericon");
    usericon.addEventListener("click", (event) => {
        window.location.href = "login.html";
    });
});

const pageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            pageObserver.unobserve(entry.target);
            loadContent();
        }
    });
}, { rootMargin: "0px 0px 500px 0px" });  // fire 500px before it enters the viewport

const lazyObserver = new IntersectionObserver( (entries) => {
    entries.forEach( entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            img.src = img.dataset.src;
            lazyObserver.unobserve(img);
        }
    });
});

function initGallery() {
    const gallery = document.getElementById("gallery");
    gallery.style.display = "flex";
    gallery.style.gap = "5px";

    for (let i = 0; i < NUM_COLUMNS; i++) {
        const col = document.createElement("div");
        col.style.flex = "1";
        col.style.display = "flex";
        col.style.flexDirection = "column";
        col.style.gap = "5px";
        gallery.appendChild(col);
        columns.push(col);
        column_heights.push(0);
    }
}

function addContentToGallery(item) {
    const container = document.createElement("div");
    container.className = "thumbnail-container";
    container.addEventListener("click", () => openLightbox(item));

    const thumbnail = document.createElement("img");
    if (item.type == "video") {
        thumbnail.dataset.src = `/api/videos/${item.id}/thumbnail`;
    } else {
        thumbnail.dataset.src = `/api/images/${item.id}/thumbnail`;
    }
    thumbnail.alt = item.title || "untitled";
    thumbnail.style = "width: 100%; display: block;";
    container.appendChild(thumbnail);

    if (item.type === "video") {
        const play = document.createElement("div");
        play.className = "play-button";
        container.appendChild(play);
    }

    const frac_height = item.height / item.width;
    const shortest_idx = column_heights.indexOf(Math.min(...column_heights));
    columns[shortest_idx].appendChild(container);
    column_heights[shortest_idx] += frac_height;
    lazyObserver.observe(thumbnail);

    return container;
}

async function loadContent() {
    if (loading) return;
    loading = true;
    const res = await fetch(`/api/gallery?page=${page}`);
    const content = await res.json();
    if (content.length === 0) { loading = false; return; }

    content.forEach( (c, i) => {
        const cc = addContentToGallery(c);
        if (i === content.length - 1) {
            pageObserver.observe(cc);
        }
    });

    page++;
    loading = false;
}

function initLightbox() {
    const lightbox = document.getElementById("lightbox");
    lightbox.addEventListener("click", (event) => {
        if (event.target === lightbox) {
            lightbox.style.display = "none";
            document.getElementById("lightbox-img").src = "";
            document.getElementById("lightbox-video").src = "";
        }
    });
    lightbox.addEventListener("keydown", (event) => {
        if (event.code == 'Escape') {
            lightbox.style.display = "none";
            document.getElementById("lightbox-img").src = "";
            document.getElementById("lightbox-video").src = "";
        }
    });
}

function openLightbox(item) {
    const lightbox = document.getElementById("lightbox");
    if (item.type == "image") {
        const vid = document.getElementById("lightbox-video");
        vid.hidden = true;

        const img = document.getElementById("lightbox-img");
        img.src = `/api/images/${item.id}`;
        img.hidden = false;
        img.focus();
    }
    else {
        const img = document.getElementById("lightbox-img");
        img.hidden = true;

        const vid = document.getElementById("lightbox-video");
        vid.src = `/api/videos/${item.id}`;
        vid.hidden = false;
        vid.focus();
    }
    document.getElementById("lightbox-title").textContent = item.title || "Untitled";
    document.getElementById("lightbox-date").textContent = item.taken_at || "Unknown date";
    lightbox.style.display = "flex";
}

window.addEventListener("scroll", () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
        loadContent();
    }
});
