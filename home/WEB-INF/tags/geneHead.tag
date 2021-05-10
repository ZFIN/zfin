<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="false" %>
<%@ attribute name="soTerm" type="org.zfin.ontology.GenericTerm" rtexprvalue="true" required="false" %>
<%@ attribute name="userID" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="geneDesc" type="org.zfin.marker.AllianceGeneDesc" rtexprvalue="true" required="false" %>

<c:set var="loggedIn" value="false"/>

<authz:authorize access="hasRole('root')">
    <c:set var="loggedIn" value="true"/>
    <script>
        markerID = '${gene.zdbID}';

        var reasonList = [];
        <c:forEach items="${markerHistoryReasonCodes}" var="reason" varStatus="status">
        reasonList.push('${reason.toString()}');
        </c:forEach>
    </script>
    <div ng-controller="NomenclatureController as control" ng-init="init('${gene.name}','${gene.abbreviation}')">
</authz:authorize>
<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">${gene.markerType.displayName} Name:</span></th>
        <td>
            <span class="name-value"><zfin:name entity="${gene}"/></span>
            <authz:authorize access="hasRole('root')">
                <span style="cursor: pointer;"
                      ng-click="control.openGeneEditor(markerID, control.geneName, 'Gene Name')"
                      ng-if="editMode">
                    <i class="far fa-edit" aria-hidden="true" style="color: red" title="Edit gene name"></i>
                </span>
            </authz:authorize>
        </td>
    </tr>
    <tr>
        <th><span class="name-label">${gene.markerType.displayName} Symbol:</span></th>
        <td>
            <span class="name-value" geneSymbol>
                <zfin:abbrev entity="${gene}"/>
            </span>
            <authz:authorize access="hasRole('root')">
                    <span style="cursor: pointer;"
                          ng-click="control.openGeneEditor(markerID, control.geneAbbreviation,'Gene Symbol')"
                          ng-if="editMode">
                    <i class="far fa-edit" aria-hidden="true" style="color: red" title="Edit gene symbol"></i></span>
            </authz:authorize>

        &nbsp;&nbsp;
            <a  class='data-note' href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature History</a>
        </td>


        <td>
            <zfin2:nomenclature geneEdit="true" showReason="${gene.type.geneOrGenep}"/>
        </td>
        <zfin2:previousNamesFast label="Previous Name" previousNames="${previousNames}" marker="${gene}"
                                 showEditControls="true"/>

    </tr>


    <tr>
        <th>${gene.markerType.displayName} Type:</th>
        <td>
            <a href="http://www.sequenceontology.org/browser/current_svn/term/${soTerm.oboID}"> ${soTerm.termName}</a>
        </td>
    </tr>


    <tr>
        <th>Location:</th>
        <td>
            <zfin2:displayLocation entity="${gene}" longDetail="true"/>
        </td>
    </tr>
    <tr>
        <td>



<tr>
    <th>Description: <a class='popup-link info-popup-link' href='/action/marker/note/automated-gene-desc'></a></th>


<td>

    ${geneDesc.gdDesc}
</td>






        <tr>

            <td>

    <zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>

            </td>
</tr>

<c:if test="${loggedIn}">
    <authz:authorize access="hasRole('root')">
        <c:set var="loggedIn" value="true"/>

        <tr curator-notes marker-id="${gene.zdbID}" edit="editMode" curator="${userID}">
        </tr>

        <tr public-note marker-id="${gene.zdbID}" edit="editMode">
        </tr>
    </authz:authorize>
</c:if>

<c:if test="${!loggedIn}">
    <zfin2:entityNotes entity="${gene}"/>
</c:if>
</table>



