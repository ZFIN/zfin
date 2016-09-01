;(function () {
    angular
        .module('app')
        .directive('dashboardBin', dashboardBin);

    function dashboardBin() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/dashboard-bin.directive.html',
            scope: {
                userId: '@'
            },
            controller: DashboardBinController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    DashboardBinController.$inject = ['PublicationService'];
    function DashboardBinController(PublicationService) {
        var vm = this;

        vm.loading = true;
        vm.allPubs = [];
        vm.pubs = [];
        vm.searchTerm = '';
        vm.locations = [];
        vm.location = null;
        vm.sortOrders = [
            {
                value: 'update,asc',
                display: 'Time in bin (oldest)'
            },
            {
                value: 'update,desc',
                display: 'Time in bin (newest)'
            },
            {
                value: 'pub_date,asc',
                display: 'Publication date (oldest)'
            },
            {
                value: 'pub_date,desc',
                display: 'Publication date (newest)'
            }
        ];
        vm.sort = vm.sortOrders[0];

        vm.fetchPubs = fetchPubs;
        vm.claimPub = claimPub;

        activate();

        function activate() {
            PublicationService.getLocations()
                .then(function (response) {
                    vm.locations = response.data.filter(function (loc) { return loc.role === 'CURATOR'; });
                    vm.location = vm.locations[0];
                    fetchPubs();
                })
        }

        function fetchPubs() {
            vm.loading = true;
            PublicationService.getPubsInBin(vm.location, vm.sort)
                .then(function (response) {
                    vm.allPubs = response.data;
                    vm.pubs = vm.allPubs;
                })
                .finally(function () {
                    vm.loading = false;
                });
        }

        function claimPub(pub) {
            pub.saving = true;
            var status = {
                pubZdbID: pub.zdbId,
                status: {
                    id: 5 // errr... this seems fragile
                },
                location: null,
                owner: {
                    zdbID: vm.userId
                }
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