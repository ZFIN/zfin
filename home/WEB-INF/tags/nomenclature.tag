<%@ attribute name="geneEdit" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div id="nomenclature-modal" class="jq-modal curation">
    <h3>
        {{control.fieldName}} Edit
    </h3>
    <table class="table">
        <tr>
            <td>ID:</td>
            <td>{{control.nomenID}}</td>
        </tr>
        <c:if test="${geneEdit}">
            <tr>
                <td>{{control.fieldName}}:</td>
                <td><input type="text" minlength="5" size="40" ng-model="control.geneNameOrAbbreviation"/></td>
            </tr>
        </c:if>
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
            <td></td>
            <td>
                <c:if test="${geneEdit}">
                    <button type="button" class="zfin-button cancel" ng-click="control.closeGeneEditor()">Cancel
                    </button>
                </c:if>
                <button type="button" class="zfin-button approve" ng-click="control.updateNomenclature()">Update
                </button>
            </td>
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
        <button type="button" class="zfin-button approve" ng-click="control.addAttribution()">Add</button>
     </span>
    <span ng-bind-html="control.errorMessage | unsafe" class="error"></span>
</div>
