var pages = window.pages || {};
pages.upload = {
    renderPage : function (arguments, renderCompleteCallback) {
        $('#upload.page').css({
            display : 'block'
        });
        this.initControls();
        renderCompleteCallback();
    },
    initControls : function () {
        $('#uploadSubmitButton').click(function(){
            var formData = new FormData($('#formUpload')[0]);
            $.ajax({
                url: '/api/addDocument',
                type: 'POST',
                xhr: function() {
                    var myXhr = $.ajaxSettings.xhr();
                    if(myXhr.upload){ // Check if upload property exists
                        myXhr.upload.addEventListener('progress',function () {
                            console.log("Progress");
                        }, false);
                        //myXhr.setRequestHeader("content-type", "multipart/form-data");
                    }
                    return myXhr;
                },
                success: function () {
                    console.log("Success");
                },
                error: function () {
                    console.log("Error");
                },
                data: formData,
                cache: false,
                processData : false,
                contentType: false
            });
        });
    }
};