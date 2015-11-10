<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="linkageMemberList" required="true" type="java.util.List" %>
<%@ attribute name="hideTitle" required="false" type="java.lang.Boolean" %>

<c:if test="${linkageMemberList.size() >=1}">
    <div class="summary">
        <table class="summary rowstripes">
            <c:if test="${!hideTitle}">
                <caption>MAPPING FROM PUBLICATIONS</caption>
            </c:if>
            <tr>
                <th style="width: 8%">Marker</th>
                <th style="width: 7%">Type</th>
                <th style="width: 5%">Chr</th>
                <th style="width: 8%">Distance</th>
                <th style="width: 17%">Publication / Person</th>
                <th style="width: 55%">Comments</th>
                <authz:authorize access="hasRole('root')">
                    <th>Linkage</th>
                </authz:authorize>
            </tr>
            <c:forEach var="member" items="${linkageMemberList}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td><zfin:link entity="${member.linkedMember}"/></td>
                    <td>${zfn:getMappingEntityType(member.linkedMember)}</td>
                    <td>${member.linkage.chromosome}</td>
                    <td>${member.distance} ${member.metric}</td>
                    <td>
                        <zfin:link entity="${member.linkage.reference}"/>
                    </td>
                    <td>
                        <zfin2:toggleTextLength text=" ${member.linkage.comments}" idName="${zfn:generateRandomDomID()}"
                                                shortLength="80"/>
                    </td>
                    <authz:authorize access="hasRole('root')">
                        <td><a href="/action/mapping/linkage/${member.linkage.zdbID}">${member.linkage.zdbID}</a></td>
                    </authz:authorize>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </div>
</c:if>
