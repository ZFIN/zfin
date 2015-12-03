;(function() {
    angular
        .module('app')
        .directive('markerRelationships', markerRelationships);

    function markerRelationships() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/marker-relationships.directive.html',
            scope: {
                id: '@',
                relationship: '@',
                relativeName: '@'
            },
            controller: MarkerRelationshipsController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    MarkerRelationshipsController.$inject = ['MarkerService'];
    function MarkerRelationshipsController(MarkerService) {

        var vm = this;

        vm.relationships = [];

        vm.newGene = '';
        vm.newReference = '';

        vm.add = add;
        vm.remove = remove;
        vm.edit = edit;
        vm.addReference = addReference;
        vm.removeReference = removeReference;
        vm.closeEditModal = closeEditModal;

        activate();

        function activate() {
            MarkerService.getRelationships(vm.id)
                .then(function (relationships) {
                    vm.relationships = relationships.filter(function(relationship) {
                        return relationship.relationship === vm.relationship;
                    });
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function add() {
            var first = {zdbID: vm.id};
            var second = {name: vm.newGene};
            MarkerService.addRelationship(first, second, vm.relationship, vm.newReference)
                .then(function(relationship) {
                    vm.relationships.unshift(relationship);
                    vm.newGene = '';
                    vm.newReference = '';
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function remove(relationship, index) {
            MarkerService.removeRelationship(relationship)
                .then(function() {
                    vm.relationships.splice(index, 1);
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function edit(relationship) {
            vm.editing = relationship;
        }

        function addReference() {
            MarkerService.addRelationshipReference(vm.editing, vm.editReference)
                .then(function(relationship) {
                    vm.editing.references = relationship.references;
                    vm.editReference = '';
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function removeReference(reference, index) {
            MarkerService.removeRelationshipReference(vm.editing, reference)
                .then(function() {
                    vm.editing.references.splice(index, 1);
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function closeEditModal() {
            vm.editReference = '';
            vm.editing = null;
        }

    }
}());