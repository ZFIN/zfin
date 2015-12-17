;(function() {
    angular
        .module('app')
        .directive('markerLinks', markerLinks);

    function markerLinks() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/marker-links.directive.html',
            scope: {
                id: '@',
                group: '@'
            },
            controller: MarkerLinksController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    MarkerLinksController.$inject = ['MarkerService', 'FieldErrorService'];
    function MarkerLinksController(MarkerService, FieldErrorService) {

        var vm = this;

        vm.newDatabase = '';
        vm.newAccession = '';
        vm.newReference = '';

        vm.links = [];
        vm.databases = [];
        vm.errors = {};
        vm.processing = false;

        vm.editing = null;

        vm.add = add;
        vm.remove = remove;
        vm.edit = edit;
        vm.addReference = addReference;
        vm.removeReference = removeReference;
        vm.closeEditModal = closeEditModal;

        activate();

        function activate() {
            MarkerService.getLinks(vm.id, vm.group)
                .then(function(links) {
                    vm.links = links;
                })
                .catch(function(error) {
                    console.error(error);
                });

            MarkerService.getLinkDatabases(vm.group)
                .then(function(databases) {
                    vm.databases = databases;
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function add() {
            vm.processing = true;
            MarkerService.addLink(vm.id, vm.newDatabase, vm.newAccession, vm.newReference)
                .then(function(link) {
                    vm.links.unshift(link);
                    vm.newDatabase = '';
                    vm.newAccession = '';
                    vm.newReference = '';
                    vm.errors = {};
                })
                .catch(function(error) {
                    vm.errors = FieldErrorService.processErrorResponse(error);
                })
                .finally(function() {
                    vm.processing = false;
                })
        }

        function remove(link, index) {
            MarkerService.removeLink(link)
                .then(function() {
                    vm.links.splice(index, 1);
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function edit(link) {
            vm.editing = link;
        }

        function closeEditModal() {
            vm.editing = null;
        }

        function addReference(pubId) {
            return MarkerService.addLinkReference(vm.editing, pubId)
                .then(function(link) {
                    vm.editing.references = link.references;
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function removeReference(reference, index) {
            return MarkerService.removeLinkReference(vm.editing, reference)
                .then(function() {
                    vm.editing.references.splice(index, 1);
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

    }
}());