let page = 0;
let loading = false;
const NUM_COLUMNS = window.innerWidth/400;
const columns = [];
const column_heights = [];

const TAG_HEIGHT = 20;
const TAG_MARGIN = 2;

window.addEventListener("DOMContentLoaded", (event) => {
    const usericon = document.getElementById("usericon");
    usericon.addEventListener("click", (event) => {
        window.location.href = "login.html";
    });
});

window.addEventListener("scroll", () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100) {
        loadContent();
    }
});

// document.addEventListener("click", (e) => {
//     const popup = document.getElementById("tag-popup");
//     if (!e.target.closest("#tag-popup") && !e.target.closest("[data-action='tags']")) {
//         popup.style.display = "none";
//     }
// });
//

/*
 * =============================================================================
 *                                 GALLERY 
 * =============================================================================
 */

const pageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            pageObserver.unobserve(entry.target);
            loadContent();
        }
    });
}, { rootMargin: "0px 0px 500px 0px" });  // fire 500px before it enters the viewport

var GALLERY = null;
function initGallery() {
    GALLERY = document.getElementById("gallery");
}

const MAX_THUMBNAIL_HEIGHT = 500;
const MIN_THUMBNAIL_HEIGHT = 300;
const THUMBNAIL_MARGIN = 4;
const VIEWPORT_WIDTH = window.visualViewport.width;
const MAX_ROW_WIDTH = 0.9*VIEWPORT_WIDTH;

function scaleWidth(current_width, current_height, desired_height) {
    return desired_height/current_height * current_width;
}

function numFromPxStr(pixel_value) {
    return parseFloat(new String(pixel_value).replace("px", ""))
}

var current_group_date = null;
var current_thumbnail_row_container = null;
var current_thumbnail_row = null;

function newThumbnailRow() {
    var row = document.createElement("row");
    row.className = "thumbnail-row";
    // Save current width of container, in row.
    row.currentWidth = 0;
    return row;
}

function newThumbnailRowContainer() {
    const container = document.createElement("div");
    container.className = "thumbnail-row-container";
    container.style.width = MAX_ROW_WIDTH+"px";
    const display_date = current_group_date;
    container.innerHTML = display_date;
    return container;
}

function refactorThumbnailRow() {
    var missing_width = MAX_ROW_WIDTH;
    for (const thumbnail_container of current_thumbnail_row.childNodes) {
        missing_width -= numFromPxStr(thumbnail_container.style.width);
        missing_width -= THUMBNAIL_MARGIN * 2;
    }
    const total_width = MAX_ROW_WIDTH-missing_width;
    const width_scale_factor = MAX_ROW_WIDTH/total_width;
    for (const thumbnail_container of current_thumbnail_row.childNodes) {
        thumbnail_container.style.width = numFromPxStr(thumbnail_container.style.width) * width_scale_factor + "px";
        var new_height = numFromPxStr(thumbnail_container.style.height) * width_scale_factor;
        if (new_height > MAX_THUMBNAIL_HEIGHT) {
            thumbnail_container.style.width = scaleWidth(numFromPxStr(thumbnail_container.style.width), new_height, MAX_THUMBNAIL_HEIGHT) + "px";
            new_height = MAX_THUMBNAIL_HEIGHT;
        }
        thumbnail_container.style.height = new_height + "px";
    }
}

function getDate(datetime) {
    if (datetime == null) {
        return "Unknown Date";
    }
    else {
        return datetime.split(" ")[0];
    }
}

function addContentToGallery(item) {
    console.log(item);
    if (getDate(item.taken_at) != current_group_date) {
        if (current_group_date != null) { 
            refactorThumbnailRow();
            current_thumbnail_row_container.appendChild(current_thumbnail_row);
            GALLERY.appendChild(current_thumbnail_row_container);
        }
        current_group_date = getDate(item.taken_at);
        // Create new row and container for rows
        current_thumbnail_row = newThumbnailRow();
        current_thumbnail_row_container = newThumbnailRowContainer();
        current_thumbnail_row_container.appendChild(current_thumbnail_row);
    }

    const img_scaled_width = scaleWidth(item.width, item.height, MIN_THUMBNAIL_HEIGHT);

    if (img_scaled_width + current_thumbnail_row.currentWidth > MAX_ROW_WIDTH) {
        console.log("NO SPACE");
        refactorThumbnailRow();
        current_thumbnail_row_container.appendChild(current_thumbnail_row);
        current_thumbnail_row = newThumbnailRow();
    }

    const thumbnail_container = document.createElement("div");
    thumbnail_container.className = "thumbnail-container";
    thumbnail_container.style.height = MIN_THUMBNAIL_HEIGHT+"px";
    thumbnail_container.style.width = img_scaled_width+"px";
    thumbnail_container.style.margin = THUMBNAIL_MARGIN+"px";

    const thumbnail = document.createElement("img");
    thumbnail.src = item.type === "video" 
        ? `/api/videos/${item.id}/thumbnail`
        : `/api/images/${item.id}/thumbnail`;
    thumbnail.className = "thumbnail";
    thumbnail.alt = item.title || "untitled";
    thumbnail.loading = "lazy"
    thumbnail.addEventListener("click", () => openLightbox(item));

    thumbnail_container.appendChild(thumbnail);

    if (item.type === "video") {
        const play = document.createElement("div");
        play.className = "play-button";
        thumbnail_container.appendChild(play);
    }

    current_thumbnail_row.appendChild(thumbnail_container);
    current_thumbnail_row.currentWidth += img_scaled_width;
    return thumbnail_container;
}

