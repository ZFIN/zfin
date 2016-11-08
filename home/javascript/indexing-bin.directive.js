;(function () {
    angular
        .module('app')
        .directive('indexingBin', indexingBin);

    indexingBin.$inject = ['IntertabEventService'];
    function indexingBin(IntertabEventService) {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/indexing-bin.directive.html',
            scope: {
                userId: '@',
                currentStatus: '@',
                nextStatus: '@'
            },
            link: link,
            controller: IndexingBinController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link(scope) {
            IntertabEventService.receiveEvents('pub-status-update', function () {
                scope.vm.fetchPubs();
            });
        }

        return directive;
    }

    IndexingBinController.$inject = ['PublicationService'];
    function IndexingBinController(PublicationService) {
        var vm = this;

        vm.loading = true;
        vm.pubs = [];
        vm.totalPubs = 0;
        vm.currentPage = 1;
        vm.pubsPerPage = 50;
        vm.priorities = [];
        vm.priority = null;
        vm.sortOrders = [
            {
                value: 'date',
                display: 'Time in bin (oldest)'
            },
            {
                value: '-date',
                display: 'Time in bin (newest)'
            },
            {
                value: 'pub.entryDate',
                display: 'Entry date (oldest)'
            },
            {
                value: '-pub.entryDate',
                display: 'Entry date (newest)'
            }
        ];
        vm.sort = vm.sortOrders[0];

        vm.fetchPubs = fetchPubs;
        vm.updatePriority = updatePriority;
        vm.claimPub = claimPub;

        activate();

        function activate() {
            PublicationService.getPriorities()
                .then(function (response) {
                    response.data.push({id: 0, name: 'Not Set'});
                    vm.priorities = response.data;
                    vm.priority = vm.priorities[0];
                    fetchPubs();
                })
        }

        function fetchPubs() {
            vm.loading = true;
            var page = arguments[0] || vm.currentPage;
            var query = {
                status: vm.currentStatus,
                location: vm.priority.id,
                sort: vm.sort.value,
                count: vm.pubsPerPage,
                offset: (page - 1) * vm.pubsPerPage
            };
            PublicationService.searchPubStatus(query)
                .then(function (response) {
                    vm.pubs = response.data.populatedResults;
                    vm.totalPubs = response.data.totalCount;
                })
                .finally(function () {
                    vm.loading = false;
                });
        }

        function updatePriority(pub) {
            pub.saving = true;
            PublicationService.updateStatus(pub.status)
                .then(function (response) {
                    pub.status = response.data;
                })
                .finally(function () {
                    pub.saving = false;
                });
        }

        function claimPub(pub) {
            pub.saving = true;
            var status = {
                pubZdbID: pub.zdbId,
                status: { id: vm.nextStatus },
                location: null,
                owner: { zdbID: vm.userId }
            };
            PublicationService.updateStatus(status)
                .then(function () {
                    pub.claimed = true;
                })
                .finally(function () {
                    pub.saving = false;
                })
        }
    }
}());