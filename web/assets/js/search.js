var search = {
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
        return search.pageNumber;
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
        search.updateSearchBox();
        search.queryValue(pageArguments.query || "");
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
    },
    populateResults : function () {
        var documents = search.documentCache;
        $('.documents-found').text(search.queryResultCount + " document" + (search.queryResultCount > 1 || search.queryResultCount == 0 ? "s" : ""));
        var resultBody = $('#result-body');
        resultBody.children().remove();
        for(var a = 0;a < documents.length;a++) {
            var row = $('<tr></tr>');
            var title = $('<td><a></a></td>');
            var date = $('<td></td>');

            title.find('a').text(documents[a].title).attr('href', "#/document/id/" + documents[a].id);

            date.text(documents[a].date);

            row.append(title, date);
            resultBody.append(row);
        }
        search.syncPagingControls();
    },
    runQuery : function () {
        var queryType = this.queryType();
        var query = this.queryValue();
        searchDocument(queryType, query, search.getPageNumber(), search.getPageSize(), function (documents) {
            search.completeCallback();
            search.completeCallback = null;
            if(documents && documents.documents && documents.documents.length) {
                search.documentCache = documents.documents;
                search.queryResultCount = documents.total;
            } else {
                search.documentCache = [];
                search.queryResultCount = 0;
            }
            search.populateResults();
        }, function (data) {
            search.completeCallback();
            if(data && data.error) {
                alert(data.message);
            }
        });
    },
    updateSearchBox : function (value) {
        var searchContainer = $('#search-input');
        var query = search.queryValue();
        var type = value || search.queryType();
        searchContainer.children().remove();
        if(type.indexOf("xquery") > -1) {
            searchContainer.append('<div placeholder="Search term" class="input" contenteditable="true"></div>');
        } else {
            searchContainer.append('<input type="text" placeholder="Search term" id="search-input">');
        }
        search.queryValue(query);
    },
    initControls : function () {
        if(search.init) {
            return;
        }
        search.init = true;
        $('.ui.dropdown').dropdown({
            onChange : search.updateSearchBox.bind(this)
        });

        $('#button-search').click(function () {
            search.pageNumber = 1;
            search.submit();
        });

        $('.page-size button').click(function() {
            $(this).addClass('active').siblings().removeClass('active');
            search.submit();
        });
        $('.pagination a.icon').click(function() {
            if($(this).find('.left')[0]) {
                if(search.pageNumber <= 1) {
                    return;
                }
                search.pageNumber -= 1;
            } else {
                if(search.pageNumber >= parseInt(Math.ceil(search.queryResultCount / search.getPageSize()))) {
                    return;
                }
                search.pageNumber += 1;
            }
            search.submit();
        });
    },
    renderPage : function (arguments, renderCompleteCallback) {
        $('#search.page').css({
            display : 'block'
        });
        $('.documents-found').text("");
        search.pageNumer = arguments.page || 1;
        search.completeCallback = renderCompleteCallback;
        search.initControls(arguments);
        search.decodePageArguments(arguments);
        search.runQuery();
    },
    syncPagingControls : function () {
        var totalToShow = 3;
        var pages = $('.pagination');
        pages.find('a:not(.icon)').remove();
        var totalPages = parseInt(Math.ceil(search.queryResultCount / search.getPageSize()));
        var currentPage = search.getPageNumber();
        var firstPageToShow = currentPage - totalToShow;
        var lastToShow = currentPage + totalToShow;
        if(lastToShow > totalPages) {
            lastToShow = totalPages;
        }
        if(firstPageToShow < 1) {
            firstPageToShow = 1;
        }
        var previousElement = pages.find('a').first();
        for(var a = firstPageToShow; a <= lastToShow;a++) {
            previousElement = $('<a class="item' + (currentPage == a ? ' active' : '') + '">' + a + '</a>').insertAfter(previousElement);
        }
        $('.pagination a:not(.icon)').click(function() {
            search.pageNumber = parseInt($(this).text());
            search.submit();
        });
    },
    submit : function () {
        var queryType = search.queryType();
        var query = search.queryValue();
        var pageSize = search.getPageSize();
        var pageNumber = search.getPageNumber();
        var oldHash = window.location.hash;
        var newHash = "#/search/type/" + queryType + "/query/" + query + "/count/" + pageSize + "/page/" + pageNumber;
        if(oldHash == newHash) {
            app.renderPage();
        } else {
            window.location.hash = newHash;
        }
    }
};