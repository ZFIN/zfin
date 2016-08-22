;(function () {
    angular
        .module('app')
        .directive('publicationStatus', publicationStatus);

    function publicationStatus() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/publication-status.directive.html',
            scope: {
                pubId: '@',
                status: '=',
                topics: '=',
                notes: '='
            },
            controller: PublicationStatusController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PublicationStatusController.$inject = ['PublicationService'];
    function PublicationStatusController(PublicationService) {
        var vm = this;

        vm.warnings = [];

        vm.unindexPub = unindexPub;
        vm.indexPub = indexPub;
        vm.validateForClose = validateForClose;
        vm.reopenPub = reopenPub;
        vm.closePub = closePub;
        vm.cancelClosePub = cancelClosePub;
        vm.hasTopics = hasTopics;

        activate();

        function activate() {
        }

        function unindexPub() {
            vm.status.indexed = false;
            vm.status.indexedDate = null;
            PublicationService.updateStatus(vm.status)
                .then(function (response) {
                    vm.status = response.data;
                    addNote('Un-indexed paper');
                });
        }

        function indexPub() {
            vm.status.indexed = true;
            vm.status.indexedDate = Date.now();
            PublicationService.updateStatus(vm.status)
                .then(function (response) {
                    vm.status = response.data;
                    addNote('Indexed paper');
                });
        }

        function validateForClose() {
            PublicationService.validatePubForClose(vm.pubId)
                .then(function (response) {
                    if (typeof response.data.warnings !== 'undefined' &&
                        response.data.warnings !== null &&
                        response.data.warnings.length > 0) {
                        vm.warnings = response.data.warnings;
                    } else {
                        vm.closePub();
                    }
                });
        }

        function reopenPub() {
            vm.status.closedDate = null;
            PublicationService.updateStatus(vm.status)
                .then(function (response) {
                    vm.status = response.data;
                    addNote('Reopened paper')
                });
        }

        function closePub() {
            vm.status.closedDate = Date.now();
            PublicationService.updateStatus(vm.status)
                .then(function (response) {
                    vm.status = response.data;
                    vm.warnings = [];
                    var noteToAdd = vm.hasTopics() ? 'Closed paper' : 'Upon review, this publication contains no information currently curated by ZFIN';
                    addNote(noteToAdd);
                    PublicationService.getTopics(vm.pubId)
                        .then(function (response) {
                            vm.topics = response.data;
                        })
                });
        }

        function cancelClosePub() {
            vm.warnings = [];
        }

        function hasTopics() {
            return vm.topics.some(function (t) { return t.dataFound; });
        }

        function addNote(text) {
            PublicationService.addNote(vm.pubId, text)
                .then(function (response) {
                    vm.notes.unshift(response.data);
                });
        }

    }

}());