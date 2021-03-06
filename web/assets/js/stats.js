var pages = window.pages || {};
pages.stats = {
    controlsInited : false,
    tableElement : null,
    renderCompleteCallback : null,
    page : 1,
    pageSize : 10,
    renderPage : function (arguments, renderCompleteCallback) {
        var me = pages.stats;
        $('#stats.page').css({
            display : 'block'
        });
        me.initControls();
        me.renderCompleteCallback = renderCompleteCallback;
        me.getSearches(undefined, function (result) {
            me.populateResults(me.tableElement, result);
        });
        me.getSearches(0 , function (result) {
            me.populateResults($('#my-stat-result-body'), result);
        });
    },
    getSearches : function (memberId, callback) {
        var me = pages.stats;
        getTopSearches(me.pageSize, me.page, memberId, callback);
    },
    populateResults : function (element, results) {
        var me = pages.stats;
        element.children().remove();
        results = results || {};
        results.searches = results.searches || [];
        for(var a = 0;a < results.searches.length;a++) {
            var search = results.searches[a];
            search.queries = search.queries || [];
            var searchQuery = "";
            var searchQueryLink = "#/search/count/50/page/1/";
            for(var b = 0; b < search.queries.length; b++) {
                if(searchQuery != "") {
                    searchQuery += "<br>AND<br>";
                }
                searchQueryLink += "type/" + search.queries[b].type + "/query/" + encodeURIComponent(search.queries[b].query) + "/";
                searchQuery += search.queries[b].query == "" ? "Nothing" : search.queries[b].query;
            }
            var row = $("<tr>");
            var queryElement = $("<td>");
            queryElement.append("<a href=\"" + searchQueryLink + "\">" + searchQuery.replace(/\n/gi, "<br>") + "</a>");
            var countElement = $("<td>");
            countElement.append("<p>" + search.searchCount + "</p>");
            row.append(queryElement, countElement);
            element.append(row);
        }
        if(me.renderCompleteCallback != null) {
            me.renderCompleteCallback();
            me.renderCompleteCallback = null;
        }
    },
    initControls : function () {
        var me = pages.stats;
        if(me.controlsInited) {
            return;
        }
        me.controlsInited = true;
        me.tableElement = $('#stat-result-body');
    }
};