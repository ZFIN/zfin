;(function() {
    angular
        .module('app')
        .directive('figureUpload', figureUpload);

    function figureUpload() {
        var template =
            '<form class="form-horizontal">' +
            '    <div class="form-group">' +
            '        <label class="col-sm-2 control-label">Label</label>' +
            '        <div class="col-sm-6">' +
            '            Fig. <input class="form-control form-control-fixed-width-sm" ng-model="vm.label">' +
            '        </div>' +
            '    </div>' +
            '    <div class="form-group">' +
            '        <label class="col-sm-2 control-label">Caption</label>' +
            '        <div class="col-sm-6">' +
            '            <textarea class="form-control" rows="6" ng-model="vm.caption"></textarea>' +
            '        </div>' +
            '    </div>' +
            '    <div class="form-group">' +
            '        <label class="col-sm-2 control-label">Images</label>' +
            '        <div class="col-sm-6">' +
            '            <div file-input files="vm.files" multiple="true"></div>' +
            '        </div>' +
            '    </div>' +
            '    <div class="form-group">' +
            '        <div class="col-sm-offset-2 col-sm-6">' +
            '            <button class="btn btn-primary" ng-click="vm.upload()" ng-disabled="vm.uploading">' +
            '              <span ng-show="!vm.uploading">Save</span>' +
            '              <span ng-show="vm.uploading"><i class="fa fa-spin fa-spinner"></i></span>' +
            '            </button>' +
            '        </div>' +
            '    </div>' +
            '</form>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                pubId: '@',
                figures: '='
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

        vm.upload = upload;

        function upload() {
            vm.uploading = true;
            FigureService.addFigure(vm.pubId, 'Fig. ' + vm.label, vm.caption, vm.files)
                .then(function (response) {
                    vm.figures.push(response.data);
                    vm.label = '';
                    vm.caption = '';
                    vm.files = [];
                })
                .finally(function () {
                    vm.uploading = false;
                });
        }
    }

}());