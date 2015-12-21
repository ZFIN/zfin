;(function() {
    angular
        .module('app')
        .directive('referenceEditor', referenceEditor);

    function referenceEditor() {
        // probably pushing the limits of an inline template
        var template =
            '<div bootstrap-modal show="vm.show">' +
            '  <div class="modal-header">' +
            '    <button type="button" class="close" ng-click="vm.close()">&times;</button>' +
            '    <h4 class="modal-title">References for {{vm.entity}}</h4>' +
            '  </div>' +
            '  <div class="modal-body">' +
            '    <form class="form-horizontal">' +
            '      <div class="form-group">' +
            '        <div class="col-sm-10">' +
            '          <input pub-lookup placeholder="Add Reference" class="form-control" ng-model="vm.newReference"/>' +
            '        </div>' +
            '        <div class="col-sm-2">' +
            '          <button type="button" class="btn btn-primary" ng-click="vm.add()" ng-disabled="!vm.newReference">' +
            '            <span ng-hide="vm.processing">Add</span>' +
            '            <i ng-show="vm.processing" class="fa fa-spinner fa-spin"></i>' +
            '          </button>' +
            '        </div>' +
            '      </div>' +
            '    </form>' +
            '    <p class="text-danger" ng-repeat="error in vm.errors.zdbID">{{error}}</p>' +
            '    <div class="row">' +
            '      <div class="col-sm-10">' +
            '        <table class="table">' +
            '          <tr ng-repeat="reference in vm.references">' +
            '            <td>' +
            '              <span class="small pull-right" ng-show="vm.references.length > 1">' +
            '                <a href ng-click="vm.onRemove({reference: reference, index: $index})">Remove</a>' +
            '              </span>' +
            '              {{reference.zdbID}}<br>' +
            '              <span class="text-muted">{{reference.title}}</span>' +
            '            </td>' +
            '          </tr>' +
            '        </table>' +
            '      </div>' +
            '    </div>' +
            '  </div>' +
            '</div>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                entity: '=',
                show: '=',
                references: '=',
                onAdd: '&',     // onAdd and onRemove are expected to return promises
                onRemove: '&',
                onClose: '&'
            },
            controller: ReferenceEditorController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    ReferenceEditorController.$inject = ['FieldErrorService'];
    function ReferenceEditorController(FieldErrorService) {

        var vm = this;

        vm.newReference = '';
        vm.errors = {};
        vm.processing = false;
        vm.add = add;
        vm.close = close;

        function add() {
            vm.processing = true;
            vm.onAdd({pubId: vm.newReference})
                .then(function() {
                    vm.newReference = '';
                    vm.errors = {};
                })
                .catch(function(error) {
                    vm.errors = FieldErrorService.processErrorResponse(error);
                })
                .finally(function() {
                    vm.processing = false;
                });
        }

        function close() {
            vm.onClose();
            vm.newReference = '';
            vm.errors = {};
            vm.processing = false;
        }

    }
}());
