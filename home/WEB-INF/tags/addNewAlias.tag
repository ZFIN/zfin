<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
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
