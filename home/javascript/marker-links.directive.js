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

        vm.links = [];
        vm.databases = [];

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

    }
}());