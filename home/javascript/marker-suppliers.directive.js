;(function() {
    angular
        .module('app')
        .directive('autocompletify', autocompletify)
        .directive('markerSuppliers', markerSuppliers);

    function autocompletify() {
        var directive = {
            scope: {
                url: '@',
                onSelect: '&'
            },
            link: link
        };

        function link(scope, element) {
            element
                .autocompletify(scope.url)
                .on('typeahead:selected', function(event, item) {
                    scope.$apply(function (scope) {
                        scope.onSelect({item: item});
                    });
                });
        }

        return directive;
    }

    function markerSuppliers() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/marker-suppliers.directive.html',
            scope: {
                id: '@'
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
        vm.supplier = null;
        vm.entry = '';
        vm.errors = FieldErrorService.clearErrors();

        vm.submit = submit;
        vm.select = select;
        vm.remove = remove;

        activate();

        function activate() {
            MarkerService.getSuppliers(vm.id)
                .then(function (suppliers) {
                    vm.suppliers = suppliers;
                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function select(item) {
            vm.errors = FieldErrorService.clearErrors();
            var supplier = { zdbID: item.id, name: item.label };
            var added = vm.suppliers.some(function (existing) {
                return existing.zdbID === supplier.zdbID;
            });
            if (added) {
                vm.errors.fields.name = ["Supplier has already been added for this marker"];
                return;
            }

            vm.supplier = item;
        }

        function submit() {
            MarkerService.addSupplier(vm.id, { zdbID: vm.supplier.id })
                .then(function (supplier) {
                    vm.suppliers.push(supplier);
                    vm.entry = '';
                    vm.supplier = null;
                    vm.errors = FieldErrorService.clearErrors();
                })
                .catch(function (response) {
                    vm.errors = FieldErrorService.processErrorResponse(response);
                });
        }

        function remove(supplier, index) {
            MarkerService.removeSupplier(vm.id, supplier)
                .then(function () {
                    vm.suppliers.splice(index, 1);
                })
                .catch(function (error) {
                    console.log(error);
                })
        }

    }
}());