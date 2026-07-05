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

var currentItem = null;
document.addEventListener("click", (e) => {
    const btn = e.target.closest(".menu-item");
    if (btn) {
        console.log(`Clicked ${currentItem.id}`, btn.dataset.action);
        switch (btn.dataset.action) {
            case "delete":
                deleteContent(currentItem);
                break;
            case "rename":
                renameContent(currentItem);
                break;
            case "tags":
                editContentTags(currentItem);
                break;
            default:
                console.log("Unkown menu item");
                break;
        }
    }
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

    var sortBy = document.getElementById("sortBy").value;
    var ordering = document.getElementById("ordering").value;

    const res = await fetch(`/api/gallery?page=${page}&sortBy=${sortBy}&ordering=${ordering}`);
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
    const menu = document.getElementById("lightbox-menu-btn")
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
    menu.addEventListener("click", (e) => {
        e.stopPropagation();
        document.getElementById("lightbox-menu-popup").classList.toggle("open");
    });

    // Close menu when clicking outside
    lightbox.addEventListener("click", () => {
        document.getElementById("lightbox-menu-popup").classList.remove("open");
    });

}

function openLightbox(item) {
    currentItem = item;
    const lightbox = document.getElementById("lightbox");
    const img = document.getElementById("lightbox-img");
    const vid = document.getElementById("lightbox-video");
    const caption = document.getElementById("lightbox-caption");

    if (item.type === "image") {
        img.src = `/api/images/${item.id}`;
        img.style.display = "block";
        vid.style.display = "none";
    } else {
        vid.src = `/api/videos/${item.id}`;
        vid.style.display = "block";
        img.style.display = "none";
    }

    document.getElementById("lightbox-title").textContent = item.title || "Untitled";
    document.getElementById("lightbox-date").textContent = item.taken_at || "Unknown date";
    lightbox.style.display = "flex";
function deleteContent(item) {
    function removeItem(item) {
        document.getElementById("lightbox").style.display = "none";
        document.getElementById("lightbox-video").src = "";
        document.getElementById("lightbox-img").src = "";
        item.element.remove();
    }
    if (item.type === "image") {
        fetch(`/api/images/${item.id}/delete`);
        removeItem(item);
    }
    else if (item.type === "video") {
        fetch(`/api/videos/${item.id}/delete`);
        removeItem(item);
    }
    else {
        console.log("Unknown type of content to delete.");
    }
}

function renameContent(item) {
    const titleEl = document.getElementById("lightbox-title");
    const original = titleEl.textContent;

    titleEl.contentEditable = "true";
    titleEl.focus();

    // Select all text
    const range = document.createRange();
    range.selectNodeContents(titleEl);
    window.getSelection().removeAllRanges();
    window.getSelection().addRange(range);

    async function onKey(e) {
        if (e.key === "Enter") {
            e.preventDefault();
            titleEl.contentEditable = "false";
            titleEl.removeEventListener("keydown", onKey);
            const newTitle = titleEl.textContent.trim();
            if (newTitle !== original) {
                item.title = newTitle;
                if (item.type === "image") {
                    await fetch(`/api/images/${item.id}/rename`, {
                        method: "PATCH",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ title: newTitle })
                    });
                }
                else if (item.type === "video") {
                    await fetch(`/api/videos/${item.id}/rename`, {
                        method: "PATCH",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ title: newTitle })
                    });
                }
            }
        }
        if (e.key === "Escape") {
            titleEl.contentEditable = "false";
            titleEl.removeEventListener("keydown", onKey);
            titleEl.textContent = original;
        }
    }
    titleEl.addEventListener("keydown", onKey);
}

window.addEventListener("scroll", () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
        loadContent();
    }
});
