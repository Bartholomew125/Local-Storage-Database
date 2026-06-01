let page = 0;
let loading = false;
const NUM_COLUMNS = window.innerWidth/400;
const columns = [];
const column_heights = [];

const pageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            pageObserver.unobserve(entry.target);
            loadImages();
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

function addImageToGallery(image) {
    const img = document.createElement("img");
    img.dataset.src = `/api/images/${image.id}/thumbnail`;
    img.alt = image.title || "untitled";
    img.style = "width: 100%; display: block;";
    img.addEventListener("click", () => openLightbox(image))

    const frac_height = image.height / image.width;

    // Pick shortest column
    const shortest_idx = column_heights.indexOf(Math.min(...column_heights));
    const shortest = columns[shortest_idx];
    shortest.appendChild(img);
    column_heights[shortest_idx] += frac_height;

    lazyObserver.observe(img);

    return img;
}

async function loadImages() {
    if (loading) return;
    loading = true;
    const res = await fetch(`/api/images?page=${page}`);
    const images = await res.json();
    if (images.length === 0) { loading = false; return; }

    // const gallery = document.getElementById("gallery");

    images.forEach( (image, i) => {
        const img = addImageToGallery(image);
        if (i === images.length - 1) {
            pageObserver.observe(img);
        }
    });

    page++;
    loading = false;
}

function initLightbox() {
    const lightbox = document.getElementById("lightbox");
    lightbox.addEventListener("click", () => {
        lightbox.style.display = "none";
        document.getElementById("lightbox-img").src = "";
    });
}

function openLightbox(image) {
    const lightbox = document.getElementById("lightbox");
    const img = document.getElementById("lightbox-img");
    document.getElementById("lightbox-title").textContent = image.title || "Untitled";
    document.getElementById("lightbox-date").textContent = image.taken_at || "Unknown date";
    img.src = `/api/images/${image.id}`;
    lightbox.style.display = "flex";
}

window.addEventListener("scroll", () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
        loadImages();
    }
});
