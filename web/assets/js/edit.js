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
            updateXmlDocument(me.codeMirrorObject.getValue(), me.documentId, function (success) {
                alert(success ? "Saved successfully" : "Failed to save");
            });
        });
        me.textAreaElement = $('#xml-document');
        me.codeMirrorObject = CodeMirror(me.textAreaElement[0], {
            viewportMargin : Infinity,
            lineNumbers: true,
            lint : {
                getAnnotations : function (documents, callback) {
                    validateXmlDocument(documents, function (response) {
                        var build = [];
                        function addError(severity, line, message) {
                            build.push({
                                message: message,
                                severity: severity,
                                from : {
                                    line : line - 2
                                },
                                to : {
                                    line : line - 1
                                }
                            });
                        }
                        for(var a = 0;a < response.errors.length;a++) {
                            addError("error", response.errors[a].lineNumber, response.errors[a].description);
                        }
                        for(var a = 0;a < response.fatalErrors.length;a++) {
                            addError("error", response.fatalErrors[a].lineNumber, response.fatalErrors[a].description);
                        }
                        for(var a = 0;a < response.warnings.length;a++) {
                            addError("error", response.warnings[a].lineNumber, response.warnings[a].description);
                        }
                        callback(build);
                    });
                },
                async : true
            }
        });
    },
    populateTextArea : function (document) {
        var me = pages.edit;
        me.codeMirrorObject.setValue(document);
        if(me.renderCompleteCallback !== null) {
            me.renderCompleteCallback();
        }
    }
};