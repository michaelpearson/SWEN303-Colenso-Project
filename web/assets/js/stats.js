var pages = window.pages || {};
pages.stats = {
    renderPage : function (arguments, renderCompleteCallback) {
        $('#stats.page').css({
            display : 'block'
        });
        renderCompleteCallback();
    }
};