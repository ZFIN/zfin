<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<z:page>
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">


    <jsp:useBean id="formBean" class="org.zfin.marker.presentation.RegionAddFormBean" scope="request"/>


    <div class="container-fluid">
        <h2 class="page-header">New NTR</h2>
        <form:form id="nonTranscribedRegion-add" cssClass="form-horizontal" commandName="formBean" action="nonTranscribedRegion-add">
            <div class="container-fluid">
                <div class="row">
                    <div class="col-6">
                        <div class="form-group row">
                            <form:label path="type" class="col-md-2 col-form-label">Type</form:label>
                            <div class="col-md-4">
                                <form:select path="type" class="form-control">
                                    <form:option value="" label="Select..." disabled="true" selected="true"/>
                                    <form:options items="${formBean.allTypes}"/>
                                </form:select>
                                <form:errors path="type" class="error"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-md-2 col-md-10">
                                <a href="http://www.sequenceontology.org/browser/current_svn/term/">
                                    Link to the SO (for reference)</a>
                            </div>
                        </div>
                        <div class="form-group row">
                            <form:label path="publicationId" class="col-md-2 col-form-label">Reference</form:label>
                            <div class="col-md-4">
                                <form:input path="publicationId" class="form-control" placeholder="ZDB-PUB-123456-7"/>
                                <form:errors path="publicationId" cssClass="error"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <form:label path="name" class="col-md-2 col-form-label">Name</form:label>
                            <div class="col-md-4">
                                <form:input path="name" class="form-control"/><form:errors path="name" class="error"/>
                            </div>
                        </div>
                        <div class="form-group row" id="abbrev-group">
                            <form:label path="abbreviation" class="col-md-2 col-form-label">Abbreviation</form:label>
                            <div class="col-md-6">
                                <form:input path="abbreviation" class="form-control"/>
                                <form:errors path="abbreviation" class="error"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <form:label path="alias" class="col-md-2 col-form-label">Alias</form:label>
                            <div class="col-md-6">
                                <form:input path="alias" class="form-control"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <form:label path="publicNote" class="col-md-2 col-form-label">Public Note</form:label>
                            <div class="col-md-10">
                                <form:textarea path="publicNote" class="form-control" rows="3"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <form:label path="curatorNote" class="col-md-2 col-form-label">Curator Note</form:label>
                            <div class="col-md-10">
                                <form:textarea path="curatorNote" class="form-control" rows="3"/>
                            </div>
                        </div>
                        <div class="form-group row">
                            <div class="offset-md-2 col-md-10">
                                <button type="submit" class="btn btn-primary">Submit</button>
                            </div>
                        </div>
                    </div>

                    <div class="col-6">
                        <div class="card">
                            <div id="term-info" class="card-body">
                                <h4 class="card-title">Term Info</h4>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form:form>
    </div>

    <script>

        $(document).ready(function () {
            $('#type').change(function () {

                var value = $('#type option:selected').val();
                oboID = value.split('|')[0];
                window.alert(oboID);
                jQuery("#term-info").load('/action/ontology/term-detail-popup?termID=' + oboID)
            });
        });

    </script>
</z:page>