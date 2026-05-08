<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="OVERVIEW"  value="Overview"/>
<c:set var="BACKGROUND" value="Background"/>
<c:set var="PEOPLE"    value="People"/>
<c:set var="MUTATIONS" value="Mutations"/>
<c:set var="ADDITIONAL" value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, BACKGROUND, PEOPLE, MUTATIONS, ADDITIONAL]}"/>

<z:dataPage sections="${sections}" title="Line Submission: ${submission.name}">

    <jsp:attribute name="entityName">${submission.name}</jsp:attribute>

    <jsp:body>

        <%-- jQuery UI for the add-submitter autocomplete (loaded per-page, matching merge-marker.jsp etc.) --%>
        <link rel="stylesheet" href="${zfn:getAssetPath('jquery-ui.css')}">
        <script src="${zfn:getAssetPath('jquery-ui.js')}"></script>
        <style>
            /* Bootstrap 4 modal sits at z-index 1050; lift the autocomplete menu above it. */
            .ui-autocomplete { z-index: 1100 !important; }
        </style>

        <p>
            <a href="/action/zirc/dashboard" class="btn btn-light btn-sm">&laquo; Back to Dashboard</a>
        </p>

        <div class="small text-uppercase text-muted">Line Submission</div>
        <h1>${submission.name}</h1>

        <z:section title="${OVERVIEW}">
            <table class="table table-borderless">
                <tbody>
                    <tr>
                        <th class="w-25">ID</th>
                        <td>${submission.zdbID}</td>
                    </tr>
                    <tr>
                        <th>Name</th>
                        <td class="editable-field" data-field="name" data-type="text"
                            data-value="<c:out value='${submission.name}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.name}"><c:out value="${submission.name}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Abbreviation</th>
                        <td class="editable-field" data-field="abbreviation" data-type="text"
                            data-value="<c:out value='${submission.abbreviation}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.abbreviation}"><c:out value="${submission.abbreviation}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Previous Names</th>
                        <td class="editable-field" data-field="previousNames" data-type="text"
                            data-value="<c:out value='${submission.previousNames}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.previousNames}"><c:out value="${submission.previousNames}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Features Linked</th>
                        <td class="editable-field" data-field="featuresLinked" data-type="bool"
                            data-value="${submission.featuresLinked == true ? 'true' : (submission.featuresLinked == false ? 'false' : '')}">
                            <span class="field-display"><c:choose>
                                <c:when test="${submission.featuresLinked == true}">Yes</c:when>
                                <c:when test="${submission.featuresLinked == false}">No</c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Date Started</th>
                        <td><fmt:formatDate value="${submission.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                    </tr>
                    <tr>
                        <th>Last Updated</th>
                        <td><fmt:formatDate value="${submission.updatedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                    </tr>
                    <tr>
                        <th>Submitter</th>
                        <td>
                            <c:choose>
                                <c:when test="${empty submission.persons}">
                                    <span class="text-muted">&mdash;</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${submission.persons}" var="lsp" varStatus="loop">
                                        <a href="/action/profile/person/view/${lsp.person.zdbID}">${lsp.person.fullName}</a><c:if test="${!loop.last}">,</c:if>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            <a href="javascript:void(0)" id="add-submitter-icon"
                               class="ml-2 text-success"
                               title="Add a submitter"
                               data-toggle="modal" data-target="#addSubmitterModal">
                                <i class="fas fa-plus-circle"></i>
                            </a>
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
                        <td class="editable-field" data-field="maternalBackground" data-type="text"
                            data-value="<c:out value='${submission.maternalBackground}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.maternalBackground}"><c:out value="${submission.maternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Paternal</th>
                        <td class="editable-field" data-field="paternalBackground" data-type="text"
                            data-value="<c:out value='${submission.paternalBackground}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.paternalBackground}"><c:out value="${submission.paternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Background Changeable</th>
                        <td class="editable-field" data-field="backgroundChangeable" data-type="bool"
                            data-value="${submission.backgroundChangeable == true ? 'true' : (submission.backgroundChangeable == false ? 'false' : '')}">
                            <span class="field-display"><c:choose>
                                <c:when test="${submission.backgroundChangeable == true}">Yes</c:when>
                                <c:when test="${submission.backgroundChangeable == false}">No</c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Concerns</th>
                        <td class="editable-field" data-field="backgroundChangeConcerns" data-type="textarea"
                            data-value="<c:out value='${submission.backgroundChangeConcerns}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.backgroundChangeConcerns}"><c:out value="${submission.backgroundChangeConcerns}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <z:section title="${PEOPLE}">
            <c:choose>
                <c:when test="${empty submission.persons}">
                    <p class="text-muted">No people associated with this submission.</p>
                </c:when>
                <c:otherwise>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Role</th>
                                <th>Name</th>
                                <th>ZDB ID</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${submission.persons}" var="lsp">
                                <tr>
                                    <td>${lsp.role}</td>
                                    <td><a href="/action/profile/person/view/${lsp.person.zdbID}">${lsp.person.fullName}</a></td>
                                    <td><code>${lsp.person.zdbID}</code></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </z:section>

        <z:section title="${MUTATIONS}">
            <c:choose>
                <c:when test="${empty submission.mutations}">
                    <p class="text-muted">No mutations recorded for this submission.</p>
                </c:when>
                <c:otherwise>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Allele Designation</th>
                                <th>Mutagenesis Protocol</th>
                                <th>Mutation Type</th>
                                <th>Discoverer</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${submission.mutations}" var="m" varStatus="loop">
                                <tr>
                                    <td>${loop.count}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty m.alleleDesignation}">${m.alleleDesignation}</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty m.mutagenesisProtocol}">${m.mutagenesisProtocol}</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty m.mutationType}">${m.mutationType}</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty m.mutationDiscoverer}">${m.mutationDiscoverer}</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </z:section>

        <z:section title="${ADDITIONAL}">
            <table class="table table-borderless">
                <tbody>
                    <tr>
                        <th class="w-25">Additional Info</th>
                        <td class="editable-field" data-field="additionalInfo" data-type="textarea"
                            data-value="<c:out value='${submission.additionalInfo}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.additionalInfo}"><c:out value="${submission.additionalInfo}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                    <tr>
                        <th>Unreported Features Details</th>
                        <td class="editable-field" data-field="unreportedFeaturesDetails" data-type="textarea"
                            data-value="<c:out value='${submission.unreportedFeaturesDetails}'/>">
                            <span class="field-display"><c:choose>
                                <c:when test="${not empty submission.unreportedFeaturesDetails}"><c:out value="${submission.unreportedFeaturesDetails}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose></span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <%-- Modal for adding a submitter via person-name autocomplete. --%>
        <div class="modal fade" id="addSubmitterModal" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Add a Submitter</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <label for="add-submitter-person-input" class="form-label">Search for a person by name</label>
                        <input type="text" id="add-submitter-person-input" class="form-control" autocomplete="off" placeholder="Start typing a name…"/>
                        <div class="form-text text-muted small mt-1">Pick a name from the dropdown to add.</div>
                        <div id="add-submitter-error" class="text-danger small mt-2" style="display:none;"></div>
                        <div id="add-submitter-progress" class="text-muted small mt-2" style="display:none;">Adding…</div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    </div>
                </div>
            </div>
        </div>

        <script>
        jQuery(function () {
            console.log('[add-submitter] script loaded; jQuery=', typeof jQuery,
                        ' jQuery.ui=', (jQuery && jQuery.ui ? jQuery.ui.version : 'MISSING'));

            var initialized = false;

            function postAdd(personZdbID) {
                console.log('[add-submitter] postAdd', personZdbID);
                var $input    = jQuery('#add-submitter-person-input');
                var $error    = jQuery('#add-submitter-error');
                var $progress = jQuery('#add-submitter-progress');
                $error.hide();
                $progress.show();
                $input.prop('disabled', true);
                jQuery.ajax({
                    url: '/action/zirc/line-submission/${submission.zdbID}/add-submitter',
                    type: 'POST',
                    data: { personZdbID: personZdbID },
                    success: function () {
                        window.location.reload();
                    },
                    error: function (xhr) {
                        $progress.hide();
                        $input.prop('disabled', false);
                        $error.text('Failed to add submitter: ' +
                            (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)).show();
                    }
                });
            }

            function initAutocomplete() {
                if (initialized) return;
                var $input = jQuery('#add-submitter-person-input');
                console.log('[add-submitter] init; input found=', $input.length,
                            ' autocomplete fn=', typeof $input.autocomplete);
                if (!$input.length || typeof $input.autocomplete !== 'function') {
                    return;
                }
                $input.autocomplete({
                    appendTo: '#addSubmitterModal .modal-body',
                    source: function (request, response) {
                        console.log('[add-submitter] search term=', request.term);
                        jQuery.ajax({
                            url: '/action/zirc/persons/search',
                            dataType: 'json',
                            data: { term: request.term },
                            success: function (data) {
                                console.log('[add-submitter] search results=', data && data.length);
                                response(data);
                            },
                            error: function (xhr) {
                                jQuery('#add-submitter-error')
                                    .text('Search failed: ' + xhr.statusText).show();
                            }
                        });
                    },
                    minLength: 2,
                    select: function (event, ui) {
                        console.log('[add-submitter] select', ui.item);
                        event.preventDefault();
                        jQuery('#add-submitter-person-input').val(ui.item.value);
                        postAdd(ui.item.zdbID);
                    }
                });

                // Workaround for Bootstrap 4 modal focus-trap eating the click on
                // jQuery UI autocomplete items: handle mousedown directly on the menu.
                jQuery(document).on('mousedown.addsubmitter',
                    '#addSubmitterModal .ui-autocomplete .ui-menu-item',
                    function (e) {
                        var data = jQuery(this).data('ui-autocomplete-item');
                        console.log('[add-submitter] menu mousedown; item=', data);
                        if (data && data.zdbID) {
                            e.preventDefault();
                            e.stopPropagation();
                            jQuery('#add-submitter-person-input').val(data.value);
                            jQuery('#add-submitter-person-input').autocomplete('close');
                            postAdd(data.zdbID);
                        }
                    });

                initialized = true;
            }

            // Reset + initialize on every modal open. Initializing on shown.bs.modal
            // guarantees the input is in the DOM and visible before autocomplete attaches.
            jQuery('#addSubmitterModal')
                .on('show.bs.modal', function () {
                    var $input = jQuery('#add-submitter-person-input');
                    $input.val('').prop('disabled', false);
                    jQuery('#add-submitter-error').hide();
                    jQuery('#add-submitter-progress').hide();
                })
                .on('shown.bs.modal', function () {
                    initAutocomplete();
                    jQuery('#add-submitter-person-input').trigger('focus');
                });
        });
        </script>

        <%-- Inline click-to-edit for line_submission scalar fields. --%>
        <style>
            .editable-field { cursor: pointer; }
            .editable-field:hover:not(.editing) .field-display {
                background-color: #fff3cd;
                box-shadow: inset 0 0 0 1px #ffe5a0;
                border-radius: 2px;
            }
            .editable-field .field-display { padding: 1px 4px; margin: -1px -4px; }
            .editable-field.editing { cursor: default; }
            .field-edit-wrapper { display: flex; align-items: flex-start; gap: 6px; }
            .field-edit-wrapper .form-control { flex: 1 1 auto; }
        </style>

        <script>
        (function () {
            var SAVE_URL = '/action/zirc/line-submission/${submission.zdbID}/update-field';

            // Render the display HTML for a saved value (returned from the server).
            function renderDisplay(type, value) {
                var blank = '<span class="text-muted">&mdash;</span>';
                if (type === 'bool') {
                    if (value === 'true' || value === true)  return 'Yes';
                    if (value === 'false' || value === false) return 'No';
                    return blank;
                }
                if (value == null || value === '') return blank;
                // Text-escape; preserve newlines as-is (they collapse to whitespace in HTML, same as before).
                return jQuery('<div>').text(String(value)).html();
            }

            jQuery(document).on('click', '.editable-field .field-display', function (e) {
                var $cell = jQuery(this).closest('.editable-field');
                if ($cell.hasClass('editing')) return;

                var field = $cell.data('field');
                var type  = $cell.data('type');
                var raw   = $cell.attr('data-value') || '';

                var $input;
                if (type === 'textarea') {
                    $input = jQuery('<textarea class="form-control" rows="3"></textarea>').val(raw);
                } else if (type === 'bool') {
                    $input = jQuery('<div></div>');
                    [['true','Yes'],['false','No'],['','—']].forEach(function (pair) {
                        var v = pair[0], label = pair[1];
                        var $r = jQuery('<div class="form-check form-check-inline"></div>');
                        var $i = jQuery('<input type="radio" class="form-check-input">')
                                    .attr('name', 'ef-' + field).val(v);
                        if (raw === v) $i.prop('checked', true);
                        $r.append($i).append(' ').append(jQuery('<label class="form-check-label"></label>').text(label));
                        $input.append($r);
                    });
                } else {
                    $input = jQuery('<input type="text" class="form-control">').val(raw);
                }

                var $save   = jQuery('<button class="btn btn-sm btn-primary">Save</button>');
                var $cancel = jQuery('<button class="btn btn-sm btn-light">Cancel</button>');
                var $status = jQuery('<span class="small ml-1"></span>');
                var $wrap   = jQuery('<div class="field-edit-wrapper"></div>')
                                .append($input, $save, $cancel, $status);

                var $display = $cell.find('.field-display');
                $display.hide();
                $cell.append($wrap).addClass('editing');
                if (type !== 'bool') $input.trigger('focus');

                function teardown() {
                    $wrap.remove();
                    $display.show();
                    $cell.removeClass('editing');
                }

                $cancel.on('click', function (ev) { ev.stopPropagation(); teardown(); });

                $save.on('click', function (ev) {
                    ev.stopPropagation();
                    var value;
                    if (type === 'bool') {
                        value = $input.find('input[type=radio]:checked').val() || '';
                    } else {
                        value = $input.val();
                    }
                    $status.removeClass('text-danger').addClass('text-muted').text('Saving…');
                    $save.prop('disabled', true);
                    $cancel.prop('disabled', true);
                    jQuery.ajax({
                        url: SAVE_URL,
                        type: 'POST',
                        data: { field: field, value: value },
                        success: function (resp) {
                            $cell.attr('data-value', resp.value == null ? '' : resp.value);
                            $display.html(renderDisplay(type, resp.value));
                            teardown();
                        },
                        error: function (xhr) {
                            $status.removeClass('text-muted').addClass('text-danger')
                                   .text('Error: ' + ((xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText));
                            $save.prop('disabled', false);
                            $cancel.prop('disabled', false);
                        }
                    });
                });
            });
        })();
        </script>

    </jsp:body>
</z:dataPage>
