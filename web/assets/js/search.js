var search = {
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
    decodePageArguments : function (pageArguments) {
        var type;
        if(pageArguments.type) {
            switch(pageArguments.type) {
                default:
                    pageArguments.type = "Simple search";
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

        search.queryValue(pageArguments.query || "");


    },
    renderPage : function (arguments, renderCompleteCallback) {
        $('#search.page').css({
            display : 'block'
        });
        search.decodePageArguments(arguments);

        $('.ui.dropdown').dropdown({
            onChange : function (value) {
                var searchContainer = $('#search-input');
                var query = search.queryValue();
                searchContainer.children().remove();
                if(value == "xquery search") {
                    searchContainer.append('<div placeholder="Search term" class="input" contenteditable="true"></div>');
                } else {
                    searchContainer.append('<input type="text" placeholder="Search term" id="search-input">');
                }
                search.queryValue(query);
            }
        });
        $('#button-search').click(function () {
            var query = search.queryValue();
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
            console.log({query : query, type : type});
        });


        renderCompleteCallback();
    }
};