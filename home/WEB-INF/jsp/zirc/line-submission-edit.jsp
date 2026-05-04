<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="OVERVIEW"   value="Overview"/>
<c:set var="BACKGROUND" value="Background"/>
<c:set var="ADDITIONAL" value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, BACKGROUND, ADDITIONAL]}"/>

<c:set var="entityLabel" value="${not empty submission.name ? submission.name : submission.zdbID}"/>

<z:dataPage sections="${sections}" title="Edit Line Submission: ${entityLabel}">

    <jsp:attribute name="entityName">${entityLabel}</jsp:attribute>

    <jsp:body>

        <p>
            <a href="/action/zirc/line-submission/${submission.zdbID}" class="btn btn-light btn-sm">&laquo; View detail</a>
            <a href="/action/zirc/dashboard" class="btn btn-light btn-sm">Back to Dashboard</a>
        </p>

        <div class="small text-uppercase text-muted">Edit Line Submission</div>
        <h1>${entityLabel}</h1>

        <z:section title="${OVERVIEW}">
            <table class="table table-borderless">
                <tbody>
                    <tr>
                        <th class="w-25">ID</th>
                        <td colspan="2"><code>${submission.zdbID}</code></td>
                    </tr>
                    <tr>
                        <th>Name</th>
                        <td>
                            <input type="text" class="form-control field-input"
                                   value="<c:out value='${submission.name}'/>"/>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="name">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Abbreviation</th>
                        <td>
                            <input type="text" class="form-control field-input"
                                   value="<c:out value='${submission.abbreviation}'/>"/>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="abbreviation">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Previous Names</th>
                        <td>
                            <input type="text" class="form-control field-input"
                                   value="<c:out value='${submission.previousNames}'/>"/>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="previousNames">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Features Linked</th>
                        <td>
                            <div class="form-check form-check-inline">
                                <input type="radio" class="form-check-input" name="field-featuresLinked"
                                       id="featuresLinked-yes" value="true"<c:if test="${submission.featuresLinked == true}"> checked</c:if>/>
                                <label class="form-check-label" for="featuresLinked-yes">Yes</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" class="form-check-input" name="field-featuresLinked"
                                       id="featuresLinked-no" value="false"<c:if test="${submission.featuresLinked == false}"> checked</c:if>/>
                                <label class="form-check-label" for="featuresLinked-no">No</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" class="form-check-input" name="field-featuresLinked"
                                       id="featuresLinked-unset" value=""<c:if test="${submission.featuresLinked == null}"> checked</c:if>/>
                                <label class="form-check-label text-muted" for="featuresLinked-unset">&mdash;</label>
                            </div>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="featuresLinked">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <z:section title="${BACKGROUND}">
            <table class="table table-borderless">
                <tbody>
                    <tr>
                        <th class="w-25">Maternal</th>
                        <td>
                            <input type="text" class="form-control field-input"
                                   value="<c:out value='${submission.maternalBackground}'/>"/>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="maternalBackground">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Paternal</th>
                        <td>
                            <input type="text" class="form-control field-input"
                                   value="<c:out value='${submission.paternalBackground}'/>"/>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="paternalBackground">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Background Changeable</th>
                        <td>
                            <div class="form-check form-check-inline">
                                <input type="radio" class="form-check-input" name="field-backgroundChangeable"
                                       id="backgroundChangeable-yes" value="true"<c:if test="${submission.backgroundChangeable == true}"> checked</c:if>/>
                                <label class="form-check-label" for="backgroundChangeable-yes">Yes</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" class="form-check-input" name="field-backgroundChangeable"
                                       id="backgroundChangeable-no" value="false"<c:if test="${submission.backgroundChangeable == false}"> checked</c:if>/>
                                <label class="form-check-label" for="backgroundChangeable-no">No</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" class="form-check-input" name="field-backgroundChangeable"
                                       id="backgroundChangeable-unset" value=""<c:if test="${submission.backgroundChangeable == null}"> checked</c:if>/>
                                <label class="form-check-label text-muted" for="backgroundChangeable-unset">&mdash;</label>
                            </div>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="backgroundChangeable">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Concerns</th>
                        <td>
                            <textarea class="form-control field-input" rows="3"><c:out value="${submission.backgroundChangeConcerns}"/></textarea>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="backgroundChangeConcerns">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <z:section title="${ADDITIONAL}">
            <table class="table table-borderless">
                <tbody>
                    <tr>
                        <th class="w-25">Unreported Features Details</th>
                        <td>
                            <textarea class="form-control field-input" rows="3"><c:out value="${submission.unreportedFeaturesDetails}"/></textarea>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="unreportedFeaturesDetails">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Additional Info</th>
                        <td>
                            <textarea class="form-control field-input" rows="4"><c:out value="${submission.additionalInfo}"/></textarea>
                        </td>
                        <td class="edit-actions">
                            <button class="btn btn-sm btn-primary save-field-btn" data-field="additionalInfo">Save</button>
                            <span class="field-status small ml-2"></span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <style>
            .edit-actions { width: 130px; vertical-align: middle; }
            .field-status.text-success::before { content: "\2713 "; }
        </style>

        <script>
        (function () {
            jQuery('.save-field-btn').on('click', function () {
                var $btn   = jQuery(this);
                var field  = $btn.data('field');
                var $row   = $btn.closest('tr');
                var $status = $row.find('.field-status');

                var value;
                var $checkedRadio = $row.find('input[type=radio]:checked');
                if ($checkedRadio.length) {
                    value = $checkedRadio.val();
                } else {
                    value = $row.find('.field-input').val();
                }

                $status.removeClass('text-success text-danger').addClass('text-muted').text('Saving…');
                $btn.prop('disabled', true);

                jQuery.ajax({
                    url: '/action/zirc/line-submission/${submission.zdbID}/update-field',
                    type: 'POST',
                    data: { field: field, value: value },
                    success: function () {
                        $status.removeClass('text-muted text-danger').addClass('text-success').text('Saved');
                        $btn.prop('disabled', false);
                        setTimeout(function () { $status.text('').removeClass('text-success'); }, 2500);
                    },
                    error: function (xhr) {
                        var msg = (xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText || 'failed';
                        $status.removeClass('text-muted text-success').addClass('text-danger')
                               .text('Error: ' + msg);
                        $btn.prop('disabled', false);
                    }
                });
            });
        })();
        </script>

    </jsp:body>
</z:dataPage>
