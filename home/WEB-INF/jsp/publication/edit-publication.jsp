<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">

    <c:set var="pubID">${publicationBean.publication.zdbID}</c:set>
    <c:set var="linkURL">/action/publication/${pubID}/link</c:set>
    <c:set var="trackURL">/action/publication/${pubID}/track</c:set>
    <c:if test="${allowCuration}">
        <c:set var="curateURL">/action/curation/${pubID}</c:set>
    </c:if>
    <c:if test="${hasCorrespondence}">
        <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
    </c:if>

    <div class="container-fluid">
        <zfin2:dataManager zdbID="${pubID}"
                           linkURL="${linkURL}"
                           trackURL="${trackURL}"
                           curateURL="${curateURL}"
                           correspondenceURL="${correspondenceURL}"
                           viewURL="/${pubID}"/>

        <div class="row">
            <div class="col-12">
                <h2>Editing ${pubID}</h2>
            </div>
        </div>

        <ul id="fig-edit-tabs" class="nav nav-tabs nav-justified mb-5" role="tablist">
            <li role="presentation" class="nav-item">
                <a href="#details" class="nav-link active" aria-controls="details" role="tab" data-toggle="tab">Details</a>
            </li>
            <li role="presentation" class="nav-item">
                <a href="#files" class="nav-link" aria-controls="files" role="tab" data-toggle="tab">Files</a>
            </li>
            <li role="presentation" class="nav-item">
                <a href="#figures" class="nav-link" aria-controls="figures" role="tab" data-toggle="tab">Figures</a>
            </li>
        </ul>

        <div class="tab-content">
            <div role="tabpanel" class="tab-pane active" id="details">
                <zfin2:publicationForm publicationBean="${publication}" pubID="${pubID}" error="${error}"/>
            </div>
            <div role="tabpanel" class="tab-pane" id="files">
                <div class="__react-root"
                     id="PubEditFiles"
                     data-pub-id="${pubID}"
                >
                </div>
            </div>
            <div role="tabpanel" class="tab-pane figure-edit-panel" id="figures">

                <div class="__react-root" id="FigureEdit" data-pub-id="${pubID}"></div>


                <div class="__react-root"
                     id="ProcessorApproval__figures"
                     data-pub-id="${pubID}"
                     data-task="ADD_FIGURES"
                >
                </div>
            </div>
        </div>

    </div>

    <script>
        $('#fig-edit-tabs').stickyTabs({initialTab: $('#fig-edit-tabs a.active')});
    </script>

    <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>