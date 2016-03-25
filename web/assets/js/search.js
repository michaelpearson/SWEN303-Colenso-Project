var pages = window.pages || {};
pages.search = {
    init : false,
    completeCallback : null,
    pageNumber : 1,
    documentCache : [],
    queryResultCount : 0,
    queryResultAllId : "",
    nestedSearchElements : [],
    nestedSearchElementHtmlTemplate : '<div class="ui right labeled input action nested-search-container">\n    <div class="search-input">\n        <input type="text" placeholder="Search term">\n    </div>\n    <div class="ui right labeled input action button-container">\n        <div class="ui dropdown label">\n            <div class="text search-type-selector">Simple search</div>\n                <i class="dropdown icon"></i>\n                <div class="menu">\n                <div class="item">Simple search</div>\n                <div class="item">Logical search</div>\n                <div class="item">XQuery search</div>\n            </div>\n        </div>\n    </div>\n</div>',
    queryValue : function (nestedIndex, val) {
        var me = pages.search;
        if(nestedIndex === undefined) {
            var build = [];
            for(var a = 0;a < me.nestedSearchElements.length;a++) {
                build.push(me.queryValue(a, val));
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
                return el.text(val).text();
            } else {
                return el.val(val).val();
            }
        }
    },
    queryType : function (nestedIndex, val) {
        var me = pages.search;
        if(nestedIndex === undefined) {
            var build = [];
            for(var a = 0;a < me.nestedSearchElements.length;a++) {
                build.push(me.queryType(a, val));
            }
            return build;
        }
        if(val) {
            var type = me.typeValueToLabel(val);
            me.nestedSearchElements[nestedIndex].find('.search-type-selector').html(type);
        }
        return me.typeLabelToValue(me.nestedSearchElements[nestedIndex].find('.search-type-selector').text());
    },
    typeLabelToValue : function (label) {
        switch(label) {
            default:
            case 'Simple search':
                return 'fulltext';
            case 'Logical search':
                return 'logical';
            case 'XQuery search':
                return 'xquery';
        }
    },
    typeValueToLabel : function (value) {
        switch (value) {
            default:
                return "Simple search";
            case 'xquery':
                return "XQuery search";
            case 'logical':
                return "Logical search";
        }
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
            me.addNestedSearch(pageArguments.type[a] || me.typeLabelToValue(), pageArguments.query[a] || "");
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
            var options = $('<td class="option-panel"><a href="#/edit/id/' + documents[a].id + '"><i class="icon edit"></i></a></td>');
            var viewCount = $('<td><p>' + (documents[a].viewCount || 0) + '</p></td>');

            var href = app.encodeHash("document", {
                id : documents[a].id,
                title : documents[a].title
            });
            title.find('a').text(documents[a].title).attr('href', href);

            date.text(documents[a].date == "" ? "Unknown" : documents[a].date);

            row.append(title, date, viewCount, options);
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
        if(Array.isArray(queryType)) {
            var url = "/api/search?chained=1&download=1";
            for(var a = 0;a < queryType.length;a++) {
                url += "&type[]=" + encodeURIComponent(queryType[a]) + "&query[]=" + encodeURIComponent(query[a]);
            }
            return url;
        } else {
            return "/api/search?type=" + encodeURIComponent(queryType) + "&query=" + encodeURIComponent(query) + "&download=1";
        }
    },
    updateSearchBox : function (nestedIndex, query, type) {
        var me = pages.search;
        var element = me.nestedSearchElements[nestedIndex];
        var searchContainer = element.find('.search-input');
        query = query || me.queryValue(nestedIndex);
        type = type || me.queryType(nestedIndex);
        searchContainer.children().remove();
        if(type == "xquery") {
            searchContainer.append('<textarea class="input" style="resize: none;"></textarea>');
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
        me.queryType(nestedIndex, type);
    },
    addNestedSearch : function (type, query) {
        var me = pages.search;
        type = type || me.typeLabelToValue();
        query = query || "";

        var searchContainer = $('#search-box');
        var index = me.nestedSearchElements.length;
        me.nestedSearchElements[index] = $(me.nestedSearchElementHtmlTemplate);
        $(me.nestedSearchElements[index].find('.dropdown')).dropdown({
            onHide : me.updateSearchBox.bind(pages.search, index)
        });
        searchContainer.append(me.nestedSearchElements[index]);
        me.updateSearchBox(index, query, type);
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