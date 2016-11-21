;(function () {
    angular
        .module('app')
        .directive('pubDashboard', pubDashboard);

    pubDashboard.$inject = ['IntertabEventService'];
    function pubDashboard(IntertabEventService) {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/pub-dashboard.directive.html',
            scope: {
                userId: '@'
            },
            link: link,
            controller: PubDashboardController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link (scope, element) {
            var $statusModal = element.find('.status-modal');
            scope.$watch('vm.statusModalPub', function (value) {
                $statusModal.modal(value ? 'show' : 'hide');
            });
            IntertabEventService.receiveEvents('pub-status-update', function () {
                scope.vm.fetchPubs();
            });
        }

        return directive;
    }

    PubDashboardController.$inject = ['PublicationService', 'ZfinUtils'];
    function PubDashboardController(PublicationService, zf) {
        var vm = this;

        vm.loading = true;
        vm.pubList = null;
        vm.pubMap = {};
        vm.totalPubs = 0;
        vm.currentPage = 1;
        vm.pubsPerPage = 50;
        vm.owners = [];
        vm.owner = null;
        vm.statuses = [];
        vm.status = null;

        vm.originalPub = null;
        vm.statusModalPub = null;
        vm.statusSaving = false;

        vm.fetchPubs = fetchPubs;
        vm.openStatusModal = openStatusModal;
        vm.saveStatusModal = saveStatusModal;
        vm.closeStatusModal = closeStatusModal;
        vm.statusNeedsOwner = PublicationService.statusNeedsOwner;

        activate();

        function activate() {
            PublicationService.getStatuses()
                .then(function (response) {
                    vm.statuses = response.data.filter(function (s) { return !s.hidden; });
                });
            PublicationService.getCurators()
                .then(function (response) {
                    var idx = zf.findIndex(response.data, function (curator) {
                        return curator.zdbID === vm.userId;
                    });
                    var me = response.data.splice(idx, 1)[0];
                    me.name = 'Me';
                    response.data.unshift({zdbID: null, name: "──────────"});
                    response.data.unshift({zdbID: '*', name: "Anyone"});
                    response.data.unshift(me);
                    vm.owners = response.data;
                    vm.owner = vm.owners[0];
                    fetchPubs();
                });
        }

        function fetchPubs() {
            vm.loading = true;
            vm.pubMap = {};
            var page = arguments[0] || vm.currentPage;
            var query = {
                owner: vm.owner.zdbID,
                status: vm.status ? vm.status.id : '',
                count: vm.pubsPerPage,
                offset: (page - 1) * vm.pubsPerPage,
                sort: "-date"
            };
            PublicationService.searchPubStatus(query)
                .then(function (response) {
                    vm.pubList = response.data.populatedResults;
                    vm.totalPubs = response.data.totalCount;
                    vm.pubMap = groupPubs(vm.pubList);
                })
                .finally(function () {
                    vm.loading = false;
                });
        }

        function groupPubs(pubArray) {
            if (!pubArray.length) {
                return null;
            }
            var pubMap = {};
            pubArray.forEach(function (pub) {
                var key = pub.status.status.name;
                if (!pubMap.hasOwnProperty(key)) {
                    pubMap[key] = []
                }
                pubMap[key].push(pub);
            });
            return pubMap;
        }

        function openStatusModal(pub) {
            vm.originalPub = pub;
            vm.statusModalPub = angular.copy(pub);
        }

        function saveStatusModal() {
            vm.statusSaving = true;
            PublicationService.updateStatus(vm.statusModalPub.status)
                .then(function () {
                    // kinda weird to refresh all the pubs, but not sure how to update just
                    // the original pub and keep the grouping / pagination correct
                    fetchPubs();
                    closeStatusModal();
                })
                .finally(function () {
                    vm.statusSaving = false;
                });
        }

        function closeStatusModal() {
            vm.statusModalPub = null;
        }

    }

}());