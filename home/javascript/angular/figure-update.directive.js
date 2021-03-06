;(function() {
    angular
        .module('app')
        .directive('figureUpdate', figureUpdate);

    function figureUpdate() {
        var template =
            '<div ng-show="vm.hasPermissions">' +
            '  <p class="image-edit-block">' +
            '    <span ng-show="vm.figure.images.length == 0" class="text-muted">No images yet</span>' +
            '    <span class="image-edit-image" ng-repeat="image in vm.figure.images">' +
            '      <img ng-src="{{image.thumbnailPath}}">' +
            '      <span class="image-delete-button" ng-click="vm.deleteImage(image, $index)" title="Remove image">' +
            '        <span class="fa-stack fa-lg">' +
            '          <i class="fas fa-circle fa-stack-1x"></i>' +
            '          <i class="fas fa-times-circle fa-stack-1x"></i>' +
            '        </span>' +
            '      </span>' +
            '    </span>' +
            '    <input type="file" ng-attr-id="file-{{::$id}}" class="image-add-input" accept="image/*">' +
            '    <label ng-show="!vm.figure.uploading" ng-attr-for="file-{{::$id}}" class="image-add-label" title="Add image">+</label>' +
            '    <span ng-show="vm.figure.uploading" class="image-add-uploading"><i class="fas fa-spinner fa-spin"></i></span>' +
            '  </p>' +
            '  <span class="text-danger" ng-show="vm.imageError">{{vm.imageError}}</span>' +
            '  <div inline-edit-textarea text="vm.figure.caption" default-text="Add caption" on-save="vm.updateFigure()"></div>' +
            '</div>' +
            '<div ng-show="!vm.hasPermissions">' +
            '  <span class="text-muted">Cannot add images and caption without permissions.</span>' +
            '</div>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                figure: '=',
                hasPermissions: '='
            },
            link: link,
            controller: FigureUpdateController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link(scope, element) {
            element.find('.image-add-input').on('change', function () {
                scope.vm.addImage(this.files[0]);
            });
        }

        return directive;
    }

    FigureUpdateController.$inject = ['FigureService'];
    function FigureUpdateController(FigureService) {
        var vm = this;

        vm.imageError = '';

        vm.addImage = addImage;
        vm.updateFigure = updateFigure;
        vm.deleteImage = deleteImage;

        function addImage(file) {
            vm.figure.uploading = true;
            FigureService.addImage(vm.figure, file)
                .then(function (response) {
                    vm.imageError = '';
                    vm.figure.images.push(response.data);
                })
                .catch(function (response) {
                    if (response.data && response.data.message) {
                        vm.imageError = response.data.message;
                    }
                })
                .finally(function () {
                    vm.figure.uploading = false;
                })
        }

        function deleteImage(img, idx) {
            FigureService.deleteImage(img)
                .then(function () {
                    vm.imageError = '';
                    vm.figure.images.splice(idx, 1);
                });
        }

        function updateFigure() {
            return FigureService.updateFigure(vm.figure)
                .then(function (response) {
                    vm.imageError = '';
                    angular.copy(response.data, vm.figure);
                });
        }
    }

}());