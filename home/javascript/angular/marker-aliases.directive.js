;(function () {
    angular
        .module('app')
        .directive('markerAliases', markerAliases);

    function markerAliases() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/marker-aliases.directive.html',
            scope: {
                markerId: '@',
                name: '@'
            },
            controller: MarkerAliasesController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    MarkerAliasesController.$inject = ['MarkerService', 'FieldErrorService'];
    function MarkerAliasesController(MarkerService, FieldErrorService) {

        var vm = this;

        vm.newAlias = '';
        vm.newReference = '';

        vm.editing = null;
        vm.aliases = [];
        vm.errors = {};
        vm.processing = false;

        vm.remove = remove;
        vm.edit = edit;
        vm.add = add;
        vm.addReference = addReference;
        vm.removeReference = removeReference;
        vm.closeEditModal = closeEditModal;

        activate();

        function activate() {
            MarkerService.getAliases(vm.markerId)
                .then(function (aliases) {
                    vm.aliases = aliases;
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function add() {
            vm.processing = true;
            MarkerService.addAlias(vm.markerId, vm.newAlias, vm.newReference)
                .then(function (alias) {
                    vm.aliases.unshift(alias);
                    vm.newAlias = '';
                    vm.newReference = '';
                    vm.errors = {};
                })
                .catch(function (error) {
                    vm.errors = FieldErrorService.processErrorResponse(error);
                })
                .finally(function () {
                    vm.processing = false;
                });
        }

        function edit(alias) {
            vm.editing = alias;
        }

        function addReference(pubId) {
            return MarkerService.addAliasReference(vm.editing, pubId)
                .then(function (alias) {
                    vm.editing.references = alias.references;
                });
        }

        function removeReference(reference, index) {
            return MarkerService.removeAliasReference(vm.editing, reference)
                .then(function () {
                    vm.editing.references.splice(index, 1);
                });
        }

        function remove(alias, index) {
            MarkerService.removeAlias(alias)
                .then(function () {
                    vm.aliases.splice(index, 1);
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function closeEditModal() {
            vm.editing = null;
        }

    }
}());
