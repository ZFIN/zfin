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
            '      <p class="image-edit-block" ng-if="figure.images.length > 0">' +
            '        <span class="image-edit-image" ng-repeat="image in figure.images">' +
            '          <img ng-src="{{image.thumbnailPath}}">' +
            '          <span class="image-delete-button" ng-click="vm.deleteImage(image, figure, $index)">' +
            '            <span class="fa-stack fa-2x">' +
            '              <i class="fa fa-circle fa-stack-1x"></i>' +
            '              <i class="fa fa-times-circle fa-stack-1x"></i>' +
            '            </span>' +
            '          </span>' +
            '        </span>' +
            '      </p>' +
            '      <div inline-edit-textarea text="figure.caption" default-text="Add caption" on-save="vm.updateFigure(figure, $index)"></div>' +
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
            '<div figure-upload pub-id="{{vm.pubId}}" figures="vm.figures"></div>';

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
                // timeout so that the data attribues are settled
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

        vm.updateFigure = updateFigure;
        vm.deleteFigure = deleteFigure;
        vm.deleteImage = deleteImage;

        activate();

        function activate() {
            FigureService.getFigures(vm.pubId)
                .then(function (response) {
                    vm.figures = response.data;
                });
        }

        function updateFigure(fig, idx) {
            return FigureService.updateFigure(fig)
                .then(function (response) {
                    vm.figures[idx] = response.data;
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

        function deleteImage(img, fig, idx) {
            FigureService.deleteImage(img)
                .then(function () {
                    fig.images.splice(idx, 1);
                });
        }
    }
}());
