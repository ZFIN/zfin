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
            '  <tr ng-show="vm.loading">' +
            '      <td class="text-muted text-center" colspan="3"><i class="fa fa-spinner fa-spin"></i> Loading...</td>' +
            '  </tr>' +
            '  <tr ng-show="!vm.loading && vm.figures.length == 0">' +
            '    <td class="text-muted text-center" colspan="3">No figures yet.</td>' +
            '  </tr>' +
            '  <tr ng-repeat="figure in vm.figures">' +
            '    <td>' +
            '      <div inline-edit-textarea text="figure.label" use-icons="true" use-input="true" error="figure.error"' +
            '           error-class="\'error\'" wrapper-class="\'fig-label-edit-container\'" on-save="vm.updateFigure(figure)"></div>' +
            '    </td>' +
            '    <td>' +
            '      <div figure-update figure="figure" has-permissions="vm.pubCanShowImages"></div>' +
            '    </td>' +
            '    <td>' +
            '      <div class="figure-delete-button pull-right" data-toggle="tooltip"' +
            '           ng-attr-data-expr-count={{figure.numExpressionStatements}}' +
            '           ng-attr-data-pheno-count={{figure.numPhenotypeStatements}}>' +
            '        <button class="btn btn-dense btn-link" ng-click="vm.deleteFigure(figure, $index)" title="Remove figure"' +
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

    FigureEditController.$inject = ['$q', 'FigureService'];
    function FigureEditController($q, FigureService) {
        var vm = this;

        vm.loading = false;
        vm.figures = [];
        vm.pubCanShowImages = false;

        vm.deleteFigure = deleteFigure;
        vm.updateFigure = updateFigure;

        activate();

        function activate() {
            vm.loading = true;
            FigureService.getFigures(vm.pubId)
                .then(function (response) {
                    vm.figures = response.data.figures;
                    vm.pubCanShowImages = response.data.pubCanShowImages;
                })
                .finally(function () {
                    vm.loading = false;
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

        function updateFigure(figure) {
            if (!figure.label) {
                figure.error = 'Label is required';
                return $q.reject();
            }
            return FigureService.updateFigure(figure)
                .then(function (response) {
                    angular.copy(response.data, figure);
                });
        }
    }
}());
