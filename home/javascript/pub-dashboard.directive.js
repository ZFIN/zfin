;(function () {
    angular
        .module('app')
        .directive('pubDashboard', pubDashboard);

    function pubDashboard() {
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
        }

        return directive;
    }

    PubDashboardController.$inject = ['PublicationService', 'ZfinUtils'];
    function PubDashboardController(PublicationService, zf) {
        var vm = this;

        vm.loading = true;
        vm.pubList = null;
        vm.pubMap = {};
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
                    vm.statuses = response.data;
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
            PublicationService.searchPubStatus({owner: vm.owner.zdbID, status: vm.status ? vm.status.id : ''})
                .then(function (response) {
                    vm.pubList = response.data;
                    vm.pubMap = groupPubs(vm.pubList);
                })
                .finally(function () {
                    vm.loading = false;
                });
        }

        function groupPubs(pubArray) {
            if (pubArray.length === 0) {
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