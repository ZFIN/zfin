;(function() {
    angular
        .module('app')
        .directive('pubLookup', pubLookup);

    function pubLookup() {
        var directive = {
            scope: {
                defaultPubs: '='
            },
            link: link
        };

        function link(scope, element) {

            var pubSearch = new Bloodhound({
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('title'),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                identify: function(obj) { return obj.zdbID; },
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
                    empty: '<p class="tt-no-results text-danger">Oof. I couldn\'t find any publications like that.</p>'
                }
            });
        }

        return directive;
    }
}());