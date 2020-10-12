<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<c:set var="NOMEN_COORDINATOR" value="${ZfinPropertiesEnum.NOMEN_COORDINATOR.value()}" />
<c:set var="ZFIN_ADMIN" value="${ZfinPropertiesEnum.ZFIN_ADMIN.value()}" />
<c:set var="WIKI_HOST" value="${ZfinPropertiesEnum.WIKI_HOST.value()}" />

<zfin2:nomenclatureForm headerText="Submit a Proposed Mutant/Transgenic Line Name">
    <jsp:attribute name="resourcesList">
        <li>
            Guides to Nomenclature for <a href="https://wiki.zfin.org/display/general/ZFIN+Zebrafish+Nomenclature+Conventions#ZFINZebrafishNomenclatureGuidelines-1.3">Mutants</a>
            and <a href="https://wiki.zfin.org/display/general/ZFIN+Zebrafish+Nomenclature+Conventions#ZFINZebrafishNomenclatureGuidelines-4.3">Transgenes</a>
        </li>
        <li><a href="/action/feature/line-designations">Laboratory Allele Designations</a></li>
        <li><a href="/search?q=&category=Fish">Search Existing Lines in ZFIN</a></li>
    </jsp:attribute>

    <jsp:attribute name="submissionForm">
        <h3>Line Information</h3>

        <p>
            Please provide names for each allele and/or transgenic insertion carried by your line. In addition to the genetic
            locus or construct name, a unique lab allele/line designation must be provided for each. Please also provide a
            description of the protocol used to generate each mutation or transgenic insertion, as well as the mutation type if
            known. For example use "ENU treated adult males" and "point", or "embryos treated with DNA" and "transgenic
            insertion". For help with naming conventions, contact the
            <a href="mailto:${NOMEN_COORDINATOR}">ZFIN Nomenclature Coordinator</a>. If you need
            a laboratory prefix set up for you, please contact us at <a href="mailto:${ZFIN_ADMIN}">${ZFIN_ADMIN}</a>.
        </p>

        <div id="line-details">
            <c:forEach var="details" items="${submission.lineDetails}" varStatus="status">
                <div class="line-form-row">
                    <div class="form-group row">
                        <label class="col-md-2 col-form-label">Genetic Background</label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].background" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group row">
                        <form:label path="${details.geneName}" class="col-md-2 col-form-label">Gene or Construct Name</form:label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].geneName" class="form-control"/>
                        </div>
                        <div class="input-addon col-md-1">
                            <a href="https://${WIKI_HOST}/display/general/ZFIN+Zebrafish+Nomenclature+Conventions#ZFINZebrafishNomenclatureGuidelines-4.3.1" target="_blank"><i class="fas fa-question-circle"></i></a>
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 col-form-label">Gene Symbol</label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].geneSymbol" class="form-control"/>
                        </div>
                        <div class="input-addon gene-search col-md-1">
                            <a href="#" target="_blank"><i class="fas fa-search"></i></a>
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 col-form-label">Allele/Line Designation</label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].designation" class="form-control"/>
                        </div>
                        <div class="input-addon col-md-1">
                            <a href="/action/feature/line-designations" target="_blank"><i class="fas fa-question-circle"></i></a>
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 col-form-label">Protocol</label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].protocol" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 col-form-label">Mutation Type</label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].mutationType" class="form-control"/>
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 col-form-label">Mutation Details</label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].mutationDetails" class="form-control"/>
                        </div>
                        <div class="input-addon col-md-1">
                            <a class="clickable" tabindex="-1" role="button" data-toggle="popover" data-trigger="focus" data-placement="right"
                               data-content="Please include any additional details about the mutation including an accession number of
                                    the WT gene that was used to compare the mutation or an accession number of the mutant sequence, genomic
                                    location (Chromosome, genome coordinates, cM) and molecular details about this allele (exon 1 deletion,
                                    insertion of GATG at 1402bp, A->G substitution a 372bp, premature stop codon at amino acid 79, etc.).">
                                <i class="fas fa-question-circle"></i>
                            </a>
                        </div>
                    </div>
                    <div class="form-group row">
                        <label class="col-md-2 col-form-label">CRISPR or TALEN Sequence</label>
                        <div class="col-md-6">
                            <form:input path="lineDetails[${status.index}].sequence" class="form-control"/>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </jsp:attribute>

    <jsp:attribute name="keepPrivateOption">
        <div class="form-group row keep-private-group">
            <label class="col-md-2 col-form-label required">Add lines and all data to database</label>
            <div class="col-md-6 radio">
                <label>
                    <form:radiobutton path="keepPrivate" value="immediately" /> immediately (will be publicly viewable immediately)
                </label>
                <label>
                    <form:radiobutton path="keepPrivate" value="after publication" /> after manuscript publication (only line designation/allele number and lab of origin will be immediately viewable; data will be added to record when publication is curated)
                </label>
            </div>
        </div>
    </jsp:attribute>
</zfin2:nomenclatureForm>

<script>
    $(function() {
        $('[data-toggle="popover"]').popover({container: 'body'});
        $("#line-details")
                .multirowTable(".line-form-row", "Add another line", function() {
                    this.find('[data-toggle="popover"]').popover({container: 'body'});
                })
                .on('click', '.gene-search a', function (evt) {
                    var gene = $(this).closest('.form-group').find(':input').val();
                    $(this).attr('href', '/search?q=' + gene + '&fq=category%3A"Gene+%2F+Transcript"&category=Gene+%2F+Transcript');
                });
    });
</script>
