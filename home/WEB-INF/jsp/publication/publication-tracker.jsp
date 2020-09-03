<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>



<c:set var="STATUS" value="Status"/>
<c:set var="TOPICS" value="Topics"/>
<c:set var="DATAOBJECTS" value="Data Objects"/>
<c:set var="NOTES" value="Notes"/>
<c:set var="CONTACTAUTHORS" value="Contact Authors"/>
<c:set var="CORRESPONDENCE" value="Correspondence"/>
<c:set var="viewURL">/${publication.zdbID}</c:set>
<c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>
<c:set var="linkURL">/action/publication/${publication.zdbID}/link</c:set>
<c:if test="${allowCuration}">
    <c:set var="curateURL">/action/curation/${publication.zdbID}</c:set>
</c:if>
<c:if test="${hasCorrespondence}">
    <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
</c:if>
<div class="container-fluid" ng-app="app">
    <zfin2:dataManager zdbID="${publication.zdbID}"
                       viewURL="${viewURL}"
                       editURL="${editURL}"
                       linkURL="${linkURL}"
                       correspondenceURL="${correspondenceURL}"
                       curateURL="${curateURL}"/>

    <p class="lead">
        <a href="/${publication.zdbID}">${publication.title}</a>
        <c:if test="${!empty publication.fileName}"> <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i class="far fa-file-pdf"></i></a></c:if>
    </p>
</div>
<z:dataPage
        sections="${[STATUS, TOPICS, NOTES, CONTACTAUTHORS, DATAOBJECTS, CORRESPONDENCE]}">

    <jsp:body>
        <z:section title="${STATUS}">
            <div class="__react-root"
                 id="PubTrackerStatusSection"
                 data-pub-id="${publication.zdbID}"
                 data-user-id="${loggedInUser.zdbID}"
                 data-user-name="${loggedInUser.display}"
                 data-user-email="${loggedInUser.email}"
            >
            </div>
        </z:section>
            <z:section title="${TOPICS}">
                <div class="__react-root"
                     id="PubTrackerTopicsSection"
                     data-pub-id="${publication.zdbID}"
                     data-user-id="${loggedInUser.zdbID}"
                     data-user-name="${loggedInUser.display}"
                     data-user-email="${loggedInUser.email}"
                >
                </div>


        </z:section>

        <z:section title="${NOTES}">
            <div class="__react-root"
                 id="PubTrackerNotesSection"
                 data-pub-id="${publication.zdbID}"
                 data-user-id="${loggedInUser.zdbID}"
                 data-user-name="${loggedInUser.display}"
                 data-user-email="${loggedInUser.email}"
            >
            </div>

        </z:section>

        <z:section title="${CONTACTAUTHORS}">
            <div class="__react-root"
                 id="PubTrackerAuthorNotificationSection"
                 data-pub-id="${publication.zdbID}"
                 data-user-id="${loggedInUser.zdbID}"
                 data-user-name="${loggedInUser.display}"
                 data-user-email="${loggedInUser.email}"
            >
            </div>

        </z:section>
        <z:section title="${DATAOBJECTS}">
            <z:section title="Genes">
                <div class="__react-root" id="PubGeneTable" data-pub-id="${publication.zdbID}"></div>
            </z:section>
            <z:section title="Sequence Targeting Reagents">
                <div class="__react-root" id="PubSTRTable" data-pub-id="${publication.zdbID}"></div>
            </z:section>
            <z:section title="Mutants and Transgenics">
                <div class="__react-root" id="PubAlleleTable" data-pub-id="${publication.zdbID}"></div>
            </z:section>
        </z:section>

        <z:section title="${CORRESPONDENCE}">
            <div class="__react-root"
                 id="PubCorrespondenceSection"
                 data-pub-id="${publication.zdbID}"
                 data-user-id="${loggedInUser.zdbID}"
                 data-user-name="${loggedInUser.display}"
                 data-user-email="${loggedInUser.email}"
            >
            </div>

        </z:section>

    </jsp:body>

</z:dataPage>
