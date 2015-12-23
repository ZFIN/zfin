;(function () {
    angular
        .module('app')
        .directive('pubLookup', pubLookup);

    function pubLookup() {
        var directive = {
            require: 'ngModel',
            scope: {
                defaultPubs: '='
            },
            link: link,
            priority: 10 // make sure ngModel directive gets applied first
        };

        function link(scope, element, attrs, ngModel) {

            var pubSearch = new Bloodhound({
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('title'),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                identify: function (obj) {
                    return obj.zdbID;
                },
                remote: {
                    url: '/action/publication/lookup?q=%QUERY',
                    wildcard: '%QUERY'
                }
            });

            function pubsWithDefault(q, sync, async) {
                if (q === '') {
                    sync(scope.defaultPubs);
                } else if (q.match(/ZDB-PUB-\d{6}-\d+/)) {
                    pubSearch.search(q, sync, async);
                }
            }

            element.typeahead({minLength: 0}, {
                name: 'pub-lookup',
                display: 'zdbID',
                source: pubsWithDefault,
                templates: {
                    suggestion: function (item) {
                        return '<div><p class="journal-abbrev">' + item.zdbID + '</p><p class="journal-name text-muted">' + item.title + '</p></div>';
                    },
                    empty: '<p class="tt-no-results text-danger">Couldn\'t find any publications with that ID.</p>'
                }
            });

            ngModel.$viewChangeListeners.push(function () {
                var value = ngModel.$viewValue;
                if (value && value.match(/^\d/)) {
                    ngModel.$setViewValue('ZDB-PUB-' + value);
                    ngModel.$render();
                }
            });

            ngModel.$render = function () {
                element.typeahead('val', ngModel.$viewValue);
            };
        }

        return directive;
    }
}());