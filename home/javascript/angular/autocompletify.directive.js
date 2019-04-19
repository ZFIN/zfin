;(function() {
    angular
        .module('app')
        .directive('autocompletify', autocompletify);

    function autocompletify() {
        var directive = {
            require: 'ngModel',
            scope: {
                url: '@',
                onSelect: '&'
            },
            link: link
        };

        function link(scope, element, attrs, ngModel) {
            element
                .autocompletify(scope.url)
                .on('typeahead:select', function (event, item) {
                    ngModel.$setViewValue(item.value, 'typeahead:select');
                    scope.$apply(function (scope) {
                        scope.onSelect({item: item});
                    });
                });

            ngModel.$render = function () {
                element.typeahead('val', ngModel.$viewValue);
            };

        }

        return directive;
    }
}());