function searchDocument(searchType, searchQuery, callback) {
    $.ajax("/api/search", {
        data : {
            type : searchType || "",
            query : searchQuery || ""
        },
        success : callback || function () {}
    });
}
function getDocument(documentId, callback) {
    $.ajax("/api/document", {
        data : {
            documentId : documentId || ""
        },
        success : callback || function () {}
    });
}