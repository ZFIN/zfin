<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>

<script src="/javascript/figure.service.js"></script>
<script src="/javascript/quick-figure.directive.js"></script>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css"/>
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

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
        border-bottom-width: 0px;
    }
</style>

<div ng-app="app">

    <zfin2:dataManager zdbID="${publication.zdbID}"
                       showLastUpdate="true"
                       trackURL="/action/publication/${publication.zdbID}/track"
    />

    <table class="table table-bordered" width="100%" style="border-bottom-width: 0px">
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
                   onClick=open("/action/marker/gene-add?type=GENE&source=${publication.zdbID}","helpwindow","scrollbars=yes,height=850,width=650,resizable=yes")>
                    Add New Gene</a> |
                <a class="small-new-link" href="javascript:"
                   onClick=open("/action/marker/sequence-targeting-reagent-add?sequenceTargetingReagentPublicationZdbID=${publication.zdbID}&sequenceTargetingReagentType=MRPHLNO","helpwindow","scrollbars=yes,height=900,width=1150,resizable=yes")>
                    Add New STR</a> |
                <a class="small-new-link" href="javascript:"
                   onClick=open("/action/antibody/add?antibodyPublicationZdbID=${publication.zdbID}","helpwindow","scrollbars=yes,height=850,width=550,resizable=yes")>
                    Add New Antibody</a> |
                <a class="small-new-link" href="javascript:"
                   onClick=open("/action/marker/gene-add?type=EFG&source=${publication.zdbID}","helpwindow","scrollbars=yes,height=850,width=650,resizable=yes")>
                    Add New EFG</a> |
                <a class="small-new-link" href="javascript:"
                   onClick=open("/action/marker/engineeredRegion-add?regionPublicationZdbID=${publication.zdbID}","helpwindow","scrollbars=yes,height=850,width=550,resizable=yes")>
                    Add New Engineered Region</a> |
                <a class="small-new-link" href="javascript:"
                   onClick=open("/action/marker/nonTranscribedRegion-add?source=${publication.zdbID}","helpwindow","scrollbars=yes,height=850,width=550,resizable=yes")>
                    Add New NTR</a> |
                <c:if test="${currentTab eq 'construct'}">
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

    <ul class="nav nav-tabs" id="curation-tabs" style="margin-top: -20px">
        <c:forEach var="curationTab" items="${curationTabs}">
        <c:choose>
        <c:when test="${curationTab.value eq currentTab}">
        <li class="active">
            </c:when>
            <c:otherwise>
        <li>
            </c:otherwise>
            </c:choose>
            <a href="#${curationTab.value}" aria-controls="${curationTab.value}" role="tab" class="nav-tabs-loading"
               onclick="handleTabToggle('${curationTab.value}')"
               data-toggle="tab" id="${curationTab.value}-tab">${curationTab.displayName}</a>
            </c:forEach>
        <li>
            <a aria-controls="refresh" onclick="refresh()" title="Refresh current tab" data-toggle="tooltip"
               class="zfin-tooltip">
                <div role="tab" style="cursor: pointer"><i class="fa fa-refresh" aria-hidden="true"></i>
                </div>
            </a>
        <li>
            <a aria-controls="history" onclick="showHistory()" title="Show history" data-toggle="tooltip"
               class="zfin-tooltip">
                <div role="tab" style="cursor: pointer"><i class="fa fa-history" aria-hidden="true"></i>
                </div>
            </a>
    </ul>

    <div class="tab-content edit-form-content">
        <c:forEach var="tab" items="${curationTabs}">
        <c:choose>
        <c:when test="${tab.value eq currentTab}">
        <div role="tabpanel" class="tab-pane active" id="${tab.value}">
            </c:when>
            <c:otherwise>
            <div role="tabpanel" class="tab-pane" id="${tab.value}">
                </c:otherwise>
                </c:choose>
                <jsp:include page="${tab.value.toLowerCase()}.jsp"/>
            </div>
            </c:forEach>
        </div>
    </div>
    <script>

        function refresh() {
            var tabName = $(".tab-pane.active").attr("id");
//            alert("Tab name: "+ tabName)
            if (tabName != 'orthology')
                refreshTab(tabName);
            else
                refreshOrthologyGeneList();
        }

        function refreshOrthologyGeneList() {
            angular.element(document.getElementById("evidence-modal")).scope().vm.fetchGenes()
        }

        function displayLoadingStatus(tabName, isLoading) {
            $('#' + tabName.toUpperCase() + '-tab').toggleClass("nav-tabs-loading", isLoading);
        }

        $(function () {

            function goToTab(hash) {
                $('#curation-tabs a[href=' + hash + ']').tab('show');
            }

            var hash = window.location.hash;
            if (hash) {
                goToTab(hash);
            }

            $('#curation-tabs a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
                var href = $(e.target).attr('href');
                if (history.pushState) {
                    history.pushState(null, null, href);
                } else {
                    location.hash = href;
                }
                var tabName = href.substring(1)
                if (tabName == 'pheno') {
                    handleTabToggle('pheno');
                }
                jQuery.ajax({
                    url: '/action/curation/currentTab/' + tabName,
                    type: 'GET',

                    success: function (response) {
                    },
                    error: function (data) {
                        alert('There was a problem with your request: ' + data);
                    }

                });
            });

            $('.edit-form-content').on('click', "a[href^='#']", function () {
                var hash = $(this).attr('href');

                goToTab(hash);
            });

            jQuery(".new-pub").on("click", function () {
                var pubID = jQuery("#pubID").val();
                jQuery('#newPublication').attr('action', "/action/curation/" + pubID);
                jQuery("#newPublication").submit();
                e.preventDefault();
            });

            window.addEventListener("popstate", function (e) {
                var hash = window.location.hash;
                if (hash) {
                    goToTab(hash);
                }
            });

            // initialize the tooltip
            //$('[data-toggle="tooltip"]').tooltip();
        });
    </script>

    <script type="text/javascript" language="javascript"
            src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

    <script type="text/javascript">
        var curationProperties = {
            zdbID: "${publication.zdbID}",
            moduleType: "${currentTab}",
            debug: "false"
        }
    </script>

    <script type="text/javascript">
        // this attaches onMouseOver handlers to each of the items in the
        // auto suggest box (popup panel). changes in the selected item should
        // trigger an update of the term info box.
        // ony body
        var showTermInfo = function () {
            var selectedTerm = $(".item-selected").text();
            var tabName = window.location.hash.substr(1).toLowerCase();
            if (selectedTerm.length > 0) {
                var div = $(".termInfoUsed").attr("class");
                if ($(".termInfoUsed").length > 0) {
                    var classArray = div.split(" ");
                    var entityName = classArray[2];
                    var selector = "select." + entityName + "_" + tabName;
                    var selectionBox = $(selector);
                    var selectedOption;
                    if (!selectionBox.length) {
                        var element = "." + entityName + "_single_" + tabName;
                        selectedOption = $(element).text();
                    } else {
                        selectedOption = selectionBox.val();
                    }
                    updateTermInfoBox(selectedTerm, selectedOption, tabName);
                }
            }
        };

        jQuery("body").on("mouseover", ".item-selected", showTermInfo)
            .on("keydown", "input", showTermInfo);

    </script>

    <script>
        $(document).ready(function () {
            $('[data-toggle="tooltip"]').tooltip();
        });
    </script>
</div>