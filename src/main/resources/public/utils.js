let page = 0;
let loading = false;

const TAG_HEIGHT = 20;
const TAG_MARGIN = 2;

window.addEventListener("DOMContentLoaded", () => {
    const usericon = document.getElementById("usericon");
    usericon.addEventListener("click", () => {
        window.location.href = "profile.html";
    });

    const tag_search = document.getElementById("tag-search");
    const tag_results = document.getElementById("tag-results");

    tag_search.addEventListener("input", async () => {
        tag_results.replaceChildren();
        const input = tag_search.value;
        if (input == "") {
            return;
        }
        const res = await fetch(`/api/tags?query=${input}`);
        const tags = await res.json();
        var exact_match_found = false;
        tags.forEach(tag => {
            if (tag.name == input) {
                exact_match_found = true;
            }
            const tag_result = document.createElement("button");
            tag_result.addEventListener("click", () => addTagToContent(tag.name));
            tag_result.innerHTML = tag.name;
            tag_result.classList.add("tag-result", "tag");
            tag_results.appendChild(tag_result);
        });
        if (!exact_match_found) {
            const new_tag_result = document.createElement("button");
            new_tag_result.innerHTML = "+"+input;
            new_tag_result.classList.add("tag-result", "tag");
            new_tag_result.addEventListener("click", async () => {
                await createNewTag(input);
                await addTagToContent(input);
            })
            tag_results.insertBefore(new_tag_result, tag_results.firstChild);
        }
    })
});

document.addEventListener("scroll", () => {
    if (document.getElementById("gallery").offsetHeight <= (window.innerHeight + window.scrollY)) {
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

var GALLERY = null;
var cur_content_con= null;
var cur_date_con= null;
var current_date = null;

function initGallery() {
    GALLERY = document.getElementById("gallery");
}

function newContainer(date) {
    const date_con = document.createElement("div");
    const date_elem = document.createElement("h1");
    const content_con = document.createElement("div");
    content_con.className = "content-container";
    date_con.className = "date-container";
    date_elem.className = "date";
    date_elem.innerHTML = date
    date_con.appendChild(date_elem);
    date_con.appendChild(content_con);
    return [date_con, content_con];
}

function newContentThumbnail(item) {
    const media_item = document.createElement("div");
    media_item.className = "media-item";
    const thumbnail = document.createElement("img");
    thumbnail.src = item.type === "video" 
        ? `/api/videos/${item.id}/thumbnail`
        : `/api/images/${item.id}/thumbnail`;
    thumbnail.alt = item.title || "untitled";
    thumbnail.loading = "lazy"
    thumbnail.className = "media-item";
    thumbnail.addEventListener("click", () => openLightbox(item));

    media_item.appendChild(thumbnail);

    if (item.type === "video") {
        const play = document.createElement("div");
        play.className = "play-button";
        media_item.appendChild(play);
    }

    return media_item;
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
    if (getDate(item.taken_at) != current_date) {
        current_date = getDate(item.taken_at);
        [cur_date_con, cur_content_con] = newContainer(current_date);
        GALLERY.appendChild(cur_date_con);
    }

    const thumbnail = newContentThumbnail(item);
    cur_content_con.appendChild(thumbnail);
    return thumbnail;
}

async function loadContent() {
    if (loading) return;
    loading = true;

    const sortBy = document.getElementById("sortBy").value;
    const ordering = document.getElementById("ordering").value;

    const res = await fetch(`/api/gallery?page=${page}&sortBy=${sortBy}&ordering=${ordering}`);
    const content = await res.json();
    if (content.length === 0) { loading = false; return; }

    content.forEach( (item) => {
        const item_elem = addContentToGallery(item);
        item.element = item_elem;
    });


    page++;
    loading = false;

    if (document.getElementById("gallery").offsetHeight <= (window.innerHeight + window.scrollY)) {
        loadContent();
    }
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
    const addtag_popup = document.getElementById("addtag-popup");

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
            if (document.getElementById("addtag-popup").style.display != "none") {
                closeAddTagPopup();
            }
            else {
                closeMenuPopup();
                closeLightbox();
            }
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
                    toggleAddTagPopup();
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
            closeAddTagPopup();
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

async function reloadTags(item) {
    document.getElementById("lightbox-tags").innerHTML = '';
    await loadTags(item);
}

async function loadTags(item) {
    const res = await fetch(`/api/tags/${item.id}`);
    const tags = await res.json();
    tags.sort((a,b) => a.name.localeCompare(b.name))
    tags.forEach(tag => {
        addTagToDisplay(tag);
    });
}

async function createNewTag(tagName) {
    await fetch(`/api/tags`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ tag: tagName })
    });
}

async function addTagToContent(tagName) {
    await fetch(`/api/tags/${currentItem.id}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ tag: tagName })
    });
    reloadTags(currentItem);
}

function closeAddTagPopup() {
    const addtag_popup = document.getElementById("addtag-popup");
    addtag_popup.style.display = "none";
}

function openAddTagPopup() {
    const addtag_popup = document.getElementById("addtag-popup");
    addtag_popup.style.display = "flex";
}

function toggleAddTagPopup() {
    const addtag_popup = document.getElementById("addtag-popup");
    if (addtag_popup.style.display == "flex") {
        addtag_popup.style.display = "none";
    }
    else {
        addtag_popup.style.display = "flex";
    }
}

/*
 * =============================================================================
 *                             Content Manipulation
 * =============================================================================
 */

function removeMediaTryContainer(elem) {
    const content_con = elem.parentElement;
    const date_con = content_con.parentElement;
    elem.remove();
    if (content_con.children.length == 0) {
        date_con.remove();
    }
}

function deleteContent(item) {
    console.log(item);
    if (item.type === "image") {
        fetch(`/api/images/${item.id}/delete`);
        removeMediaTryContainer(item.element);
    }
    else if (item.type === "video") {
        fetch(`/api/videos/${item.id}/delete`);
        removeMediaTryContainer(item.element);
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
