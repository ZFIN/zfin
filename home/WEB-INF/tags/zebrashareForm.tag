<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="formBean" type="org.zfin.zebrashare.presentation.SubmissionFormBean" required="true" %>

<form:form method="POST" commandName="formBean" cssClass="form-horizontal" id="zebrashareForm" enctype="multipart/form-data">
    <h3>Submitter</h3>
    <p>We will contact this person if we have questions about this submission.</p>
    <div class="form-group row">
        <label class="col-md-3 col-form-label">Submitter Name</label>
        <div class="col-md-8">
            <form:input path="submitterName" cssClass="form-control"/>
            <form:errors path="submitterName" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group row">
        <label class="col-md-3 col-form-label">Submitter Email</label>
        <div class="col-md-8">
            <form:input path="submitterEmail" cssClass="form-control"/>
            <form:errors path="submitterEmail" cssClass="text-danger" />
        </div>
    </div>

    <hr />

    <h3>Attribution</h3>
    <p>We will make a ZFIN publication record and use it for attribution of the alleles.</p>
    <div class="form-group row">
        <label class="col-md-3 col-form-label">Authors</label>
        <div class="col-md-8">
            <form:textarea path="authors" cssClass="form-control"/>
            <form:errors path="authors" cssClass="text-danger" />
            <span class="form-text text-muted">
                Enter each author as the last name followed by the first and (where appproriate) middle initials
                followed by periods. Each name should be separated by a comma. For example:
                <span class="nowrap">Hargrove J.M.</span>, <span class="nowrap">Lacasse N.D.</span>, and
                <span class="nowrap">Kataoka U.</span>
            </span>
        </div>
    </div>

    <div class="form-group row">
        <label class="col-md-3 col-form-label">Title</label>
        <div class="col-md-8">
            <form:textarea path="title" cssClass="form-control" placeholder="e.g. A CRISPR Mutagenesis Screen of yfg1a
"/>
            <form:errors path="title" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group row">
        <label class="col-md-3 col-form-label">Abstract</label>
        <div class="col-md-8">
            <form:textarea path="abstractText" cssClass="form-control" rows="8" placeholder="e.g. CRISPRs were used to create premature stops in each exon of 'your favorite gene 1a' (yfg1a). Only mutations in yfg1a exon 1 and 2 resulted in any observable phenotype, which included small eyes (N=20/20 exon 1 mutants, N=17/17 exon 2 mutants, and 0/17 exon 3 mutants have small eyes)."/>
            <form:errors path="abstractText" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group row">
        <label class="col-md-3 col-form-label">Lab of Origin</label>
        <div class="col-md-8">
        <c:choose>
            <c:when test="${!empty labOptions}">
                <form:select path="labZdbId" items="${labOptions}" itemLabel="name" itemValue="zdbID" cssClass="form-control"/>
                <span class="form-text text-muted">
                    Don't see your lab listed here? Please
                    <zfin2:mailTo subject="Lab update request">contact us</zfin2:mailTo> to update your information.
                </span>
            </c:when>
            <c:otherwise>
                <p class="text-danger form-control-plaintext">
                    <b>Uh oh!</b> Your profile is not associated with any labs. Please
                    <zfin2:mailTo subject="Lab update request">contact us</zfin2:mailTo> to update your information
                    before proceeding.
                </p>
            </c:otherwise>
        </c:choose>
        </div>
    </div>

    <div class="form-group row">
        <label class="col-md-3 col-form-label">Who Can Edit Alleles Associated With This Publication?</label>
        <div class="col-md-8">
            <input id="userLookup" class="form-control" />
            <div id="selectedUsers"></div>
        </div>
    </div>

    <hr />

    <h3>Files</h3>
    <div class="form-group row">
        <label class="col-md-3 col-form-label">Submission Workbook(*<i>required</i>)</label>
        <div class="col-md-8">
            <label class="btn btn-outline-secondary">
                Choose file
                <input type="file" id="dataFile" name="dataFile" style="display: none;"
                       accept=".xls,.xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"/>
            </label>
            <span id="dataFileName"></span>
            <form:errors path="dataFile" cssClass="text-danger" />

        </div>
    </div>

    <div class="form-group row">
        <label class="col-md-3 col-form-label">Images</label>
        <div class="col-md-8">
            <div class="file-drag-target">
                <input multiple type="file" id="imageFiles" name="imageFiles"
                       accept=".png,.gif,.jpeg,.jpg,image/png,image/gif,image/jpeg" />
                <label for="imageFiles" class="btn btn-outline-secondary" >Choose files</label> or drag them here
            </div>
            <span class="form-text text-muted">
                    Acceptable file formats include png, jpeg, jpg and gif.
                </span>
            <div class="right-align">
                <a id="clearImages" class="btn btn-link d-none">
                <span class="text-danger">
                    <i class="fas fa-times"></i> Remove images
                </span>
                </a>
            </div>

            <div id="selectedFiles" >
                </div>
        </div>
    </div>

    <div class="form-group row">
        <div class="offset-md-3 col-md-8">
            <button type="submit" class="btn btn-primary">Submit</button>
            <a class="btn btn-outline-secondary" href="/">Cancel</a>
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
                '<div class="media mb-3">' +
                '  <div class="mr-3">' +
                '    <div style="width: 128px; height: 128px; text-align: center">' +
                '      <img class="media-object thumb-image" src="">' +
                '    </div>' +
                '  </div>' +
                '  <div class="media-body">' +
                '    <h5>' + file.name + '</h5>' +
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
                var userDisplay = $('<p class="form-control-plaintext">' + item.value + '</p>');
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
            $('#clearImages').removeClass('d-none');
            handleImageFiles();
        });
        $('#dataFile').on('change', function () {
            $('#dataFileName').text(this.files[0].name);
        });
        $('#clearImages').on('click', function (event) {
            event.preventDefault();
            $('.file-drag-target').show();
            $(this).addClass('d-none');
            imageFiles[0].value = '';
            handleImageFiles();
        });
    });
</script>