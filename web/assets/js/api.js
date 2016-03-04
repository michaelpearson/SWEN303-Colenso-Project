function searchDocument(searchType, searchQuery, callback, errorCallback) {
    $.ajax("/api/search", {
        data : {
            type : searchType || "",
            query : searchQuery || ""
        },
        success : callback || function () {},
        statusCode : {
            500 : function (error) {
                if(typeof errorCallback == 'function') {
                    errorCallback(error.responseJSON);
                }
            }
        }
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