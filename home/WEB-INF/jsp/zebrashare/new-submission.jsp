<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="/javascript/dist/bootstrap.bundle.css">

<div class="container-fluid">
    <h1>Submit ZebraShare Workbook</h1>
    <p>
        ZFIN curators will process this submission as part of their high priority literature curation workflow. You may be
        contacted if the curators have questions during the processing of your submission. Once complete, an allele record
        will be created for each allele submitted. The line submitter and others specified will then be able to edit
        some of the details associated with their alleles.
    </p>
    <zfin2:zebrashareForm formBean="${formBean}"/>
</div>
