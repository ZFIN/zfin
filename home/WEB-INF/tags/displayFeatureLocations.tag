<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="allelicFeatures" required="true" type="java.util.List" %>
<%@ attribute name="hideTitle" required="false" type="java.lang.Boolean" %>

<c:if test="${allelicFeatures.size() >=1}">
    <div class="summary">
        <table class="summary rowstripes">

            <tr>
                <th style="width: 8%">Feature</th>
                <th style="width: 7%">Chr</th>
                <th style="width: 5%">Position</th>
                <th style="width: 8%">Assembly</th>
                <th style="width: 17%">Citations</th>


            </tr>
            <c:forEach var="member" items="${allelicFeatures}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td><zfin:link entity="${member.feature}"/></td>
                    <td>${member.chromosome}</td>
                    <td>${member.start}<c:if test="${!empty member.end && member.end != member.end}">
                        - ${member.end}
                    </c:if></td>
                    <td>${member.assembly}</td>
                    <td>
                        <zfin:link entity="${member.attribution}"/>
                    </td>

                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </div>
</c:if>
