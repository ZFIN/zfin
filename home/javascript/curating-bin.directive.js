;(function () {
    angular
        .module('app')
        .directive('curatingBin', curatingBin);

    function curatingBin() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/curating-bin.directive.html',
            scope: {
                userId: '@',
                currentStatus: '@',
                nextStatus: '@'
            },
            link: link,
            controller: CuratingBinController,
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

    CuratingBinController.$inject = ['PublicationService'];
    function CuratingBinController(PublicationService) {
        var vm = this;

        vm.loading = true;
        vm.pubs = [];
        vm.totalPubs = 0;
        vm.currentPage = 1;
        vm.pubsPerPage = 50;
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
                    vm.locations = response.data;
                    vm.location = vm.locations[0];
                    fetchPubs();
                })
        }

        function fetchPubs() {
            vm.loading = true;
            var page = arguments[0] || vm.currentPage;
            var query = {
                status: vm.currentStatus,
                location: vm.location ? vm.location.id : '',
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

        function claimPub(pub) {
            pub.saving = true;
           
            var status = {
                pubZdbID: pub.zdbId,
                status: { id: vm.nextStatus },
                location: null,
                owner: { zdbID: vm.userId }
            };

            PublicationService.updateStatus(status,false)
                .then(function (response) {
                    pub.claimed = true;
                   
                })
                .catch(function (response) {
                   
                    if (response.data && response.data.message) {
                        vm.claimError = response.data.message;
                    }
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