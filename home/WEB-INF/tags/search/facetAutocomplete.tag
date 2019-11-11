<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<div ng-app="app">

    <div id="facet-list-controller" ng-controller="facetListController">
        <div ng-hide="facetValues">
            Loading...
        </div>
        <div ng-show="facetValues">
            <div>
                Filter: <input ng-model="query" ng-model-options="{debounce: 300}" ng-change="page = 1">

            </div>
            <ul class="list-unstyled modal-body-scrolling" style="padding: 10px;">
                <li style="clear:both" class="selectable-facet-value facet-value" ng-repeat="value in ( filteredValues = (facetValues | match:query) ) | paging:perPage:page">
                    <a class="facet-include" href="${baseUrlWithoutPage}fq={{field}}:%22{{value.name}}%22" ng-click="sendGAEvent('include', value.name)"><i class="include-exclude-icon fa fa-plus-circle"></i></a>
                    <a class="facet-exclude" href="${baseUrlWithoutPage}fq=-{{field}}:%22{{value.name}}%22" ng-click="sendGAEvent('exclude', value.name)"><i class="include-exclude-icon fa fa-minus-circle"></i></a>
                    <a href="${baseUrlWithoutPage}fq={{field}}:%22{{value.name}}%22" ng-click="sendGAEvent('include', value.name)" ng-bind-html="value.value | trustedHtml"></a>
                      <span style="padding-left: 1em;" class="pull-right">({{value.count}})</span>
                </li>
            </ul>
            <div style="margin-top: .5em;">

                <%--Page:--%>
                <button style="margin-left: 3em;" class="btn btn-outline-secondary" ng-click="prevPage()"><i class="fas fa-chevron-left"></i></button>
                {{page}}/{{   Math.ceil(filteredValues.length/perPage)  }}
                <button class="btn btn-outline-secondary" ng-click="nextPage()"><i class="fas fa-chevron-right"></i></button>

                <div class="pull-right">
                    Show:
                    <select style="width: 4em; position:relative; top: .3em;"
                            ng-init="perPage = 10; maxPage = -1" ng-model="perPage" ng-change="page = 1">
                        <option>10</option>
                        <option>100</option>
                        <option value="9999999999">All</option>
                    </select>
                </div>
            </div>
        </div>
    </div>

</div>


<script>

    angular.module('app')
            .filter('match', function() {

                //important!  the input to a filter is the entire list, not one record at a time
                return function(input, query) {
                    var split_on = ["-", ":", ",", "(", ")", ".", ";"]; //in addition to spaces
                    var output = [];
                    if (input === undefined) {
                        return;
                    }
                    if (query != undefined && query != "") {
                        var query_value_string = query.toLowerCase();
                        //todo: if we run into performance trouble, do this once when the list is loaded rather than for each keystroke
                        split_on.forEach(function(split_char) {
                            query_value_string = query_value_string.replace(split_char," ");
                        });
                        var query_list = query_value_string.split(" ");
                        input.forEach(function(facetValue) {
                            var value_string = facetValue.value.toLowerCase();
                            //split on commas and colons
                            split_on.forEach(function(split_char) {
                                value_string = value_string.replace(split_char," ");
                            });
                            var value_list = value_string.split(" ");
                            var query_hit_map = [];
                            query_list.forEach(function(query_component) {
                                query_hit_map[query_component] = false;
                            });

                            for (var j = 0 ; j < value_list.length ; j++) {
                              for (var i = 0 ; i < query_list.length ; i++) {
                                  if (value_list[j].indexOf(query_list[i]) == 0) {
                                      query_hit_map[query_list[i]] = true;
                                  }
                              }                               
                            }

                            var addToOutput = true;
                            query_list.forEach(function(query_component) {
                                if (query_hit_map[query_component] == false) {
                                    addToOutput = false;
                                }
                            });

                            if (addToOutput) {
                                output.push(facetValue)
                            }
                        });
                    } else { //if there was no query, return everything...
                        return input;
                    }
                    return output;
                }
            })
            .filter('paging', function() {
                return function(input, pageSize, page) {
                    if (input === undefined) {
                        return;
                    }

                    pageSize = parseInt(pageSize);
                    page = parseInt(page);

                    var offset = parseInt((page-1) * pageSize);
                    return input.slice(offset, offset + pageSize);
                }

            })
            .controller('facetListController', function ($scope, $http) {

                //This let's me use Math.ceil
                $scope.Math = window.Math;

                $scope.page = 1;
                $scope.pageSize = 10;

                $scope.fetchValues = function(category, field, title) {

                    //clear the query, probably not meaningful for the new values
                    $scope.query = "";
                    $scope.page = 1;
                    $scope.pageSize = 10;
                    $scope.facetValues = null;
                    $scope.title = title;
                    $scope.field = field;
                    $scope.category = category;

        //            $scope.url = '/action/quicksearch/facet-autocomplete?fq=category%3A%22' + category + '%22&category=' + category + '&field=' + field + '&term=&limit=-1&sort=index';
                    $scope.url = '/action/quicksearch/facet-autocomplete?' + jQuery('#query-string').val() + '&field=' + field + '&term=&limit=-1&sort=index';

                    $http.get($scope.url).success(function(data) {
                        $scope.facetValues = data;

                    });
                };

                $scope.nextPage = function() {
                    var nextPage = parseInt($scope.page) + 1;

        //            console.log('maxPage: ' + $scope.maxPage + ", nextPage:" + nextPage);

        //            $scope.maxPage = $scope.filteredValues.length / $scope.perPage;
                    if (nextPage <= parseInt(Math.ceil($scope.filteredValues.length/$scope.perPage),10)) {
                        $scope.page = nextPage;
                    }

                };

                $scope.prevPage = function() {
                    var prevPage = parseInt($scope.page) - 1;
                    if (prevPage > 0) {
                        $scope.page = prevPage;
                    }
                };

                $scope.sendGAEvent = function(action, value) {
                    ga('send', 'event', $scope.category + ' : ' + $scope.title + ' Facet', action, value);
                };
            });

</script>
