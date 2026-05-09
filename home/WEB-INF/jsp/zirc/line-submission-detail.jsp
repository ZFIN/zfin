<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="OVERVIEW"           value="Overview"/>
<c:set var="ACCEPTANCE_REASONS" value="Acceptance Reasons"/>
<c:set var="LINKED_FEATURES"    value="Linked Features"/>
<c:set var="BACKGROUND"         value="Background"/>
<c:set var="PEOPLE"             value="People"/>
<c:set var="MUTATIONS"          value="Mutations"/>
<c:set var="ADDITIONAL"         value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, ACCEPTANCE_REASONS, LINKED_FEATURES, BACKGROUND, PEOPLE, MUTATIONS, ADDITIONAL]}"/>

<z:dataPage sections="${sections}" title="Line Submission: ${submission.name}">

    <jsp:attribute name="entityName">${submission.name}</jsp:attribute>

    <jsp:attribute name="pageBar">
        <nav class="navbar navbar-light admin text-center border-bottom">
            <a class="col-sm" href="/action/zirc/dashboard">Dashboard</a>
            <span class="col-sm">Detail</span>
            <a class="col-sm" href="/action/zirc/line-submission/${submission.zdbID}/edit">Edit</a>
        </nav>
    </jsp:attribute>

    <jsp:body>

        <%-- jQuery UI for the add-submitter autocomplete (loaded per-page, matching merge-marker.jsp etc.) --%>
        <link rel="stylesheet" href="${zfn:getAssetPath('jquery-ui.css')}">
        <script src="${zfn:getAssetPath('jquery-ui.js')}"></script>
        <style>
            /* Bootstrap 4 modal sits at z-index 1050; lift the autocomplete menu above it. */
            .ui-autocomplete { z-index: 1100 !important; }
        </style>

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
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.name}"><c:out value="${submission.name}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Abbreviation</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.abbreviation}"><c:out value="${submission.abbreviation}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Previous Names</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.previousNames}"><c:out value="${submission.previousNames}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Features Linked</th>
                        <td>
                            <c:choose>
                                <c:when test="${submission.featuresLinked == true}">Yes</c:when>
                                <c:when test="${submission.featuresLinked == false}">No</c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
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

        <z:section title="${ACCEPTANCE_REASONS}">
            <c:choose>
                <c:when test="${empty submission.reasons and empty submission.reasonsOther}">
                    <p class="text-muted">No reasons selected.</p>
                </c:when>
                <c:otherwise>
                    <ul>
                        <c:forEach items="${submission.reasons}" var="r">
                            <li>
                                <c:choose>
                                    <c:when test="${r == 'frequently_requested'}">Currently frequently requested</c:when>
                                    <c:when test="${r == 'expect_high_demand'}">Expect high demand</c:when>
                                    <c:when test="${r == 'interesting_gene'}">Interesting gene</c:when>
                                    <c:when test="${r == 'community_resource'}">Community resource/tool</c:when>
                                    <c:when test="${r == 'mutant_gene_cloned'}">Mutant gene cloned</c:when>
                                    <c:when test="${r == 'danger_of_losing'}">Danger of losing line</c:when>
                                    <c:when test="${r == 'lack_of_space_or_funding'}">Lack of space or funding to maintain line</c:when>
                                    <c:otherwise><code>${r}</code></c:otherwise>
                                </c:choose>
                            </li>
                        </c:forEach>
                        <c:if test="${not empty submission.reasonsOther}">
                            <li>Other: <c:out value="${submission.reasonsOther}"/></li>
                        </c:if>
                    </ul>
                </c:otherwise>
            </c:choose>
        </z:section>

        <z:section title="${LINKED_FEATURES}">
            <c:choose>
                <c:when test="${empty submission.linkedFeatures}">
                    <p class="text-muted">No linked features.</p>
                </c:when>
                <c:otherwise>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Feature</th>
                                <th>Distance Known</th>
                                <th>cM</th>
                                <th>Mb</th>
                                <th>Additional Info</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${submission.linkedFeatures}" var="lf">
                                <tr>
                                    <td><c:out value="${lf.feature}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${lf.distanceKnown == true}">Yes</c:when>
                                            <c:when test="${lf.distanceKnown == false}">No</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.distanceCentimorgans}">${lf.distanceCentimorgans}</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.distanceMegabases}">${lf.distanceMegabases}</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.additionalInfo}"><c:out value="${lf.additionalInfo}"/></c:when>
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

        <z:section title="${BACKGROUND}">
            <table class="table table-borderless">
                <tbody>
                    <tr>
                        <th class="w-25">Maternal</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.maternalBackground}"><c:out value="${submission.maternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Paternal</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.paternalBackground}"><c:out value="${submission.paternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Background Changeable</th>
                        <td>
                            <c:choose>
                                <c:when test="${submission.backgroundChangeable == true}">Yes</c:when>
                                <c:when test="${submission.backgroundChangeable == false}">No</c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Concerns</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.backgroundChangeConcerns}"><c:out value="${submission.backgroundChangeConcerns}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
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
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.additionalInfo}"><c:out value="${submission.additionalInfo}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <tr>
                        <th>Unreported Features Details</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.unreportedFeaturesDetails}"><c:out value="${submission.unreportedFeaturesDetails}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
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
            var initialized = false;

            function postAdd(personZdbID) {
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
                if (!$input.length || typeof $input.autocomplete !== 'function') {
                    return;
                }
                $input.autocomplete({
                    appendTo: '#addSubmitterModal .modal-body',
                    source: function (request, response) {
                        jQuery.ajax({
                            url: '/action/zirc/persons/search',
                            dataType: 'json',
                            data: { term: request.term },
                            success: function (data) { response(data); },
                            error: function (xhr) {
                                jQuery('#add-submitter-error')
                                    .text('Search failed: ' + xhr.statusText).show();
                            }
                        });
                    },
                    minLength: 2,
                    select: function (event, ui) {
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

    </jsp:body>
</z:dataPage>
