var pages = window.pages || {};
pages.upload = {
    init : false,
    renderPage : function (arguments, renderCompleteCallback) {
        $('#upload.page').css({
            display : 'block'
        });
        this.initControls();
        renderCompleteCallback();
    },
    initControls : function () {
        if(pages.upload.init) {
            return;
        }
        pages.upload.init = true;
        $('#browse').click(function () {
            $('#formUpload').find('input[type=file]').click();
            return false;
        });
        $('#formUpload').find('input[type=file]').change(function () {
            $('#formUpload').find('#uploadSubmitButton').attr('disabled', this.files.length == 0);
            $('#file-name-display').text(this.files.length > 0 ? this.files[0].name : '');
        }).change();

        $('#uploadSubmitButton').click(function(){
            app.beginNavigation();
            var formData = new FormData($('#formUpload')[0]);
            $.ajax({
                url: '/api/addDocument',
                type: 'POST',
                xhr: function() {
                    var myXhr = $.ajaxSettings.xhr();
                    if(myXhr.upload) {
                        myXhr.upload.addEventListener('progress',function () {
                            console.log("Progress");
                        }, false);
                    }
                    return myXhr;
                },
                success: function () {
                    $('#file-name-display').text("File successfully uploaded");

                    app.endNavigation();
                },
                error: function () {
                    $('#file-name-display').text("Error uploading file");
                    app.endNavigation();
                },
                data: formData,
                cache: false,
                processData : false,
                contentType: false
            });
        });
    }
};