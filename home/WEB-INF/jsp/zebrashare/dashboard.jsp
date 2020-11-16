<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

    <div class="container-fluid">

        <h1>Your ZebraShare Submissions</h1>

        <hr>

        <c:if test="${empty publications}">
            <p>You don't have any ZebraShare submissions yet. <a href="/action/zebrashare">Start a new one here.</a></p>
        </c:if>

        <c:forEach items="${publications}" var="publication" varStatus="pubLoop">
            <div>
                <div><zfin:link entity="${publication}">${publication.zdbID}</zfin:link></div>
                <div>
                    <b>${publication.title}</b>
                </div>
                <div>
                    <c:if test="${empty features[publication.zdbID]}">
                        <i class="text-muted">No curated lines yet</i>
                    </c:if>
                    <c:forEach items="${features[publication.zdbID]}" var="feature">
                        <div>
                                ${feature.name}
                            <c:if test="${!empty feature.aliases}">
                                <small>
                                    [<c:forEach items="${feature.aliases}" var="featureAlias" varStatus="featureLoop">
                                    ${featureAlias.alias}<c:if test="${!featureLoop.last}">; </c:if>
                                </c:forEach>]
                                </small>
                            </c:if>
                            &mdash;
                            <a href="/${feature.zdbID}">View</a>
                            <a href="/action/zebrashare/line-edit/${feature.zdbID}">Edit</a>
                        </div>
                    </c:forEach>
                </div>
            </div>
            <c:if test="${!pubLoop.last}"><hr></c:if>
        </c:forEach>


    </div>
</z:page>