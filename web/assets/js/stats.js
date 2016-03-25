var pages = window.pages || {};
pages.stats = {
    controlsInited : false,
    tableElement : null,
    renderCompleteCallback : null,
    pageIndex : 0,
    pageSize : 10,
    renderPage : function (arguments, renderCompleteCallback) {
        var me = pages.stats;
        $('#stats.page').css({
            display : 'block'
        });
        me.initControls();
        me.renderCompleteCallback = renderCompleteCallback;
        me.getSearches(me.populateResults);
    },
    getSearches : function (callback) {
        var me = pages.stats;
        getTopSearches(me.pageSize, me.pageIndex * me.pageSize, callback);
    },
    populateResults : function (results) {
        var me = pages.stats;
        me.tableElement.children().remove();
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
                searchQueryLink += "type/" + search.queries[b].type + "/query/" + search.queries[b].query + "/";
                searchQuery += search.queries[b].query == "" ? "Nothing" : search.queries[b].query;
            }
            var row = $("<tr>");
            var queryElement = $("<td>");
            queryElement.append("<a href=\"" + searchQueryLink + "\">" + searchQuery + "</a>");
            var countElement = $("<td>");
            countElement.append("<p>" + search.searchCount + "</p>");
            row.append(queryElement, countElement);
            me.tableElement.append(row);
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