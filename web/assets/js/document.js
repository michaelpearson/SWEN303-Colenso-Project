var pages = pages || {};
pages.document = {
    renderCompleteCallback : null,
    controlsInitialised : false,
    titleElement : null,
    documentCache : {},
    documentId : 0,
    renderPage : function (pageArguments, renderCompleteCallback) {
        var me = pages.document;
        $('#document.page').css({
            display : 'block'
        });
        me.renderCompleteCallback = renderCompleteCallback;
        me.initControls();
        if(pageArguments.title) {
            me.title(pageArguments.title);
        } else {
            me.title("Loading document");
        }
        $('#document-view').children().remove();
        me.loadDocument(pageArguments.id, me.setDocument.bind(me));
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
        var me = pages.document;
        $('#edit-document-link').attr('href', "#/edit/id/" + me.documentId);
        $('#document-download-link').attr('href', "/api/search?type=id&query=" + me.documentId + "&download=1");
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
        me.titleElement.text($('h1.maintitle').html());
        if(me.renderCompleteCallback != null) {
            me.renderCompleteCallback();
            me.renderCompleteCallback = null;
        }
    },
    loadDocument : function (documentId, loadComplete) {
        var me = this;
        me.documentId = documentId;
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