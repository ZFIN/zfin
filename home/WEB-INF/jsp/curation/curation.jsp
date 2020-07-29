<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<script src="${zfn:getAssetPath("angular.js")}"></script>
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>
<script src="${zfn:getAssetPath("curation.js")}"></script>

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
            <c:if test="${!empty publication.fileName}"> <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i class="far fa-file-pdf"></i></a></c:if>
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
            
                <a class="small-new-link" href="javascript:"
                   onClick=open("/action/infrastructure/controlled-vocabulary-add","helpwindow","scrollbars=yes,height=850,width=750,resizable=yes")>
                    Add New Species (for Constructs)</a> |

            <span quick-figure pub-id="${publication.zdbID}"></span> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/publication/${publication.zdbID}/feature-list","helpwindow","scrollbars=yes,height=850,width=700,resizable=yes")>FEATURE
                TABLE</a> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/publication/${publication.zdbID}/genotype-list","helpwindow","scrollbars=yes,height=850,width=700,resizable=yes")>GENOTYPE
                TABLE</a> |
            <a class="small-new-link" href="javascript:"
               onClick=open("/action/publication/${publication.zdbID}/directly-attributed","helpwindow","scrollbars=yes,height=850,width=700,resizable=yes")>DIRECTLY ATTRIBUTED</a>
        </div>

        <div class="curation-head-row">
            <div id="directAttributionName"></div>
        </div>
    </div>

    <nav class="pub-navigator navbar navbar-expand-sm" id="curation-nav">
        <div class="container-fluid">
            <ul class="nav navbar-nav mr-auto" id="curation-tabs" role="tablist">
                <c:forEach var="curationTab" items="${curationTabs}">
                    <li class="nav-item" role="presentation">

                        <c:if test="${curationTab.displayName ne 'GO' && currentUser.zdbID ne 'ZDB-PERS-050429-23'}">
                        <a href="#${curationTab.value}" aria-controls="${curationTab.value}" role="tab" class="nav-link nav-tabs-loading ${curationTab.value eq currentTab ? 'active' : ''}"
                           onclick="handleTabToggle('${curationTab.value}')" data-toggle="tab" id="${curationTab.value}-tab">

                                 ${curationTab.displayName}
                        </a>
                        </c:if>

                        <c:if test="${currentUser.zdbID eq 'ZDB-PERS-050429-23'}">
                        <a href="#${curationTab.value}" aria-controls="${curationTab.value}" role="tab" class="nav-link nav-tabs-loading ${curationTab.value eq currentTab ? 'active' : ''}"
                           onclick="handleTabToggle('${curationTab.value}')" data-toggle="tab" id="${curationTab.value}-tab">

                            ${curationTab.displayName}
                        </a>
                        </c:if>
                    </li>
                </c:forEach>
                <li class="nav-item">
                    <a aria-controls="refresh" onclick="refresh()" role="tab" title="Refresh current tab" class="nav-link zfin-tooltip">
                        <i class="fas fa-sync" aria-hidden="true"></i>
                    </a>
                </li>
                <li class="nav-item">
                    <a aria-controls="history" onclick="showHistory()" role="tab" title="Show history" class="nav-link zfin-tooltip">
                        <i class="fas fa-history" aria-hidden="true"></i>
                    </a>
                </li>
            </ul>
            <ul class="nav navbar-nav mr-2">
                <li class="navbar-text" id="status-message"></li>
            </ul>
            <form class="navbar-form d-none" id="claim-form">
                <button type="submit" class="btn btn-light">Claim</button>
            </form>
        </div>
    </nav>

    <div class="tab-content edit-form-content p-2">
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
          $('#curation-tabs a[href="' + hash + '"]').tab('show');
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
            window.updateTermInfoBox(selectedTerm, selectedOption, tabName);
          }
        }
      };

      $("body").on("mouseover", ".item-selected", showTermInfo)
        .on("keydown", "input", showTermInfo);

      (function () {
        var $navbar = $('#curation-nav');
        var $claimForm = $('#claim-form')
        var $claimButton = $claimForm.find('button');
        var $statusMessage = $('#status-message');
        var statusEndpoint = '/action/publication/${publication.zdbID}/status';

        $claimButton.on('click', function () {
          var status = {
            pubZdbID: '${publication.zdbID}',
            status: { id: ${curatingStatus.id} },
            location: null,
            owner: { zdbID: '${currentUser.zdbID}' }
          };
          $claimButton
            .prop('disabled', true)
            .html('<i class="fas fa-spinner fa-spin"></i>');
          $.ajax({
            type: 'POST',
            url: statusEndpoint + '?checkOwner=true',
            data: JSON.stringify(status),
            contentType: 'application/json'
          }).always(resetStatusIndicator)
            .done(updateStatusIndicator)
            .fail(showStatusError);
        });

        function resetStatusIndicator() {
          $navbar.removeClass('success danger warning');
          $claimForm.addClass('d-none');
          $claimButton
            .prop('disabled', false)
            .html('Claim');
          $statusMessage.html('');
        }

        function updateStatusIndicator(status) {
          if (status.owner && status.owner.zdbID !== '${currentUser.zdbID}') {
            $statusMessage.html('Owner: <b>' + status.owner.name + '</b>');
            $navbar.addClass('danger');
          } else if (status.status.type !== 'CURATING') {
            if (status.status.type === 'READY_FOR_CURATION') {
              $claimForm.removeClass('d-none');
            }
            $statusMessage.html('Status: <b>' + status.status.name + '</b>');
            $navbar.addClass('warning');
          } else {
            $navbar.addClass('success');
          }
        }

        function showStatusError(error) {
          console.error(error);
          var response = error.responseJSON;
          var message = (response && response.message) || 'Failed to load pub status';
          $statusMessage.html('<b class="error-inline">' + message + '</b>');
        }

        function fetchStatus() {
          $.get(statusEndpoint)
            .always(resetStatusIndicator)
            .done(updateStatusIndicator)
            .fail(showStatusError);
        }

        fetchStatus();
      })();
    </script>

    <script type="text/javascript" language="javascript"
            src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>
</div>