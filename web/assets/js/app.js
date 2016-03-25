$(function () {
    $(window).on('hashchange', function (e) {
        app.renderPage();
    }).trigger('hashchange');
    $(window).on('resize', function () {
        if(pages[app.currentPage] && pages[app.currentPage].resize) {
            pages[app.currentPage].resize();
        }
    });
});
// Array Remove - By John Resig (MIT Licensed)
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};
var pages = window.pages || {};
var app = {
    disableNavigation : false,
    currentHash : "",
    currentPage : null,
    decodeHash : function() {
        var me = app;
        try {
            var hash = window.location.hash.match(/^#?(.+)$/)[1];
        } catch (e) {
            hash = "/home"
        }
        var arguments = hash.split("/");
        var argumentMap = {};
        var pageName = "home";
        for (var a = 1; a < arguments.length; a++) {
            if (a == 1) {
                pageName = decodeURIComponent(arguments[a]);
                continue;
            }
            if (a < arguments.length - 1) {
                var key = decodeURIComponent(arguments[a]);
                var value = decodeURIComponent(arguments[a + 1]);
                if(argumentMap[key] !== undefined) {
                    if(!Array.isArray(argumentMap[key])) {
                        argumentMap[key] = [argumentMap[key]];
                    }
                    argumentMap[key].push(value);
                } else {
                    argumentMap[key] = value;
                }
                a++;
            }
        }
        var build = {
            arguments : argumentMap,
            pageName : pageName == "" ? "home" : pageName,
            previousHash : me.currentHash
        };
        me.currentHash = hash;
        return build;
    },
    encodeHash : function(pageName, pageArguments, setHash) {
        setHash = setHash || false;
        var hash = "#/" + pageName;
        for (var k in pageArguments) {
            if (!pageArguments.hasOwnProperty(k)) {
                continue;
            }
            if(Array.isArray(pageArguments[k])) {
                for(var a = 0;a < pageArguments[k].length;a++) {
                    hash += '/' + k + '/' + encodeURIComponent(pageArguments[k][a]);
                }
            } else {
                hash += '/' + k + '/' + encodeURIComponent(pageArguments[k]);
            }
        }
        if (setHash) {
            window.location.hash = hash;
        }
        return hash;
    },
    renderPage : function() {
        if(app.disableNavigation) {
            return;
        }
        var pageState = app.decodeHash();
        $('div.menu a.item.active').removeClass("active");
        $('a[href="#/' + pageState.pageName + '"]').addClass("active");
        $('.page').css({
            display: 'none'
        });
        if (pages[pageState.pageName] && pages[pageState.pageName].renderPage) {
            app.currentPage = pageState.pageName;
            app.beginNavigation();
            pages[pageState.pageName].renderPage(pageState.arguments, app.endNavigation.bind(this), pageState.previousHash);
            if(pages[pageState.pageName].resize) {
                pages[pageState.pageName].resize();
            }
        }
    },
    beginNavigation : function() {
        $('.loadingbar').remove();
        var bar = $('<div class="loadingbar"></div>');
        bar.animate({
            width: "90%"
        }, {
            duration: 1500
        });
        $('body').append(bar);
    },
    endNavigation : function(final) {
        var bar = $('.loadingbar');
        final = final || false;
        if (final) {
            bar.remove();
        } else {
            bar.animate({
                width: "100%",
                opacity: 0
            }, {
                duration: 500,
                queue: false,
                complete: app.endNavigation.bind(this, true)
            });
        }
    }
};