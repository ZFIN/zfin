;(function () {
    angular
        .module('app')
        .directive('strSequence', strSequence);

    function strSequence() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/str-sequence.directive.html',
            scope: {
                reportedSequenceName: '@',
                displayedSequenceName: '@',
                reversedName: '@',
                complementedName: '@',
                type: '@',
                reportedSequence: '=?',
                sequence: '=?',
                isReversed: '=?',
                isComplemented: '=?'
            },
            controller: STRSequenceController,
            controllerAs: 'vm',
            bindToController: true,
            link: link
        };

        function link(scope, element, attrs) {
            if (attrs.sequenceText) {
                scope.vm.reportedSequence = attrs.sequenceText;
            }
        }

        return directive;
    }

    STRSequenceController.$inject = ['$scope'];
    function STRSequenceController($scope) {

        var vm = this;

        activate();

        function activate() {
            $scope.$watchGroup(['vm.reportedSequence', 'vm.isReversed', 'vm.isComplemented'], function (newValue) {
                if (!vm.reportedSequence) {
                    vm.sequence = '';
                    return;
                }

                vm.reportedSequence = stripBadCharacters(vm.reportedSequence.toUpperCase());
                vm.sequence = vm.reportedSequence;
                if (vm.isReversed) {
                    vm.sequence = reverseString(vm.sequence);
                }
                if (vm.isComplemented) {
                    vm.sequence = complementString(vm.sequence);
                }
            });
        }

        function stripBadCharacters(str) {
            var bases = 'ATGC';
            if (vm.type === 'TALEN') {
                bases += 'R';
            }
            return str.replace(new RegExp('[^' + bases + ']', 'ig'), '');
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