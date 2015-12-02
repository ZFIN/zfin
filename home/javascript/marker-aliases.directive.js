;(function() {
    angular
        .module('app')
        .directive('markerAliases', markerAliases);

    function markerAliases() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/marker-aliases.directive.html',
            scope: {
                id: '@',
                name: '@'
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
        vm.editReference = '';

        vm.editing = null;
        vm.aliases = [];

        vm.remove = remove;
        vm.edit = edit;
        vm.add = add;
        vm.addReference = addReference;
        vm.removeReference = removeReference;
        vm.closeEditModal = closeEditModal;

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
                    vm.aliases.unshift(alias);
                    vm.newAlias = '';
                    vm.newReference = '';
                })
        }

        function edit(alias) {
            vm.editing = alias;
        }

        function addReference() {
            MarkerService.addAliasReference(vm.editing, vm.editReference)
                .then(function(alias) {
                    vm.editing.references = alias.references;
                    vm.editReference = '';
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function removeReference(reference, index) {
            MarkerService.removeAliasReference(vm.editing, reference)
                .then(function() {
                    vm.editing.references.splice(index, 1);
                })
                .catch(function(error) {
                    console.error(error);
                });
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

        function closeEditModal() {
            vm.editReference = '';
            vm.editing = null;
        }

    }
}());