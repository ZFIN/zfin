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
                curatorId: '@',
                topics: '=',
                notes: '='
            },
            controller: PublicationStatusController,
            controllerAs: 'vm',
            bindToController: true,
            link: link
        };

        function link(scope) {
            scope.$watch('vm.statusForm.$dirty', function (isDirty) {
                if (isDirty) {
                    scope.vm.saved = false;
                }
            });
        }

        return directive;
    }

    PublicationStatusController.$inject = ['PublicationService', 'IntertabEventService', 'ZfinUtils'];
    function PublicationStatusController(PublicationService, IntertabEventService, zf) {
        var vm = this;

        vm.statuses = [];
        vm.indexedStatus = null;
        vm.locations = [];
        vm.priorities = [];
        vm.curators = [];
        vm.curator = null;

        vm.current = null;
        vm.original = null;

        vm.warnings = [];
        vm.processing = false;
        vm.saved = false;

        vm.hasTopics = hasTopics;
        vm.updateStatus = updateStatus;
        vm.readyToSave = readyToSave;
        vm.handleStatusChange = handleStatusChange;
        vm.statusNeedsOwner = PublicationService.statusNeedsOwner;
        vm.statusNeedsLocation = PublicationService.statusNeedsLocation;
        vm.statusHasPriority = PublicationService.statusHasPriority;
        vm.reset = reset;

        activate();

        function activate() {
            PublicationService.getStatuses()
                .then(function (response) {
                    vm.indexedStatus = zf.find(response.data, function (s) {
                        return s.type === 'INDEXED';
                    });
                    vm.statuses = response.data.filter(function (s) {
                        return !s.hidden;
                    });
                });
            PublicationService.getLocations()
                .then(function (response) {
                    vm.locations = response.data;
                });
            PublicationService.getPriorities()
                .then(function (response) {
                    vm.priorities = response.data;
                });
            PublicationService.getCurators()
                .then(function (response) {
                    vm.curators = response.data;
                    vm.curator = vm.curators.find(function (c) {
                        return c.zdbID === vm.curatorId;
                    });
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
            if (vm.statusNeedsLocation(vm.current.status) || (!statusChanged && vm.statusHasPriority(vm.current.status))) {
                if (!vm.original.location) {
                    return vm.current.location;
                }
                return vm.current.location && (statusChanged || vm.current.location.id !== vm.original.location.id);
            }
            if (vm.statusNeedsOwner(vm.current.status)) {
                if (!vm.original.owner) {
                    return vm.current.owner;
                }
                return vm.current.owner && (statusChanged || vm.current.owner.zdbID !== vm.original.owner.zdbID);
            }
            return statusChanged;
        }

        function handleStatusChange() {
            if (vm.current.status.type === 'CURATING') {
                vm.current.owner = vm.curator;
            } else {
                vm.current.owner = vm.original.owner;
            }
        }

        function validateBeforeClose() {
            return PublicationService.validatePubForClose(vm.pubId)
                .then(function (response) {
                    if (!zf.isEmpty(response.data.warnings)) {
                        vm.warnings = response.data.warnings;
                    } else {
                        return doStatusUpdate(true);
                    }
                });
        }

        function doStatusUpdate(isClosing) {
            if (!vm.statusNeedsLocation(vm.current.status) && !vm.statusHasPriority(vm.current.status)) {
                vm.current.location = null;
            }
            if (!vm.statusNeedsOwner(vm.current.status)) {
                vm.current.owner = null;
            }
            if (!vm.current.pubZdbID) {
                vm.current.pubZdbID = vm.pubId;
            }
            return PublicationService.updateStatus(vm.current)
                .then(storeStatus)
                .then(function () {
                    vm.saved = true;
                    vm.statusForm.$setPristine();
                    IntertabEventService.fireEvent('pub-status-update');
                    if (isClosing) {
                        return PublicationService.getTopics(vm.pubId)
                            .then(function (response) {
                                vm.topics = response.data;
                            });
                    }
                })
        }

        function updateStatus(validate) {
            vm.processing = true;
            var isClosing = vm.current.status.type === 'CLOSED';
            var isIndexed = false;
            if (vm.original.status != null)
                isIndexed = vm.original.status.type === 'INDEXING' &&
                    vm.current.status.type === 'READY_FOR_CURATION';
            var update;
            if (validate && isClosing) {
                update = validateBeforeClose();
            }
            if (isIndexed) {
                var indexed = {
                    location: null,
                    owner: vm.curator,
                    pubZdbID: vm.pubId,
                    status: vm.indexedStatus
                };
                update = PublicationService.updateStatus(indexed)
                    .then(function () {
                        doStatusUpdate(isClosing)
                    });
            } else {
                update = doStatusUpdate(isClosing);
            }
            update.finally(function () {
                vm.processing = false;
            });
        }

        function hasTopics() {
            return vm.topics.some(function (t) {
                return t.dataFound;
            });
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
