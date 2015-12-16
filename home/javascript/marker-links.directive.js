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

    MarkerLinksController.$inject = ['MarkerService'];
    function MarkerLinksController(MarkerService) {

        var vm = this;

        vm.newDatabase = '';
        vm.newAccession = '';
        vm.newReference = '';

        vm.links = [];
        vm.databases = [];

        vm.add = add;
        vm.remove = remove;

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
            MarkerService.addLink(vm.id, vm.newDatabase, vm.newAccession, vm.newReference)
                .then(function(link) {
                    vm.links.unshift(link);
                    vm.newDatabase = '';
                    vm.newAccession = '';
                    vm.newReference = '';
                })
                .catch(function(error) {
                    console.error(error);
                });
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

    }
}());