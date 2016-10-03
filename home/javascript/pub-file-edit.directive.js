;(function() {
    angular
        .module('app')
        .directive('pubFileEdit', pubFileEdit);

    function pubFileEdit() {
        var template =
            '<table class="table">' +
            '  <thead>' +
            '  <tr>' +
            '    <th width="125px">Type</th>' +
            '    <th>File</th>' +
            '    <th width="75px">Remove</th>' +
            '  </tr>' +
            '  </thead>' +
            '  <tbody>' +
            '  <tr ng-show="vm.loading">' +
            '      <td class="text-muted text-center" colspan="3"><i class="fa fa-spinner fa-spin"></i> Loading...</td>' +
            '  </tr>' +
            '  <tr ng-show="!vm.loading && vm.files.length == 0">' +
            '    <td class="text-muted text-center" colspan="3">No files yet.</td>' +
            '  </tr>' +
            '  <tr ng-repeat="file in vm.files">' +
            '    <td>{{file.type}}</td>' +
            '    <td>{{file.fileName}}</td>' +
            '    <td>' +
            '      <div class="figure-delete-button pull-right">' +
            '        <button class="btn btn-dense btn-link" title="Remove file">' +
            '          <i class="fa fa-trash"></i>' +
            '        </button>' +
            '      </div>' +
            '    </td>' +
            '  </tr>' +
            '  </tbody>' +
            '</table>' +
            '<h4>Upload New File</h4>' +
            '<div pub-file-upload pub-id="{{vm.pubId}}" files="vm.files"></div>';

        var directive = {
            restrict: 'AE',
            template: template,
            scope: {
                pubId: '@'
            },
            //link: link,
            controller: PubFileEditController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PubFileEditController.$inject = ['PublicationService'];
    function PubFileEditController(PublicationService) {
        var vm = this;

        vm.files = [];

        activate();

        function activate() {
            PublicationService.getFiles(vm.pubId)
                .then(function (response) {
                    vm.files = response.data;
                });
        }
    }
}());