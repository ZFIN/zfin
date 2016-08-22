<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@attribute name="criteria" type="org.zfin.search.presentation.MarkerSearchCriteria" required="true" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css">


<%--<form:form method="get" action="search-results" commandName="criteria" name="markersearchform">--%>




<div class="search-form-top-bar" style="margin: .25em">
    <div class="search-form-title" style="display: inline-block;">
        Search for Genes / Markers / Clones
    </div>

    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Genes / Markers / Clones Search"/>
        </tiles:insertTemplate>
    </div>
</div>


<form:form action="/action/marker/search-results" method="get" modelAttribute="criteria">


    <div class="row">
        <div class="col-sm-6 form-inline form-inline-input-group">
            <form:label path="matchType">Name / Symbol</form:label>&nbsp;
            <form:select path="matchType" cssClass="form-control">
                <form:option value="Matches">Matches</form:option>
                <form:option value="Contains">Contains</form:option>
                <form:option value="Begins With">Begins With</form:option>
            </form:select>&nbsp;
            <form:input type="text" path="name" cssClass="form-control"/>

        </div>
        <div class="col-sm-6 form-inline form-inline-input-group">
            <form:label path="accession">Accession</form:label>&nbsp;
            <form:input type="text" path="accession"
                   id="accession" cssClass="form-control"/>
        </div>
    </div>

<%--
    <div class="row">
        <div class="col-sm-6 form-inline form-inline-input-group">
            <div>
                <label for="type">Types (Choose one or more)</label>
            </div>

            <select name="type" id="type" multuple="true" size="19" class="form-control">
                <option>Gene</option>
                <option>EGFP</option>
            </select>

        </div>
        <div class="col-sm-6 form-inline form-inline-input-group">
            <label for="chromosome">Chromosome</label>
            <select name="chromosome" id="chromosome" class="form-control">
                <option>1</option>
                <option>2</option>
                <option>3</option>
            </select>
        </div>
    </div>
--%>

    <div class="search-form-bottom-bar" style="text-align:left; margin:.25em;">

        <form:hidden path="page"/>
        <form:hidden path="rows"/>


        <button type="submit" class="btn btn-default btn-zfin">Search</button>
        <a href="/action/marker/search" class="btn btn-default">Reset</a>
    </div>
</form:form>
<%--

</form:form>--%>
