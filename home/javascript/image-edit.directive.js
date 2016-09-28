;(function() {
    angular
        .module('app')
        .directive('imageEdit', imageEdit);

    function imageEdit() {
        var template =
            '<p class="image-edit-block">' +
            '  <span ng-show="vm.figure.images.length == 0" class="text-muted">No images yet</span>' +
            '  <span class="image-edit-image" ng-repeat="image in vm.figure.images">' +
            '    <img ng-src="{{image.thumbnailPath}}">' +
            '    <span class="image-delete-button" ng-click="vm.deleteImage(image, $index)">' +
            '      <span class="fa-stack fa-lg">' +
            '        <i class="fa fa-circle fa-stack-1x"></i>' +
            '        <i class="fa fa-times-circle fa-stack-1x"></i>' +
            '      </span>' +
            '    </span>' +
            '  </span>' +
            '  <input type="file" ng-attr-id="file-{{::$id}}" class="image-add-input" accept="image/*">' +
            '  <label ng-show="!vm.figure.uploading" ng-attr-for="file-{{::$id}}" class="image-add-label">+</label>' +
            '  <span ng-show="vm.figure.uploading" class="image-add-uploading"><i class="fa fa-spinner fa-spin"></i></span>' +
            '</p>' +
            '<span class="error" ng-show="vm.imageError">{{vm.imageError}}</span>' +
            '<div inline-edit-textarea text="vm.figure.caption" default-text="Add caption" on-save="vm.updateFigure()"></div>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                figure: '='
            },
            link: link,
            controller: ImageEditController,
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

    ImageEditController.$inject = ['FigureService'];
    function ImageEditController(FigureService) {
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