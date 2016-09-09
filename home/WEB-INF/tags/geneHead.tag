<%@ taglib prefix="zfin" uri="/WEB-INF/tld/zfin-tags.tld" %>
<%@ taglib prefix="zfin2" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="false" %>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>

<script>
    var reasonList = [];
    <c:forEach items="${gene.markerHistory.iterator().next().reasonArray}" var="reason" varStatus="status">
    reasonList.push('${reason.toString()}');
    </c:forEach>
</script>

<div ng-app="nomenclature">
    <div ng-controller="NomenclatureController as control">
        <table class="primary-entity-attributes">
            <tr>
                <th><span class="name-label">${gene.markerType.displayName} Name:</span></th>
                <td>
                    <span class="name-value"><zfin:name entity="${gene}"/></span>
                <span style="cursor: pointer;"
                      ng-click="control.openGeneEditor('${gene.zdbID}','${gene.name}', 'Gene Name')">
                    <i class="fa fa-pencil-square-o" aria-hidden="true" style="color: red"></i>
                </span>
                </td>
            </tr>
            <tr>
                <th><span class="name-label">${gene.markerType.displayName} Symbol:</span></th>
                <td>
                    <span class="name-value"><zfin:abbrev entity="${gene}"/></span>
                    <span style="cursor: pointer;"
                          ng-click="control.openGeneEditor('${gene.zdbID}','${gene.abbreviation}', 'Gene Symbol')">
                    <i class="fa fa-pencil-square-o" aria-hidden="true" style="color: red"></i></span>
                </td>
            </tr>

            <c:if test="${!empty previousNames}">
                <zfin2:previousNamesFast label="Previous Name" previousNames="${previousNames}"/>
            </c:if>
            <tr>
                <th>Location:</th>
                <td>
                    <zfin2:displayLocation entity="${gene}" longDetail="true"/>
                </td>
            </tr>
            <c:if test="${formBean.hasMarkerHistory}">
                <tr>
                    <td colspan="2">
                        <a class="data-note" href="/action/nomenclature/history/${formBean.marker.zdbID}">Nomenclature
                            History</a>
                    </td>
                </tr>
            </c:if>

            <zfin2:entityNotes entity="${gene}"/>

        </table>

        <zfin2:nomenclature/>
    </div>
</div>





