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
                <input type="button" name="submit" value="Add Alias" class="btn btn-primary"
                       ng-click="control.createNewAlias()">
                <span style="text-align: right"><a href="" ng-click="control.closeAliasEditor()">Cancel</a></span>
            </td>
        </tr>
    </table>
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
                <button ng-click="control.addAttribution()" class="btn btn-primary">Add</button>
                <span style="text-align: right"><a href=""
                                                   ng-click="control.closeAliasAttributionEditor()">Cancel</a></span>
            </td>
        </tr>
    </table>
</div>
