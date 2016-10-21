;(function() {
    angular
        .module('app')
        .directive('pubFileUpload', pubFileUpload);

    function pubFileUpload() {
        var template =
            '<form class="form-horizontal">' +
            '    <div class="form-group">' +
            '        <label class="col-sm-2 control-label">Type</label>' +
            '        <div class="col-sm-3">' +
            '            <select class="form-control" ng-model="vm.type" ng-options="type.name for type in vm.fileTypes track by type.id"></select>' +
            '            <p class="text-warning" ng-show="vm.checkOriginalArticle()">' +
            '                Pub can have only one original article. The existing file will be replaced.' +
            '            </p>' +
            '        </div>' +
            '    </div>' +
            '    <div class="form-group">' +
            '        <label class="col-sm-2 control-label">File</label>' +
            '        <div class="col-sm-6">' +
            '            <div file-input files="vm.file" multiple="false" error-message="vm.errorMessage"></div>' +
            '        </div>' +
            '    </div>' +
            '    <div class="form-group">' +
            '        <div class="col-sm-offset-2 col-sm-6">' +
            '            <button class="btn btn-primary" ng-click="vm.upload()" ng-disabled="!vm.readyToUpload() || vm.uploading">' +
            '              <span ng-show="!vm.uploading">Save</span>' +
            '              <span ng-show="vm.uploading"><i class="fa fa-spin fa-spinner"></i></span>' +
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
                files: '='
            },
            controller: PubFileUploadController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PubFileUploadController.$inject = ['PublicationService', 'ZfinUtils'];
    function PubFileUploadController(PublicationService, zf) {
        var vm = this;

        vm.fileTypes = [];
        vm.errorMessage = '';
        vm.uploading = false;

        vm.file = [];
        vm.type = null;

        vm.upload = upload;
        vm.readyToUpload = readyToUpload;
        vm.checkOriginalArticle = checkOriginalArticle;

        activate();

        function activate() {
            PublicationService.getFileTypes()
                .then(function (response) {
                    vm.fileTypes = response.data;
                });
        }

        function upload() {
            vm.uploading = true;
            PublicationService.addFile(vm.pubId, vm.type.id, vm.file[0])
                .then(function (response) {
                    vm.files = response.data;
                    vm.type = null;
                    vm.file = [];
                })
                .catch(function (response) {
                    vm.errorMessage = response.data.message;
                })
                .finally(function () {
                    vm.uploading = false;
                });
        }

        function readyToUpload() {
            return vm.type !== null && vm.file.length > 0;
        }

        function checkOriginalArticle() {
            var orig = 'Original Article';
            return vm.type &&
                vm.type.name === orig &&
                zf.find(vm.files, function (f) { return f.type === orig; });
        }
    }
}());