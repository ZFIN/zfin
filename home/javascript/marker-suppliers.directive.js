;(function() {
    angular
        .module('app')
        .directive('autocompletify', autocompletify)
        .directive('markerSuppliers', markerSuppliers);

    function autocompletify() {
        var directive = {
            require: 'ngModel',
            scope: {
                url: '@',
                onSelect: '&'
            },
            link: link
        };

        function link(scope, element, attrs, ngModel) {
            element
                .autocompletify(scope.url)
                .on('typeahead:select', function(event, item) {
                    ngModel.$setViewValue(item.value, 'typeahead:select');
                    scope.$apply(function (scope) {
                        scope.onSelect({item: item});
                    });
                });

            ngModel.$render = function() {
                element.typeahead('val', ngModel.$viewValue);
            };

        }

        return directive;
    }

    function markerSuppliers() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/marker-suppliers.directive.html',
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
        vm.supplier = '';
        vm.errors = {};

        vm.submit = submit;
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

        function submit() {
            var added = vm.suppliers.some(function (existing) {
                return existing.name === vm.supplier;
            });
            if (added) {
                vm.errors.fields.name = ["Supplier has already been added for this marker"];
                return;
            }
            MarkerService.addSupplier(vm.id, { name: vm.supplier })
                .then(function (supplier) {
                    vm.suppliers.push(supplier);
                    vm.supplier = '';
                    vm.errors = {};
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