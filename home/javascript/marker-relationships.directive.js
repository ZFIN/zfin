;(function () {
    angular
        .module('app')
        .directive('markerRelationships', markerRelationships);

    function markerRelationships() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/marker-relationships.directive.html',
            scope: {
                markerId: '@',
                relationship: '@',
                relativeName: '@'
            },
            controller: MarkerRelationshipsController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    MarkerRelationshipsController.$inject = ['MarkerService', 'FieldErrorService'];
    function MarkerRelationshipsController(MarkerService, FieldErrorService) {

        var vm = this;

        vm.relationships = [];

        vm.newGene = '';
        vm.newReference = '';
        vm.errors = {};
        vm.processing = false;

        vm.add = add;
        vm.remove = remove;
        vm.edit = edit;
        vm.addReference = addReference;
        vm.removeReference = removeReference;
        vm.closeEditModal = closeEditModal;

        activate();

        function activate() {
            MarkerService.getRelationships(vm.markerId)
                .then(function (relationships) {

                    vm.relationships = relationships.filter(function (relationship) {
                        return relationship.relationship === vm.relationship;
                    });
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function add() {
            vm.processing = true;
            var first = {zdbID: vm.markerId};
            var second = {name: vm.newGene};
            MarkerService.addRelationship(first, second, vm.relationship, vm.newReference)
                .then(function (relationship) {
                    vm.relationships.unshift(relationship);
                    vm.newGene = '';
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

        function remove(relationship, index) {
            MarkerService.removeRelationship(relationship)
                .then(function () {
                    vm.relationships.splice(index, 1);
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function edit(relationship) {
            vm.editing = relationship;
        }

        function addReference(pubId) {
            return MarkerService.addRelationshipReference(vm.editing, pubId)
                .then(function (relationship) {
                    vm.editing.references = relationship.references;
                });
        }

        function removeReference(reference, index) {
            return MarkerService.removeRelationshipReference(vm.editing, reference)
                .then(function () {
                    vm.editing.references.splice(index, 1);
                });
        }

        function closeEditModal() {
            vm.editing = null;
        }

    }
}());