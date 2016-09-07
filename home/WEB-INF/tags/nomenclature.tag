<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div id="evidence-modal" class="jq-modal curation">
    <table>
        <tr>
            <td colspan="2">
                <h3>
                    {{control.fieldName}} Edit
                </h3>
            </td>
        </tr>
        <tr>
            <td>ID:</td>
            <td>{{control.nomenID}}</td>
        </tr>
        <span ng-show="control.hasGeneEdit">
            <tr>
                <td>{{control.fieldName}}:</td>
                <td><input type="text" minlength="5" ng-model="control.geneNameOrAbbreviation"/></td>
            </tr>
        </span>
        <tr>
            <td>Reason:</td>
            <td>
                <select ng-model="control.reason"
                        ng-options="reas for reas in reasonList"></select>
            </td>
        </tr>
        <tr>
            <td>Comments:</td>
            <td><textarea cols="40" ng-model="control.comments">{{control.comments}}</textarea></td>
        </tr>
        <tr>
            <td>
                <button ng-click="control.updateNomenclature()">Update</button>
            </td>
            <td></td>
        </tr>
    </table>
    <span ng-show="control.showAttribution">
        <h4>
            Attributions
        </h4>
        <table>
            <tr ng-repeat="pub in control.publicationDtoList">
                <td>{{pub.zdbID}}</td>
                <td><a href><img src="/images/delete-button.png" ng-click="control.deleteAttribution(pub.zdbID)"/>
                </a></td>
            </tr>
        </table>
        <input size="20" name="publicationID" ng-model="control.publicationID"/>
        <button ng-click="control.addAttribution()">Add</button>
     </span>
</div>
