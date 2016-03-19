var pages = window.pages || {};
pages.edit = {
    documentId : null,
    initComplete : false,
    textAreaElement : null,
    renderCompleteCallback : null,
    codeMirrorObject : null,
    renderPage : function (pageArguments, renderCompleteCallback) {
        var me = pages.edit;
        me.renderCompleteCallback = renderCompleteCallback;
        if(!pageArguments.id) {
            window.location.href = "#";
            return;
        }
        me.documentId = pageArguments.id;
        $('#edit.page').css({
            display : 'block'
        });
        me.initControls();
        getDocumentXml(me.documentId, me.populateTextArea);
    },
    initControls : function () {
        var me = pages.edit;
        if(me.initComplete) {
            return;
        }
        me.initComplete = true;
        $('#edit.page').find('.btn-save').click(function () {
            $.ajax("/api/saveDocument?id=" + me.documentId, {
                method : "post",
                data : {
                    xml : "Test data"
                }
            });
        });
        me.textAreaElement = $('#xml-document');
        me.codeMirrorObject = CodeMirror(me.textAreaElement[0], {
            viewportMargin : Infinity,
            lineNumbers: true
        });
    },
    populateTextArea : function (document) {
        var me = pages.edit;
        var xml;
        try {
            var processor = new XSLTProcessor();
            var xsl = new DOMParser().parseFromString("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n    <xsl:output omit-xml-declaration=\"yes\" indent=\"yes\"/>\n    <xsl:template match=\"node()|@*\">\n        <xsl:copy>\n            <xsl:apply-templates select=\"node()|@*\"/>\n        </xsl:copy>\n    </xsl:template>\n</xsl:stylesheet>", "text/xml");
            processor.importStylesheet(xsl);
            var result = processor.transformToFragment(document, window.document);
            xml = '<?xml version="1.0" encoding="UTF-8"?>\n' + new XMLSerializer().serializeToString(result);
        } catch (e) {
            xml = new XMLSerializer().serializeToString(document);
        }
        me.codeMirrorObject.setValue(xml);
        if(me.renderCompleteCallback !== null) {
            me.renderCompleteCallback();
        }
    },
    getTextAreaContent : function () {

    }
};