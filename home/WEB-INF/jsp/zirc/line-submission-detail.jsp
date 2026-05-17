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
            .field-history-trigger:focus,
            .field-history-trigger:hover { opacity: 1; }
        </style>

        <div class="small text-uppercase text-muted">ZIRC Line Submission</div>
        <h1><z:zirc-status-badge status="${overallStatus}"/> ${submission.name}</h1>

        <c:set var="overviewBadge"><z:zirc-status-badge status="${sectionStatus['Overview']}"/></c:set>
        <z:section title="${OVERVIEW}" prependedText="${overviewBadge}">
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
                            <z:zirc-field-history fieldName="name" label="Name" updates="${fieldUpdates['name']}"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['previousNames']}"/></span>Previous Names</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.previousNames}"><c:out value="${submission.previousNames}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="previousNames" label="Previous Names" updates="${fieldUpdates['previousNames']}"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"></span>Date Started</th>
                        <td><fmt:formatDate value="${submission.createdAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"></span>Last Updated</th>
                        <td><fmt:formatDate value="${submission.updatedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"></span>Submitter</th>
                        <td>
                            <c:choose>
                                <c:when test="${empty submission.persons}">
                                    <span class="text-muted">&mdash;</span>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${submission.persons}" var="lsp" varStatus="loop">
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
                            <z:zirc-field-history fieldName="reasons" label="Acceptance Reasons" updates="${fieldUpdates['reasons']}"/>
                            <z:zirc-field-history fieldName="reasonsOther" label="Acceptance Reasons (Other)" updates="${fieldUpdates['reasonsOther']}"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <c:set var="mutationsBadge"><z:zirc-status-badge status="${sectionStatus['Mutations']}"/></c:set>
        <z:section title="${MUTATIONS}" prependedText="${mutationsBadge}">
            <c:choose>
                <c:when test="${empty submission.mutations}">
                    <p class="text-muted">No mutations recorded for this submission.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${submission.mutations}" var="m" varStatus="loop">
                        <c:set var="mFieldStatus" value="${mutationFieldStatus[m.id]}"/>
                        <c:set var="mFieldUpdates" value="${mutationFieldUpdates[m.id]}"/>
                        <c:set var="mScope" value="mut${m.id}"/>
                        <c:set var="mSectStat" value="${mutationSectionStatus[m.id]}"/>
                        <c:set var="mSectionBadge"><z:zirc-status-badge status="${mutationStatus[m.id]}"/></c:set>
                        <c:set var="overviewBadge"><z:zirc-status-badge status="${mSectStat['Overview']}"/></c:set>
                        <c:set var="genesBadge"><z:zirc-status-badge status="${mSectStat['Genes']}"/></c:set>
                        <c:set var="lesionsBadge"><z:zirc-status-badge status="${mSectStat['Lesions']}"/></c:set>
                        <c:set var="gaBadge"><z:zirc-status-badge status="${mSectStat['Genotyping Assays']}"/></c:set>
                        <c:set var="phenoBadge"><z:zirc-status-badge status="${mSectStat['Phenotypes']}"/></c:set>
                        <c:set var="lethalityBadge"><z:zirc-status-badge status="${mSectStat['Lethality']}"/></c:set>
                        <c:set var="pubsBadge"><z:zirc-status-badge status="${mSectStat['Publications']}"/></c:set>
                        <z:section title="Mutation ${loop.count}" prependedText="${mSectionBadge}" cssClass="ml-4">
                            <p class="text-right mb-2">
                                <a class="btn btn-sm btn-outline-primary"
                                   href="/action/zirc/mutation/${m.id}/edit">Edit</a>
                            </p>

                            <z:section title="Overview" prependedText="${overviewBadge}" cssClass="ml-4" sectionID="mutation-${loop.count}-overview">
                                <table class="table table-borderless w-auto">
                                    <tbody>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['alleleInZfin']}"/></span>Allele in ZFIN</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${m.alleleInZfin == true}">Yes</c:when>
                                                    <c:when test="${m.alleleInZfin == false}">No</c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="alleleInZfin" label="Allele in ZFIN" updates="${mFieldUpdates['alleleInZfin']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['alleleDesignation']}"/></span>Allele Designation</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.alleleDesignation}"><c:out value="${m.alleleDesignation}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="alleleDesignation" label="Allele Designation" updates="${mFieldUpdates['alleleDesignation']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutagenesisStage']}"/></span>Mutagenesis Stage</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutagenesisStage}"><c:out value="${m.mutagenesisStage}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutagenesisStage" label="Mutagenesis Stage" updates="${mFieldUpdates['mutagenesisStage']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutagenesisProtocol']}"/></span>Mutagenesis Protocol</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutagenesisProtocol}"><c:out value="${m.mutagenesisProtocol}"/><c:if test="${m.mutagenesisProtocol == 'other' and not empty m.mutagenesisProtocolOther}"> (<c:out value="${m.mutagenesisProtocolOther}"/>)</c:if></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutagenesisProtocol" label="Mutagenesis Protocol" updates="${mFieldUpdates['mutagenesisProtocol']}" scope="${mScope}"/>
                                                <z:zirc-field-history fieldName="mutagenesisProtocolOther" label="Mutagenesis Protocol (Other)" updates="${mFieldUpdates['mutagenesisProtocolOther']}" scope="${mScope}"/>
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
                                                <z:zirc-field-history fieldName="molecularlyCharacterized" label="Molecularly Characterized" updates="${mFieldUpdates['molecularlyCharacterized']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutationType']}"/></span>Mutation Type</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutationType}"><c:out value="${m.mutationType}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutationType" label="Mutation Type" updates="${mFieldUpdates['mutationType']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['zfinRecordEstablished']}"/></span>ZFIN Record Established</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${m.zfinRecordEstablished == true}">Yes</c:when>
                                                    <c:when test="${m.zfinRecordEstablished == false}">No</c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="zfinRecordEstablished" label="ZFIN Record Established" updates="${mFieldUpdates['zfinRecordEstablished']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['cellGenomicFeature']}"/></span>ZDB Genomic Feature #</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.cellGenomicFeature}"><c:out value="${m.cellGenomicFeature}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="cellGenomicFeature" label="ZDB Genomic Feature #" updates="${mFieldUpdates['cellGenomicFeature']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutationDiscoverer']}"/></span>Discoverer</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutationDiscoverer}"><c:out value="${m.mutationDiscoverer}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutationDiscoverer" label="Discoverer" updates="${mFieldUpdates['mutationDiscoverer']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"><z:zirc-status-badge status="${mFieldStatus['mutationInstitution']}"/></span>Institution</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.mutationInstitution}"><c:out value="${m.mutationInstitution}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="mutationInstitution" label="Institution" updates="${mFieldUpdates['mutationInstitution']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </z:section>

                            <z:section title="Genes" prependedText="${genesBadge}" cssClass="ml-4" sectionID="mutation-${loop.count}-genes">
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
                                                    <tr>
                                                        <td>${gloop.count}</td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['mutatedGene']}"/> <c:choose><c:when test="${not empty g.mutatedGene}"><a href="/${g.mutatedGene.zdbID}"><c:out value="${g.mutatedGene.abbreviation}"/></a></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['linkageGroup']}"/> <c:choose><c:when test="${not empty g.linkageGroup}"><c:out value="${g.linkageGroup}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['genbankGenomicDna']}"/> <c:choose><c:when test="${not empty g.genbankGenomicDna}"><c:out value="${g.genbankGenomicDna}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td>
                                                        <td><z:zirc-status-badge status="${gFieldStatus['genbankCdna']}"/> <c:choose><c:when test="${not empty g.genbankCdna}"><c:out value="${g.genbankCdna}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Lesions" prependedText="${lesionsBadge}" cssClass="ml-4" sectionID="mutation-${loop.count}-lesions">
                                <c:choose>
                                    <c:when test="${empty m.lesions}">
                                        <p class="text-muted">No lesions recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${m.lesions}" var="lz" varStatus="lloop">
                                            <c:set var="lzFieldStatus" value="${lesionFieldStatus[lz.id]}"/>
                                            <z:section title="Lesion ${lloop.count}" cssClass="ml-4">
                                                <table class="table table-borderless w-auto">
                                                    <tbody>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['lesionType']}"/></span>Lesion Type</th>
                                                            <td><c:choose><c:when test="${not empty lz.lesionType}"><c:out value="${lz.lesionType}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['lesionSizeBp']}"/></span>Lesion Size (bp)</th>
                                                            <td><c:choose><c:when test="${not empty lz.lesionSizeBp}">${lz.lesionSizeBp}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['insertionSizeBp']}"/></span>Insertion Size (bp)</th>
                                                            <td><c:choose><c:when test="${not empty lz.insertionSizeBp}">${lz.insertionSizeBp}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['nucleotideChange']}"/></span>Nucleotide Change</th>
                                                            <td><c:choose><c:when test="${not empty lz.nucleotideChange}"><c:out value="${lz.nucleotideChange}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['deletedSequence']}"/></span>Deleted Sequence</th>
                                                            <td><c:choose><c:when test="${not empty lz.deletedSequence}"><c:out value="${lz.deletedSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['insertedSequence']}"/></span>Inserted Sequence</th>
                                                            <td><c:choose><c:when test="${not empty lz.insertedSequence}"><c:out value="${lz.insertedSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['transgeneSequence']}"/></span>Transgene Sequence</th>
                                                            <td><c:choose><c:when test="${not empty lz.transgeneSequence}"><c:out value="${lz.transgeneSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['locationInline']}"/></span>Location (inline)</th>
                                                            <td><c:choose><c:when test="${not empty lz.locationInline}"><c:out value="${lz.locationInline}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['fivePrimeFlank']}"/></span>5' Flank</th>
                                                            <td><c:choose><c:when test="${not empty lz.fivePrimeFlank}"><c:out value="${lz.fivePrimeFlank}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['threePrimeFlank']}"/></span>3' Flank</th>
                                                            <td><c:choose><c:when test="${not empty lz.threePrimeFlank}"><c:out value="${lz.threePrimeFlank}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['hasLargeVariant']}"/></span>Has Large Variant</th>
                                                            <td><c:choose><c:when test="${lz.hasLargeVariant == true}">Yes</c:when><c:when test="${lz.hasLargeVariant == false}">No</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['mutatedAminoAcids']}"/></span>Mutated Amino Acids</th>
                                                            <td><c:choose><c:when test="${not empty lz.mutatedAminoAcids}"><c:out value="${lz.mutatedAminoAcids}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['mutatedAminoAcidsHgvs']}"/></span>Mutated Amino Acids (HGVS)</th>
                                                            <td><c:choose><c:when test="${not empty lz.mutatedAminoAcidsHgvs}"><c:out value="${lz.mutatedAminoAcidsHgvs}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${lzFieldStatus['additionalInfo']}"/></span>Additional Info</th>
                                                            <td><c:choose><c:when test="${not empty lz.additionalInfo}"><c:out value="${lz.additionalInfo}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                    </tbody>
                                                </table>
                                            </z:section>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Genotyping Assays" prependedText="${gaBadge}" cssClass="ml-4" sectionID="mutation-${loop.count}-genotyping-assays">
                                <c:choose>
                                    <c:when test="${empty m.genotypingAssays}">
                                        <p class="text-muted">No genotyping assays recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${m.genotypingAssays}" var="ga" varStatus="galoop">
                                            <c:set var="gaFieldStatus" value="${assayFieldStatus[ga.id]}"/>
                                            <z:section title="Genotyping Assay ${galoop.count}" cssClass="ml-4">
                                                <table class="table table-borderless w-auto">
                                                    <tbody>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['assayType']}"/></span>Assay Type</th>
                                                            <td><c:choose><c:when test="${not empty ga.assayType}"><c:out value="${ga.assayType}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['forwardPrimer']}"/></span>Forward Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.forwardPrimer}"><c:out value="${ga.forwardPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['reversePrimer']}"/></span>Reverse Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.reversePrimer}"><c:out value="${ga.reversePrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedWtPcr']}"/></span>Expected WT PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedWtPcr}"><c:out value="${ga.expectedWtPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedMutPcr']}"/></span>Expected Mut PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedMutPcr}"><c:out value="${ga.expectedMutPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['restrictionEnzymeName']}"/></span>Restriction Enzyme Name</th>
                                                            <td><c:choose><c:when test="${not empty ga.restrictionEnzymeName}"><c:out value="${ga.restrictionEnzymeName}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['restrictionEnzymeCatalog']}"/></span>Restriction Enzyme Catalog</th>
                                                            <td><c:choose><c:when test="${not empty ga.restrictionEnzymeCatalog}"><c:out value="${ga.restrictionEnzymeCatalog}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['enzymeCleaves']}"/></span>Enzyme Cleaves</th>
                                                            <td><c:choose><c:when test="${not empty ga.enzymeCleaves}"><c:forEach items="${ga.enzymeCleaves}" var="ec" varStatus="ecloop"><c:if test="${!ecloop.first}">, </c:if><c:out value="${ec}"/></c:forEach></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedWtDigest']}"/></span>Expected WT Digest</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedWtDigest}"><c:out value="${ga.expectedWtDigest}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['expectedMutDigest']}"/></span>Expected Mut Digest</th>
                                                            <td><c:choose><c:when test="${not empty ga.expectedMutDigest}"><c:out value="${ga.expectedMutDigest}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sequencingPrimer']}"/></span>Sequencing Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.sequencingPrimer}"><c:out value="${ga.sequencingPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['dcapsMismatchPrimer']}"/></span>dCAPS Mismatch Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.dcapsMismatchPrimer}"><c:out value="${ga.dcapsMismatchPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['wtSpecificPrimer']}"/></span>WT Specific Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.wtSpecificPrimer}"><c:out value="${ga.wtSpecificPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['mutSpecificPrimer']}"/></span>Mut Specific Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.mutSpecificPrimer}"><c:out value="${ga.mutSpecificPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['commonPrimer']}"/></span>Common Primer</th>
                                                            <td><c:choose><c:when test="${not empty ga.commonPrimer}"><c:out value="${ga.commonPrimer}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['kaspGenomicSequence']}"/></span>KASP Genomic Sequence</th>
                                                            <td><c:choose><c:when test="${not empty ga.kaspGenomicSequence}"><c:out value="${ga.kaspGenomicSequence}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpMarkerName']}"/></span>SSLP Marker Name</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpMarkerName}"><c:out value="${ga.sslpMarkerName}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpDistance']}"/></span>SSLP Distance</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpDistance}"><c:out value="${ga.sslpDistance}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpGenomicLocation']}"/></span>SSLP Genomic Location</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpGenomicLocation}"><c:out value="${ga.sslpGenomicLocation}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpInducedBackground']}"/></span>SSLP Induced Background</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpInducedBackground}"><c:out value="${ga.sslpInducedBackground}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpOutcrossedBackground']}"/></span>SSLP Outcrossed Background</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpOutcrossedBackground}"><c:out value="${ga.sslpOutcrossedBackground}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpInducedPcr']}"/></span>SSLP Induced PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpInducedPcr}"><c:out value="${ga.sslpInducedPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['sslpOutcrossedPcr']}"/></span>SSLP Outcrossed PCR</th>
                                                            <td><c:choose><c:when test="${not empty ga.sslpOutcrossedPcr}"><c:out value="${ga.sslpOutcrossedPcr}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${gaFieldStatus['additionalInfo']}"/></span>Additional Info</th>
                                                            <td><c:choose><c:when test="${not empty ga.additionalInfo}"><c:out value="${ga.additionalInfo}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                    </tbody>
                                                </table>
                                            </z:section>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Phenotypes" prependedText="${phenoBadge}" cssClass="ml-4" sectionID="mutation-${loop.count}-phenotypes">
                                <c:choose>
                                    <c:when test="${empty m.phenotypes}">
                                        <p class="text-muted">No phenotypes recorded.</p>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${m.phenotypes}" var="ph" varStatus="phloop">
                                            <c:set var="phFieldStatus" value="${phenotypeFieldStatus[ph.id]}"/>
                                            <z:section title="Phenotype ${phloop.count}" cssClass="ml-4">
                                                <table class="table table-borderless w-auto">
                                                    <tbody>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['description']}"/></span>Description</th>
                                                            <td><c:choose><c:when test="${not empty ph.description}"><c:out value="${ph.description}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['hpfStart']}"/></span>HPF Start</th>
                                                            <td><c:choose><c:when test="${not empty ph.hpfStart}">${ph.hpfStart}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['hpfEnd']}"/></span>HPF End</th>
                                                            <td><c:choose><c:when test="${not empty ph.hpfEnd}">${ph.hpfEnd}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['stage']}"/></span>Stage</th>
                                                            <td><c:choose><c:when test="${not empty ph.stage}"><c:out value="${ph.stage}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['zfinImagePermission']}"/></span>ZFIN Image Permission</th>
                                                            <td><c:choose><c:when test="${ph.zfinImagePermission == true}">Yes</c:when><c:when test="${ph.zfinImagePermission == false}">No</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['zircImagePermission']}"/></span>ZIRC Image Permission</th>
                                                            <td><c:choose><c:when test="${ph.zircImagePermission == true}">Yes</c:when><c:when test="${ph.zircImagePermission == false}">No</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['nonMendelianPercentage']}"/></span>Non-Mendelian Percentage</th>
                                                            <td><c:choose><c:when test="${not empty ph.nonMendelianPercentage}">${ph.nonMendelianPercentage}</c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['nonMendelianComment']}"/></span>Non-Mendelian Comment</th>
                                                            <td><c:choose><c:when test="${not empty ph.nonMendelianComment}"><c:out value="${ph.nonMendelianComment}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['segregation']}"/></span>Segregation</th>
                                                            <td><c:choose><c:when test="${not empty ph.segregation}"><c:forEach items="${ph.segregation}" var="sg" varStatus="sgloop"><c:if test="${!sgloop.first}">, </c:if><c:out value="${sg}"/></c:forEach></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                        <tr><th><span class="status-slot"><z:zirc-status-badge status="${phFieldStatus['type']}"/></span>Type</th>
                                                            <td><c:choose><c:when test="${not empty ph.type}"><c:forEach items="${ph.type}" var="t" varStatus="tloop"><c:if test="${!tloop.first}">, </c:if><c:out value="${t}"/></c:forEach></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td></tr>
                                                    </tbody>
                                                </table>
                                            </z:section>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </z:section>

                            <z:section title="Lethality" prependedText="${lethalityBadge}" cssClass="ml-4" sectionID="mutation-${loop.count}-lethality">
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
                                                <z:zirc-field-history fieldName="homozygousLethal" label="Homozygous Lethal" updates="${mFieldUpdates['homozygousLethal']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Stage Typical</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalityStageTypical}"><c:out value="${m.lethalityStageTypical}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalityStageTypical" label="Lethality Stage Typical" updates="${mFieldUpdates['lethalityStageTypical']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Specific Timepoint</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalitySpecificTimepoint}"><c:out value="${m.lethalitySpecificTimepoint}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalitySpecificTimepoint" label="Lethality Specific Timepoint" updates="${mFieldUpdates['lethalitySpecificTimepoint']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Window</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalityWindowStart or not empty m.lethalityWindowEnd}"><c:out value="${m.lethalityWindowStart}"/> &ndash; <c:out value="${m.lethalityWindowEnd}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalityWindowStart" label="Lethality Window Start" updates="${mFieldUpdates['lethalityWindowStart']}" scope="${mScope}"/>
                                                <z:zirc-field-history fieldName="lethalityWindowEnd" label="Lethality Window End" updates="${mFieldUpdates['lethalityWindowEnd']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th><span class="status-slot"></span>Lethality Additional Info</th>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty m.lethalityAdditionalInfo}"><c:out value="${m.lethalityAdditionalInfo}"/></c:when>
                                                    <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                                </c:choose>
                                                <z:zirc-field-history fieldName="lethalityAdditionalInfo" label="Lethality Additional Info" updates="${mFieldUpdates['lethalityAdditionalInfo']}" scope="${mScope}"/>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </z:section>

                            <z:section title="Publications" prependedText="${pubsBadge}" cssClass="ml-4" sectionID="mutation-${loop.count}-publications">
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

        <c:set var="linkedBadge"><z:zirc-status-badge status="${sectionStatus['Linked Features']}"/></c:set>
        <z:section title="${LINKED_FEATURES}" prependedText="${linkedBadge}">
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
                                <tr>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.mutationA.alleleDesignation}"><c:out value="${lf.mutationA.alleleDesignation}"/></c:when>
                                            <c:otherwise>#${lf.mutationA.sortOrder}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.mutationB.alleleDesignation}"><c:out value="${lf.mutationB.alleleDesignation}"/></c:when>
                                            <c:otherwise>#${lf.mutationB.sortOrder}</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${lf.distanceKnown == true}">Yes</c:when>
                                            <c:when test="${lf.distanceKnown == false}">No</c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty lf.distanceCentimorgans}">${lf.distanceCentimorgans} cM</c:when>
                                            <c:when test="${not empty lf.distanceMegabases}">${lf.distanceMegabases} Mb</c:when>
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

        <c:set var="backgroundBadge"><z:zirc-status-badge status="${sectionStatus['Background']}"/></c:set>
        <z:section title="${BACKGROUND}" prependedText="${backgroundBadge}">
            <table class="table table-borderless w-auto">
                <tbody>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['maternalBackground']}"/></span>Maternal</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.maternalBackground}"><c:out value="${submission.maternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="maternalBackground" label="Maternal Background" updates="${fieldUpdates['maternalBackground']}"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['paternalBackground']}"/></span>Paternal</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.paternalBackground}"><c:out value="${submission.paternalBackground}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="paternalBackground" label="Paternal Background" updates="${fieldUpdates['paternalBackground']}"/>
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
                            <z:zirc-field-history fieldName="backgroundChangeable" label="Background Changeable" updates="${fieldUpdates['backgroundChangeable']}"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['backgroundChangeConcerns']}"/></span>Concerns</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.backgroundChangeConcerns}"><c:out value="${submission.backgroundChangeConcerns}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="backgroundChangeConcerns" label="Concerns" updates="${fieldUpdates['backgroundChangeConcerns']}"/>
                        </td>
                    </tr>
                </tbody>
            </table>
        </z:section>

        <c:set var="additionalBadge"><z:zirc-status-badge status="${sectionStatus['Additional Info']}"/></c:set>
        <z:section title="${ADDITIONAL}" prependedText="${additionalBadge}">
            <table class="table table-borderless w-auto">
                <tbody>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['additionalInfo']}"/></span>Additional Info</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.additionalInfo}"><c:out value="${submission.additionalInfo}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="additionalInfo" label="Additional Info" updates="${fieldUpdates['additionalInfo']}"/>
                        </td>
                    </tr>
                    <tr>
                        <th><span class="status-slot"><z:zirc-status-badge status="${fieldStatus['unreportedFeaturesDetails']}"/></span>Unreported Features Details</th>
                        <td>
                            <c:choose>
                                <c:when test="${not empty submission.unreportedFeaturesDetails}"><c:out value="${submission.unreportedFeaturesDetails}"/></c:when>
                                <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                            </c:choose>
                            <z:zirc-field-history fieldName="unreportedFeaturesDetails" label="Unreported Features Details" updates="${fieldUpdates['unreportedFeaturesDetails']}"/>
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
        });
        </script>

    </jsp:body>
</z:dataPage>
