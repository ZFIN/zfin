<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css">



<div class="search-form-top-bar">
    <div class="search-form-title" style="display: inline-block;">
        Search for Gene Expression Data
<%-- todo: needs a bootstrap popup --%>
<%--        <a href="/ZFIN/misc_html/xpatselect_search_tips.html" class="popup-link help-popup-link"
           id="xpatsel_expression_tips" rel="#searchtips"></a>--%>
    </div>

    <div class="search-form-your-input-welcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Expression Search"/>
        </tiles:insertTemplate>
    </div>
</div>


<form:form action="/action/expression/results" method="get" modelAttribute="criteria">

    <div class="row">
        <div class="col-sm-6 form-inline form-inline-input-group">
            <form:label path="geneField">Gene/EST:</form:label>&nbsp;
            <form:input type="text" path="geneField" cssClass="form-control"/>

        </div>
        <div class="col-sm-6 form-inline form-inline-input-group">
[Stage Range]
<%--            <form:label path="accession">Stage Range</form:label>&nbsp;
            <form:input type="text" path="accession"
                        id="accession" cssClass="form-control"/>--%>
        </div>
    </div>

<%--
    <div class="row">
        <div class="col-sm-6 form-inline form-inline-input-group">
            <label for="selected-type">Type</label>
            <form:select path="selectedType" id="selected-type"
                         cssClass="form-control">
                <form:option value="">Select type...</form:option>
                <form:options items="${criteria.typeOptions}"/>
            </form:select>

        </div>
        <div class="col-sm-6 form-inline form-inline-input-group">
            <label for="chromosome">Chromosome</label>
            <form:select path="chromosome" id="chromosome"
                         cssClass="form-control">
                <form:option value="">Select chromosome....</form:option>
                <form:options items="${criteria.chromosomeOptions}"/>
            </form:select>

        </div>
    </div>
--%>

    <div class="search-form-bottom-bar" style="text-align:left; margin:.25em;">

        <form:hidden path="rows"/>

        <button type="submit" class="btn btn-default btn-zfin">Search</button>
        <a href="/action/expression/search" class="btn btn-default">Reset</a>
    </div>
</form:form>