<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<z:page>
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

    <script src="${zfn:getAssetPath("angular.js")}"></script>

    <jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentAddBean" scope="request"/>

    <div class="container-fluid">
        <h2 class="page-header">New Sequence Targeting Reagent</h2>

        <form:form id="str-form"
                   cssClass="form-horizontal"
                   action="sequence-targeting-reagent-add"
                   commandName="formBean"
                   ng-app="app"
                   method="post">
            <div class="form-group row">
                <form:label path="strType" class="col-md-2 col-form-label">Type</form:label>
                <div class="col-md-4">
                    <form:select path="strType" class="form-control">
                        <form:option value="" label="Select..." disabled="true" selected="true"/>
                        <form:options items="${formBean.strTypes}"/>
                    </form:select>
                    <form:errors path="strType" class="error"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="publicationID" class="col-md-2 col-form-label">Reference</form:label>
                <div class="col-md-4">
                    <form:input path="publicationID" class="form-control" placeholder="ZDB-PUB-123456-7"/>
                    <form:errors path="publicationID" cssClass="error"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="name" class="col-md-2 col-form-label">Name</form:label>
                <div class="col-md-4">
                    <form:input path="name" class="form-control"/>
                    <form:errors path="name" class="error"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="alias" class="col-md-2 col-form-label">Alias</form:label>
                <div class="col-md-4">
                    <form:input path="alias" class="form-control"/>
                    <form:errors path="alias" class="error"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="targetGeneSymbol" class="col-md-2 col-form-label">Target Gene</form:label>
                <div class="col-md-4">
                    <div class="scrollable-dropdown-menu">
                        <form:input path="targetGeneSymbol" class="form-control"/>
                    </div>
                    <form:errors path="targetGeneSymbol" cssClass="error"/>
                </div>
            </div>
            <div class="form-group row">
                <label id="sequence-label" class="col-md-2 col-form-label">Sequence</label>
                <div class="col-md-6">
                    <div str-sequence
                         type="${formBean.strType}"
                         sequence-text="${formBean.sequence}"
                         reported-sequence-name="reportedSequence"
                         displayed-sequence-name="sequence"
                         reversed-name="reversed"
                         complemented-name="complemented">
                    </div>
                    <form:errors path="sequence" cssClass="error"/>
                </div>
            </div>
            <div id="sequence2-group" class="form-group row">
                <label class="col-md-2 col-form-label">Target Sequence 2</label>
                <div class="col-md-6">
                    <div str-sequence
                         type="${formBean.strType}"
                         sequence-text="${formBean.sequence2}"
                         reported-sequence-name="reportedSequence2"
                         displayed-sequence-name="sequence2"
                         reversed-name="reversed2"
                         complemented-name="complemented2">
                    </div>
                    <form:errors path="sequence2" cssClass="error"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="publicNote" class="col-md-2 col-form-label">Public Note</form:label>
                <div class="col-md-6">
                    <form:textarea path="publicNote" class="form-control" rows="3"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="curatorNote" class="col-md-2 col-form-label">Curator Note</form:label>
                <div class="col-md-6">
                    <form:textarea path="curatorNote" class="form-control" rows="3"/>
                </div>
            </div>
            <div class="form-group row">
                <div class="offset-md-2 col-md-10">
                    <button type="submit" class="btn btn-primary">Submit</button>
                </div>
            </div>
        </form:form>
    </div>

    <script type="text/javascript">
        ;(function () {
            function displaySequenceControls() {
                var $sequenceLabel = $('#sequence-label');
                var $sequence2Group = $('#sequence2-group');
                var strType = $('#strType').val();
                if (strType === 'CRISPR') {
                    $sequenceLabel.text('Target Sequence');
                    $sequence2Group.hide();
                } else if (strType === 'TALEN') {
                    $sequenceLabel.text('Target Sequence 1');
                    $sequence2Group.show();
                } else {
                    $sequenceLabel.text('Sequence');
                    $sequence2Group.hide();
                }
            }

            $('#str-form').find('input').keypress(function(event) {
                if (event.which == 13) {
                    event.preventDefault();
                    return false;
                }
            });

            $('#strType').on('change', displaySequenceControls);
            $('#supplier').autocompletify('/action/marker/find-suppliers?term=%QUERY');
            //$('#targetGeneSymbol').autocompletify('/action/quicksearch/autocomplete?q=%QUERY&category=Gene+%2F+Transcript&fq=type%3A("Gene"+OR+"Pseudogene"+OR+"miRNA+Gene"+OR+"tRNA+Gene"+OR+"snoRNA+Gene"+OR+"rRNA+Gene"+OR+"lncRNA+Gene"+OR+"lincRNA+Gene"+OR+"piRNA+Gene"+OR+"scRNA+Gene")');
            $('#targetGeneSymbol').autocompletify('/action/marker/find-targetGenes?term=%QUERY',{limit: 1000});


            displaySequenceControls();
        }());
    </script>
</z:page>

