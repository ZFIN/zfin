<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="OVERVIEW"           value="Overview"/>
<c:set var="LINKED_FEATURES"    value="Linked Features"/>
<c:set var="BACKGROUND"         value="Background"/>
<c:set var="MUTATIONS"          value="Mutations"/>
<c:set var="ADDITIONAL"         value="Additional Info"/>

<c:set var="sections" value="${[OVERVIEW, MUTATIONS, LINKED_FEATURES, BACKGROUND, PEOPLE, ADDITIONAL]}"/>

<z:dataPage sections="${sections}" sectionStatus="${sectionStatus}" subSections="${subSections}" subSectionStatus="${subSectionStatus}" subSubSections="${subSubSections}" subSubSectionStatus="${subSubSectionStatus}" title="Line Submission: ${submission.name}">

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
            /* Fixed-width slot for the field-status badge so labels and badges
               line up across rows even when the badge is absent (e.g. an N/A
               conditional field). */
            .status-slot { display: inline-block; width: 2.25em; text-align: center; margin-right: 0.4em; }
            /* History-icon hover reveal: stay invisible until the curator hovers
               the row (or the icon itself takes focus, so keyboard users still
               see it). Opacity rather than display:none so layout doesn't shift. */
            .field-history-trigger { opacity: 0; transition: opacity 0.12s ease-in; }
            tr:hover .field-history-trigger,
            h1:hover .field-history-trigger,
            .section .heading:hover .field-history-trigger,
            .field-history-trigger:focus,
            .field-history-trigger:hover { opacity: 1; }

            /* Workflow-stage status bar at the top of the page. */
            .status-overview-bar { border-collapse: collapse; }
            .status-overview-bar th, .status-overview-bar td {
                border: 1px solid #333;
                font-size: 0.75rem;
                line-height: 1.1;
                text-align: center;
                vertical-align: middle;
                padding: 0.35rem 0.4rem;
                min-width: 4.5em;
                max-width: 6.5em;
            }
            .status-overview-bar th { font-weight: 600; background: #fff; }
            .status-overview-bar td.data-cell    { background: #fffbe6; font-weight: 500; }
            .status-overview-bar td.status-cell  { height: 2.2em; background: #fff; }
            /* Match the FieldStatus badge palette so a quick glance at the
               bar tells you the rollup state — red=Missing, amber=In
               Progress, green=Complete, blue=Approved. */
            .status-overview-bar td.status-cell.status-missing     { background: #dc3545; }
            .status-overview-bar td.status-cell.status-in-progress { background: #ffc107; }
            .status-overview-bar td.status-cell.status-complete    { background: #4f9c4a; }
            .status-overview-bar td.status-cell.status-approved    { background: #007bff; }
        </style>

        <div class="small text-uppercase text-muted">ZIRC Line Submission</div>
        <%-- submission.name is curator-entered; escape on every direct HTML emission. --%>
        <h1><z:zirc-status-badge status="${overallStatus}"/> <c:out value="${submission.name}"/><z:zirc-section-history key="submission" label="Line Submission ${submission.name}" updates="${submissionAllUpdates}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="section" sectionName="submission" label="Line Submission ${submission.name}"/></h1>

        <%-- Workflow status bar: first three cells display data, the rest
             mirror the page's top-level sections and turn green when that
             section's status is COMPLETE or APPROVED. Cell clicks jump
             to the matching section anchor. --%>
        <h5 class="mt-3 mb-1">Status Overview
            <small class="status-legend text-muted ml-3">
                <span class="badge badge-danger">M</span>&nbsp;Missing
                <span class="badge badge-warning ml-2">IP</span>&nbsp;In&nbsp;Progress
                <span class="badge badge-success ml-2">C</span>&nbsp;Complete
                <span class="badge badge-primary ml-2">A</span>&nbsp;Approved
            </small>
        </h5>
        <table class="status-overview-bar mb-4">
            <thead>
                <tr>
                    <th>Submitter</th>
                    <th>Line</th>
                    <th>Submission<br/>date</th>
                    <th>Overview</th>
                    <th>Mutations</th>
                    <th>Linked<br/>Features</th>
                    <th>Background</th>
                    <th>Additional<br/>Info</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td class="data-cell">
                        <c:choose>
                            <c:when test="${not empty submission.persons}"><c:forEach items="${submission.persons}" var="lsp" varStatus="lspLoop"><c:if test="${not empty lsp.person.firstName}">${fn:substring(lsp.person.firstName, 0, 1)}. </c:if><c:out value="${lsp.person.lastName}"/><c:if test="${!lspLoop.last}">, </c:if></c:forEach></c:when>
                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="data-cell">
                        <c:choose>
                            <c:when test="${not empty submission.name}"><c:out value="${submission.name}"/></c:when>
                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="data-cell">
                        <c:choose>
                            <c:when test="${not empty submission.createdAt}"><fmt:formatDate value="${submission.createdAt}" pattern="yyyy-MM-dd"/></c:when>
                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                        </c:choose>
                    </td>
                    <c:forEach var="sec" items="${[['Overview', OVERVIEW], ['Mutations', MUTATIONS], ['Linked Features', LINKED_FEATURES], ['Background', BACKGROUND], ['Additional Info', ADDITIONAL]]}">
                        <c:set var="sStatus" value="${sectionStatus[sec[0]]}"/>
                        <c:set var="statusName" value="${sStatus == null ? '' : sStatus.name()}"/>
                        <c:set var="statusClass" value=""/>
                        <c:if test="${statusName == 'MISSING'}">    <c:set var="statusClass" value="status-missing"/></c:if>
                        <c:if test="${statusName == 'IN_PROGRESS'}"><c:set var="statusClass" value="status-in-progress"/></c:if>
                        <c:if test="${statusName == 'COMPLETE'}">   <c:set var="statusClass" value="status-complete"/></c:if>
                        <c:if test="${statusName == 'APPROVED'}">   <c:set var="statusClass" value="status-approved"/></c:if>
                        <c:set var="secDomId" value="${zfn:makeDomIdentifier(sec[1])}"/>
                        <td class="status-cell ${statusClass}" data-section-id="${secDomId}" title="${sec[0]}: ${sStatus == null ? '—' : sStatus.displayName}">
                            <a href="#${secDomId}" class="d-block w-100 h-100 text-decoration-none">&nbsp;</a>
                        </td>
                    </c:forEach>
                </tr>
            </tbody>
        </table>

        <c:set var="overviewBadge"><z:zirc-status-badge status="${sectionStatus['Overview']}"/><z:zirc-section-history key="overview" label="Overview" updates="${sectionUpdates['Overview']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="section" sectionName="overview" label="Overview"/></c:set>
        <c:set var="overviewApproval"><z:zirc-section-approval recId="${submission.zdbID}" sectionName="overview" approved="${sectionApprovals[submission.zdbID.concat('|overview')]}" enabled="${(sectionStatus['Overview'] != null) and (sectionStatus['Overview'].name() == 'COMPLETE' or sectionStatus['Overview'].name() == 'APPROVED')}"/></c:set>
        <z:section title="${OVERVIEW}" prependedText="${overviewBadge}" appendedText="${overviewApproval}">
            <table class="table table-borderless w-auto">
                <tbody>
                    <tr>
                        <th><span class="status-slot"></span> ID</th>
                        <td>
                            <span id="zdb-id-value">${submission.zdbID}</span>
                            <a href="javascript:void(0)" id="copy-zdb-id"
                               class="ml-2 text-muted" title="Copy ID to clipboard">
                                <i class="far fa-copy"></i>
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['name']}"/></span>Name</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.name}"><c:out value="${submission.name}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="name" label="Name" updates="${fieldUpdates['name']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="name" label="Name"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['previousNames']}"/></span>Previous Names</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.previousNames}">
                                    <ul class="list-unstyled mb-0">
                                        <c:forEach var="prev" items="${submission.previousNames}">
                                            <li><c:out value="${prev}"/></li>
                                        </c:forEach>
                                    </ul>
                                </c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="previousNames" label="Previous Names" updates="${fieldUpdates['previousNames']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="previousNames" label="Previous Names"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"></span>Date Started - Last Updated</th>
                        <td>
                            <fmt:formatDate value="${submission.createdAt}" pattern="yyyy-MM-dd HH:mm"/> - <fmt:formatDate value="${submission.updatedAt}" pattern="yyyy-MM-dd HH:mm"/>
                        </td>
                    </tr>
                    <%-- Filter by lsp_role so each row shows only the people who
                         hold that role on the submission (ZFIN-10325). --%>
                    <c:set var="submitters" value="${[]}"/>
                    <c:set var="pis"        value="${[]}"/>
                    <c:forEach items="${submission.persons}" var="lsp">
                        <c:if test="${lsp.role eq 'submitter'}">
                            <c:set var="submitters" value="${submitters.add(lsp) ? submitters : submitters}"/>
                        </c:if>
                        <c:if test="${lsp.role eq 'pi'}">
                            <c:set var="pis" value="${pis.add(lsp) ? pis : pis}"/>
                        </c:if>
                    </c:forEach>
                    <tr>
                        <th><span class="status-slot"></span>Submitter</th>
                        <td>
                            <c:choose>
                                <c:when test="${empty submitters}">
                                    <span class="text-muted">&mdash;</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${submitters}" var="lsp" varStatus="loop">
                                        <a href="/action/profile/person/view/${lsp.person.zdbID}"><c:if test="${not empty lsp.person.firstName}">${fn:substring(lsp.person.firstName, 0, 1)}. </c:if><c:out value="${lsp.person.lastName}"/></a><c:if test="${!loop.last}">, </c:if>
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
                    <tr>
                        <th><span class="status-slot"></span>PI</th>
                        <td>
                            <c:choose>
                                <c:when test="${empty pis}">
                                    <span class="text-muted">&mdash;</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${pis}" var="lsp" varStatus="loop">
                                        <a href="/action/profile/person/view/${lsp.person.zdbID}"><c:if test="${not empty lsp.person.firstName}">${fn:substring(lsp.person.firstName, 0, 1)}. </c:if><c:out value="${lsp.person.lastName}"/></a><c:if test="${!loop.last}">, </c:if>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            <a href="javascript:void(0)" id="add-pi-icon"
                               class="ml-2 text-success"
                               title="Add a PI"
                               data-toggle="modal" data-target="#addPiModal">
                                <i class="fas fa-plus-circle"></i>
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"></span>Admin</th>
                        <td>
                            <%-- Hard-coded list per ZFIN-10325; revisit once admin
                                 tagging is modeled per-submission. --%>
                            Andrzej Nasdiaka, Amy Singer, Zoltan Varga, April Freeman
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['reasons']}"/></span>Acceptance Reasons</th>
                        <td>
                            <c:choose>
                                <c:when test="${empty submission.reasons and empty submission.reasonsOther}">
                                    <span class="text-muted">&mdash;</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${submission.reasons}" var="r" varStatus="loop"><c:if test="${!loop.first}">, </c:if><c:choose><c:when test="${r == 'frequently_requested'}">Currently frequently requested</c:when><c:when test="${r == 'expect_high_demand'}">Expect high demand</c:when><c:when test="${r == 'interesting_gene'}">Interesting gene</c:when><c:when test="${r == 'community_resource'}">Community resource/tool</c:when><c:when test="${r == 'mutant_gene_cloned'}">Mutant gene cloned</c:when><c:when test="${r == 'danger_of_losing'}">Danger of losing line</c:when><c:when test="${r == 'lack_of_space_or_funding'}">Lack of space or funding to maintain line</c:when><c:otherwise><code>${r}</code></c:otherwise></c:choose></c:forEach>
                                    <c:if test="${not empty submission.reasonsOther}"><c:if test="${not empty submission.reasons}">, </c:if>Other: <c:out value="${submission.reasonsOther}"/></c:if>
                                </c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="reasons" label="Acceptance Reasons" updates="${fieldUpdates['reasons']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="reasons" label="Acceptance Reasons"/>
                            <z:zirc-field-history fieldName="reasonsOther" label="Acceptance Reasons (Other)" updates="${fieldUpdates['reasonsOther']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="reasonsOther" label="Acceptance Reasons (Other)"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <c:set var="mutationsBadge"><z:zirc-status-badge status="${sectionStatus['Mutations']}"/><z:zirc-section-history key="mutations" label="Mutations" updates="${sectionUpdates['Mutations']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="section" sectionName="mutations" label="Mutations"/></c:set>
        <c:set var="mutationsApproval"><z:zirc-section-approval recId="${submission.zdbID}" sectionName="mutations" approved="${sectionApprovals[submission.zdbID.concat('|mutations')]}" enabled="${(sectionStatus['Mutations'] != null) and (sectionStatus['Mutations'].name() == 'COMPLETE' or sectionStatus['Mutations'].name() == 'APPROVED')}"/></c:set>
        <z:section title="${MUTATIONS}" prependedText="${mutationsBadge}" appendedText="${mutationsApproval}">
            <c:choose>
                <c:when test="${empty submission.mutations}">
                    <p class="text-muted">No mutations recorded for this submission.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${submission.mutations}" var="m" varStatus="loop">
                        <c:set var="mFieldStatus" value="${mutationFieldStatus[m.id]}"/>
                        <c:set var="mFieldUpdates" value="${mutationFieldUpdates[m.id]}"/>
                        <c:set var="mScope" value="mut${m.id}"/>
                        <c:set var="mRecId" value="ZIRC-MUT-${m.id}"/>
                        <c:set var="mSectStat" value="${mutationSectionStatus[m.id]}"/>
                        <c:set var="mSectUpdates" value="${mutationSectionUpdates[m.id]}"/>
                        <c:set var="mSectionBadge"><z:zirc-status-badge status="${mutationStatus[m.id]}"/><z:zirc-section-history key="mutation" label="Mutation ${loop.count}" updates="${mutationAllUpdates[m.id]}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="section" sectionName="mutation" label="Mutation ${loop.count}"/></c:set>
                        <c:set var="mSectionApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="mutation" approved="${sectionApprovals[mRecId.concat('|mutation')]}" enabled="${(mutationStatus[m.id] != null) and (mutationStatus[m.id].name() == 'COMPLETE' or mutationStatus[m.id].name() == 'APPROVED')}"/></c:set>
                        <c:set var="overviewBadge"><z:zirc-status-badge status="${mSectStat['Overview']}"/><z:zirc-section-history key="overview" label="Mutation ${loop.count} &mdash; Overview" updates="${mSectUpdates['Overview']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="section" sectionName="overview" label="Mutation ${loop.count} &mdash; Overview"/></c:set>
                        <c:set var="overviewApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="overview" approved="${sectionApprovals[mRecId.concat('|overview')]}" enabled="${(mSectStat['Overview'] != null) and (mSectStat['Overview'].name() == 'COMPLETE' or mSectStat['Overview'].name() == 'APPROVED')}"/></c:set>
                        <c:set var="genesBadge"><z:zirc-status-badge status="${mSectStat['Genes']}"/><z:zirc-section-history key="genes" label="Mutation ${loop.count} &mdash; Genes" updates="${mSectUpdates['Genes']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="section" sectionName="genes" label="Mutation ${loop.count} &mdash; Genes"/></c:set>
                        <c:set var="genesApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="genes" approved="${sectionApprovals[mRecId.concat('|genes')]}" enabled="${(mSectStat['Genes'] != null) and (mSectStat['Genes'].name() == 'COMPLETE' or mSectStat['Genes'].name() == 'APPROVED')}"/></c:set>
                        <c:set var="lesionsBadge"><z:zirc-status-badge status="${mSectStat['Lesions']}"/><z:zirc-section-history key="lesions" label="Mutation ${loop.count} &mdash; Lesions" updates="${mSectUpdates['Lesions']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="section" sectionName="lesions" label="Mutation ${loop.count} &mdash; Lesions"/></c:set>
                        <c:set var="lesionsApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="lesions" approved="${sectionApprovals[mRecId.concat('|lesions')]}" enabled="${(mSectStat['Lesions'] != null) and (mSectStat['Lesions'].name() == 'COMPLETE' or mSectStat['Lesions'].name() == 'APPROVED')}"/></c:set>
                        <c:set var="gaBadge"><z:zirc-status-badge status="${mSectStat['Genotyping Assays']}"/><z:zirc-section-history key="genotyping-assays" label="Mutation ${loop.count} &mdash; Genotyping Assays" updates="${mSectUpdates['Genotyping Assays']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="section" sectionName="genotyping-assays" label="Mutation ${loop.count} &mdash; Genotyping Assays"/></c:set>
                        <c:set var="gaApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="genotyping-assays" approved="${sectionApprovals[mRecId.concat('|genotyping-assays')]}" enabled="${(mSectStat['Genotyping Assays'] != null) and (mSectStat['Genotyping Assays'].name() == 'COMPLETE' or mSectStat['Genotyping Assays'].name() == 'APPROVED')}"/></c:set>
                        <c:set var="phenoBadge"><z:zirc-status-badge status="${mSectStat['Phenotypes']}"/><z:zirc-section-history key="phenotypes" label="Mutation ${loop.count} &mdash; Phenotypes" updates="${mSectUpdates['Phenotypes']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="section" sectionName="phenotypes" label="Mutation ${loop.count} &mdash; Phenotypes"/></c:set>
                        <c:set var="phenoApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="phenotypes" approved="${sectionApprovals[mRecId.concat('|phenotypes')]}" enabled="${(mSectStat['Phenotypes'] != null) and (mSectStat['Phenotypes'].name() == 'COMPLETE' or mSectStat['Phenotypes'].name() == 'APPROVED')}"/></c:set>
                        <c:set var="lethalityBadge"><z:zirc-status-badge status="${mSectStat['Lethality']}"/><z:zirc-section-history key="lethality" label="Mutation ${loop.count} &mdash; Lethality" updates="${mSectUpdates['Lethality']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="section" sectionName="lethality" label="Mutation ${loop.count} &mdash; Lethality"/></c:set>
                        <c:set var="lethalityApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="lethality" approved="${sectionApprovals[mRecId.concat('|lethality')]}" enabled="${(mSectStat['Lethality'] != null) and (mSectStat['Lethality'].name() == 'COMPLETE' or mSectStat['Lethality'].name() == 'APPROVED')}"/></c:set>
                        <c:set var="pubsBadge"><z:zirc-status-badge status="${mSectStat['Publications']}"/></c:set>
                        <c:set var="pubsApproval"><z:zirc-section-approval recId="${mRecId}" sectionName="publications" approved="${sectionApprovals[mRecId.concat('|publications')]}" enabled="${(mSectStat['Publications'] != null) and (mSectStat['Publications'].name() == 'COMPLETE' or mSectStat['Publications'].name() == 'APPROVED')}"/></c:set>
                        <z:section title="Mutation ${loop.count}" prependedText="${mSectionBadge}" appendedText="${mSectionApproval}" cssClass="ml-4">
                            <p class="text-right mb-2">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="/action/zirc/mutation/${m.id}/edit">Edit</a>
                            </p>

                            <z:section title="Overview" prependedText="${overviewBadge}" appendedText="${overviewApproval}" cssClass="ml-4" sectionID="mutation-${loop.count}-overview">
                                <table class="table table-borderless w-auto">
                                    <tbody>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['alleleInZfin']}"/></span>ZFIN Record Established</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${m.alleleInZfin == true}">Yes</c:when>
                                                    <c:when test="${m.alleleInZfin == false}">No</c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="alleleInZfin" label="ZFIN Record Established" updates="${mFieldUpdates['alleleInZfin']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="alleleInZfin" label="ZFIN Record Established"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['alleleDesignation']}"/></span>Allele Designation</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.alleleDesignation}"><c:out value="${m.alleleDesignation}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="alleleDesignation" label="Allele Designation" updates="${mFieldUpdates['alleleDesignation']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="alleleDesignation" label="Allele Designation"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutagenesisStage']}"/></span>Mutagenesis Stage</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutagenesisStage}"><c:out value="${m.mutagenesisStage}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutagenesisStage" label="Mutagenesis Stage" updates="${mFieldUpdates['mutagenesisStage']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="mutagenesisStage" label="Mutagenesis Stage"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutagenesisProtocol']}"/></span>Mutagenesis Protocol</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutagenesisProtocol}"><c:out value="${m.mutagenesisProtocol}"/><c:if test="${m.mutagenesisProtocol == 'other' and not empty m.mutagenesisProtocolOther}"> (<c:out value="${m.mutagenesisProtocolOther}"/>)</c:if></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutagenesisProtocol" label="Mutagenesis Protocol" updates="${mFieldUpdates['mutagenesisProtocol']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="mutagenesisProtocol" label="Mutagenesis Protocol"/>
                                                <z:zirc-field-history fieldName="mutagenesisProtocolOther" label="Mutagenesis Protocol (Other)" updates="${mFieldUpdates['mutagenesisProtocolOther']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="mutagenesisProtocolOther" label="Mutagenesis Protocol (Other)"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['molecularlyCharacterized']}"/></span>Molecularly Characterized</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${m.molecularlyCharacterized == true}">Yes</c:when>
                                                    <c:when test="${m.molecularlyCharacterized == false}">No</c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="molecularlyCharacterized" label="Molecularly Characterized" updates="${mFieldUpdates['molecularlyCharacterized']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="molecularlyCharacterized" label="Molecularly Characterized"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutationType']}"/></span>Mutation Type</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutationType}"><c:out value="${m.mutationType}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutationType" label="Mutation Type" updates="${mFieldUpdates['mutationType']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="mutationType" label="Mutation Type"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutationDiscoverer']}"/></span>Discoverer</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutationDiscoverer}"><c:out value="${m.mutationDiscoverer}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutationDiscoverer" label="Discoverer" updates="${mFieldUpdates['mutationDiscoverer']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="mutationDiscoverer" label="Discoverer"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutationInstitution']}"/></span>Institution</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutationInstitution}"><c:out value="${m.mutationInstitution}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutationInstitution" label="Institution" updates="${mFieldUpdates['mutationInstitution']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="mutationInstitution" label="Institution"/>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </z:section>

                            <z:section title="Genes" prependedText="${genesBadge}" appendedText="${genesApproval}" cssClass="ml-4" sectionID="mutation-${loop.count}-genes">
                                <c:choose>
                                    <c:when test="${empty m.genes}">
                                        <p class="text-muted">No genes recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <table class="table table-striped">
                                            <thead>
                                                <tr>
                                                    <th>#</th>
                                                    <th>Gene</th>
                                                    <th>Linkage Group</th>
                                                    <th>GenBank Genomic DNA</th>
                                                    <th>GenBank cDNA</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${m.genes}" var="g" varStatus="gloop">
                                                    <c:set var="gFieldStatus" value="${geneFieldStatus[g.id]}"/>
                                                    <c:set var="gFieldUpdates" value="${geneFieldUpdates[g.id]}"/>
                                                    <c:set var="gScope" value="gene${g.id}"/>
                                                    <c:set var="gRecId" value="ZIRC-GENE-${g.id}"/>
                                                    <tr>
                                                        <td>${gloop.count}</td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['mutatedGene']}"/> <c:choose><c:when test="${not empty g.mutatedGene}"><a href="/${g.mutatedGene.zdbID}"><c:out value="${g.mutatedGene.abbreviation}"/></a></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="mutatedGene" label="Gene" updates="${gFieldUpdates['mutatedGene']}" scope="${gScope}"/><z:zirc-field-comments recId="${gRecId}" scope="field" fieldName="mutatedGene" label="Gene"/></td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['linkageGroup']}"/> <c:choose><c:when test="${not empty g.linkageGroup}"><c:out value="${g.linkageGroup}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="linkageGroup" label="Linkage Group" updates="${gFieldUpdates['linkageGroup']}" scope="${gScope}"/><z:zirc-field-comments recId="${gRecId}" scope="field" fieldName="linkageGroup" label="Linkage Group"/></td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['genbankGenomicDna']}"/> <c:choose><c:when test="${not empty g.genbankGenomicDna}"><c:out value="${g.genbankGenomicDna}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="genbankGenomicDna" label="GenBank Genomic DNA" updates="${gFieldUpdates['genbankGenomicDna']}" scope="${gScope}"/><z:zirc-field-comments recId="${gRecId}" scope="field" fieldName="genbankGenomicDna" label="GenBank Genomic DNA"/></td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['genbankCdna']}"/> <c:choose><c:when test="${not empty g.genbankCdna}"><c:out value="${g.genbankCdna}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="genbankCdna" label="GenBank cDNA" updates="${gFieldUpdates['genbankCdna']}" scope="${gScope}"/><z:zirc-field-comments recId="${gRecId}" scope="field" fieldName="genbankCdna" label="GenBank cDNA"/></td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Lesions" prependedText="${lesionsBadge}" appendedText="${lesionsApproval}" cssClass="ml-4" sectionID="mutation-${loop.count}-lesions">
                                <c:choose>
                                    <c:when test="${empty m.lesions}">
                                        <p class="text-muted">No lesions recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${m.lesions}" var="lz" varStatus="lloop">
                                            <c:set var="lzFieldStatus" value="${lesionFieldStatus[lz.id]}"/>
                                            <c:set var="lzFieldUpdates" value="${lesionFieldUpdates[lz.id]}"/>
                                            <c:set var="lzScope" value="lesion${lz.id}"/>
                                            <c:set var="lzRecId" value="ZIRC-LESION-${lz.id}"/>
                                            <z:section title="Lesion ${lloop.count}" cssClass="ml-4">
                                                <table class="table table-borderless w-auto">
                                                    <tbody>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['lesionType']}"/></span>Lesion Type</th>
                                                            <td><c:choose><c:when test="${not empty lz.lesionType}"><c:out value="${lz.lesionType}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="lesionType" label="Lesion Type" updates="${lzFieldUpdates['lesionType']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="lesionType" label="Lesion Type"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['lesionSizeBp']}"/></span>Lesion Size (bp)</th>
                                                            <td><c:choose><c:when test="${not empty lz.lesionSizeBp}">${lz.lesionSizeBp}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="lesionSizeBp" label="Lesion Size (bp)" updates="${lzFieldUpdates['lesionSizeBp']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="lesionSizeBp" label="Lesion Size (bp)"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['insertionSizeBp']}"/></span>Insertion Size (bp)</th>
                                                            <td><c:choose><c:when test="${not empty lz.insertionSizeBp}">${lz.insertionSizeBp}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="insertionSizeBp" label="Insertion Size (bp)" updates="${lzFieldUpdates['insertionSizeBp']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="insertionSizeBp" label="Insertion Size (bp)"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['nucleotideChange']}"/></span>Nucleotide Change</th>
                                                            <td><c:choose><c:when test="${not empty lz.nucleotideChange}"><c:out value="${lz.nucleotideChange}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="nucleotideChange" label="Nucleotide Change" updates="${lzFieldUpdates['nucleotideChange']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="nucleotideChange" label="Nucleotide Change"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['deletedSequence']}"/></span>Deleted Sequence</th>
                                                            <td><c:choose><c:when test="${not empty lz.deletedSequence}"><c:out value="${lz.deletedSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="deletedSequence" label="Deleted Sequence" updates="${lzFieldUpdates['deletedSequence']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="deletedSequence" label="Deleted Sequence"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['insertedSequence']}"/></span>Inserted Sequence</th>
                                                            <td><c:choose><c:when test="${not empty lz.insertedSequence}"><c:out value="${lz.insertedSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="insertedSequence" label="Inserted Sequence" updates="${lzFieldUpdates['insertedSequence']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="insertedSequence" label="Inserted Sequence"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['transgeneSequence']}"/></span>Transgene Sequence</th>
                                                            <td><c:choose><c:when test="${not empty lz.transgeneSequence}"><c:out value="${lz.transgeneSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="transgeneSequence" label="Transgene Sequence" updates="${lzFieldUpdates['transgeneSequence']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="transgeneSequence" label="Transgene Sequence"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['locationInline']}"/></span>Location (inline)</th>
                                                            <td><c:choose><c:when test="${not empty lz.locationInline}"><c:out value="${lz.locationInline}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="locationInline" label="Location (inline)" updates="${lzFieldUpdates['locationInline']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="locationInline" label="Location (inline)"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['fivePrimeFlank']}"/></span>5' Flank</th>
                                                            <td><c:choose><c:when test="${not empty lz.fivePrimeFlank}"><c:out value="${lz.fivePrimeFlank}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="fivePrimeFlank" label="5' Flank" updates="${lzFieldUpdates['fivePrimeFlank']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="fivePrimeFlank" label="5' Flank"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['threePrimeFlank']}"/></span>3' Flank</th>
                                                            <td><c:choose><c:when test="${not empty lz.threePrimeFlank}"><c:out value="${lz.threePrimeFlank}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="threePrimeFlank" label="3' Flank" updates="${lzFieldUpdates['threePrimeFlank']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="threePrimeFlank" label="3' Flank"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['hasLargeVariant']}"/></span>Has Large Variant</th>
                                                            <td><c:choose><c:when test="${lz.hasLargeVariant == true}">Yes</c:when><c:when test="${lz.hasLargeVariant == false}">No</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="hasLargeVariant" label="Has Large Variant" updates="${lzFieldUpdates['hasLargeVariant']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="hasLargeVariant" label="Has Large Variant"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['mutatedAminoAcids']}"/></span>Mutated Amino Acids</th>
                                                            <td><c:choose><c:when test="${not empty lz.mutatedAminoAcids}"><c:out value="${lz.mutatedAminoAcids}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="mutatedAminoAcids" label="Mutated Amino Acids" updates="${lzFieldUpdates['mutatedAminoAcids']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="mutatedAminoAcids" label="Mutated Amino Acids"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['mutatedAminoAcidsHgvs']}"/></span>Mutated Amino Acids (HGVS)</th>
                                                            <td><c:choose><c:when test="${not empty lz.mutatedAminoAcidsHgvs}"><c:out value="${lz.mutatedAminoAcidsHgvs}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="mutatedAminoAcidsHgvs" label="Mutated Amino Acids (HGVS)" updates="${lzFieldUpdates['mutatedAminoAcidsHgvs']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="mutatedAminoAcidsHgvs" label="Mutated Amino Acids (HGVS)"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['additionalInfo']}"/></span>Additional Info</th>
                                                            <td><c:choose><c:when test="${not empty lz.additionalInfo}"><c:out value="${lz.additionalInfo}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="additionalInfo" label="Additional Info" updates="${lzFieldUpdates['additionalInfo']}" scope="${lzScope}"/><z:zirc-field-comments recId="${lzRecId}" scope="field" fieldName="additionalInfo" label="Additional Info"/></td></tr>
                                                    </tbody>
                                                </table>
                                            </z:section>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Genotyping Assays" prependedText="${gaBadge}" appendedText="${gaApproval}" cssClass="ml-4" sectionID="mutation-${loop.count}-genotyping-assays">
                                <c:choose>
                                    <c:when test="${empty m.genotypingAssays}">
                                        <p class="text-muted">No genotyping assays recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${m.genotypingAssays}" var="ga" varStatus="galoop">
                                            <c:set var="gaFieldStatus" value="${assayFieldStatus[ga.id]}"/>
                                            <c:set var="gaFieldUpdates" value="${assayFieldUpdates[ga.id]}"/>
                                            <c:set var="gaScope" value="ga${ga.id}"/>
                                            <c:set var="gaRecId" value="ZIRC-GA-${ga.id}"/>
                                            <z:section title="Genotyping Assay ${galoop.count}" cssClass="ml-4">
                                                <table class="table table-borderless w-auto">
                                                    <tbody>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['assayType']}"/></span>Assay Type</th>
                                                            <td><c:choose><c:when test="${not empty ga.assayType}"><c:out value="${ga.assayType}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="assayType" label="Assay Type" updates="${gaFieldUpdates['assayType']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="assayType" label="Assay Type"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['forwardPrimer']}"/></span>Forward Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.forwardPrimer}"><c:out value="${ga.forwardPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="forwardPrimer" label="Forward Primer" updates="${gaFieldUpdates['forwardPrimer']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="forwardPrimer" label="Forward Primer"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['reversePrimer']}"/></span>Reverse Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.reversePrimer}"><c:out value="${ga.reversePrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="reversePrimer" label="Reverse Primer" updates="${gaFieldUpdates['reversePrimer']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="reversePrimer" label="Reverse Primer"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedWtPcr']}"/></span>Expected WT PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedWtPcr}"><c:out value="${ga.expectedWtPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="expectedWtPcr" label="Expected WT PCR" updates="${gaFieldUpdates['expectedWtPcr']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="expectedWtPcr" label="Expected WT PCR"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedMutPcr']}"/></span>Expected Mut PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedMutPcr}"><c:out value="${ga.expectedMutPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="expectedMutPcr" label="Expected Mut PCR" updates="${gaFieldUpdates['expectedMutPcr']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="expectedMutPcr" label="Expected Mut PCR"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['restrictionEnzymeName']}"/></span>Restriction Enzyme Name</th>
                                                            <td><c:choose><c:when test="${not empty ga.restrictionEnzymeName}"><c:out value="${ga.restrictionEnzymeName}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="restrictionEnzymeName" label="Restriction Enzyme Name" updates="${gaFieldUpdates['restrictionEnzymeName']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="restrictionEnzymeName" label="Restriction Enzyme Name"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['restrictionEnzymeCatalog']}"/></span>Restriction Enzyme Catalog</th>
                                                            <td><c:choose><c:when test="${not empty ga.restrictionEnzymeCatalog}"><c:out value="${ga.restrictionEnzymeCatalog}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="restrictionEnzymeCatalog" label="Restriction Enzyme Catalog" updates="${gaFieldUpdates['restrictionEnzymeCatalog']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="restrictionEnzymeCatalog" label="Restriction Enzyme Catalog"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['enzymeCleaves']}"/></span>Enzyme Cleaves</th>
                                                            <td><c:choose><c:when test="${not empty ga.enzymeCleaves}"><c:forEach items="${ga.enzymeCleaves}" var="ec" varStatus="ecloop"><c:if test="${!ecloop.first}">, </c:if><c:out value="${ec}"/></c:forEach></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="enzymeCleaves" label="Enzyme Cleaves" updates="${gaFieldUpdates['enzymeCleaves']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="enzymeCleaves" label="Enzyme Cleaves"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedWtDigest']}"/></span>Expected WT Digest</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedWtDigest}"><c:out value="${ga.expectedWtDigest}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="expectedWtDigest" label="Expected WT Digest" updates="${gaFieldUpdates['expectedWtDigest']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="expectedWtDigest" label="Expected WT Digest"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedMutDigest']}"/></span>Expected Mut Digest</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedMutDigest}"><c:out value="${ga.expectedMutDigest}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="expectedMutDigest" label="Expected Mut Digest" updates="${gaFieldUpdates['expectedMutDigest']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="expectedMutDigest" label="Expected Mut Digest"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sequencingPrimer']}"/></span>Sequencing Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.sequencingPrimer}"><c:out value="${ga.sequencingPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sequencingPrimer" label="Sequencing Primer" updates="${gaFieldUpdates['sequencingPrimer']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sequencingPrimer" label="Sequencing Primer"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['dcapsMismatchPrimer']}"/></span>dCAPS Mismatch Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.dcapsMismatchPrimer}"><c:out value="${ga.dcapsMismatchPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="dcapsMismatchPrimer" label="dCAPS Mismatch Primer" updates="${gaFieldUpdates['dcapsMismatchPrimer']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="dcapsMismatchPrimer" label="dCAPS Mismatch Primer"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['wtSpecificPrimer']}"/></span>WT Specific Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.wtSpecificPrimer}"><c:out value="${ga.wtSpecificPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="wtSpecificPrimer" label="WT Specific Primer" updates="${gaFieldUpdates['wtSpecificPrimer']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="wtSpecificPrimer" label="WT Specific Primer"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['mutSpecificPrimer']}"/></span>Mut Specific Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.mutSpecificPrimer}"><c:out value="${ga.mutSpecificPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="mutSpecificPrimer" label="Mut Specific Primer" updates="${gaFieldUpdates['mutSpecificPrimer']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="mutSpecificPrimer" label="Mut Specific Primer"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['commonPrimer']}"/></span>Common Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.commonPrimer}"><c:out value="${ga.commonPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="commonPrimer" label="Common Primer" updates="${gaFieldUpdates['commonPrimer']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="commonPrimer" label="Common Primer"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['kaspGenomicSequence']}"/></span>KASP Genomic Sequence</th>
                                                            <td><c:choose><c:when test="${not empty ga.kaspGenomicSequence}"><c:out value="${ga.kaspGenomicSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="kaspGenomicSequence" label="KASP Genomic Sequence" updates="${gaFieldUpdates['kaspGenomicSequence']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="kaspGenomicSequence" label="KASP Genomic Sequence"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpMarkerName']}"/></span>SSLP Marker Name</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpMarkerName}"><c:out value="${ga.sslpMarkerName}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sslpMarkerName" label="SSLP Marker Name" updates="${gaFieldUpdates['sslpMarkerName']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sslpMarkerName" label="SSLP Marker Name"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpDistance']}"/></span>SSLP Distance</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpDistance}"><c:out value="${ga.sslpDistance}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sslpDistance" label="SSLP Distance" updates="${gaFieldUpdates['sslpDistance']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sslpDistance" label="SSLP Distance"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpGenomicLocation']}"/></span>SSLP Genomic Location</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpGenomicLocation}"><c:out value="${ga.sslpGenomicLocation}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sslpGenomicLocation" label="SSLP Genomic Location" updates="${gaFieldUpdates['sslpGenomicLocation']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sslpGenomicLocation" label="SSLP Genomic Location"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpInducedBackground']}"/></span>SSLP Induced Background</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpInducedBackground}"><c:out value="${ga.sslpInducedBackground}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sslpInducedBackground" label="SSLP Induced Background" updates="${gaFieldUpdates['sslpInducedBackground']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sslpInducedBackground" label="SSLP Induced Background"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpOutcrossedBackground']}"/></span>SSLP Outcrossed Background</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpOutcrossedBackground}"><c:out value="${ga.sslpOutcrossedBackground}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sslpOutcrossedBackground" label="SSLP Outcrossed Background" updates="${gaFieldUpdates['sslpOutcrossedBackground']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sslpOutcrossedBackground" label="SSLP Outcrossed Background"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpInducedPcr']}"/></span>SSLP Induced PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpInducedPcr}"><c:out value="${ga.sslpInducedPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sslpInducedPcr" label="SSLP Induced PCR" updates="${gaFieldUpdates['sslpInducedPcr']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sslpInducedPcr" label="SSLP Induced PCR"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpOutcrossedPcr']}"/></span>SSLP Outcrossed PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpOutcrossedPcr}"><c:out value="${ga.sslpOutcrossedPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="sslpOutcrossedPcr" label="SSLP Outcrossed PCR" updates="${gaFieldUpdates['sslpOutcrossedPcr']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="sslpOutcrossedPcr" label="SSLP Outcrossed PCR"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['additionalInfo']}"/></span>Additional Info</th>
                                                            <td><c:choose><c:when test="${not empty ga.additionalInfo}"><c:out value="${ga.additionalInfo}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="additionalInfo" label="Additional Info" updates="${gaFieldUpdates['additionalInfo']}" scope="${gaScope}"/><z:zirc-field-comments recId="${gaRecId}" scope="field" fieldName="additionalInfo" label="Additional Info"/></td></tr>
                                                    </tbody>
                                                </table>
                                            </z:section>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Phenotypes" prependedText="${phenoBadge}" appendedText="${phenoApproval}" cssClass="ml-4" sectionID="mutation-${loop.count}-phenotypes">
                                <c:choose>
                                    <c:when test="${empty m.phenotypes}">
                                        <p class="text-muted">No phenotypes recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${m.phenotypes}" var="ph" varStatus="phloop">
                                            <c:set var="phFieldStatus" value="${phenotypeFieldStatus[ph.id]}"/>
                                            <c:set var="phFieldUpdates" value="${phenotypeFieldUpdates[ph.id]}"/>
                                            <c:set var="phScope" value="phen${ph.id}"/>
                                            <c:set var="phRecId" value="ZIRC-PHEN-${ph.id}"/>
                                            <z:section title="Phenotype ${phloop.count}" cssClass="ml-4">
                                                <table class="table table-borderless w-auto">
                                                    <tbody>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['description']}"/></span>Description</th>
                                                            <td><c:choose><c:when test="${not empty ph.description}"><c:out value="${ph.description}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="description" label="Description" updates="${phFieldUpdates['description']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="description" label="Description"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['hpfStart']}"/></span>HPF Start</th>
                                                            <td><c:choose><c:when test="${not empty ph.hpfStart}">${ph.hpfStart}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="hpfStart" label="HPF Start" updates="${phFieldUpdates['hpfStart']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="hpfStart" label="HPF Start"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['hpfEnd']}"/></span>HPF End</th>
                                                            <td><c:choose><c:when test="${not empty ph.hpfEnd}">${ph.hpfEnd}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="hpfEnd" label="HPF End" updates="${phFieldUpdates['hpfEnd']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="hpfEnd" label="HPF End"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['stage']}"/></span>Stage</th>
                                                            <td><c:choose><c:when test="${not empty ph.stage}"><c:out value="${ph.stage}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="stage" label="Stage" updates="${phFieldUpdates['stage']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="stage" label="Stage"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['zfinImagePermission']}"/></span>ZFIN Image Permission</th>
                                                            <td><c:choose><c:when test="${ph.zfinImagePermission == true}">Yes</c:when><c:when test="${ph.zfinImagePermission == false}">No</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="zfinImagePermission" label="ZFIN Image Permission" updates="${phFieldUpdates['zfinImagePermission']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="zfinImagePermission" label="ZFIN Image Permission"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['zircImagePermission']}"/></span>ZIRC Image Permission</th>
                                                            <td><c:choose><c:when test="${ph.zircImagePermission == true}">Yes</c:when><c:when test="${ph.zircImagePermission == false}">No</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="zircImagePermission" label="ZIRC Image Permission" updates="${phFieldUpdates['zircImagePermission']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="zircImagePermission" label="ZIRC Image Permission"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['nonMendelianPercentage']}"/></span>Non-Mendelian Percentage</th>
                                                            <td><c:choose><c:when test="${not empty ph.nonMendelianPercentage}">${ph.nonMendelianPercentage}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="nonMendelianPercentage" label="Non-Mendelian Percentage" updates="${phFieldUpdates['nonMendelianPercentage']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="nonMendelianPercentage" label="Non-Mendelian Percentage"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['nonMendelianComment']}"/></span>Non-Mendelian Comment</th>
                                                            <td><c:choose><c:when test="${not empty ph.nonMendelianComment}"><c:out value="${ph.nonMendelianComment}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="nonMendelianComment" label="Non-Mendelian Comment" updates="${phFieldUpdates['nonMendelianComment']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="nonMendelianComment" label="Non-Mendelian Comment"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['segregation']}"/></span>Segregation</th>
                                                            <td><c:choose><c:when test="${not empty ph.segregation}"><c:forEach items="${ph.segregation}" var="sg" varStatus="sgloop"><c:if test="${!sgloop.first}">, </c:if><c:out value="${sg}"/></c:forEach></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="segregation" label="Segregation" updates="${phFieldUpdates['segregation']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="segregation" label="Segregation"/></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['type']}"/></span>Type</th>
                                                            <td><c:choose><c:when test="${not empty ph.type}"><c:forEach items="${ph.type}" var="t" varStatus="tloop"><c:if test="${!tloop.first}">, </c:if><c:out value="${t}"/></c:forEach></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose><z:zirc-field-history fieldName="type" label="Type" updates="${phFieldUpdates['type']}" scope="${phScope}"/><z:zirc-field-comments recId="${phRecId}" scope="field" fieldName="type" label="Type"/></td></tr>
                                                    </tbody>
                                                </table>
                                            </z:section>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Lethality" prependedText="${lethalityBadge}" appendedText="${lethalityApproval}" cssClass="ml-4" sectionID="mutation-${loop.count}-lethality">
                                <table class="table table-borderless w-auto">
                                    <tbody>
                                        <tr>
                                            <th><span class="status-slot"></span>Homozygous Lethal</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${m.homozygousLethal == true}">Yes</c:when>
                                                    <c:when test="${m.homozygousLethal == false}">No</c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="homozygousLethal" label="Homozygous Lethal" updates="${mFieldUpdates['homozygousLethal']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="homozygousLethal" label="Homozygous Lethal"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Stage Typical</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalityStageTypical}"><c:out value="${m.lethalityStageTypical}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalityStageTypical" label="Lethality Stage Typical" updates="${mFieldUpdates['lethalityStageTypical']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="lethalityStageTypical" label="Lethality Stage Typical"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Specific Timepoint</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalitySpecificTimepoint}"><c:out value="${m.lethalitySpecificTimepoint}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalitySpecificTimepoint" label="Lethality Specific Timepoint" updates="${mFieldUpdates['lethalitySpecificTimepoint']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="lethalitySpecificTimepoint" label="Lethality Specific Timepoint"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Window</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalityWindowStart or not empty m.lethalityWindowEnd}"><c:out value="${m.lethalityWindowStart}"/> &ndash; <c:out value="${m.lethalityWindowEnd}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalityWindowStart" label="Lethality Window Start" updates="${mFieldUpdates['lethalityWindowStart']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="lethalityWindowStart" label="Lethality Window Start"/>
                                                <z:zirc-field-history fieldName="lethalityWindowEnd" label="Lethality Window End" updates="${mFieldUpdates['lethalityWindowEnd']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="lethalityWindowEnd" label="Lethality Window End"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Additional Info</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalityAdditionalInfo}"><c:out value="${m.lethalityAdditionalInfo}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalityAdditionalInfo" label="Lethality Additional Info" updates="${mFieldUpdates['lethalityAdditionalInfo']}" scope="${mScope}"/><z:zirc-field-comments recId="${mRecId}" scope="field" fieldName="lethalityAdditionalInfo" label="Lethality Additional Info"/>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </z:section>

                            <z:section title="Publications" prependedText="${pubsBadge}" appendedText="${pubsApproval}" cssClass="ml-4" sectionID="mutation-${loop.count}-publications">
                                <c:choose>
                                    <c:when test="${empty m.publications}">
                                        <p class="text-muted">No publications recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <p>
                                            <c:forEach items="${m.publications}" var="p" varStatus="ploop"><c:if test="${!ploop.first}">, </c:if><c:out value="${p}"/></c:forEach>
                                        </p>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>
                        </z:section>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </z:section>

        <c:set var="linkedBadge"><z:zirc-status-badge status="${sectionStatus['Linked Features']}"/><z:zirc-section-history key="linked-features" label="Linked Features" updates="${sectionUpdates['Linked Features']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="section" sectionName="linked-features" label="Linked Features"/></c:set>
        <c:set var="linkedApproval"><z:zirc-section-approval recId="${submission.zdbID}" sectionName="linked-features" approved="${sectionApprovals[submission.zdbID.concat('|linked-features')]}" enabled="${(sectionStatus['Linked Features'] != null) and (sectionStatus['Linked Features'].name() == 'COMPLETE' or sectionStatus['Linked Features'].name() == 'APPROVED')}"/></c:set>
        <z:section title="${LINKED_FEATURES}" prependedText="${linkedBadge}" appendedText="${linkedApproval}">
            <c:choose>
                <c:when test="${empty submission.linkedFeatures}">
                    <p class="text-muted">No linked features.</p>
                </c:when>
                <c:otherwise>
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Mutation A</th>
                                <th>Mutation B</th>
                                <th>Distance Known</th>
                                <th>Distance</th>
                                <th>Additional Info</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${submission.linkedFeatures}" var="lf">
                                <c:set var="lfKey" value="${lf.mutationA.id}-${lf.mutationB.id}"/>
                                <c:set var="lfUpdates" value="${linkedFeatureFieldUpdates[lfKey]}"/>
                                <c:set var="lfScope" value="lf${lfKey}"/>
                                <c:set var="lfRecId" value="ZIRC-LF-${lfKey}"/>
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.mutationA.alleleDesignation}"><c:out value="${lf.mutationA.alleleDesignation}"/></c:when>
                                            <c:otherwise>#${lf.mutationA.sortOrder}</c:otherwise>
                                        </c:choose>
                                        <z:zirc-field-history fieldName="mutationA" label="Mutation A" updates="${lfUpdates['mutationA']}" scope="${lfScope}"/><z:zirc-field-comments recId="${lfRecId}" scope="field" fieldName="mutationA" label="Mutation A"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.mutationB.alleleDesignation}"><c:out value="${lf.mutationB.alleleDesignation}"/></c:when>
                                            <c:otherwise>#${lf.mutationB.sortOrder}</c:otherwise>
                                        </c:choose>
                                        <z:zirc-field-history fieldName="mutationB" label="Mutation B" updates="${lfUpdates['mutationB']}" scope="${lfScope}"/><z:zirc-field-comments recId="${lfRecId}" scope="field" fieldName="mutationB" label="Mutation B"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${lf.distanceKnown == true}">Yes</c:when>
                                            <c:when test="${lf.distanceKnown == false}">No</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                        <z:zirc-field-history fieldName="distanceKnown" label="Distance Known" updates="${lfUpdates['distanceKnown']}" scope="${lfScope}"/><z:zirc-field-comments recId="${lfRecId}" scope="field" fieldName="distanceKnown" label="Distance Known"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.distanceCentimorgans}">${lf.distanceCentimorgans} cM</c:when>
                                            <c:when test="${not empty lf.distanceMegabases}">${lf.distanceMegabases} Mb</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                        <z:zirc-field-history fieldName="distanceCentimorgans" label="Distance (cM)" updates="${lfUpdates['distanceCentimorgans']}" scope="${lfScope}"/><z:zirc-field-comments recId="${lfRecId}" scope="field" fieldName="distanceCentimorgans" label="Distance (cM)"/>
                                        <z:zirc-field-history fieldName="distanceMegabases" label="Distance (Mb)" updates="${lfUpdates['distanceMegabases']}" scope="${lfScope}"/><z:zirc-field-comments recId="${lfRecId}" scope="field" fieldName="distanceMegabases" label="Distance (Mb)"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.additionalInfo}"><c:out value="${lf.additionalInfo}"/></c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                        <z:zirc-field-history fieldName="additionalInfo" label="Additional Info" updates="${lfUpdates['additionalInfo']}" scope="${lfScope}"/><z:zirc-field-comments recId="${lfRecId}" scope="field" fieldName="additionalInfo" label="Additional Info"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </z:section>

        <c:set var="backgroundBadge"><z:zirc-status-badge status="${sectionStatus['Background']}"/><z:zirc-section-history key="background" label="Background" updates="${sectionUpdates['Background']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="section" sectionName="background" label="Background"/></c:set>
        <c:set var="backgroundApproval"><z:zirc-section-approval recId="${submission.zdbID}" sectionName="background" approved="${sectionApprovals[submission.zdbID.concat('|background')]}" enabled="${(sectionStatus['Background'] != null) and (sectionStatus['Background'].name() == 'COMPLETE' or sectionStatus['Background'].name() == 'APPROVED')}"/></c:set>
        <z:section title="${BACKGROUND}" prependedText="${backgroundBadge}" appendedText="${backgroundApproval}">
            <table class="table table-borderless w-auto">
                <tbody>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['maternalBackground']}"/></span>Maternal</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.maternalBackground}"><c:out value="${submission.maternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="maternalBackground" label="Maternal Background" updates="${fieldUpdates['maternalBackground']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="maternalBackground" label="Maternal Background"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['paternalBackground']}"/></span>Paternal</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.paternalBackground}"><c:out value="${submission.paternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="paternalBackground" label="Paternal Background" updates="${fieldUpdates['paternalBackground']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="paternalBackground" label="Paternal Background"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['backgroundChangeable']}"/></span>Background Changeable</th>
                        <td>
                            <c:choose>
                                <c:when test="${submission.backgroundChangeable == true}">Yes</c:when>
                                <c:when test="${submission.backgroundChangeable == false}">No</c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="backgroundChangeable" label="Background Changeable" updates="${fieldUpdates['backgroundChangeable']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="backgroundChangeable" label="Background Changeable"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['backgroundChangeConcerns']}"/></span>Concerns</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.backgroundChangeConcerns}"><c:out value="${submission.backgroundChangeConcerns}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="backgroundChangeConcerns" label="Concerns" updates="${fieldUpdates['backgroundChangeConcerns']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="backgroundChangeConcerns" label="Concerns"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <c:set var="additionalBadge"><z:zirc-status-badge status="${sectionStatus['Additional Info']}"/><z:zirc-section-history key="additional-info" label="Additional Info" updates="${sectionUpdates['Additional Info']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="section" sectionName="additional-info" label="Additional Info"/></c:set>
        <c:set var="additionalApproval"><z:zirc-section-approval recId="${submission.zdbID}" sectionName="additional-info" approved="${sectionApprovals[submission.zdbID.concat('|additional-info')]}" enabled="${(sectionStatus['Additional Info'] != null) and (sectionStatus['Additional Info'].name() == 'COMPLETE' or sectionStatus['Additional Info'].name() == 'APPROVED')}"/></c:set>
        <z:section title="${ADDITIONAL}" prependedText="${additionalBadge}" appendedText="${additionalApproval}">
            <table class="table table-borderless w-auto">
                <tbody>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['additionalInfo']}"/></span>Additional Info</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.additionalInfo}"><c:out value="${submission.additionalInfo}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="additionalInfo" label="Additional Info" updates="${fieldUpdates['additionalInfo']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="additionalInfo" label="Additional Info"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['unreportedFeaturesDetails']}"/></span>Unreported Features Details</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.unreportedFeaturesDetails}"><c:out value="${submission.unreportedFeaturesDetails}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="unreportedFeaturesDetails" label="Unreported Features Details" updates="${fieldUpdates['unreportedFeaturesDetails']}"/><z:zirc-field-comments recId="${submission.zdbID}" scope="field" fieldName="unreportedFeaturesDetails" label="Unreported Features Details"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <%-- Shared comments modal (one per page). The trigger anchors that
             open it carry the target context as data-* attributes; the
             JS at the bottom of the page reads them on show, fetches the
             thread via AJAX, and rebinds the form's submit handler to
             POST against the same target.
             No `fade` — we open/close manually so we don't depend on
             Bootstrap's transition end firing reliably for this modal. --%>
        <div class="modal" id="zircCommentsModal" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="zirc-comments-title">Comments</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <form id="zirc-comments-form" class="mb-3">
                            <div class="form-group mb-2">
                                <textarea id="zirc-comments-input" class="form-control" rows="3"
                                          placeholder="Add a comment&hellip;" required></textarea>
                            </div>
                            <div class="d-flex justify-content-between align-items-center">
                                <div id="zirc-comments-error" class="text-danger small" style="display:none;"></div>
                                <button type="submit" class="btn btn-sm btn-primary ml-auto">Post</button>
                            </div>
                        </form>
                        <hr/>
                        <div id="zirc-comments-list">
                            <p class="text-muted small">Loading&hellip;</p>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

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
                        <input type="text" id="add-submitter-person-input" class="form-control" autocomplete="off" placeholder="Start typing a name&hellip;"/>
                        <div class="form-text text-muted small mt-1">Pick a name from the dropdown to add.</div>
                        <div id="add-submitter-error" class="text-danger small mt-2" style="display:none;"></div>
                        <div id="add-submitter-progress" class="text-muted small mt-2" style="display:none;">Adding&hellip;</div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    </div>
                </div>
            </div>
        </div>

        <%-- Modal for adding a PI via person-name autocomplete (ZFIN-10325). --%>
        <div class="modal fade" id="addPiModal" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Add a PI</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <label for="add-pi-person-input" class="form-label">Search for a person by name</label>
                        <input type="text" id="add-pi-person-input" class="form-control" autocomplete="off" placeholder="Start typing a name&hellip;"/>
                        <div class="form-text text-muted small mt-1">Pick a name from the dropdown to add.</div>
                        <div id="add-pi-error" class="text-danger small mt-2" style="display:none;"></div>
                        <div id="add-pi-progress" class="text-muted small mt-2" style="display:none;">Adding&hellip;</div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    </div>
                </div>
            </div>
        </div>

        <script>
        jQuery(function () {
            // Copy ZDB ID to clipboard when the copy icon is clicked
            jQuery('#copy-zdb-id').on('click', function (e) {
                e.preventDefault();
                var text = jQuery('#zdb-id-value').text().trim();
                if (navigator.clipboard) {
                    navigator.clipboard.writeText(text);
                }
                var $icon = jQuery(this);
                var original = $icon.attr('title');
                $icon.attr('title', 'Copied!');
                if ($icon.tooltip) {
                    $icon.tooltip('dispose').tooltip('show');
                    setTimeout(function () { $icon.tooltip('dispose').attr('title', original); }, 1000);
                }
            });

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

            // ─── Add-PI modal (ZFIN-10325) ──────────────────────────────────
            // Independent of the submitter flow but uses the same person
            // autocomplete + role-specific endpoint. The DB has a UNIQUE
            // (submission, person) constraint, so picking the same person
            // here who is already a submitter returns a friendly 200 without
            // inserting (server-side gate in addPersonWithRole).
            var piInitialized = false;

            function postAddPi(personZdbID) {
                var $input    = jQuery('#add-pi-person-input');
                var $error    = jQuery('#add-pi-error');
                var $progress = jQuery('#add-pi-progress');
                $error.hide();
                $progress.show();
                $input.prop('disabled', true);
                jQuery.ajax({
                    url: '/action/zirc/line-submission/${submission.zdbID}/add-pi',
                    type: 'POST',
                    data: { personZdbID: personZdbID },
                    success: function () {
                        window.location.reload();
                    },
                    error: function (xhr) {
                        $progress.hide();
                        $input.prop('disabled', false);
                        $error.text('Failed to add PI: ' +
                            (xhr.responseJSON && xhr.responseJSON.message || xhr.statusText)).show();
                    }
                });
            }

            function initPiAutocomplete() {
                if (piInitialized) return;
                var $input = jQuery('#add-pi-person-input');
                if (!$input.length || typeof $input.autocomplete !== 'function') {
                    return;
                }
                $input.autocomplete({
                    appendTo: '#addPiModal .modal-body',
                    source: function (request, response) {
                        jQuery.ajax({
                            url: '/action/zirc/persons/search',
                            dataType: 'json',
                            data: { term: request.term },
                            success: function (data) { response(data); },
                            error: function (xhr) {
                                jQuery('#add-pi-error')
                                    .text('Search failed: ' + xhr.statusText).show();
                            }
                        });
                    },
                    minLength: 2,
                    select: function (event, ui) {
                        event.preventDefault();
                        jQuery('#add-pi-person-input').val(ui.item.value);
                        postAddPi(ui.item.zdbID);
                    }
                });

                jQuery(document).on('mousedown.addpi',
                    '#addPiModal .ui-autocomplete .ui-menu-item',
                    function (e) {
                        var data = jQuery(this).data('ui-autocomplete-item');
                        if (data && data.zdbID) {
                            e.preventDefault();
                            e.stopPropagation();
                            jQuery('#add-pi-person-input').val(data.value);
                            jQuery('#add-pi-person-input').autocomplete('close');
                            postAddPi(data.zdbID);
                        }
                    });

                piInitialized = true;
            }

            jQuery('#addPiModal')
                .on('show.bs.modal', function () {
                    var $input = jQuery('#add-pi-person-input');
                    $input.val('').prop('disabled', false);
                    jQuery('#add-pi-error').hide();
                    jQuery('#add-pi-progress').hide();
                })
                .on('shown.bs.modal', function () {
                    initPiAutocomplete();
                    jQuery('#add-pi-person-input').trigger('focus');
                });

            // ─── Per-field / per-section comments modal ───────────────────
            // Trigger anchors (.zirc-comments-trigger) carry the thread
            // target as data-* attributes; the modal is shared. We open
            // it via JS instead of data-toggle so the target context
            // travels along on the event.
            // Reparent to <body> so position:fixed is relative to the
            // viewport, not whichever transformed ancestor we'd otherwise
            // be trapped in (which is what makes the modal disappear
            // behind the backdrop with only the grey overlay visible).
            var $cmodal  = jQuery('#zircCommentsModal').appendTo('body');
            // Manual open/close — we found Bootstrap's .modal('show')
            // intermittently bails before adding .show on this modal
            // (display:block was set, .show class never got added → grey
            // backdrop with no dialog). Hand-rolling the four bits of
            // state Bootstrap juggles works around it.
            function openCommentsModal() {
                $cmodal.css('display', 'block').addClass('show');
                jQuery('body').addClass('modal-open');
                if (jQuery('.zirc-comments-backdrop').length === 0) {
                    jQuery('<div class="modal-backdrop show zirc-comments-backdrop"></div>')
                        .appendTo('body')
                        .on('click', closeCommentsModal);
                }
            }
            function closeCommentsModal() {
                $cmodal.css('display', 'none').removeClass('show');
                jQuery('.zirc-comments-backdrop').remove();
                if (jQuery('.modal.show').length === 0) {
                    jQuery('body').removeClass('modal-open');
                }
            }
            // Wire close on the [data-dismiss="modal"] buttons inside our modal
            // (we own them; the modal plugin's own delegate doesn't fire when
            // we open manually).
            $cmodal.find('[data-dismiss="modal"]').on('click', closeCommentsModal);
            // Escape key closes too.
            jQuery(document).on('keydown.zircComments', function (e) {
                if (e.key === 'Escape' && $cmodal.hasClass('show')) {
                    closeCommentsModal();
                }
            });
            var $cTitle  = jQuery('#zirc-comments-title');
            var $cInput  = jQuery('#zirc-comments-input');
            var $cError  = jQuery('#zirc-comments-error');
            var $cList   = jQuery('#zirc-comments-list');
            var $cForm   = jQuery('#zirc-comments-form');
            var ctx      = {};   // current thread context

            function esc(s) { return jQuery('<div/>').text(s == null ? '' : s).html(); }
            function fmtWhen(iso) {
                if (!iso) return '';
                try {
                    var d = new Date(iso);
                    return d.toLocaleString();
                } catch (e) { return iso; }
            }
            function renderComments(rows) {
                if (!rows || !rows.length) {
                    $cList.html('<p class="text-muted small">No comments yet. Be the first.</p>');
                    return;
                }
                var html = rows.map(function (r) {
                    var who = r.authorZdbId
                        ? '<a href="/action/profile/person/view/' + esc(r.authorZdbId) + '">' + esc(r.author) + '</a>'
                        : esc(r.author);
                    return ''
                        + '<div class="border rounded p-2 mb-2">'
                        + '  <div class="small text-muted d-flex justify-content-between">'
                        + '    <span>' + who + '</span>'
                        + '    <span>' + esc(fmtWhen(r.createdAt)) + '</span>'
                        + '  </div>'
                        + '  <div style="white-space:pre-wrap;">' + esc(r.comment) + '</div>'
                        + '</div>';
                }).join('');
                $cList.html(html);
            }
            function loadComments() {
                $cList.html('<p class="text-muted small">Loading&hellip;</p>');
                var params = {
                    recId: ctx.recId,
                    scope: ctx.scope
                };
                if (ctx.scope === 'field')   params.fieldName   = ctx.name;
                if (ctx.scope === 'section') params.sectionName = ctx.name;
                jQuery.getJSON('/action/zirc/comments', params)
                    .done(renderComments)
                    .fail(function (xhr) {
                        $cList.html('<p class="text-danger small">Failed to load: ' + esc(xhr.statusText) + '</p>');
                    });
            }

            // Open the shared modal when any trigger is clicked.
            jQuery(document).on('click', '.zirc-comments-trigger', function (e) {
                e.preventDefault();
                var $t = jQuery(this);
                ctx = {
                    recId: $t.data('rec-id'),
                    scope: $t.data('scope'),
                    name:  $t.data('scope') === 'field'
                                ? $t.data('field-name')
                                : $t.data('section-name'),
                    label: $t.data('label')
                };
                $cTitle.text('Comments \u2014 ' + ctx.label);
                $cInput.val('');
                $cError.hide().text('');
                openCommentsModal();
                loadComments();
                $cInput.trigger('focus');
            });

            $cForm.on('submit', function (e) {
                e.preventDefault();
                var text = jQuery.trim($cInput.val());
                if (!text) { $cInput.trigger('focus'); return; }
                $cError.hide();
                var data = {
                    recId: ctx.recId,
                    scope: ctx.scope,
                    comment: text
                };
                if (ctx.scope === 'field')   data.fieldName   = ctx.name;
                if (ctx.scope === 'section') data.sectionName = ctx.name;
                jQuery.post('/action/zirc/comments', data)
                    .done(function () {
                        $cInput.val('');
                        loadComments();
                    })
                    .fail(function (xhr) {
                        var msg = (xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText;
                        $cError.text('Failed to post: ' + msg).show();
                    });
            });

            // Cmd/Ctrl+Enter posts the comment for quick keyboard flow.
            $cInput.on('keydown', function (e) {
                if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                    e.preventDefault();
                    $cForm.trigger('submit');
                }
            });

            // ─── Per-section "Approved" checkbox ──────────────────────────
            // Toggling POSTs to /action/zirc/section-approval immediately;
            // checkbox state is rolled back if the server rejects.
            // Badge flip helpers. Since the Approved checkbox is only
            // enabled when the section's underlying status is COMPLETE
            // (or already APPROVED), un-approving always returns the
            // badge to COMPLETE — no need to cache base state.
            function badgeToApproved($badge) {
                if (!$badge.length) return;
                $badge.removeClass('badge-success badge-warning badge-danger')
                      .addClass('badge-primary')
                      .attr('title', 'Approved')
                      .text('A');
            }
            function badgeToComplete($badge) {
                if (!$badge.length) return;
                $badge.removeClass('badge-warning badge-danger badge-primary')
                      .addClass('badge-success')
                      .attr('title', 'Complete')
                      .text('C');
            }

            // Sync the top-of-page status-overview bar from the current
            // section-badge classes. Run after every approval toggle so
            // the bar reflects the live UI without a page reload.
            function syncStatusBar() {
                jQuery('.status-overview-bar td.status-cell').each(function () {
                    var $cell = jQuery(this);
                    var secId = $cell.data('section-id');
                    if (!secId) return;
                    var $badge = jQuery('section#' + secId).children('.heading').find('.badge').first();
                    $cell.removeClass('status-missing status-in-progress status-complete status-approved');
                    if (!$badge.length) return;
                    if      ($badge.hasClass('badge-danger'))  $cell.addClass('status-missing');
                    else if ($badge.hasClass('badge-warning')) $cell.addClass('status-in-progress');
                    else if ($badge.hasClass('badge-success')) $cell.addClass('status-complete');
                    else if ($badge.hasClass('badge-primary')) $cell.addClass('status-approved');
                });
            }
            // For each parent section above $section, recompute "are all
            // its direct-child sections approved?" and flip its badge.
            // Continues up until we run out of parent sections, then
            // also updates the page <h1> overall badge.
            function propagateApprovalUp($section) {
                while ($section.length) {
                    var $parent = $section.parent().closest('section.section');
                    if (!$parent.length) break;
                    var $childCBs = $parent.children('section.section')
                                           .children('.heading')
                                           .find('.zirc-section-approval');
                    if ($childCBs.length) {
                        var allChecked = $childCBs.toArray().every(function (cb) { return cb.checked; });
                        var $parentBadge = $parent.children('.heading').find('.badge').first();
                        if (allChecked) badgeToApproved($parentBadge);
                        else            badgeToComplete($parentBadge);
                    }
                    $section = $parent;
                }
                // Page header: all top-level section approvals?
                var $topCBs = jQuery('section.section').filter(function () {
                    return !jQuery(this).parent().closest('section.section').length;
                }).children('.heading').find('.zirc-section-approval');
                if ($topCBs.length) {
                    var allTop = $topCBs.toArray().every(function (cb) { return cb.checked; });
                    var $h1Badge = jQuery('h1 > .badge').first();
                    if (allTop) badgeToApproved($h1Badge);
                    else        badgeToComplete($h1Badge);
                }
            }

            jQuery(document).on('change', '.zirc-section-approval', function () {
                var $cb = jQuery(this);
                var approved = $cb.is(':checked');
                $cb.prop('disabled', true);
                jQuery.post('/action/zirc/section-approval', {
                    recId:       $cb.data('rec-id'),
                    sectionName: $cb.data('section-name'),
                    approved:    approved
                })
                .done(function () {
                    // Flip THIS section's badge. The checkbox was enabled
                    // only when its base was COMPLETE (or already
                    // APPROVED), so it's safe to revert to "C" on uncheck.
                    var $badge = $cb.closest('.heading').find('.badge').first();
                    if (approved) badgeToApproved($badge);
                    else          badgeToComplete($badge);
                    // Walk up: parent sections roll-up = all-children-approved.
                    propagateApprovalUp($cb.closest('section.section'));
                    syncStatusBar();
                })
                .always(function () { $cb.prop('disabled', false); })
                .fail(function (xhr) {
                    $cb.prop('checked', !approved);
                    var msg = (xhr.responseJSON && xhr.responseJSON.message) || xhr.statusText;
                    alert('Failed to save approval: ' + msg);
                });
            });
        });
        </script>

    </jsp:body>
</z:dataPage>
