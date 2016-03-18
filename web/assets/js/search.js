var pages = window.pages || {};
pages.search = {
    init : false,
    completeCallback : null,
    pageNumber : 1,
    documentCache : [],
    queryResultCount : 0,
    queryResultAllId : "",
    nestedSearchElements : [],
    nestedSearchElementHtmlTemplate : '<div class="ui right labeled input action nested-search-container">\n    <div class="search-input">\n        <input type="text" placeholder="Search term">\n    </div>\n    <div class="ui right labeled input action button-container">\n        <div class="ui dropdown label">\n            <div class="text search-type-selector">Simple search</div>\n                <i class="dropdown icon"></i>\n                <div class="menu">\n                <div class="item">Simple search</div>\n                <div class="item">Logical search</div>\n                <div class="item">XQuery search</div>\n            </div>\n        </div>\n        <!--\n        <button class="ui button primary" id="button-search">Search</button>\n        -->\n    </div>\n</div>',
    queryValue : function (nestedIndex, val) {
        var me = pages.search;
        if(nestedIndex === undefined) {
            var build = [];
            for(var a = 0;a < me.nestedSearchElements.length;a++) {
                build.push(me.queryValue(a));
            }
            return build;
        }
        var el = me.nestedSearchElements[nestedIndex].find('.search-input *');
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
    queryType : function (nestedIndex) {
        var me = pages.search;
        if(nestedIndex === undefined) {
            var build = [];
            for(var a = 0;a < me.nestedSearchElements.length;a++) {
                build.push(me.queryType(a));
            }
            return build;
        }
        var type = me.nestedSearchElements[nestedIndex].find('.search-type-selector').text();
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
        var me = pages.search;
        if(!Array.isArray(pageArguments.type)) {
            pageArguments.type = [pageArguments.type];
            pageArguments.query = [pageArguments.query];
        }
        for(var a = 0;a < pageArguments.type.length; a++) {
            var type;
            if (pageArguments.type[a]) {
                switch (pageArguments.type[a]) {
                    default:
                        type = "Simple search";
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
            me.addNestedSearch(type, pageArguments.query[a] || "");
        }
        if (pageArguments.count) {
            $('.page-size').children().each(function () {
                if ($(this).text() == pageArguments.count) {
                    $(this).addClass("active");
                } else {
                    $(this).removeClass("active");
                }
                return (true);
            });
        }
        if (!pageArguments.page) {
            me.pageNumber = 1;
        }
    },
    populateResults : function () {
        var documents = pages.search.documentCache;
        $('.documents-found').text(pages.search.queryResultCount + " document" + (pages.search.queryResultCount > 1 || pages.search.queryResultCount == 0 ? "s" : ""));
        $('.documents-found-download').attr('href', pages.search.getDownloadLink());
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
    runQuery : function (download) {
        download = download === true;
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
                pages.search.queryResultAllId = documents.allDocumentIDs;
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
    getDownloadLink : function () {
        var queryType = this.queryType();
        var query = this.queryValue();
        return "/api/search?type=" + encodeURIComponent(queryType) + "&query=" + encodeURIComponent(query) + "&download=1";
    },
    updateSearchBox : function (nestedIndex) {
        var me = pages.search;
        var element = me.nestedSearchElements[nestedIndex];
        var searchContainer = element.find('.search-input');
        var query = me.queryValue(nestedIndex);
        var type = me.queryType(nestedIndex);
        searchContainer.children().remove();
        if(type.indexOf("xquery") > -1) {
            searchContainer.append('<div class="input" contenteditable="true"></div>');
        } else {
            var el = searchContainer.append('<input type="text" placeholder="Search term" id="search-input">');
            el.off("keydown");
            el.keydown(function (event) {
                if(event.keyCode == 13) {
                    me.pageNumber = 1;
                    me.submit();
                }
            });
        }
        me.queryValue(nestedIndex, query);
    },
    addNestedSearch : function (type, query) {
        type = type || "Simple Search";
        query = query || "";

        console.log("TODO");
        var searchContainer = $('#search-box');
        var me = pages.search;
        var index = me.nestedSearchElements.length;
        me.nestedSearchElements[index] = $(me.nestedSearchElementHtmlTemplate);
        $(me.nestedSearchElements[index].find('.dropdown')).dropdown({
            onHide : me.updateSearchBox.bind(pages.search, index)
        });
        searchContainer.append(me.nestedSearchElements[index]);
        me.updateSearchBox(index);
    },
    removeNestedSearch : function (force) {
        var me = pages.search;
        var index = me.nestedSearchElements.length - 1;
        if(index > 0 || force === true) {
            me.nestedSearchElements[index].remove();
            me.nestedSearchElements.remove(index);
        }
    },
    initControls : function () {
        var me = pages.search;
        while(me.nestedSearchElements.length > 0) {
            me.removeNestedSearch(true);
        }
        if(pages.search.init) {
            return;
        }
        pages.search.init = true;

        $('#search-nested-add').click(pages.search.addNestedSearch.bind(pages.search));
        $('#search-nested-remove').click(pages.search.removeNestedSearch.bind(pages.search));

        $('#search-submit').click(function () {
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
        if(oldHash == newHash) {
            app.renderPage();
        } else {
            window.location.hash = newHash;
        }
    }
};