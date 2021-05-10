;(function() {
    angular
        .module('app')
        .directive('figureUpload', figureUpload);

    function figureUpload() {
        var template =
            '<form class="form-horizontal">' +
            '    <div class="form-group row">' +
            '        <label class="col-md-2 col-form-label">Label</label>' +
            '        <div class="col-md-6">' +
            '            Fig. <input class="form-control form-control-fixed-width-sm" ng-model="vm.label">' +
            '        </div>' +
            '    </div>' +
            '    <div ng-show="vm.hasPermissions">' +
            '      <div class="form-group row">' +
            '          <label class="col-md-2 col-form-label">Caption</label>' +
            '          <div class="col-md-6">' +
            '              <textarea class="form-control" rows="6" ng-model="vm.caption"></textarea>' +
            '          </div>' +
            '      </div>' +
            '      <div class="form-group row">' +
            '          <label class="col-md-2 col-form-label">Images</label>' +
            '          <div class="col-md-6">' +
            '              <div file-input files="vm.files" multiple="true" accept="image/*" error-message="vm.errorMessage"></div>' +
            '          </div>' +
            '      </div>' +
            '    </div>' +
            '    <div ng-show="!vm.hasPermissions">' +
            '      <div class="row">' +
            '        <div class="offset-md-2 col-md-6">' +
            '          <div class="alert alert-warning">' +
            '            Publication\'s journal does not grant automatic permission to display captions and images. If ' +
            '            this publication has permission, indicate so in the <a href="#details">Details</a> tab.' +
            '          </div>' +
            '        </div>' +
            '      </div>' +
            '    </div>' +
            '    <div class="form-group row">' +
            '        <div class="offset-md-2 col-md-6">' +
            '            <button class="btn btn-primary" ng-click="vm.upload()" ng-disabled="!vm.label || vm.uploading">' +
            '              <span ng-show="!vm.uploading">Save</span>' +
            '              <span ng-show="vm.uploading"><i class="fas fa-spin fa-spinner"></i></span>' +
            '            </button>' +
            '            <span class="text-danger" ng-show="vm.errorMessage">{{vm.errorMessage}}</span>' +
            '        </div>' +
            '    </div>' +
            '</form>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                pubId: '@',
                figures: '=',
                hasPermissions: '='
            },
            controller: FigureUploadController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    FigureUploadController.$inject = ['FigureService'];
    function FigureUploadController(FigureService) {
        var vm = this;

        vm.label = '';
        vm.caption = '';
        vm.files = [];
        vm.uploading = false;
        vm.errorMessage = '';

        vm.upload = upload;

        function upload() {
            vm.uploading = true;
            FigureService.addFigure(vm.pubId, 'Fig. ' + vm.label, vm.caption, vm.files)
                .then(function (response) {
                    vm.figures.push(response.data);
                    vm.label = '';
                    vm.caption = '';
                    vm.files = [];
                    vm.errorMessage = '';
                })
                .catch(function (response) {
                    if (response.data && response.data.message) {
                        vm.errorMessage = response.data.message;
                    }
                })
                .finally(function () {
                    vm.uploading = false;
                });
        }
    }

}());