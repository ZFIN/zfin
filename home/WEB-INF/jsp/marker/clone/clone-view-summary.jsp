<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${formBean.marker.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Name">
        <zfin:name entity="${formBean.marker}"/>
    </z:attributeListItem>

    <z:attributeListItem label="Symbol">
        <zfin:abbrev entity="${formBean.marker}"/>
       
    </z:attributeListItem>

    <z:attributeListItem label="Previous Names">
        <ul class="comma-separated">
            <c:forEach var="markerAlias" items="${formBean.previousNames}" varStatus="loop">
                <li>${markerAlias.linkWithAttribution}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Type">
        <zfin2:externalLink
                href="http://www.sequenceontology.org/browser/current_svn/term/${formBean.zfinSoTerm.oboID}">${formBean.zfinSoTerm.termName}</zfin2:externalLink>
    </z:attributeListItem>


    <z:attributeListItem label="Location">
        <zfin2:displayLocation entity="${formBean.marker}" longDetail="true"/>
    </z:attributeListItem>
    <z:attributeListItem label="Genome Resources">
        <ul class="comma-separated">
            <c:forEach var="link" items="${formBean.otherMarkerPages}" varStatus="loop">
                <li><a href="${link.link}">${link.displayName}</a>
                        ${link.attributionLink}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>
    <c:if test="${!empty formBean.clone.problem}">
        <z:attributeListItem label="Clone Problem Type"> ${formBean.clone.problem} <img src="/images/warning-noborder.gif" width="20" height="20" align="top"></z:attributeListItem>

    </c:if>

    <z:attributeListItem label="Species">
        ${formBean.clone.probeLibrary.species}
    </z:attributeListItem>

    <z:attributeListItem label="Library">
        ${formBean.clone.probeLibrary.name}
    </tr> </z:attributeListItem>

    <%--<zfin2:cloneData clone="${formBean.clone}" isThisseProbe="${formBean.isThisseProbe}"/>--%>
    <zfin2:cloneSummary formBean="${formBean}"/>

    <zfin2:uninformativeCloneName name="${formBean.marker.abbreviation}" chimericClone="${formBean.marker.chimeric}"/>
    
    



</z:attributeList>