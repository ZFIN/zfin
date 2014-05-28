<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="linkageMemberList" required="true" type="java.util.List" %>
<%@ attribute name="hideTitle" required="false" type="java.lang.Boolean" %>

<c:if test="${linkageMemberList.size() >=1}">
    <div class="summary">
        <table class="summary rowstripes" style="table-layout:fixed;">
            <c:if test="${!hideTitle}">
                <caption>MAPPING FROM PUBLICATIONS</caption>
            </c:if>
            <tr>
                <th style="width: 10%">Marker</th>
                <th style="width: 10%">Type</th>
                <th style="width: 5%">Chr</th>
                <th style="width: 5%">Distance</th>
                <th style="width: 20%">Publication / Person</th>
                <th style="width: 60%">Comments</th>
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
                        <zfin2:toggleTextLength text=" ${member.linkage.comments}" idName="${zfn:generateRandomDomID()}" shortLength="80"/>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </div>
</c:if>
<script type="javascript">

    $(document).ready(function () {
        $("#textWrapper").dotdotdot({
            after: "a.readmore"
        });
    });


    $("#button").click(function () {
        $("#textWrapper").trigger("update");
    });


</script>
