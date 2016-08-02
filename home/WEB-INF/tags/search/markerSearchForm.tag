<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>

<%@attribute name="criteria" type="org.zfin.search.presentation.MarkerSearchCriteria" required="true" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css">


<%--<form:form method="get" action="search-results" commandName="criteria" name="markersearchform">--%>




<div class="search-form-top-bar">
    <div class="search-form-title" style="display: inline-block;">
        Search for Genes / Markers / Clones
    </div>

    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Genes / Markers / Clones Search"/>
        </tiles:insertTemplate>
    </div>
</div>

<form action="/action/marker/search-results" method="get">

    <div class="row">
        <div class="col-sm-6 form-inline">
            <label for="searchtype">Name / Symbol</label>
            <select name="searchtype" id="searchtype" class="form-control">
                <option>Contains</option>
                <option>Begins With</option>
            </select>
            <input type="text" name="name" class="form-control" value="${criteria.name}"/>

        </div>
        <div class="col-sm-6 form-inline">
            <label for="accession">Accession</label>
            <input type="text" name="accession"
                   id="accession" class="form-control"
                   value="${criteria.accession}"/>
        </div>
    </div>

<%--
    <div class="row">
        <div class="col-sm-6 form-inline">
            <div>
                <label for="type">Types (Choose one or more)</label>
            </div>

            <select name="type" id="type" multuple="true" size="19" class="form-control">
                <option>Gene</option>
                <option>EGFP</option>
            </select>

        </div>
        <div class="col-sm-6 form-inline">
            <label for="chromosome">Chromosome</label>
            <select name="chromosome" id="chromosome" class="form-control">
                <option>1</option>
                <option>2</option>
                <option>3</option>
            </select>
        </div>
    </div>
--%>

    <div class="search-form-bottom-bar" style="text-align:left;">

        <input type="hidden" name="page" value="${criteria.page}"/>
        <input type="hidden" name="rows" value="${criteria.rows}"/>

        <button type="submit" class="btn btn-zfin">Search</button>
        <a href="/action/marker/search" class="btn btn-default">Reset</a>
    </div>
</form>
<%--

</form:form>--%>
