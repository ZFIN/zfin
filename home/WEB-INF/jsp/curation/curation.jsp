<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>

<script src="/javascript/figure.service.js"></script>
<script src="/javascript/quick-figure.directive.js"></script>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css"/>
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<c:if test="${hasCorrespondence}">
    <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
</c:if>

<div ng-app="app">

    <zfin2:dataManager zdbID="${publication.zdbID}"
                       showLastUpdate="true"
                       viewURL="/${publication.zdbID}"
                       trackURL="/action/publication/${publication.zdbID}/track"
                       correspondenceURL="${correspondenceURL}"
                       editURL="/action/publication/${publication.zdbID}/edit"
                       linkURL="/action/publication/${publication.zdbID}/link"
    />

    <div class="curation-head">
        <div class="curation-head-row">
            <zfin2:toggleTextLength text="${publication.authors}${publication.title}"
                                    idName="${zfn:generateRandomDomID()}"
                                    shortLength="80"
                                    url="${publication.zdbID}"/>
            <c:if test="${!empty publication.fileName}"> <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i class="fa fa-file-pdf-o"></i></a></c:if>
        </div>

        <div class="curation-head-row">
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
                TABLE</a> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/publication/${publication.zdbID}/directly-attributed","helpwindow","scrollbars=yes,height=850,width=700,resizable=yes")>DIRECTLY ATTRIBUTED</a>
        </div>

        <div class="curation-head-row">
            <div id="directAttributionName"></div>
        </div>
    </div>

    <nav class="pub-navigator navbar navbar-default navbar-static-top">
        <div class="container-fluid">
            <ul class="nav navbar-nav" id="curation-tabs">
                <c:forEach var="curationTab" items="${curationTabs}">
                    <li class="${curationTab.value eq currentTab ? 'active' : ''}">
                        <a href="#${curationTab.value}" aria-controls="${curationTab.value}" role="tab" class="nav-tabs-loading"
                           onclick="handleTabToggle('${curationTab.value}')"
                           data-toggle="tab" id="${curationTab.value}-tab">
                                ${curationTab.displayName}
                        </a>
                    </li>
                </c:forEach>
                <li>
                    <a aria-controls="refresh" onclick="refresh()" role="tab" title="Refresh current tab" class="zfin-tooltip">
                        <i class="fa fa-refresh" aria-hidden="true"></i>
                    </a>
                </li>
                <li>
                    <a aria-controls="history" onclick="showHistory()" role="tab" title="Show history" class="zfin-tooltip">
                        <i class="fa fa-history" aria-hidden="true"></i>
                    </a>
                </li>
            </ul>
        </div>
    </nav>

    <div class="tab-content edit-form-content">
        <c:forEach var="tab" items="${curationTabs}">
            <div role="tabpanel" class="tab-pane ${tab.value eq currentTab ? 'active' : ''}" id="${tab.value}">
                <jsp:include page="${tab.value.toLowerCase()}.jsp"/>
            </div>
        </c:forEach>
    </div>

    <script>

      function refresh () {
        var tabName = $(".tab-pane.active").attr("id");
        if (tabName !== 'orthology') {
          refreshTab(tabName);
        } else {
          refreshOrthologyGeneList();
        }
      }

      function refreshOrthologyGeneList () {
        angular.element(document.getElementById("evidence-modal")).scope().vm.fetchGenes()
      }

      function displayLoadingStatus (tabName, isLoading) {
        $('#' + tabName.toUpperCase() + '-tab').toggleClass("nav-tabs-loading", isLoading);
      }

      $(function () {
        $('.zfin-tooltip').tipsy({gravity: 's'});

        function goToTab (hash) {
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

        $(".new-pub").on("click", function () {
          var pubID = jQuery("#pubID").val();
          $('#newPublication').attr('action', "/action/curation/" + pubID);
          $("#newPublication").submit();
          e.preventDefault();
        });

        window.addEventListener("popstate", function (e) {
          var hash = window.location.hash;
          if (hash) {
            goToTab(hash);
          }
        });
      });

      var curationProperties = {
        zdbID: "${publication.zdbID}",
        moduleType: "${currentTab}",
        debug: "false"
      };

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

      $("body").on("mouseover", ".item-selected", showTermInfo)
        .on("keydown", "input", showTermInfo);
    </script>

    <script type="text/javascript" language="javascript"
            src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>
</div>