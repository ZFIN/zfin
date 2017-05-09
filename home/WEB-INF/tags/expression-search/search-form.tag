<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="title" type="java.lang.String" required="true" %>
<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<style>
    /* overriding, see case 9050 */
    input#searchTerm.error { color: black; }
    .form-group {
        margin-bottom: 0.5em;
    }
</style>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>

<div class="titlebar">
    <h1>${title}</h1>
    <a href="/ZFIN/misc_html/xpatselect_search_tips.html" class="popup-link help-popup-link"
       id="xpatsel_expression_tips" rel="#searchtips"></a>
</div>

<form:form action="/action/expression/results" method="get" modelAttribute="criteria">
    <form:hidden path="rows"/>
    <table width="100%">
        <tr valign="top">
            <td width="50%">
                <table class="primary-entity-attributes">
                    <tr>
                        <th><form:label path="geneField" cssClass="namesearchLabel">Gene/EST</form:label></th>
                        <td><form:input type="text" path="geneField"/></td>
                    </tr>
                    <tr>
                        <th><form:label path="fish">Fish</form:label></th>
                        <td><form:input type="text" path="fish" /></td>
                    </tr>
                    <tr>
                        <th><form:label path="targetGeneField" cssClass="namesearchLabel">Target Gene</form:label></th>
                        <td><form:input type="text" path="targetGeneField"/></td>
                    </tr>
                </table>
                <table border="0" bgcolor="#EEEEEE">
                    <tr>
                        <td><form:label path="anatomy" cssClass="namesearchLabel">Anatomy Terms</form:label></td>
                    </tr>
                    <tr>
                        <td>
                            <form:hidden path="anatomyTermIDs"/>
                            <form:hidden path="anatomyTermNames"/>
                            <div id="searchTermList"></div>
                            <div id="anatomyTermInput"></div>
                        </td>
                    </tr>
                </table>
                <script type="text/javascript">
                    var LookupProperties = {
                        inputDiv:"anatomyTermInput",
                        termListDiv:"searchTermList",
                        inputName:"searchTerm",
                        showError:true,
                        <c:if test='${criteria.anatomyTermNames != null}' >
                        previousTableValues:"${criteria.anatomyTermNames}",
                        </c:if>
                        hiddenNames:"anatomyTermNames",
                        hiddenIds:"anatomyTermIDs",
                        type:"<%= LookupComposite.GDAG_TERM_LOOKUP %>",
                        ontologyName:"<%= Ontology.ANATOMY %>",
                        action:"<%= LookupComposite.ACTION_ANATOMY_SEARCH %>",
                        width:40,
                        wildcard:false,
                        termsWithDataOnly:true,
                        useTermTable:true
                    };
                    $('#anatomyTermInput').on('keyup keypress', function (e) {
                        if (e.which === 13) {
                            e.preventDefault();
                            return false;
                        }
                    });
                </script>
                <script language="javascript"
                        src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js" type=""></script>
            </td>
            <td width="50%">
                <div class="form-group">
                    <span class="namesearchLabel">Between stages</span>
                    <div>
                        <form:select path="startStageId" items="${stages}"/>
                        &nbsp;<span class="namesearchLabel">&</span>
                    </div>
                    <div>
                        <form:select path="endStageId" items="${stages}"/>
                    </div>
                    <div>
                        <a href="/zf_info/zfbook/stages/index.html">Developmental Staging Series</a>
                    </div>
                </div>
                <div class="form-group">
                    <form:label path="assayName" cssClass="namesearchLabel">Assay</form:label>
                    <form:select path="assayName">
                        <form:option value="">Any</form:option>
                        <form:options items="${assays}" itemLabel="name" itemValue="name" />
                    </form:select>
                </div>
                <div class="form-group">
                    <form:checkbox path="onlyFiguresWithImages"/> <label>Show only figures with images</label>
                </div>
            </td>
        </tr>
    </table>

    <div class="submitbar">
        <button type="submit">Search</button>
        <button type="reset">Reset</button>
    </div>
</form:form>

<script>

    function decorateTermList() {
        termCount = jQuery('#searchTermList .gwt-Hyperlink').size();

        if (termCount == 0) {
            jQuery('#searchTermList').hide();
            jQuery('#term-list-remove-all-link').hide();


        } else if (termCount == 1) {
            jQuery('#searchTermList').show();
            jQuery('#term-list-remove-all-link').hide();

        } else {
            jQuery('#searchTermList').show();
            jQuery('#term-list-remove-all-link').show();
        }


    }

    jQuery(document).ready(function() {

        jQuery('input[name=anatomyTermIDs]').change(function () {
            decorateTermList();
        });

    });

</script>
