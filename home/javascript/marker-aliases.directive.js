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

        vm.newModalOpen = false;
        vm.editModalOpen = false;

        vm.newAlias = '';
        vm.newReference = '';
        vm.editing = null;
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
        vm.edit = edit;
        vm.add = add;
        vm.addReference = addReference;
        vm.removeReference = removeReference;
        vm.openAddModal = openAddModal;
        vm.closeAddModal = closeAddModal;
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
                    vm.closeAddModal();
                })
        }

        function edit(alias) {
            vm.editing = alias;
            vm.editModalOpen = true;
        }

        function addReference() {
            MarkerService.addAliasReference(vm.editing, vm.newReference)
                .then(function(alias) {
                    vm.editing.references = alias.references;
                    vm.newReference = '';
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

        function openAddModal() {
            vm.newModalOpen = true;
        }

        function closeAddModal() {
            vm.newModalOpen = false;
            vm.newAlias = '';
            vm.newReference = '';
        }

        function closeEditModal() {
            vm.editModalOpen = false;
            vm.newReference = '';
            vm.editing = null;
        }

    }
}());