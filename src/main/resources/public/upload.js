document.addEventListener("DOMContentLoaded", init);

function init() {
    const preview = document.getElementById("preview");
    const input = document.getElementById("input");
    const button = document.getElementById("submit-button")

    button.addEventListener("click", () => {
        for (const file of input.files) {
            sendFileToServer(file);
        }
    });

    input.addEventListener("change", () => {
        preview.replaceChildren();
        console.log("CURRENT FILES");
        for (const file of input.files) {
            const item = createNewPreviewItem(file);
            preview.appendChild(item);
        }
    });
}

function createNewPreviewItem(file) {
    const item = document.createElement("div");
    item.className = "preview-item";
    const img = document.createElement("img");
    img.src = URL.createObjectURL(file);
    item.appendChild(img);
    return item;
}

function sendFileToServer(file) {
    const formData = new FormData();
    formData.append("file", file);
    fetch(`/api/images`, {
        method: "POST",
        body: formData
    });
}
