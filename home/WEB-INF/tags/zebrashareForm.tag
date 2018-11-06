<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="formBean" type="org.zfin.zebrashare.presentation.SubmissionFormBean" required="true" %>

<form:form method="POST" commandName="formBean" class="form-horizontal" id="zebrashareForm" enctype="multipart/form-data">
    <div class="form-group">
        <label class="col-sm-3 control-label">Authors</label>
        <div class="col-sm-8">
            <form:textarea path="authors" cssClass="form-control"/>
            <form:errors path="authors" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Title</label>
        <div class="col-sm-8">
            <form:textarea path="title" cssClass="form-control"/>
            <form:errors path="title" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Abstract</label>
        <div class="col-sm-8">
            <form:textarea path="abstractText" cssClass="form-control" rows="8"/>
            <form:errors path="abstractText" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Submitter Name</label>
        <div class="col-sm-8">
            <form:input path="submitterName" cssClass="form-control"/>
            <form:errors path="submitterName" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Submitter Email</label>
        <div class="col-sm-8">
            <form:input path="submitterEmail" cssClass="form-control"/>
            <form:errors path="submitterEmail" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Lab of Origin</label>
        <div class="col-sm-8">
        <c:choose>
            <c:when test="${!empty labOptions}">
                <form:select path="labZdbId" items="${labOptions}" itemLabel="name" itemValue="zdbID" cssClass="form-control"/>
                <span class="help-block">
                    Don't see your lab listed here? Please
                    <zfin2:mailTo subject="Lab update request">contact us</zfin2:mailTo> to update your information.
                </span>
            </c:when>
            <c:otherwise>
                <p class="text-danger form-control-static">
                    <b>Uh oh!</b> Your profile is not associated with any labs. Please
                    <zfin2:mailTo subject="Lab update request">contact us</zfin2:mailTo> to update your information
                    before proceeding.
                </p>
            </c:otherwise>
        </c:choose>
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Who Can Edit Alleles Associated With This Publication?</label>
        <div class="col-sm-8">
            <input id="userLookup" class="form-control" />
            <div id="selectedUsers"></div>
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Data File</label>
        <div class="col-sm-8">
            <label class="btn btn-default">
                Choose file
                <input type="file" id="dataFile" name="dataFile" style="display: none;"
                       accept=".xls,.xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"/>
            </label>
            <span id="dataFileName"></span>
            <form:errors path="dataFile" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Images</label>
        <div class="col-sm-8">
            <div class="file-drag-target">
                <input multiple type="file" id="imageFiles" name="imageFiles"
                       accept=".png,.gif,.jpeg,.jpg,image/png,image/gif,image/jpeg"/>
                <label for="imageFiles" class="btn btn-default">Choose files</label> or drag them here
            </div>
            <div class="right-align">
                <a id="clearImages" class="btn btn-link hidden">
                <span class="text-danger">
                    <i class="fas fa-times"></i> Remove images
                </span>
                </a>
            </div>

            <div id="selectedFiles"></div>
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-8">
            <button type="submit" class="btn btn-primary">Submit</button>
            <a class="btn btn-default" href="/">Cancel</a>
        </div>
    </div>
</form:form>

<script>
    $(function () {
        var form = $('#zebrashareForm');
        var imageFiles = $('#imageFiles');

        var handleImageFiles = function () {
            var selectedFiles = $('#selectedFiles');
            selectedFiles.empty();
            var fileList = imageFiles[0].files;
            for (var i = 0; i < fileList.length; i++) {
                var file = fileList[i];
                if (file.type !== 'image/gif' && file.type !== 'image/jpeg' && file.type !== 'image/png') {
                    continue
                }
                selectedFiles.append(renderCaptionInput(file));
            }
        };

        var renderCaptionInput = function (file) {
            var reader = new FileReader();
            var mediaContainer = $(
                '<div class="media">' +
                '  <div class="media-left">' +
                '    <div style="width: 128px; height: 128px; text-align: center">' +
                '      <img class="media-object thumb-image" src="">' +
                '    </div>' +
                '  </div>' +
                '  <div class="media-body">' +
                '    <h4 class="media-heading">' + file.name + '</h4>' +
                '    <textarea name="captions" class="form-control" rows="4" placeholder="Enter caption here"></textarea>' +
                '  </div>' +
                '</div>'
            );
            reader.onload = function (e) {
                mediaContainer.find('img').attr('src', e.target.result);
            };
            reader.readAsDataURL(file);
            return mediaContainer;
        };

        form.on('keyup keypress', function(e) {
            var keyCode = e.keyCode || e.which;
            if (keyCode === 13) {
                e.preventDefault();
                return false;
            }
        });
        $('#userLookup')
            .autocompletify('/action/profile/find-member?term=%QUERY')
            .on('typeahead:select', function(event, item) {
                $(this).typeahead('val', '');
                var hiddenInput = $('<input hidden name="editors" value="' + item.id  + '"/>');
                var userDisplay = $('<p class="form-control-static">' + item.value + '</p>');
                var removeButton = $('<button class="btn btn-link"><i class="fas fa-times"></i></button>');
                removeButton.on('click', function () {
                    hiddenInput.remove();
                    userDisplay.remove();
                });
                userDisplay.append(removeButton);
                form.append(hiddenInput);
                $('#selectedUsers').append(userDisplay);
            });
        $('.file-drag-target')
            .on('dragover', function (evt) {
                evt.preventDefault();
                $(this).addClass('hover');
            })
            .on('dragleave', function (evt) {
                evt.preventDefault();
                $(this).removeClass('hover');
            })
            .on('drop', function (evt) {
                evt.preventDefault();
                $(this).removeClass('hover');
                imageFiles[0].files = evt.originalEvent.dataTransfer.files;
                imageFiles.trigger('change');
            });
        imageFiles.on('change', function() {
            $('.file-drag-target').hide();
            $('#clearImages').removeClass('hidden');
            handleImageFiles();
        });
        $('#dataFile').on('change', function () {
            $('#dataFileName').text(this.files[0].name);
        });
        $('#clearImages').on('click', function (event) {
            event.preventDefault();
            $('.file-drag-target').show();
            $(this).addClass('hidden');
            imageFiles[0].value = '';
            handleImageFiles();
        });
    });
</script>