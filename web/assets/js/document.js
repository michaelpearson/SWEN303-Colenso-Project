var pages = pages || {};
pages.document = {
    renderCompleteCallback : null,
    controlsInitialised : false,
    titleElement : null,
    documentCache : {},
    renderPage : function (pageArguments, renderCompleteCallback) {
        $('#document.page').css({
            display : 'block'
        });
        this.renderCompleteCallback = renderCompleteCallback;
        this.initControls();
        if(pageArguments.title) {
            this.title(pageArguments.title);
        } else {
            this.title("Loading document");
        }
        $('#document-view').children().remove();
        this.loadDocument(pageArguments.id, this.setDocument.bind(this));
    },
    initControls : function () {
        if(this.controlsInitialised) {
            return;
        }
        this.titleElement = $('#document.page').find('div.title h1');
    },
    title : function (title) {
        if(title) {
            this.titleElement.text(title);
            this.titleElement.css({
                display: "block"
            });
            return;
        }
        return this.titleElement.text();
    },
    setDocument : function (document) {
        this.titleElement.css({
            display: "none"
        });
        var documentEl = $('#document-view');
        documentEl.children().remove();
        documentEl.append(document);
        documentEl.find("a[href]").click(function () {
            var href = $(this).attr('href');
            if(href[0] == "#") {
                $('html,body').animate({scrollTop: $(href).offset().top},'slow');
                return false;
            }
            return true;
        });
        if(this.renderCompleteCallback != null) {
            this.renderCompleteCallback();
            this.renderCompleteCallback = null;
        }
    },
    loadDocument : function (documentId, loadComplete) {
        var me = this;
        if(this.documentCache[documentId]) {
            loadComplete(this.documentCache[documentId]);
        }
        getDocument(documentId, function (documentHtml) {
            var doc = (new DOMParser()).parseFromString(documentHtml,"text/html");
            me.documentCache[documentId] = doc.body.innerHTML;
            loadComplete.bind(me)(me.documentCache[documentId]);
        });
    }
};