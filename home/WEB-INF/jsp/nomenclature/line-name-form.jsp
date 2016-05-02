<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<h3>Line Information</h3>

<p>
    Please provide names for each allele and/or transgenic insertion carried by your line. In addition to the genetic
    locus or construct name, a unique lab allele/line designation must be provided for each. Please also provide a
    description of the protocol used to generate each mutation or transgenic insertion, as well as the mutation type if
    known. For example use "ENU treated adult males" and "point", or "embryos treated with DNA" and "transgenic
    insertion". For help with naming conventions, contact the
    <a href="mailto:<%= ZfinPropertiesEnum.NOMEN_COORDINATOR.value()%>">ZFIN Nomenclature Coordinator</a>. If you need
    a laboratory prefix set up for you, please contact us at <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a>.
</p>

<div id="line-details">
    <c:forEach var="details" items="${submission.lineDetails}" varStatus="status">
        <div class="line-form-row">
            <div class="form-group">
                <label class="col-sm-2 control-label">Genetic Background</label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].background" class="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <form:label path="${details.geneName}" class="col-sm-2 control-label">Gene or Construct Name</form:label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].geneName" class="form-control"/>
                </div>
                <div class="input-addon col-sm-1">
                    <a href="https://<%= ZfinPropertiesEnum.WIKI_HOST.value() %>/display/general/ZFIN+Zebrafish+Nomenclature+Guidelines#ZFINZebrafishNomenclatureGuidelines-4.3.1" target="_blank"><i class="fa fa-question-circle"></i></a>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">Gene Symbol</label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].geneSymbol" class="form-control"/>
                </div>
                <div class="input-addon gene-search col-sm-1">
                    <a href="#" target="_blank"><i class="fa fa-search"></i></a>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">Allele/Line Designation</label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].designation" class="form-control"/>
                </div>
                <div class="input-addon col-sm-1">
                    <a href="/action/feature/line-designations" target="_blank"><i class="fa fa-question-circle"></i></a>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">Protocol</label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].protocol" class="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">Mutation Type</label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].mutationType" class="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">Mutation Details</label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].mutationDetails" class="form-control"/>
                </div>
                <div class="input-addon col-sm-1">
                    <a class="clickable" tabindex="-1" role="button" data-toggle="popover" data-trigger="focus" data-placement="right"
                            data-content="Please include any additional details about the mutation including an accession number of
                            the WT gene that was used to compare the mutation or an accession number of the mutant sequence, genomic
                            location (Chromosome, genome coordinates, cM) and molecular details about this allele (exon 1 deletion,
                            insertion of GATG at 1402bp, A->G substitution a 372bp, premature stop codon at amino acid 79, etc.).">
                        <i class="fa fa-question-circle"></i>
                    </a>
                </div>
            </div>
            <div class="form-group">
                <label class="col-sm-2 control-label">CRISPR or TALEN Sequence</label>
                <div class="col-sm-6">
                    <form:input path="lineDetails[${status.index}].sequence" class="form-control"/>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

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
