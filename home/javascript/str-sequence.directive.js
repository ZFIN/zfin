;(function() {
    angular
        .module('app', [])
        .directive('strSequence', strSequence);

    function strSequence() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/str-sequence.directive.html',
            scope: {
                reportedSequenceName: '@',
                displayedSequenceName: '@'
            },
            controller: STRSequenceController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    STRSequenceController.$inject = ['$scope'];
    function STRSequenceController($scope) {

        var vm = this;

        vm.reportedSequence = '';
        vm.displayedSequence = '';
        vm.isReversed = false;
        vm.isComplemented = false;

        activate();

        function activate() {
            $scope.$watchGroup(['vm.reportedSequence', 'vm.isReversed', 'vm.isComplemented'], function (newValue) {
                vm.reportedSequence = stripBadCharacters(vm.reportedSequence.toUpperCase());
                vm.displayedSequence = vm.reportedSequence;
                if (vm.isReversed) {
                    vm.displayedSequence = reverseString(vm.displayedSequence);
                }
                if (vm.isComplemented) {
                    vm.displayedSequence = complementString(vm.displayedSequence);
                }
            });
        }

        function stripBadCharacters(str) {
            return str.replace(/[^ATGC]/ig, '');
        }

        function reverseString(str) {
            return str.split('').reverse().join('');
        }

        function complementString(str) {
            var complemented = "";
            for (var i = 0; i < str.length; i++) {
                if (str.charAt(i) == 'A') {
                    complemented = complemented + "T";
                }
                else if (str.charAt(i) == 'T') {
                    complemented = complemented + "A";
                }
                else if (str.charAt(i) == 'C') {
                    complemented = complemented + "G";
                }
                else if (str.charAt(i) == 'G') {
                    complemented = complemented + "C";
                }
            }
            return complemented;
        }
    }

}());