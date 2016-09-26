<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="marker" type="org.zfin.marker.Marker" scope="request"/>

<zfin2:dataManager zdbID="${marker.zdbID}"
                   deleteURL="none"
                   rtype="marker"
                   showLastUpdate="true"
                   editURL="javascript:editNomenclature();"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${marker.abbreviation} "/>
    </tiles:insertTemplate>
</div>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>

<script>
    var reasonList = new Array();
    <c:forEach items="${marker.markerHistory.iterator().next().reasonArray}" var="reason" varStatus="status">
    reasonList.push('${reason.toString()}');
    </c:forEach>
</script>

<div ng-app="nomenclature">
    <div ng-controller="NomenclatureController as control">
        <zfin2:subsection title="Nomenclature History"
                          test="${!empty marker.markerHistory }"
                          showNoData="true">
            <authz:authorize access="hasRole('root')">
                <span id="showAllEventsToggle"><a href="javascript:showAll(true)">Show All Events</a></span>
        <span id="showReducedEventsToggle" style="display: none"><a href="javascript:showAll(false)">Hide Naming
            Events</a></span>
            </authz:authorize>
            <table class="summary sortable">
                <th id="edit_" style="display: none">Edit</th>
                <th>New Value</th>
                <th>Event</th>
                <th>Old Value</th>
                <th>Date</th>
                <th>Reason</th>
                <th>Comments</th>
                <c:forEach var="markerHistory" items="${marker.markerHistory}" varStatus="loop">
                    <c:if test="${markerHistory.event.toString() ne 'renamed'}">
                        <tr id="reduced_${loop.index}">
                            <td id="edit_${loop.index}" style="display: none">
                                <span ng-click="control.openEditor('${markerHistory.zdbID}','${markerHistory.comments}','${markerHistory.reason.toString()}')"><a
                                        href>Edit</a></span>
                            </td>
                            <td><span class="genedom">${marker.abbreviation}</span></td>
                            <td>${markerHistory.event.display}</td>
                            <td><span class="genedom">${markerHistory.oldSymbol}</span></td>
                            <td><fmt:formatDate value="${markerHistory.date}" pattern="yyyy-MM-dd"/></td>
                            <td>${markerHistory.reason.toString()}
                                <c:if test="${!empty markerHistory.attributions }">
                                    <c:choose>
                                        <c:when test="${markerHistory.attributions.size() ==1 }">
                                            (<a href="/${markerHistory.attributions.iterator().next().publication.zdbID}">1</a>)
                                        </c:when>
                                        <c:otherwise>
                                            (<a href='/action/publication/list/${markerHistory.zdbID}'>${markerHistory.attributions.size()}</a>)
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </td>
                            <td>${markerHistory.comments}</td>
                        </tr>
                    </c:if>
                </c:forEach>
                <c:forEach var="markerHistory" items="${marker.markerHistory}" varStatus="loop">
                    <tr style="display: none" id="all_${loop.index}">
                        <td id="edit_${loop.index}" style="display: none">
                                <span ng-click="control.openEditor('${markerHistory.zdbID}','${markerHistory.comments}','${markerHistory.reason.toString()}')"><a
                                        href>Edit</a></span>
                        </td>
                        <td><span class="genedom">${markerHistory.newValue}</span></td>
                        <td>${markerHistory.event.display}</td>
                        <td><span class="genedom"> ${markerHistory.oldSymbol}</span></td>
                        <td><fmt:formatDate value="${markerHistory.date}" pattern="yyyy-MM-dd"/></td>
                        <td>${markerHistory.reason.toString()}
                            <c:if test="${!empty markerHistory.attributions }">
                                <c:choose>
                                    <c:when test="${markerHistory.attributions.size() ==1 }">
                                        (<a href="/${markerHistory.attributions.iterator().next().publication.zdbID}">1</a>)
                                    </c:when>
                                    <c:otherwise>
                                        (<a href='/action/publication/list/${markerHistory.zdbID}'>${markerHistory.attributions.size()}</a>)
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </td>
                        <td>${markerHistory.comments}</td>
                    </tr>
                </c:forEach>
            </table>
        </zfin2:subsection>

        <div id="evidence-modal" class="jq-modal curation">
            <table>
                <tr>
                    <td colspan="2">
                        <h3> Nomenclature Edit </h3>
                    </td>
                    <td><span ng-click="control.reload()" style="cursor: pointer;">X</span></td>
                </tr>
                <tr>
                    <td>ID:</td>
                    <td>{{control.nomenID}}</td>
                </tr>
                <tr>
                    <td>Reason:</td>
                    <td>
                        <select ng-model="control.reason"
                                ng-options="reas for reas in reasonList"></select>
                    </td>
                </tr>
                <tr>
                    <td>Comments:</td>
                    <td><textarea cols="40" ng-model="control.comments">{{control.comments}}</textarea></td>
                </tr>
                <tr>
                    <td>
                        <button ng-click="control.updateNomenclature()">Update</button>
                    </td>
                    <td></td>
                </tr>
            </table>
            <h4>
                Attributions
            </h4>
            <table>
                <tr ng-repeat="pub in control.publicationDtoList">
                    <td>{{pub.zdbID}}</td>
                    <td><a href><img src="/images/delete-button.png" ng-click="control.deleteAttribution(pub.zdbID)"/>
                    </a></td>
                </tr>
            </table>
            <input size="20" name="publicationID" ng-model="control.publicationID"/>
            <button ng-click="control.addAttribution()">Add</button>
        </div>

        <script>
            function openEditNomenclature(nomenID) {
                alert("ID: " + nomenID);
                $('#evidence-modal')
                        .modal({
                            escapeClose: false,
                            clickClose: false,
                            showClose: false,
                            fadeDuration: 100
                        })
                        .on($.modal.AFTER_CLOSE, function () {
                            //control.document.nomenID = nomenID;
                        });
            }
        </script>

        <script>
            function showAll(all) {
                if (all) {
                    jQuery("tr[id^='all_']").show();
                    jQuery("tr[id^='reduced_']").hide();
                    jQuery("#showAllEventsToggle").hide();
                    jQuery("#showReducedEventsToggle").show();
                } else {
                    jQuery("tr[id^='all_']").hide();
                    jQuery("tr[id^='reduced_']").show();
                    jQuery("#showAllEventsToggle").show();
                    jQuery("#showReducedEventsToggle").hide();
                }
            }

            function editNomenclature() {
                jQuery("td[id^='edit_']").show();
                jQuery("th[id^='edit_']").show();
            }

            function hideEditNomenclature() {
                jQuery("td[id^='edit_']").hide();
                jQuery("th[id^='edit_']").hide();
            }

        </script>

    </div>
