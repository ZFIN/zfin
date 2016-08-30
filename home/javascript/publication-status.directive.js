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
        vm.processing = false;

        vm.hasTopics = hasTopics;
        vm.updateStatus = updateStatus;
        vm.readyToSave = readyToSave;
        vm.statusNeedsOwner = statusNeedsOwner;
        vm.statusNeedsLocation = statusNeedsLocation;
        vm.reset = reset;

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
                .then(storeStatus);
        }

        function readyToSave() {
            if (!vm.current) {
                return false;
            }
            // TODO: this is a lot of crazy logic -- can it be simplified?
            var statusChanged = !vm.original || vm.current.status.id !== vm.original.status.id;
            if (statusNeedsLocation(vm.current.status)) {
                if (!vm.original.location) {
                    return vm.current.location;
                }
                return vm.current.location && (statusChanged || vm.current.location.id !== vm.original.location.id);
            }
            if (statusNeedsOwner(vm.current.status)) {
                if (!vm.original.owner) {
                    return vm.current.owner;
                }
                return vm.current.owner && (statusChanged || vm.current.owner.zdbID !== vm.original.owner.zdbID);
            }
            return statusChanged;
        }

        function updateStatus(validate) {
            vm.processing = true;
            var isClosing = vm.current.status.type == 'CLOSED';
            if (validate && isClosing) {
                PublicationService.validatePubForClose(vm.pubId)
                    .then(function (response) {
                        if (typeof response.data.warnings !== 'undefined' &&
                            response.data.warnings !== null &&
                            response.data.warnings.length > 0) {
                            vm.warnings = response.data.warnings;
                        } else {
                            updateStatus(false);
                        }
                    })
                    .finally(function() {
                        vm.processing = false;
                    });
            } else {
                if (!statusNeedsLocation(vm.current.status)) {
                    vm.current.location = null;
                }
                if (!statusNeedsOwner(vm.current.status)) {
                    vm.current.owner = null;
                }
                PublicationService.updateStatus(vm.current)
                    .then(storeStatus)
                    .then(function() {
                        if (isClosing) {
                            PublicationService.getTopics(vm.pubId)
                                .then(function (response) {
                                    vm.topics = response.data;
                                });
                        }
                    })
                    .finally(function() {
                        vm.processing = false;
                    });
            }
        }

        function hasTopics() {
            return vm.topics.some(function (t) { return t.dataFound; });
        }

        function statusNeedsOwner(status) {
            return typeof status !== 'undefined' && status !== null && (status.type === 'CURATING' || status.type === 'WAIT');
        }

        function statusNeedsLocation(status) {
            return typeof status !== 'undefined' && status !== null && status.type === 'READY_FOR_CURATION';
        }

        function reset() {
            vm.current = angular.copy(vm.original);
            vm.warnings = [];
        }

        function storeStatus(response) {
            vm.current = response.data;
            vm.original = angular.copy(vm.current);
            vm.warnings = [];
        }

    }

}());