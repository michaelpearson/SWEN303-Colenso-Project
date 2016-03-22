function searchDocument(searchType, searchQuery, pageIndex, pageCount, callback, errorCallback) {
    $.ajax("/api/search", {
        data : {
            type : searchType || "",
            query : searchQuery || "",
            page: pageIndex || 1,
            count : pageCount || 50,
            chained : Array.isArray(searchQuery) ? "1" : "0"
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

function getDocumentXml(documentId, callback) {
    $.ajax("/api/document", {
        data : {
            documentId : documentId || "",
            type : "xml"
        },
        dataType: "text",
        success : callback || function () {}
    });
}

function validateXmlDocument(xml, callback) {
    $.ajax("/api/saveDocument", {
        method : "post",
        data : {
            xml : xml,
            save : "0"
        },
        success : callback
    });
}