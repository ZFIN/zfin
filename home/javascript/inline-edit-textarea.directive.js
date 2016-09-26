;(function() {
    angular
        .module('app')
        .directive('inlineEditTextarea', inlineEditTextarea);

    function inlineEditTextarea() {
        var template =
            '<div ng-class="{{vm.wrapperClass}}">' +
            '  <div ng-class="{{vm.errorClass}}" ng-show="vm.error">{{vm.error}}</div>' +
            '  <div ng-click="vm.beginEdit()" ng-hide="vm.editing" class="inline-edit" title="Click to edit">' +
            // TODO: flag for html vs plain text?
            //'    <div ng-show="vm.text" class="keep-breaks">{{vm.text}}</div>' +
            '    <div ng-show="vm.text" ng-bind-html="vm.text | trustedHtml"></div>' +
            '    <div ng-hide="vm.text" class="muted">{{vm.defaultText}}</div>' +
            '  </div>' +
            '  <div ng-show="vm.editing">' +
            '    <textarea ng-model="vm.text" ng-class="{{vm.textAreaClass}}" rows="5"></textarea>' +
            '    <button type="button" ng-click="vm.cancelEdit()" ng-class="{{vm.cancelButtonClass}}">Cancel</button>' +
            '    <button type="button" ng-click="vm.saveEdit()" ng-class="{{vm.saveButtonClass}}" ng-disabled="vm.saving">' +
            '      <span ng-show="!vm.saving">Save</span>' +
            '      <span ng-show="vm.saving"><i class="fa fa-spinner fa-spin"></i></span>' +
            '    </button>' +
            '  </div>' +
            '</div>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                text: '=',
                error: '=?',
                onSave: '&',
                wrapperClass: '@',
                errorClass: '@',
                defaultText: '@',
                textAreaClass: '@',
                saveButtonClass: '@',
                cancelButtonClass: '@'
            },
            controller: InlineEditTextareaController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    function InlineEditTextareaController() {
        var vm = this;

        vm.editing = false;
        vm.originalText = '';
        vm.saving = false;

        vm.beginEdit = beginEdit;
        vm.cancelEdit = cancelEdit;
        vm.saveEdit = saveEdit;

        activate();

        function activate() {
            vm.defaultText = vm.defaultText || 'Click to add';
            vm.errorClass = vm.errorClass || 'error';
            vm.textAreaClass = vm.textAreaClass || ['form-control', 'form-group'];
            vm.saveButtonClass = vm.saveButtonClass || ['btn', 'btn-primary'];
            vm.cancelButtonClass = vm.cancelButtonClass || ['btn', 'btn-default'];
        }

        function beginEdit() {
            vm.originalText = vm.text;
            vm.editing = true;
        }

        function cancelEdit() {
            vm.text = vm.originalText;
            exitEdit();
        }

        function saveEdit() {
            vm.saving = true;
            var save = vm.onSave();
            if (angular.isDefined(save) && angular.isFunction(save.finally)) {
                // we got a promise
                save.finally(function () {
                    exitEdit()
                });
            } else {
                // onSave() was synchronous or didn't return a promise
                exitEdit();
            }
        }

        function exitEdit() {
            vm.saving = false;
            vm.editing = false;
            vm.error = '';
        }

    }

}());