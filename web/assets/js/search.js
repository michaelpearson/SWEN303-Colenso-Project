var pages = window.pages || {};
pages.search = {
    init : false,
    completeCallback : null,
    pageNumber : 1,
    documentCache : [],
    queryResultCount : 0,
    queryValue : function (val) {
        var el = $('#search-input *');
        if(val === undefined) {
            if(el[0].tagName == "DIV") {
                return el.text();
            } else {
                return el.val();
            }
        } else {
            if(el[0].tagName == "DIV") {
                return el.text(val);
            } else {
                return el.val(val);
            }
        }
    },
    queryType : function () {
        var type = $('#search-type').text();
        switch(type) {
            default:
            case 'Simple search':
                type = 'fulltext';
                break;
            case 'Logical search':
                type = 'logical';
                break;
            case 'XQuery search':
                type = 'xquery';
                break;
        }
        return type;
    },
    getPageSize : function () {
        return parseInt($('.page-size button.active').text()) || 50;
    },
    getPageNumber : function () {
        return pages.search.pageNumber;
    },
    decodePageArguments : function (pageArguments) {
        var type;
        if(pageArguments.type) {
            switch(pageArguments.type) {
                default:
                    pageArguments.type = "Simple search";
                    break;
                case 'xquery':
                    type = "XQuery search";
                    break;
                case 'logical':
                    type = "Logical search";
                    break;
            }
        } else {
            type = "Simple search";
        }
        $('#search-type').text(type);
        pages.search.updateSearchBox();
        pages.search.queryValue(pageArguments.query || "");
        if(pageArguments.count) {
            $('.page-size').children().each(function () {
                if($(this).text() == pageArguments.count) {
                    $(this).addClass("active");
                } else {
                    $(this).removeClass("active");
                }
                return(true);
            });
        }
        if(!pageArguments.page) {
            pages.search.pageNumber = 1;
        }
    },
    populateResults : function () {
        var documents = pages.search.documentCache;
        $('.documents-found').text(pages.search.queryResultCount + " document" + (pages.search.queryResultCount > 1 || pages.search.queryResultCount == 0 ? "s" : ""));
        var resultBody = $('#result-body');
        resultBody.children().remove();
        for(var a = 0;a < documents.length;a++) {
            var row = $('<tr></tr>');
            var title = $('<td><a></a></td>');
            var date = $('<td></td>');

            var href = app.encodeHash("document", {
                id : documents[a].id,
                title : documents[a].title
            });
            title.find('a').text(documents[a].title).attr('href', href);

            date.text(documents[a].date);

            row.append(title, date);
            resultBody.append(row);
        }
        pages.search.syncPagingControls();
    },
    runQuery : function () {
        var queryType = this.queryType();
        var query = this.queryValue();
        searchDocument(queryType, query, pages.search.getPageNumber(), pages.search.getPageSize(), function (documents) {
            if(pages.search.completeCallback != null) {
                pages.search.completeCallback();
                pages.search.completeCallback = null;
            }
            if(documents && documents.documents && documents.documents.length) {
                pages.search.documentCache = documents.documents;
                pages.search.queryResultCount = documents.total;
            } else {
                pages.search.documentCache = [];
                pages.search.queryResultCount = 0;
            }
            pages.search.populateResults();
        }, function (data) {
            pages.search.completeCallback();
            if(data && data.error) {
                alert(data.message);
            }
        });
    },
    updateSearchBox : function (value) {
        var searchContainer = $('#search-input');
        var query = pages.search.queryValue();
        var type = value || pages.search.queryType();
        searchContainer.children().remove();
        if(type.indexOf("xquery") > -1) {
            searchContainer.append('<div placeholder="Search term" class="input" contenteditable="true"></div>');
        } else {
            var el = searchContainer.append('<input type="text" placeholder="Search term" id="search-input">');
            el.keydown(function (event) {
                if(event.keyCode == 13) {
                    pages.search.pageNumber = 1;
                    pages.search.submit();
                }
            });
        }
        pages.search.queryValue(query);
    },
    initControls : function () {
        if(pages.search.init) {
            return;
        }
        pages.search.init = true;
        $('.ui.dropdown').dropdown({
            onChange : pages.search.updateSearchBox.bind(this)
        });

        $('#button-search').click(function () {
            pages.search.pageNumber = 1;
            pages.search.submit();
        });

        $('.page-size button').click(function() {
            $(this).addClass('active').siblings().removeClass('active');
            pages.search.submit();
        });
        $('.pagination a.icon').click(function() {
            if($(this).find('.left')[0]) {
                if(pages.search.pageNumber <= 1) {
                    return;
                }
                pages.search.pageNumber -= 1;
            } else {
                if(pages.search.pageNumber >= parseInt(Math.ceil(pages.search.queryResultCount / pages.search.getPageSize()))) {
                    return;
                }
                pages.search.pageNumber += 1;
            }
            pages.search.submit();
        });
    },
    renderPage : function (arguments, renderCompleteCallback) {
        $('#search.page').css({
            display : 'block'
        });
        $('.documents-found').text("");
        pages.search.pageNumer = arguments.page || 1;
        pages.search.completeCallback = renderCompleteCallback;
        pages.search.initControls(arguments);
        pages.search.decodePageArguments(arguments);
        pages.search.runQuery();
    },
    syncPagingControls : function () {
        var totalToShow = 3;
        var pageElements = $('.pagination');
        pageElements.find('a:not(.icon)').remove();
        var totalPages = parseInt(Math.ceil(pages.search.queryResultCount / pages.search.getPageSize()));
        var currentPage = pages.search.getPageNumber();
        var firstPageToShow = currentPage - totalToShow;
        var lastToShow = currentPage + totalToShow;
        if(lastToShow > totalPages) {
            lastToShow = totalPages;
        }
        if(firstPageToShow < 1) {
            firstPageToShow = 1;
        }
        var previousElement = pageElements.find('a').first();
        for(var a = firstPageToShow; a <= lastToShow;a++) {
            previousElement = $('<a class="item' + (currentPage == a ? ' active' : '') + '">' + a + '</a>').insertAfter(previousElement);
        }
        $('.pagination a:not(.icon)').click(function() {
            pages.search.pageNumber = parseInt($(this).text());
            pages.search.submit();
        });
    },
    submit : function () {
        var queryType = pages.search.queryType();
        var query = pages.search.queryValue();
        var pageSize = pages.search.getPageSize();
        var pageNumber = pages.search.getPageNumber();
        var oldHash = window.location.hash;
        var newHash = app.encodeHash("search", {
            type : queryType,
            query : query,
            count : pageSize,
            page : pageNumber
        });
        console.log(newHash);
        if(oldHash == newHash) {
            app.renderPage();
        } else {
            window.location.hash = newHash;
        }
    }
};