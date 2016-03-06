var pages = window.pages || {};
pages.upload = {
    renderPage : function (arguments, renderCompleteCallback) {
        $('#upload.page').css({
            display : 'block'
        });
        renderCompleteCallback();
    }
};