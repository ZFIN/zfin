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
            '        <div class="col-sm-offset-2 col-sm-8">' +
            '          <input pub-lookup placeholder="Add Reference" class="form-control" ng-model="vm.newReference"/>' +
            '        </div>' +
            '        <div class="col-sm-2">' +
            '          <button type="button" class="btn btn-primary" ng-click="vm.add()" ng-disabled="!vm.newReference">Add</button>' +
            '        </div>' +
            '      </div>' +
            '    </form>' +
            '    <div class="row">' +
            '      <div class="col-sm-offset-2 col-sm-8">' +
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

    function ReferenceEditorController() {

        var vm = this;

        vm.newReference = '';
        vm.add = add;
        vm.close = close;

        function add() {
            vm.onAdd({pubId: vm.newReference})
                .then(function() {
                    vm.newReference = '';
                });
        }

        function close() {
            vm.onClose();
            vm.newReference = '';
        }

    }
}());
