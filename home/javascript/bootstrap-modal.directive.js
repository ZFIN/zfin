;(function() {
    angular
        .module('app')
        .directive('bootstrapModal', bootstrapModal);

    function bootstrapModal() {
        var template =
            '<div class="modal fade" data-backdrop="static">' +
            '  <div class="modal-dialog">' +
            '    <div class="modal-content" ng-transclude></div>' +
            '  </div>' +
            '</div>';
        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                title: '@',
                show: '='
            },
            transclude: true,
            link: link
        };

        function link(scope, element) {
            var $modal = element.find('.modal');
            scope.$watch('show', function(value) {
                if (value) {
                    $modal.modal('show');
                } else {
                    $modal.modal('hide');
                }
            });
        }

        return directive;
    }
}());
