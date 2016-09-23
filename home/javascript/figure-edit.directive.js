;(function() {
    angular
        .module('app')
        .directive('figureEdit', figureEdit);

    function figureEdit() {
        var template =
            '<table class="table">' +
            '  <thead>' +
            '  <tr>' +
            '    <th width="75px">Label</th>' +
            '    <th>Details</th>' +
            '    <th width="75px">Remove</th>' +
            '  </tr>' +
            '  </thead>' +
            '  <tbody>' +
            '  <tr ng-repeat="figure in vm.figures">' +
            '    <td>{{figure.label}}</td>' +
            '    <td>' +
            '      <p ng-if="figure.images.length > 0">' +
            '        <span ng-repeat="image in figure.images">' +
            '          <img ng-src="{{image.thumbnailPath}}">' +
            '        </span>' +
            '      </p>' +
            '      <p ng-bind-html="figure.caption | trustedHtml"></p>' +
            '    </td>' +
            '    <td><a href class="pull-right"><i class="fa fa-trash"></i></a></td>' +
            '  </tr>' +
            '  </tbody>' +
            '</table>' +
            '<h4>Create New Figure</h4>' +
            '<div figure-upload pub-id="{{vm.pubId}}" figures="vm.figures"></div>';

        var directive = {
            restrict: 'AE',
            template: template,
            scope: {
                pubId: '@'
            },
            controller: FigureEditController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    FigureEditController.$inject = ['FigureService'];
    function FigureEditController(FigureService) {
        var vm = this;

        vm.figures = [];

        activate();

        function activate() {
            FigureService.getFigures(vm.pubId)
                .then(function (response) {
                    vm.figures = response.data;
                });
        }
    }
}());
