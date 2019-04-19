;(function () {
    angular
        .module('app')
        .directive('selectAllList', selectAllList);

    function selectAllList() {
        var template =
            '<ul class="list-unstyled">' +
            '  <li ng-show="vm.items.length > 0">' +
            '    <div class="checkbox">' +
            '      <label>' +
            '        <input type="checkbox" ng-model="vm.allSelected" ng-change="vm.toggleAll()">' +
            '        <b>{{ vm.allLabel }}</b>' +
            '      </label>' +
            '    </div>' +
            '  </li>' +
            '  <li ng-repeat="item in vm.items">' +
            '    <div class="checkbox">' +
            '      <label>' +
            '        <input type="checkbox" ng-model="item.selected" ng-change="vm.updateAllSelected()">' +
            '        <span ng-bind-html="vm.safeItemLabel({item: item})">' +
            '      </label>' +
            '    </div>' +
            '  </li>' +
            '</ul>';

        var directive = {
            restrict: 'EA',
            template: template,
            // for details on directive scopes:
            // http://www.undefinednull.com/2014/02/11/mastering-the-scope-of-a-directive-in-angularjs/
            scope: {
                items: '=',
                allLabel: '@',
                itemLabel: '&'
            },
            controller: SelectAllListController,
            controllerAs: 'vm',
            bindToController: true,
            link: link
        };

        return directive;
    }

    SelectAllListController.$inject = ['$sce'];
    function SelectAllListController($sce) {
        var vm = this;

        vm.toggleAll = toggleAll;
        vm.updateAllSelected = updateAllSelected;
        vm.safeItemLabel = safeItemLabel;

        function toggleAll() {
            vm.items.forEach(function (i) {
                i.selected = vm.allSelected;
            });
        }

        function updateAllSelected() {
            vm.allSelected = vm.items.every(function(i) {
                return i.selected;
            });
        }

        function safeItemLabel(locals) {
            return $sce.trustAsHtml(vm.itemLabel(locals));
        }
    }

    function link(scope) {
        var unwatch = scope.$watch('vm.items', function(newValue) {
            if (newValue) {
                scope.vm.updateAllSelected();
                unwatch();
            }
        });
    }
}());