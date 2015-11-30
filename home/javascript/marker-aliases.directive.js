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

        vm.aliases = [];

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

    }
}());