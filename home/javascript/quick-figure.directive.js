;(function() {
    angular
        .module('app')
        .directive('quickFigure', quickFigure);

    quickFigure.$inject = ['$compile'];
    function quickFigure($compile) {
        var template = '<a href data-toggle="popover" class="quick-fig small-new-link" ng-click="vm.toggle()">Add Figure</a>';
        var popoverTemplate =
            '<div class="quick-fig-content">' +
            '  <div>' +
            '    <button type="button" class="close" ng-click="vm.toggle()"><span aria-hidden="true">&times;</span></button>' +
            '    <h4>Quick Figure</h4>' +
            '    <form class="form-inline">' +
            '      <select class="form-control" ng-model="vm.type" ng-change="vm.label = \'\'" ng-options="t for t in vm.types">' +
            '      </select>' +
            '      <input type="text" class="form-control form-control-fixed-width-sm" ng-model="vm.label" ng-disabled="vm.type == \'text only\'">' +
            '      <button class="btn btn-primary" ng-click="vm.submit()" ng-disabled="vm.submitting || !vm.readyToSubmit()">' +
            '        <i ng-show="!vm.submitting" class="fa fa-check"></i>' +
            '        <i ng-show="vm.submitting" class="fa fa-spinner fa-spin"></i>' +
            '      </button>' +
            '    </form>' +
            '    <p class="text-success">{{vm.successMessage}}</p>' +
            '    <p class="text-danger" ng-show="vm.errorMessage">{{vm.errorMessage}}</p>' +
            '    <small><a ng-href="/action/publication/{{vm.pubId}}/edit#figures">Add figure with images and caption</a></small>' +
            '  </div>' +
            '</div>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                pubId: '@'
            },
            link: link,
            controller: QuickFigureController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link(scope, element) {
            scope.$watch('vm.open', function (val) {
                element.popover(val ? 'show' : 'hide');
            });

            $compile(popoverTemplate)(scope, function (popoverEl) {
                element.popover({
                    trigger: 'manual',
                    html: true,
                    placement: 'bottom',
                    content: function () {
                        return popoverEl;
                    }
                });
            });
        }

        return directive;
    }

    QuickFigureController.$inject = ['FigureService'];
    function QuickFigureController(FigureService) {
        var vm = this;

        vm.types = ['Fig.', 'text only', 'Table'];
        vm.open = false;
        vm.submitting = false;

        vm.toggle = toggle;
        vm.submit = submit;
        vm.readyToSubmit = readyToSubmit;

        activate();

        function activate() {
            reset();
        }

        function reset() {
            vm.type = vm.types[0];
            vm.label = '';
            vm.successMessage = '';
            vm.errorMessage = '';
        }

        function toggle() {
            vm.open = !vm.open;
            reset();
        }

        function submit() {
            var label = vm.type;
            if (label != 'text only') {
                label += ' ' + vm.label;
            }
            vm.submitting = true;
            FigureService.addFigure(vm.pubId, label, '')
                .then(function (response) {
                    vm.successMessage = response.data.label + " created";
                    vm.label = '';
                    vm.errorMessage = '';
                })
                .catch(function (response) {
                    vm.successMessage = '';
                    vm.errorMessage = response.data.message;
                })
                .finally(function () {
                    vm.submitting = false;
                });
        }

        function readyToSubmit() {
            return (vm.type == 'text only') || (vm.label != '');
        }
    }
}());