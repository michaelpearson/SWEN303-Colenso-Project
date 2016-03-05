$(function () {
    function decodeHash() {
        try {
            var hash = window.location.hash.match(/^#?(.+)$/)[1];
        } catch (e) {
            hash = "/home"
        }
        var arguments = hash.split("/");
        var argumentMap = {};
        var pageName = "home";
        for(var a = 1;a < arguments.length;a++) {
            if(a == 1) {
                pageName = decodeURIComponent(arguments[a]);
                continue;
            }
            if(a < arguments.length - 1) {
                argumentMap[decodeURIComponent(arguments[a])] = decodeURIComponent(arguments[a + 1]);
                a++;
            }
        }
        return {
            arguments: argumentMap,
            pageName : pageName == "" ? "home" : pageName
        };
    }
    function encodeHash(pageName, pageArguments, setHash) {
        setHash = setHash || false;
        var hash = "/" + pageName;
        for(var k in pageArguments) {
            if(!pageArguments.hasOwnProperty(k)) {
                continue;
            }
            hash += '/' + k + '/' + pageArguments[k];
        }
        if(setHash) {
            window.location.hash = hash;
        }
        return hash;
    }

    $(window).on('hashchange', function () {
        renderPage();
    }).trigger('hashchange');

    function renderPage() {
        var pageState = decodeHash();
        $('div.menu a.item.active').removeClass("active");
        $('a[href="#/' + pageState.pageName + '"]').addClass("active");
        $('.page').css({
            display : 'none'
        });
        console.log(pageState.pageName);
        if(window[pageState.pageName] && window[pageState.pageName].renderPage) {
            beginNavigation();
            window[pageState.pageName].renderPage(pageState.arguments, endNavigation.bind(this));
        }
    }

    function beginNavigation() {
        console.log("Begin navigation");
        $('.loadingbar').remove();
        var bar = $('<div class="loadingbar"></div>');
        bar.animate({
            width : "90%"
        }, {
            duration : 1500
        });
        $('body').append(bar);
    }


    function endNavigation(final) {
        var bar = $('.loadingbar');
        final = final || false;
        if(final) {
            console.log("end navigation");
            bar.remove();
        } else {
            bar.animate({
                width: "100%",
                opacity : 0
            }, {
                duration : 500,
                queue : false,
                complete : endNavigation.bind(this, true)
            });
        }
    }
});