async function loadContent() {
    if (loading) return;
    loading = true;

    const sortBy = document.getElementById("sortBy").value;
    const ordering = document.getElementById("ordering").value;

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

/*
 * =============================================================================
 *                                LIGHTBOX 
 * =============================================================================
 */

function initLightbox() {
    const lightbox = document.getElementById("lightbox");
    const menu_button = document.getElementById("lightbox-menu-button");
    const menu_popup = document.getElementById("lightbox-menu-popup");
    const tags_bar = document.getElementById("lightbox-tags");
    const toggle_tags_button = document.getElementById("toggle-tags-button");
    const lightbox_image = document.getElementById("lightbox-image");
    const lightbox_video = document.getElementById("lightbox-video");
    const lightbox_caption = document.getElementById("lightbox-caption");
    const lightbox_caption_title = document.getElementById("lightbox-caption-title");
    const lightbox_caption_date = document.getElementById("lightbox-caption-date");

    function closeMenuPopup() {
        menu_popup.style.display = "none";
        menu_button.style.display = "flex";
    }

    function openMenuPopup() {
        menu_popup.style.display = "flex";
        menu_button.style.display = "none";
    }

    function toggleMenuPopup() {
        if (menu_popup.style.display == "flex") {
            closeMenuPopup();
        }
        else {
            openMenuPopup();
        }
    }

    function toggleTagsBar() {
        if (tags_bar.style.display == "inline-flex") {
            tags_bar.style.display = "none";
            toggle_tags_button.innerHTML = "Show tags";
        }
        else {
            tags_bar.style.display = "inline-flex";
            toggle_tags_button.innerHTML = "Hide tags";
        }
    }

    window.addEventListener("keydown", (e) => {
        if (e.key == "Escape") {
            closeMenuPopup();
            closeLightbox();
        }
    });

    lightbox.addEventListener("click", (e) => {
        if (e.target == menu_button) {
            toggleMenuPopup();
        }
        else if (e.target == menu_popup) {}
        else if (e.target.classList.contains("menu-item-button")) {
            const action = e.target.dataset.action;
            switch (action) {
                case "rename":
                    renameContent(currentItem);
                    closeMenuPopup();
                    break;
                case "add tags":
                    addTag();
                    break;
                case "remove":
                    deleteContent(currentItem);
                    closeMenuPopup();
                    closeLightbox();
                    break;
                case "toggle tags":
                    toggleTagsBar();
                    break;
                default:
                    console.log("UNKOWN ACTION: "+action);
                    break;
            }
        }
        else if (e.target == lightbox_image || e.target == lightbox_video) {
            closeMenuPopup();
        }
        else if (e.target == lightbox_caption) {}
        else if (e.target == lightbox_caption_title) {
            renameContent(currentItem);
        }
        else if (e.target == lightbox_caption_date) {}
        else {
            closeMenuPopup();
            closeLightbox();
        }
    });
}

function closeLightbox() {
    const lightbox = document.getElementById("lightbox");
    document.getElementById("lightbox-image").src = "";
    document.getElementById("lightbox-video").src = "";
    lightbox.style.display = "none";
    document.body.style.overflow = ""; // Allow scrolling
    // Remove all child nodes.
    document.getElementById("lightbox-tags").innerHTML = '';
}

function openLightbox(item) {
    currentItem = item;
    const lightbox = document.getElementById("lightbox");
    const img = document.getElementById("lightbox-image");
    const vid = document.getElementById("lightbox-video");
    const title = document.getElementById("lightbox-caption-title");
    const date = document.getElementById("lightbox-caption-date");

    if (item.type === "image") {
        img.src = `/api/images/${item.id}`;
        img.style.display = "block";
        vid.style.display = "none";
    } else {
        vid.src = `/api/videos/${item.id}`;
        vid.style.display = "block";
        img.style.display = "none";
    }

    title.textContent = item.title || "Untitled";
    date.textContent = item.taken_at || "Unknown date";
    lightbox.style.display = "flex";
    document.body.style.overflow = "hidden"; // Prevent scrolling
    loadTags(item);
}

/*
 * =============================================================================
 *                               TAGS 
 * =============================================================================
 */

function addTagToDisplay(tag) {
    const tags = document.getElementById("lightbox-tags");
    const max_height = document.getElementById("lightbox-image") 
                    || document.getElementById("lightbox-video");
    const max_length = max_height/(TAG_HEIGHT+TAG_MARGIN*2);
    const name = tag.name;

    function newColumn() {
        const col = document.createElement("div");
        col.className = "tag-column";
        tags.append(col);
    }

    if (tags.childNodes.length == 0) {
        newColumn();
    }

    var i = 0;
    while (tags.childNodes[i].childNodes.length+1 > max_length) {
        i++;
        if (i == tags.childNodes.length) {
            newColumn();
        }
    }

    // el.innerHTML = `${tag.name}<span class="tag-remove" data-tag="${tag.name}">✕</span>`;
    // el.querySelector(".tag-remove").onclick = async () => {
    //     await fetch(`/api/tags/${item.id}/${encodeURIComponent(tag.name)}`, { method: "DELETE" });
    //     await loadTags(item);
    // };

    const display_tag = document.createElement("button");
    display_tag.className = "tag";
    display_tag.innerHTML = name;
    display_tag.style.height = TAG_HEIGHT+"px";
    display_tag.style.margin = TAG_MARGIN+"px";
    tags.childNodes[i].append(display_tag);
}

async function loadTags(item) {
    const res = await fetch(`/api/tags/${item.id}`);
    const tags = await res.json();
    tags.forEach(tag => {
        addTagToDisplay(tag);
    });
}


/*
 * =============================================================================
 *                             Content Manipulation
 * =============================================================================
 */

function deleteContent(item) {
    console.log(item);
    if (item.type === "image") {
        fetch(`/api/images/${item.id}/delete`);
        item.element.remove();
    }
    else if (item.type === "video") {
        fetch(`/api/videos/${item.id}/delete`);
        item.element.remove();
    }
    else {
        console.log("Unknown type of content to delete.");
    }
}

function renameContent(item) {
    const title_elem = document.getElementById("lightbox-caption-title");
    const original = title_elem.textContent;

    title_elem.contentEditable = "true";
    title_elem.focus();

    // Select all text
    const range = document.createRange();
    range.selectNodeContents(title_elem);
    window.getSelection().removeAllRanges();
    window.getSelection().addRange(range);

    async function onKey(e) {
        if (e.key === "Enter") {
            e.preventDefault();
            title_elem.contentEditable = "false";
            title_elem.removeEventListener("keydown", onKey);
            const newTitle = title_elem.textContent.trim();
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
            title_elem.contentEditable = "false";
            title_elem.removeEventListener("keydown", onKey);
            title_elem.textContent = original;
        }
    }
    title_elem.addEventListener("keydown", onKey);
}

function addTag() {
    const addtag_popup = document.getElementById("lightbox-addtag-popup");
    addtag_popup.style.display = "block";
    addtag_popup.innerHTML = "HELLO LOL";
}

// function editContentTags(item) {
//     const popup = document.getElementById("tag-popup");
//     popup.style.display = popup.style.display === "none" ? "block" : "none";
//     if (popup.style.display === "none") return;
//
//     const search = document.getElementById("tag-search");
//     search.value = "";
//     search.oninput = null; // remove old handler
//     search.replaceWith(search.cloneNode(true)); // remove old keydown listeners
//     const freshSearch = document.getElementById("tag-search");
//
//     freshSearch.focus();
//     renderTagResults("", item);
//
//     freshSearch.oninput = () => renderTagResults(freshSearch.value.trim(), item);
//     // freshSearch.addEventListener("keydown", async (e) => {
//     //     if (e.key === "Enter" && freshSearch.value.trim()) {
//     //         await addTag(item, freshSearch.value.trim());
//     //         freshSearch.value = "";
//     //         await renderTagResults("", item);
//     //     }
//     // });
// }

// async function renderTagResults(likeName, item) {
//     if (likeName === "") {
//         return;
//     }
//     const res = await fetch(`/api/tags?q=${encodeURIComponent(likeName)}`);
//     const tags = await res.json();
//     const container = document.getElementById("tag-results");
//     container.innerHTML = "";
//
//     tags.forEach(tag => {
//         const btn = document.createElement("button");
//         btn.className = "tag-result";
//         btn.textContent = tag.name;
//         btn.onclick = () => addTag(item, tag.name);
//         container.appendChild(btn);
//     });
//
//     if (likeName && !tags.find(t => t.name === likeName)) {
//         const btn = document.createElement("button");
//         btn.className = "tag-result";
//         btn.textContent = `+ Create "${likeName}"`;
//         btn.onclick = () => addTag(item, likeName);
//         container.appendChild(btn);
//     }
// }

// async function addTag(item, tagName) {
//     await fetch(`/api/tags/${item.id}`, {
//         method: "POST",
//         headers: { "Content-Type": "application/json" },
//         body: JSON.stringify({ tag: tagName })
//     });
//     document.getElementById("tag-popup").style.display = "none";
//     await loadTags(item);
// }
//
