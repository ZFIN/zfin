;(function () {
    angular
        .module('app')
        .directive('markerSuppliers', markerSuppliers);

    function markerSuppliers() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/marker-suppliers.directive.html',
            scope: {
                markerId: '@'
            },
            controller: MarkerSuppliersController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    MarkerSuppliersController.$inject = ['MarkerService', 'FieldErrorService'];
    function MarkerSuppliersController(MarkerService, FieldErrorService) {

        var vm = this;

        vm.suppliers = [];
        vm.supplier = '';
        vm.errors = {};
        vm.processing = false;

        vm.submit = submit;
        vm.remove = remove;

        activate();

        function activate() {
            MarkerService.getSuppliers(vm.markerId)
                .then(function (suppliers) {
                    vm.suppliers = suppliers;
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function submit() {
            vm.processing = true;
            MarkerService.addSupplier(vm.markerId, vm.supplier)
                .then(function (supplier) {
                    vm.suppliers.push(supplier);
                    vm.supplier = '';
                    vm.errors = {};
                })
                .catch(function (response) {
                    vm.errors = FieldErrorService.processErrorResponse(response);
                })
                .finally(function () {
                    vm.processing = false;
                });
        }

        function remove(supplier, index) {
            MarkerService.removeSupplier(vm.markerId, supplier)
                .then(function () {
                    vm.suppliers.splice(index, 1);
                })
                .catch(function (error) {
                    console.log(error);
                });
        }

    }
}());