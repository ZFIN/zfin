<%--
  Created by IntelliJ IDEA.
  User: kschaper
  Date: 6/18/14
  Time: 5:43 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <title>AngularJS Facet Autocomplete Test</title>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.3.0-beta.13/angular.min.js"></script>
    <script src="/javascript/header.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="/css/zfin.css">
    <link rel="stylesheet" type="text/css" href="/css/header.css">
    <link rel="stylesheet" type="text/css" href="/css/footer.css">
    <link rel=stylesheet type="text/css" href="/css/searchresults.css">
    <link rel=stylesheet type="text/css" href="/css/summary.css">
    <link rel=stylesheet type="text/css" href="/css/Lookup.css">
    <link rel=stylesheet type="text/css" href="/css/datapage.css">
    <link rel=stylesheet type="text/css" href="/css/popup.css">
    <link rel=stylesheet type="text/css" href="/css/tipsy.css">

    <script type="text/javascript" src="/javascript/jquery.tipsy.js"></script>
    <script type="text/javascript" src="/javascript/sorttable.js"></script>

    <script src="/javascript/header-menu.js" type="text/javascript"></script>

    <!-- Begin Inspectlet Embed Code -->
    <script type="text/javascript" id="inspectletjs">
        window.__insp = window.__insp || [];
        __insp.push(['wid', 000]);
        (function() {
            function __ldinsp(){var insp = document.createElement('script'); insp.type = 'text/javascript'; insp.async = true; insp.id = "inspsync"; insp.src = ('https:' == document.location.protocol ? 'https' : 'http') + '://cdn.inspectlet.com/inspectlet.js'; var x = document.getElementsByTagName('script')[0]; x.parentNode.insertBefore(insp, x); }
            if (window.attachEvent){
                window.attachEvent('onload', __ldinsp);
            }else{
                window.addEventListener('load', __ldinsp, false);
            }
        })();
    </script>
    <!-- End Inspectlet Embed Code -->

</head>
<body>


<div class="allcontent">

<div ng-app="facetApp">
    <div ng-controller="facetListController">

        <div>
            Search: <input ng-model="query" ng-model-options="{debounce: 300}">
        </div>
        <ul>
            <li ng-repeat="value in facetValues | filter:query | limitTo:20">
                <a href="#">+</a>
                <a href="#">-</a>
                <a href="#">{{value.name}}</a> ({{value.count}})
            </li>
        </ul>
    </div>

    <div ng-controller="anotherFacetListController">

        <div>
            Search: <input ng-model="query" ng-model-options="{debounce: 300}">
        </div>
        <ul>
            <li ng-repeat="value in facetValues | filter:query | limitTo:20">
                <a href="#">+</a>
                <a href="#">-</a>
                <a href="#">{{value.name}}</a> ({{value.count}})
            </li>
        </ul>
    </div>


</div>

</div>

<script>


    var facetApp = angular.module('facetApp', []);

    facetApp.controller('facetListController', function ($scope, $http) {
        var field = 'sequence_alteration';
        $scope.url = 'http://tango.zfin.org/action/quicksearch/facet-autocomplete?fq=category%3A%22Publication%22&category=Publication&field=' + field + '&term=&limit=-1&sort=index';
        $http.get($scope.url).success(function(data) {
            $scope.facetValues = data;
        });
    });

    facetApp.controller('anotherFacetListController', function ($scope, $http) {
        $scope.url = 'http://tango.zfin.org/action/quicksearch/facet-autocomplete?fq=category%3A%22Gene%22&category=Gene&field=molecular_function_tf&term=&limit=-1&sort=index';
        $http.get($scope.url).success(function(data) {
            $scope.facetValues = data;
        });
    });

</script>



    <script language="JavaScript" src="/javascript/footer.js" type="text/javascript"></script>



<script>
jQuery(document).ready(function() { jQuery(".default-input").focus(); })
</script>

</body>
</html>
