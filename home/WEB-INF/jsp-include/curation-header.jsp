<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>

<script src="/javascript/figure.service.js"></script>
<script src="/javascript/quick-figure.directive.js"></script>

<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css"/>

<style type="text/css">
    .sub-data-manager {
        margin: 0px;
        margin-bottom: 0px;
        padding: 0px;
        padding-left: 10px;
        background-color: #9c9;
        padding-bottom: 2px;
        padding-top: 2px;
    }

    .nav-tabs {
        background-color: #699;
    }

    .nav-tabs > li > a {
        color: #000;
        font-weight: bold;
        border-radius: 5px;
    }

    .nav-tabs > li > a:hover {
        background-color: #33cc99 !important;
        border-radius: 5px;
        color: #000;
        border: 1px solid black;
    }

    .nav-tabs > li.active > a,
    .nav-tabs > li.active > a:focus,
    .nav-tabs > li.active > a:hover {
        background-color: #066 !important;
        color: #fff;
        border: 2px solid #3F515F;
    }

    .table-hover > tbody > tr > td:hover, .table-hover > tbody > tr > td:hover {
        background-color: #699 !important;
    }

    .table-bordered > tbody > tr > td {
        border-right-width: 0px;
        border-left-width: 0px;
    }
</style>

<zfin2:dataManager zdbID="${publication.zdbID}"
                   showLastUpdate="true"
                   rtype="publication"
                   trackURL="/action/publication/${publication.zdbID}/track"
        />

<table class="table table-bordered" width="100%">
    <tbody>
    <tr>
        <td>
            <zfin2:toggleTextLength text="${publication.authors}${publication.title}"
                                    idName="${zfn:generateRandomDomID()}"
                                    shortLength="80"
                                    url="${publication.zdbID}"/>
        </td>
    </tr>
    <tr>
        <td>
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/marker/gene-add?type=GENE&source=\'${publication.zdbID}\'","helpwindow","scrollbars=yes,height=850,width=550,resizable=yes")>
                Add New Gene</a> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentPublicationZdbID=${publication.zdbID}&sequenceTargetingReagentType=MRPHLNO","helpwindow","scrollbars=yes,height=900,width=1150,resizable=yes")>
                Add New STR</a> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/antibody/add?antibodyPublicationZdbID=${publication.zdbID}","helpwindow","scrollbars=yes,height=850,width=550,resizable=yes")>
                Add New Antibody</a> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/marker/gene-add?type=EFG&source='${publication.zdbID}'","helpwindow","scrollbars=yes,height=850,width=550,resizable=yes")>
                Add New EFG</a> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/marker/region-add?regionPublicationZdbID=${publication.zdbID}","helpwindow","scrollbars=yes,height=850,width=550,resizable=yes")>
                Add New Engineered Region</a> |
            <c:if test="${module eq 'construct'}">
                <a class="small-new-link" href="javascript:"
                   onClick=open("/action/infrastructure/controlled-vocabulary-add","helpwindow","scrollbars=yes,height=850,width=750,resizable=yes")>
                    Add New Species</a> |
            </c:if>
            <span quick-figure pub-id="${publication.zdbID}"></span> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/publication/${publication.zdbID}/feature-list","helpwindow","scrollbars=yes,height=850,width=700,resizable=yes")>FEATURE
                TABLE</a>
        </td>
    </tr>
    <tr>
        <td>
            <div id="directAttributionName"></div>
        </td>
    </tr>
</table>

<ul class="nav nav-tabs" id="myTab">
    <c:forEach var="curationTab" items="${curationTabs}">
        <c:choose>
            <c:when test="${curationTab.name eq module}">
                <li class="active">
            </c:when>
            <c:otherwise>
                <li>
            </c:otherwise>
        </c:choose>
        <a href="/action/curation/${curationTab.name}/${publication.zdbID}">${curationTab.displayName}</a></li>
    </c:forEach>
</ul>

<script>
    jQuery(document).ready(function () {
        jQuery(".new-pub").on("click", function () {
            var pubID = jQuery("#pubID").val();
            jQuery('#newPublication').attr('action', "/action/curation/${module}/" + pubID);
            jQuery("#newPublication").submit();
            e.preventDefault();
        });
    });
</script>