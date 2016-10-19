<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:dataManager zdbID="${linkage.zdbID}"/>

<p/>

<div class="titlebar">
    <h1>Linkage Detail</h1>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Chromosome:</span></th>
        <td><span class="name-value">${linkage.chromosome}</span></td>
    </tr>
    <tr>
        <th><span class="name-label">Comment:</span>
            <authz:authorize access="hasRole('root')">
                <span> (<a href="#" onclick="jQuery('#editComment').show()">Edit</a>)</span>
            </authz:authorize>
        </th>
        <td><span class="name-value">${linkage.comments}</span></td>
    </tr>
    <authz:authorize access="hasRole('root')">
        <tr style="display: none" id="editComment">
            <th><span class="name-label">Edit
                :</span></th>
            <td>

                <form:form action="edit-comment" modelAttribute="linkage" method="POST" id="edit-linkage">
                    <form:textarea path="comments" cols="100" rows="6"/>
                    <form:hidden path="zdbID"/>
                    <input value="Save" onclick="this.form.submit()" type="button">
                </form:form>

            </td>
        </tr>
    </authz:authorize>
    <tr>
        <th><span class="name-label">Submitter:</span></th>
        <td><span class="name-value"><zfin:link entity="${linkage.person}"/></span></td>
    </tr>
    <tr>
        <th><span class="name-label">Publication:</span></th>
        <td><span class="name-value"><zfin:link entity="${linkage.publication}"/></span></td>
    </tr>
    <tr>
        <th><span class="name-label">Person Reference:</span></th>
        <td><span class="name-value"></span></td>
    </tr>
</table>

<p/>

<p/>
<c:if test="${linkage.linkageMemberSet.size() > 0}">
    <table class="summary rowstripes sortable">
        <caption>Linkage Memberships</caption>
        <tr>
            <th> ID</th>
            <th> Entity One</th>
            <th> Entity Two</th>
            <th> Distance</th>
            <th> Metric</th>
            <th width="60%"> LOD</th>
        </tr>
        <c:forEach var="member" items="${linkage.linkageMemberSet}">
            <tr>
                <td>${member.id} </td>
                <td width="10%">
                    <zfin:link entity="${member.entityOne}"/>
                </td>
                <td width="10%">
                    <zfin:link entity="${member.entityTwo}"/>
                </td>
                <td>${member.distance} </td>
                <td>${member.metric} </td>
                <td>${member.lod} </td>
            </tr>
        </c:forEach>
    </table>
</c:if>

