;(function() {
    angular
        .module('app')
        .directive('fileInput', fileInput);

    function fileInput() {
        var template =
            '<div class="file-drag-target">' +
            '  <ul class="list-unstyled">' +
            '    <li ng-repeat="file in vm.files">{{file.name}}</li>' +
            '  </ul>' +
            '  <input type="file" id="file" multiple />' +
            '  <label for="file"><strong>Choose a file</strong></label> or drag it here.' +
            '</div>';

        var directive = {
            restrict: 'A',
            template: template,
            scope: {
                files: '=',
                multiple: '='
            },
            link: link,
            controller: FileInputController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link(scope, element, attrs) {
            var input = element.find('input');
            var dragTarget = element.find('.file-drag-target');

            input.on('change', function () {
                scope.$apply(function () {
                    scope.vm.files = input[0].files;
                });
            });

            dragTarget
                .on('dragover', function (evt) {
                    evt.preventDefault();
                    $(this).addClass('hover');
                })
                .on('dragleave', function (evt) {
                    evt.preventDefault();
                    $(this).removeClass('hover');
                })
                .on('drop', function (evt) {
                    evt.preventDefault();
                    $(this).removeClass('hover');
                    scope.$apply(function () {
                        scope.vm.files = evt.originalEvent.dataTransfer.files;
                    });
                });
        }

        return directive;
    }

    function FileInputController() {
    }
}());