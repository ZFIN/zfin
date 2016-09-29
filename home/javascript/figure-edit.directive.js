;(function() {
    angular
        .module('app')
        .directive('figureEdit', figureEdit);

    figureEdit.$inject = ['$timeout'];
    function figureEdit($timeout) {
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
            '      <div figure-update figure="figure" has-permissions="vm.pubCanShowImages"></div>' +
            '    </td>' +
            '    <td>' +
            '      <div class="figure-delete-button pull-right" data-toggle="tooltip"' +
            '           ng-attr-data-expr-count={{figure.numExpressionStatements}}' +
            '           ng-attr-data-pheno-count={{figure.numPhenotypeStatements}}>' +
            '        <button class="btn btn-dense btn-link" ng-click="vm.deleteFigure(figure, $index)"' +
            '                ng-disabled="figure.deleting || figure.numExpressionStatements || figure.numPhenotypeStatements">' +
            '          <i class="fa fa-trash"></i>' +
            '        </button>' +
            '      </div>' +
            '    </td>' +
            '  </tr>' +
            '  </tbody>' +
            '</table>' +
            '<h4>Create New Figure</h4>' +
            '<div figure-upload pub-id="{{vm.pubId}}" figures="vm.figures" has-permissions="vm.pubCanShowImages"></div>';

        var directive = {
            restrict: 'AE',
            template: template,
            scope: {
                pubId: '@'
            },
            link: link,
            controller: FigureEditController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link(scope, element) {
            scope.$watch('vm.figures', function () {
                // timeout so that the data attributes are settled
                $timeout(function() {
                    element.find('.figure-delete-button').each(function () {
                        var $button = angular.element(this);
                        var numExpr = $button.data('expr-count');
                        var numPheno = $button.data('pheno-count');
                        if (numExpr || numPheno) {
                            var title = 'This figure is used in ';
                            if (numExpr) {
                                title += '<b>' + numExpr + ' expression</b> ';
                            }
                            if (numExpr && numPheno) {
                                title += 'and ';
                            }
                            if (numPheno) {
                                title += '<b>' + numPheno + ' phenotype</b> ';
                            }
                            title += 'statements';
                            $button.tooltip({
                                title: title,
                                html: true,
                                placement: 'left'
                            });
                        }
                    });
                });
            });

        }

        return directive;
    }

    FigureEditController.$inject = ['FigureService'];
    function FigureEditController(FigureService) {
        var vm = this;

        vm.figures = [];
        vm.pubCanShowImages = false;

        vm.deleteFigure = deleteFigure;

        activate();

        function activate() {
            FigureService.getFigures(vm.pubId)
                .then(function (response) {
                    vm.figures = response.data.figures;
                    vm.pubCanShowImages = response.data.pubCanShowImages;
                });
        }

        function deleteFigure(fig, idx) {
            fig.deleting = true;
            FigureService.deleteFigure(fig)
                .then(function () {
                    vm.figures.splice(idx, 1);
                })
                .finally(function () {
                    fig.deleting = false;
                });
        }
    }
}());
