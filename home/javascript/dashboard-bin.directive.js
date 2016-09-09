;(function () {
    angular
        .module('app')
        .directive('dashboardBin', dashboardBin);

    function dashboardBin() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/dashboard-bin.directive.html',
            scope: {
                userId: '@',
                statusId: '@'
            },
            link: link,
            controller: DashboardBinController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link(scope, elem) {
            var $modal = $(elem).find('.modal');
            $modal.find('.figure-gallery-modal-image').on('load', function () {
                $modal.figureGalleryResize();
            });
            $modal.on('hidden.bs.modal', function() {
                scope.$apply(function () {
                    scope.vm.modal = null;
                });
            });
            scope.$watch('vm.modal', function (value) {
                if (value) {
                    $modal.modal('show');
                } else {
                    $modal.modal('hide');
                }
            });
        }

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
        vm.modal = null;

        vm.fetchPubs = fetchPubs;
        vm.claimPub = claimPub;
        vm.openModal = openModal;

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
                status: { id: vm.statusId },
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

        function openModal(pub, idx) {
            vm.modal = {
                pub: pub,
                image: pub.images[idx],
                prev: idx > 0 ? idx - 1 : null,
                next: idx < pub.images.length - 1 ? idx + 1 : null
            };
        }

    }
}());