<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>

<script src="/javascript/angular/angular.min.js"></script>
<script>
    ;(function() {
        angular.module('app', []);
    }());
</script>
<script src="/javascript/str-sequence.directive.js"></script>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentAddBean" scope="request"/>

<div class="container-fluid">
    <h2 class="page-header">New Sequence Targeting Reagent</h2>

    <form:form id="str-form"
               class="form-horizontal"
               action="sequence-targeting-reagent-add"
               commandName="formBean"
               ng-app="app"
               method="post">
        <div class="form-group">
            <form:label path="strType" class="col-sm-2 control-label">Type</form:label>
            <div class="col-sm-4">
                <form:select path="strType" class="form-control">
                    <form:option value="" label="Select..." disabled="true" selected="true"/>
                    <form:options items="${formBean.strTypes}"/>
                </form:select>
                <form:errors path="strType" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="publicationID" class="col-sm-2 control-label">Reference</form:label>
            <div class="col-sm-4">
                <form:input path="publicationID" class="form-control" placeholder="ZDB-PUB-123456-7"/>
                <form:errors path="publicationID" cssClass="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="name" class="col-sm-2 control-label">Name</form:label>
            <div class="col-sm-4">
                <form:input path="name" class="form-control"/>
                <form:errors path="name" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="alias" class="col-sm-2 control-label">Alias</form:label>
            <div class="col-sm-4">
                <form:input path="alias" class="form-control"/>
                <form:errors path="alias" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="targetGeneSymbol" class="col-sm-2 control-label">Target Gene</form:label>
            <div class="col-sm-4">
                <form:input path="targetGeneSymbol" class="form-control"/>
                <form:errors path="targetGeneSymbol" cssClass="error"/>
            </div>
        </div>
        <div class="form-group">
            <label id="sequence-label" class="col-sm-2 control-label">Sequence</label>
            <div class="col-sm-6">
                <div str-sequence
                     reported-sequence-name="reportedSequence"
                     displayed-sequence-name="sequence"
                     reversed-name="reversed"
                     complemented-name="complemented">
                </div>
                <form:errors path="sequence" cssClass="error"/>
            </div>
        </div>
        <div id="sequence2-group" class="form-group">
            <label class="col-sm-2 control-label">Target Sequence 2</label>
            <div class="col-sm-6">
                <div str-sequence
                     reported-sequence-name="reportedSequence2"
                     displayed-sequence-name="sequence2"
                     reversed-name="reversed2"
                     complemented-name="complemented2">
                </div>
                <form:errors path="sequence2" cssClass="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="publicNote" class="col-sm-2 control-label">Public Note</form:label>
            <div class="col-sm-6">
                <form:textarea path="publicNote" class="form-control" rows="3"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="curatorNote" class="col-sm-2 control-label">Curator Note</form:label>
            <div class="col-sm-6">
                <form:textarea path="curatorNote" class="form-control" rows="3"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
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
        $('#targetGeneSymbol').autocompletify('/action/quicksearch/autocomplete?q=%QUERY&category=Gene+%2F+Transcript&type=Gene');

        displaySequenceControls();
    }());
</script>


