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

function updateXmlDocument(xml, id, callback) {
    $.ajax("/api/saveDocument?id=" + id + "&save=1", {
        method : "post",
        data : {
            xml : xml
        },
        success : callback.bind(callback, true),
        error : callback.bind(callback, false)
    });
}

function getTopSearches(count, page, memberId, callback) {
    $.ajax("/api/getSearchStats", {
        error : function () {
            alert("An error occured communicating with the server");
        },
        success : callback,
        method: "get",
        data : {
            count : count || 10,
            page : page || 1,
            memberid : memberId === undefined ? -1 : memberId || 0
        }
    });
}

function tagDocuments(tagType, tagValue, documents, callback) {
    $.ajax("/api/tagDocuments", {
        method: "post",
        data : {
            ids : documents || [],
            tagValue : tagValue || "",
            tagType : tagType || ""
        },
        success : callback
    });
}