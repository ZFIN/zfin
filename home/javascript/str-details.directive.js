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

    STRDetailsController.$inject = ['STRService'];
    function STRDetailsController(STRService) {

        var vm = this;

        vm.str = {};
        vm.reportedSequence1 = '';
        vm.reportedSequence2 = '';

        vm.processing = false;
        vm.saved = false;
        vm.failed = false;
        vm.errors = {};

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
            vm.failed = false;
            vm.errors = {};
            STRService.saveStrDetails(vm.id, vm.str)
                .then(function (data) {
                    vm.str = data;
                    vm.saved = true;
                })
                .catch(processErrors)
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

        function processErrors(response) {
            response.data.fieldErrors.forEach(function (error) {
                if (!vm.errors.hasOwnProperty(error.field)) {
                    vm.errors[error.field] = [];
                }
                vm.errors[error.field].push(error.message);
            });
            if (!response.data.fieldErrors.length) {
                vm.failed = true;
            }
        }

    }
}());