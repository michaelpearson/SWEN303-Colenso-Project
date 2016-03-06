var pages = window.pages || {};
pages.home = {
    renderPage : function (arguments, renderCompleteCallback) {
        $('#home.page').css({
            display : 'block'
        });
        renderCompleteCallback();
        //setTimeout(renderCompleteCallback, 1000);
    }
};