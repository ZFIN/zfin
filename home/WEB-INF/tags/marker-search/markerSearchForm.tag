<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="criteria" type="org.zfin.search.presentation.MarkerSearchCriteria" required="true" %>


<div class="search-form-top-bar">
    <div class="search-form-title" style="display: inline-block;">
        Search for Genes / Markers / Clones
    </div>
</div>


<form:form action="/action/marker/search-results" method="get" modelAttribute="criteria">

    <div class="row">
        <div class="col-md-6 form-inline form-inline-input-group">
            <form:label path="matchType">Name / Symbol</form:label>&nbsp;
            <form:select path="matchType" cssClass="form-control">
                <form:option value="Matches">Matches</form:option>
                <form:option value="Contains">Contains</form:option>
                <form:option value="Begins With">Begins With</form:option>
            </form:select>&nbsp;
            <form:input type="text" path="name" cssClass="form-control"/>

        </div>
        <div class="col-md-6 form-inline form-inline-input-group">
            <form:label path="accession">Accession</form:label>&nbsp;
            <form:input type="text" path="accession"
                   id="accession" cssClass="form-control"/>
        </div>
    </div>

    <div class="row">
        <div class="col-md-6 form-inline form-inline-input-group">
            <label for="selected-type">Type</label>
            <form:select path="selectedType" id="selected-type"
                         cssClass="form-control">
                <form:option value="">Select type...</form:option>
                <form:options items="${criteria.typeOptions}"/>
            </form:select>

        </div>
        <div class="col-md-6 form-inline form-inline-input-group">
            <label for="chromosome">Chromosome</label>
            <form:select path="chromosome" id="chromosome"
                         cssClass="form-control">
                <form:option value="">Select chromosome....</form:option>
                <form:options items="${criteria.chromosomeOptions}"/>
            </form:select>

        </div>
    </div>

    <div class="search-form-bottom-bar" style="text-align:left; margin:.25em;">

        <form:hidden path="rows"/>


        <button type="submit" class="btn btn-primary">Search</button>
        <a href="/action/marker/search" class="btn btn-outline-secondary">Reset</a>
    </div>
</form:form>
<%--

</form:form>--%>
