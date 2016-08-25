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

        vm.statuses = [];
        vm.locations = [];
        vm.curators = [];

        vm.current = null;
        vm.original = null;

        vm.warnings = [];

        vm.unindexPub = unindexPub;
        vm.indexPub = indexPub;
        vm.validateForClose = validateForClose;
        vm.reopenPub = reopenPub;
        vm.closePub = closePub;
        vm.cancelClosePub = cancelClosePub;
        vm.hasTopics = hasTopics;

        vm.updateStatus = updateStatus;
        vm.readyToSave = readyToSave;
        vm.statusNeedsOwner = statusNeedsOwner;
        vm.statusNeedsLocation = statusNeedsLocation;

        activate();

        function activate() {
            PublicationService.getStatuses()
                .then(function (response) {
                    vm.statuses = response.data;
                });
            PublicationService.getLocations()
                .then(function (response) {
                    vm.locations = response.data;
                });
            PublicationService.getCurators()
                .then(function (response) {
                    vm.curators = response.data;
                });
            PublicationService.getStatus(vm.pubId)
                .then(function (response) {
                    vm.current = response.data || {status: vm.statuses[0], owner: null, location: null};
                    vm.original = angular.copy(vm.current);
                });
        }

        function readyToSave() {
            if (!vm.current) {
                return false;
            }
            var statusChanged = vm.current.status.id !== vm.original.status.id;
            if (statusNeedsLocation(vm.current.status)) {
                return vm.current.location && (statusChanged || vm.current.location.id !== vm.original.location.id);
            }
            if (statusNeedsOwner(vm.current.status)) {
                return vm.current.owner && (statusChanged || vm.current.owner.zdbID !== vm.original.owner.zdbID);
            }
            return statusChanged;
        }

        function updateStatus(validate) {
            if (validate && vm.current.status.type == 'CLOSED') {
                PublicationService.validatePubForClose(vm.pubId)
                    .then(function (response) {
                        if (typeof response.data.warnings !== 'undefined' &&
                            response.data.warnings !== null &&
                            response.data.warnings.length > 0) {
                            vm.warnings = response.data.warnings;
                        } else {
                            updateStatus(false);
                        }
                    });
            } else {
                if (!statusNeedsLocation(vm.current.status)) {
                    vm.current.location = null;
                }
                if (!statusNeedsOwner(vm.current.status)) {
                    vm.current.owner = null;
                }
                vm.original = angular.copy(vm.current);
                vm.warnings = [];
            }
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
            vm.current = angular.copy(vm.original);
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

        function statusNeedsOwner(status) {
            return typeof status !== 'undefined' && status !== null && (status.type === 'CURATING' || status.type === 'WAIT');
        }

        function statusNeedsLocation(status) {
            return typeof status !== 'undefined' && status !== null && status.type === 'READY_FOR_CURATION';
        }

    }

}());