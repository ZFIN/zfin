<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<div id="alias-modal" class="jq-modal curation">
    <table class="table table-condensed">
        <tr>
            <td>Alias</td>
            <td><input type="text" name="newAlias" ng-model="control.newAlias"></td>
            <td style="text-align: right"></td>
        </tr>
        <tr>
            <td>Attribution</td>
            <td><input type="text" name="newAttribution" ng-model="control.newAttribution"></td>
        </tr>
        <tr>
            <td></td>
            <td>
                <button ng-click="control.closeAliasEditor()" class="zfin-button cancel">Cancel</button>
                <button ng-click="control.createNewAlias()" class="zfin-button approve">Add Alias</button>
            </td>
        </tr>
    </table>
    <span ng-bind-html="control.errorMessage | unsafe" class="error"></span>
</div>

<div id="alias-attribution-modal" class="jq-modal curation">
    <h4>
        Attributions for Alias: {{control.newAlias}}
    </h4>
    <table>
        <tr ng-repeat="pub in control.publicationDtoList">
            <td>{{pub.zdbID}}</td>
            <td><a href><img src="/images/delete-button.png" ng-click="control.deleteAliasAttribution(pub.zdbID)"/>
            </a></td>
        </tr>
        <tr>
            <td colspan="2">
                <input size="20" name="publicationID" ng-model="control.publicationID"/>
        </tr>
        <tr>
            <td colspan="2">
                <button ng-click="control.closeAliasAttributionEditor()" class="zfin-button cancel">Cancel</button>
                <button ng-click="control.addAttribution()" class="zfin-button approve">Add</button>
            </td>
        </tr>
    </table>
    <span ng-bind-html="control.errorMessage | unsafe" class="error"></span>
</div>

<div id="delete-modal" class="jq-modal curation">
    <h3>
        Delete Alias: <span ng-bind-html="control.newAlias | unsafe"></span>

        <p/>
    </h3>
    (Including Attributions)
    <div class="popup-actions">
        <button type="button" class="zfin-button cancel" ng-click="control.cancelDeleteAlias()">Cancel</button>
        <button type="button" class="zfin-button reject" ng-click="control.deleteAlias(control.aliasID)">Delete</button>
    </div>
</div>
