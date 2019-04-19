;(function() {
    angular
        .module('app')
        .directive('fileInput', fileInput);

    fileInput.$inject = ['ZfinUtils'];
    function fileInput(zf) {
        var template =
            '<div class="file-drag-target">' +
            '  <ul class="list-unstyled">' +
            '    <li ng-repeat="file in files">{{file.name}}</li>' +
            '  </ul>' +
            '  <input type="file" ng-attr-id="file-input-{{::$id}}" ng-attr-accept="{{accept}}"/>' +
            '  <label ng-attr-for="file-input-{{::$id}}"><strong>{{label}}</strong></label> or {{dragMsg}}.' +
            '</div>';

        var directive = {
            restrict: 'A',
            template: template,
            scope: {
                files: '=',
                errorMessage: '=',
                accept: '@'
            },
            link: link
        };

        function link(scope, element, attrs) {
            var input = element.find('input');
            var dragTarget = element.find('.file-drag-target');

            scope.multiple = scope.$eval(attrs.multiple);
            if (scope.multiple) {
                input.attr('multiple', 'multiple');
            }

            scope.label = scope.multiple ? 'Choose files' : 'Choose a file';
            scope.dragMsg = scope.multiple ? 'drag them here' : 'drag it here';

            scope.accept = scope.accept || '';

            function applyFilesToScope(files) {
                var validFiles = [];
                var invalidFiles = [];
                var errorMessage = '';
                var validType = scope.accept.replace(/\*$/, '');
                for (var i = 0; i < files.length; i++) {
                    if (files[i].type.substr(0, validType.length) == validType) {
                        validFiles.push(files[i]);
                    } else {
                        invalidFiles.push(files[i]);
                    }
                }
                if (!zf.isEmpty(invalidFiles)) {
                    errorMessage = "Invalid file type: " + invalidFiles.map(function (f) { return f.name; }).join(', ');
                }
                scope.$apply(function () {
                    if (!scope.multiple) {
                        validFiles.splice(1, validFiles.length - 1);
                    }
                    scope.files = validFiles;
                    scope.errorMessage = errorMessage;
                });
            }

            input.on('change', function () {
                applyFilesToScope(input[0].files);
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
                    applyFilesToScope(evt.originalEvent.dataTransfer.files);
                });
        }

        return directive;
    }
}());