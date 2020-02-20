<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>

<script src="${zfn:getAssetPath("angular.js")}"></script>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID: "${formBean.marker.zdbID}"
    };

    function refreshSequenceInformation() {
        angular.element(document.getElementById('sequenceInformation')).scope().getSequences();
    }
</script>

<authz:authorize access="hasRole('root')">

    <c:set var="deleteURL">none</c:set>
    <zfin2:dataManager
                zdbID="${formBean.marker.zdbID}"
                editURL=""
                deleteURL="${deleteURL}"/>

    <div style="font-size: large; text-align: center;">
            <a href="/action/marker/sequence/view/${formBean.marker.zdbID}">[View Sequences]</a>
    </div>

    <zfin2:sequenceHead gene="${formBean.marker}"/>

    <div id="newProteinSequence"></div>

    <div id="newStemLoopSequence"></div>

    <div ng-app="app" ng-controller="sequenceInfoCtrl as si" ng-init="initiate('${formBean.marker.zdbID}')" ng-cloak id="sequenceInformation">

        <div class="summaryTitle">SEQUENCE INFORMATION
           <span style="cursor: pointer;">
            <i style="color: red; font-weight: bold;" title="Create new sequence information" ng-click="si.openAddSequenceInfo()">New</i>
           </span>
        </div>
        <table class="summary rowstripes" ng-show="si.linkDisplays.length">
            <tbody>
            <tr>
                <th width="30%">Type</th>
                <th width="50%"> Accession # </th>
                <th width="20%"> Length (nt/aa) </th>
            </tr>
            <tr ng-repeat="link in si.linkDisplays" ng-class-odd="'odd'" ng-class-even="'even'">
                <td>{{link.dataType}}</td>
                <td><a ng-href="{{link.urlPrefix + link.accession}}">{{link.referenceDatabaseName}}:{{link.accession}}</a>
                    <span ng-bind-html="link.refLink"></span>
                      <span style="cursor: pointer;" ng-click="si.openUpdateSequenceInfo(link)">
                        <i class="far fa-edit red" aria-hidden="true" title="Update the sequence information"></i>
                      </span>
                      <span style="cursor: pointer;" ng-click="si.openDeleteSequenceInfo(link)">
                        <i class="fas fa-trash red" aria-hidden="true" title="Delete the sequence information"></i>
                      </span>
                </td>
                <td>{{link.length}}</td>
            </tr>
            </tbody>
        </table>

        <div id="new-sequence-information-modal" class="jq-modal curation">
            <h3>
                Add New Sequence Information
            </h3>
            <table>
                <tr>
                    <td>Database:</td><td><select class="form-control"  ng-model="si.newDatabase" ng-change="si.errorDb=''"
                                                     ng-options="database.zdbID as database.label for database in si.databases"></select></td>
                    <td><span class="error">{{si.errorDb}}</span></td>
                </tr>
                <tr>
                    <td>Accession:</td><td><input ng-model="si.newAccession" ng-keyup="si.errorAcc = ''" /></td>
                    <td><span class="error">{{si.errorAcc}}</span></td>
                </tr>
                <tr>
                    <td>Length:</td><td><input ng-model="si.newLength" ng-keyup="si.errorLength = ''" /></td>
                    <td><span class="error">{{si.errorLength}}</span></td>
                </tr>
                <tr>
                    <td>Reference:</td><td><input ng-model="si.newReference" ng-keyup="si.errorRef = ''; si.errorMessage = ''" /></td>
                    <td><span class="error">{{si.errorRef}}</span></td>
                </tr>
                <tr>
                    <td colspan="3" nowrap>
                        <button class="zfin-button cancel" ng-click="si.close()">Cancel</button>
                        <button class="zfin-button approve" ng-click="si.addSequenceInfo()">Add</button>
                    </td>
                </tr>
            </table>
            <span class="error" ng-show="si.errorRef === ''">{{si.errorMessage}}</span>
        </div>

        <div id="delete-sequence-info-modal" class="jq-modal curation">
            <h3>
                Delete Sequence Information
            </h3>
            <table>
                <tr>
                    <td>
                        <a style="font-weight: bold;" target="_blank" ng-href="{{si.seqenceInfo.urlPrefix + si.seqenceInfo.accession}}">{{si.seqenceInfo.referenceDatabaseName}}:{{si.seqenceInfo.accession}}</a>
                    </td>
                </tr>
                <tr ng-repeat="ref in si.seqenceInfo.references">
                    <td>
                        {{ref.zdbID}}<br>
                  <span ng-if="ref.title.length < 80"><a target="_blank" ng-href="/{{ref.zdbID}}">{{ref.title}}</a></span>
                  <span ng-if="ref.title.length >= 80"><a target="_blank" ng-href="/{{ref.zdbID}}">{{ref.title | limitTo:55}}...</a></span>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div class="popup-actions">
                            <button class="zfin-button cancel" ng-click="si.close()">Cancel</button>
                            <button class="zfin-button reject" ng-click="si.deleteSeqenceInfo()">Delete</button>
                        </div>
                    </td>
                </tr>
            </table>
            <span class="error">{{si.errorMessage}}</span>
        </div>

        <div id="update-sequence-information-modal" class="jq-modal curation">
            <h3>
                Update <a style="font-weight: bold;" target="_blank" ng-href="{{si.seqenceInfo.urlPrefix + si.seqenceInfo.accession}}">{{si.seqenceInfo.referenceDatabaseName}}:{{si.seqenceInfo.accession}}</a>
            </h3>
            <table>
                <tr>
                    <td>Database:&nbsp;&nbsp;{{si.seqenceInfo.referenceDatabaseDisplay}}</td>
                    <td><span class="error">{{si.errorDb}}</span></td>
                </tr>
                <tr>
                    <td>Accession:&nbsp;<input placeholder="{{si.accessionEdit}}" ng-model="si.accessionEdit" ng-keyup="si.errorAcc = ''" /></td>
                    <td><span class="error">{{si.errorAcc}}</span></td>
                </tr>
                <tr>
                    <td>Length:&nbsp;<input placeholder="{{si.lengthEdit}}" ng-model="si.lengthEdit" ng-keyup="si.errorLength = ''" /></td>
                    <td><span class="error">{{si.errorLength}}</span></td>
                </tr>
                <tr>
                    <td colspan="2" nowrap>
                        <button class="zfin-button cancel" ng-click="si.close()">Cancel</button>
                        <button class="zfin-button approve" ng-click="si.updateSequenceInfo()">Update</button>
                    </td>
                </tr>
                <tr></tr>
            </table>
            <table>
                <tr ng-repeat="ref in si.seqenceInfo.references">
                    <td><a target="_blank" ng-href="/{{ref.zdbID}}">{{ref.zdbID}}</a></td>
                    <td><a ng-show="si.seqenceInfo.references.length > 1" ng-click="si.deleteAttribution($index)" href><img src="/images/delete-button.png" />
                    </a></td>
                </tr>
                <tr>
                    <td>Reference:&nbsp;<input size="20" name="publicationID" ng-model="si.referenceEdit" ng-keyup="si.errorRef = ''" /></td>
                    <td><span class="error">{{si.errorRef}}</span></td>
                </tr>
                <tr>
                    <td colspan="2" nowrap>
                        <button ng-click="si.close()" class="zfin-button cancel">Close</button>
                        <button ng-click="si.addAttribution()" class="zfin-button approve">Add</button>
                    </td>
                </tr>
            </table>
            <span class="error">{{si.errorMessage}}</span>
        </div>

    </div>

</authz:authorize>




