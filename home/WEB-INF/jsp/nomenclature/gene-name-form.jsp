<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<c:set var="NOMEN_COORDINATOR" value="${ZfinPropertiesEnum.NOMEN_COORDINATOR.value()}" />

<z:page bootstrap="true">
    <zfin2:nomenclatureForm headerText="Submit a Proposed Gene Name">
        <jsp:attribute name="resourcesList">
            <li>
                <a href="https://wiki.zfin.org/display/general/ZFIN+Zebrafish+Nomenclature+Conventions#ZFINZebrafishNomenclatureGuidelines-1">
                    Guide to Nomenclature for Genes
                </a>
            </li>
            <li><a href="https://wiki.zfin.org/display/general/Nomenclature+Tutorial">Tutorial for Proposing Zebrafish Gene Nomenclature</a></li>
            <li><a href="/search?category=Gene+%2F+Transcript&q=">Search Existing Genes in ZFIN</a></li>
        </jsp:attribute>

        <jsp:attribute name="submissionForm">
            <h3>Proposed Nomenclature</h3>

            <div class="form-group row">
                <form:label path="geneSymbol" cssClass="col-md-2 col-form-label required">Gene Symbol</form:label>
                <div class="col-md-4">
                    <form:input path="geneSymbol" cssClass="form-control" placeholder="e.g. ndr2" />
                </div>
            </div>

            <div class="form-group row">
                <form:label path="geneName" cssClass="col-md-2 col-form-label required">Gene Name</form:label>
                <div class="col-md-4">
                    <form:input path="geneName" cssClass="form-control" placeholder="e.g. nodal-related 2" />
                </div>
            </div>

            <div class="form-group row">
                <form:label path="otherNames" cssClass="col-md-2 col-form-label">Other Names</form:label>
                <div class="col-md-4">
                    <form:input path="otherNames" cssClass="form-control" />
                </div>
            </div>

            <h3>Supporting Information</h3>

            <p>
                An appropriate gene name and symbol may depend on various pieces of evidence, such as orthology to a mammalian
                gene that has an established name, or whether it appears to be a duplicate resulting from whole-genome
                duplication, based on high sequence identity and conserved synteny.
            </p>
            <p>
                To expedite the review process for your proposed name, please provide the following information, if possible.
                If it is more convenient to provide data in a separate file, please
                <a href="mailto:${NOMEN_COORDINATOR}">email it directly to us</a>.
            </p>
            <p>
                <em>Sequence data will be treated in complete confidence.</em>
            </p>

            <div class="form-group row">
                <form:label path="genBankID" cssClass="col-md-2 col-form-label">GenBank ID</form:label>
                <div class="col-md-4">
                    <form:input path="genBankID" cssClass="form-control" />
                </div>
            </div>

            <div class="form-group row">
                <form:label path="sequence" cssClass="col-md-2 col-form-label">Sequence</form:label>
                <div class="col-md-6">
                    <form:textarea path="sequence" rows="5" cssClass="form-control" />
                </div>
            </div>

            <div class="form-group row">
                <form:label path="chromosome" cssClass="col-md-2 col-form-label">Chromosome</form:label>
                <div class="col-md-4">
                    <form:input path="chromosome" cssClass="form-control" />
                </div>
            </div>

            <h4>Homology</h4>

            <div id="homologs">
                <c:forEach var="homolog" items="${submission.homologyInfoList}" varStatus="status">
                    <div class="line-form-row">
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label">Species</label>
                            <div class="col-md-4">
                                <form:input path="homologyInfoList[${status.index}].species" class="form-control" />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label">Gene Symbol</label>
                            <div class="col-md-4">
                                <form:input path="homologyInfoList[${status.index}].geneSymbol" class="form-control" />
                            </div>
                        </div>
                        <div class="form-group row">
                            <label class="col-md-2 col-form-label">Database ID</label>
                            <div class="col-md-4">
                                <form:input path="homologyInfoList[${status.index}].databaseID" class="form-control" />
                                <span class="form-text text-muted">
                                An identifier from <a href="http://www.ncbi.nlm.nih.gov/Genbank/index.html" target="_blank">GenBank</a>,
                                <a href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=OMIM" target="_blank">OMIM</a>,
                                <a href="http://www.informatics.jax.org/" target="_blank">Mouse Genome Informatics</a>, etc.
                            </span>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </jsp:attribute>
    </zfin2:nomenclatureForm>


    <script>
      $(function () {
        $("#homologs").multirowTable(".line-form-row", "Add another homolog");
      });
    </script>
</z:page>