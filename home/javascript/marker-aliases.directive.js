;(function() {
    angular
        .module('app')
        .directive('markerAliases', markerAliases);

    function markerAliases() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/marker-aliases.directive.html',
            scope: {
                id: '@'
            },
            controller: MarkerAliasesController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    MarkerAliasesController.$inject = ['MarkerService'];
    function MarkerAliasesController(MarkerService) {

        var vm = this;

        vm.newAlias = '';
        vm.newReference = '';
        vm.aliases = [];
        vm.defaultPubs = [
            {
                zdbID: 'ZDB-PUB-111111-1',
                title: 'The birch canoe slid on the smooth planks'
            },
            {
                zdbID: 'ZDB-PUB-111111-2',
                title: 'Glue the sheet to the dark blue background'
            },
            {
                zdbID: 'ZDB-PUB-111111-3',
                title: 'These days a chicken leg is a rare dish'
            }
        ];

        vm.remove = remove;
        vm.add = add;

        activate();

        function activate() {
            MarkerService.getAliases(vm.id)
                .then(function (aliases) {
                    vm.aliases = aliases;
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function add() {
            MarkerService.addAlias(vm.id, vm.newAlias, vm.newReference)
                .then(function(alias) {
                    vm.aliases.push(alias);
                    vm.newAlias = '';
                    vm.newReference = '';
                })
        }

        function remove(alias, index) {
            MarkerService.removeAlias(alias)
                .then(function() {
                    vm.aliases.splice(index, 1);
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

    }
}());