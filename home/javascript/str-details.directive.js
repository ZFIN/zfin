;(function() {
    angular
        .module('app')
        .directive('strDetails', strDetails);

    function strDetails() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/str-details.directive.html',
            scope: {
                id: '@',
                type: '@'
            },
            controller: STRDetailsController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    STRDetailsController.$inject = ['STRService', 'FieldErrorService'];
    function STRDetailsController(STRService, FieldErrorService) {

        var vm = this;

        vm.str = {};
        vm.reportedSequence1 = '';
        vm.reportedSequence2 = '';

        vm.processing = false;
        vm.saved = false;
        vm.errors = FieldErrorService.clearErrors();

        vm.save = save;
        vm.reset = reset;

        activate();

        function activate() {
            if (vm.type === 'CRISPR') {
                vm.sequenceLabel = 'Target Sequence';
            } else if (vm.type === 'TALEN') {
                vm.sequenceLabel = 'Target Sequence 1';
            } else {
                vm.sequenceLabel = 'Sequence';
            }

            reset();
        }

        function save() {
            vm.processing = true;
            vm.saved = false;
            vm.errors = FieldErrorService.clearErrors();
            STRService.saveStrDetails(vm.id, vm.str)
                .then(function (data) {
                    vm.str = data;
                    vm.saved = true;
                })
                .catch(function (response) {
                    vm.errors = FieldErrorService.processErrorResponse(response);
                })
                .finally(function() {
                    vm.processing = false;
                });
        }

        function reset() {
            STRService.getStrDetails(vm.id)
                .then(function (data) {
                    vm.reportedSequence1 = data.sequence1;
                    vm.reportedSequence2 = data.sequence2;
                    vm.str = data;
                })
                .catch(function (error) {
                    console.log(error);
                });
        }

    }
}());