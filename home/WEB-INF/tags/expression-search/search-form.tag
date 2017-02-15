<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="title" type="java.lang.String" required="true" %>
<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<div class="titlebar">
    <h1>${title}</h1>
    <a href="/ZFIN/misc_html/xpatselect_search_tips.html" class="popup-link help-popup-link"
       id="xpatsel_expression_tips" rel="#searchtips"></a>
</div>

<form:form action="/action/expression/results" method="get" modelAttribute="criteria">
    <form:hidden path="rows"/>
    <table class="primary-entity-attributes">
        <tr>
            <th><form:label path="geneField" cssClass="namesearchLabel">Gene/EST</form:label></th>
            <td><form:input type="text" path="geneField"/></td>
            <th></th>
            <td><form:checkbox path="onlyFiguresWithImages"/> <label>Show only figures with images</label></td>
        </tr>
        <tr>
            <th><form:label path="anatomy">Anatomy:</form:label></th>
            <td>
                <style>
                    /* overriding, see case 9050 */
                    input#searchTerm.error { color: black; }
                </style>
                <form:hidden path="anatomyTermIDs"/>
                <form:hidden path="anatomyTermNames"/>
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
                        width:30,
                        wildcard:false,
                        termsWithDataOnly:true,
                        useTermTable:true
                    }
                </script>

                <link rel="stylesheet" type="text/css" href="/css/Lookup.css"/>
                <script language="javascript"
                        src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js" type=""></script>
                <div id="anatomyTermInput"></div>
                <div id="searchTermList"></div>
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
