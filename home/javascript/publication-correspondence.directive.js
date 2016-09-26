;(function () {
    angular
        .module('app')
        .directive('publicationCorrespondence', publicationCorrespondence);

    function publicationCorrespondence() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/publication-correspondence.directive.html',
            scope: {
                pubId: '@'
            },
            link: link,
            controller: PublicationCorrespondenceController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link(scope, element) {
            element
                .on("mouseenter", ".hover-trigger", function () {
                    angular.element(this).find(".hover-reveal").show();
                })
                .on("mouseleave", ".hover-trigger", function () {
                    angular.element(this).find(".hover-reveal").hide();
                });
        }

        return directive;
    }

    PublicationCorrespondenceController.$inject = ['PublicationService'];
    function PublicationCorrespondenceController(PublicationService) {
        var vm = this;

        vm.correspondences = [];

        vm.allCorrespondencesClosed = allCorrespondencesClosed;
        vm.newCorrespondence = newCorrespondence;
        vm.deleteCorrespondence = deleteCorrespondence;
        vm.reopenCorrespondence = reopenCorrespondence;
        vm.closeCorrespondence = closeCorrespondence;

        activate();

        function activate() {
            PublicationService.getCorrespondences(vm.pubId)
                .then(function (response) {
                    vm.correspondences = response.data;
                });
        }

        function allCorrespondencesClosed() {
            return vm.correspondences.length == 0 || vm.correspondences[0].closedDate;
        }

        function newCorrespondence() {
            PublicationService.addCorrespondence(vm.pubId)
                .then(function (response) {
                    vm.correspondences.unshift(response.data);
                });
        }

        function deleteCorrespondence(corr, idx) {
            PublicationService.deleteCorrespondence(corr)
                .then(function () {
                    vm.correspondences.splice(idx, 1);
                });
        }

        function reopenCorrespondence(corr, idx) {
            corr.closedDate = null;
            updateCorrespondence(corr, idx);
        }

        function closeCorrespondence(replied, corr, idx) {
            corr.closedDate = Date.now();
            corr.replyReceived = replied;
            updateCorrespondence(corr, idx);
        }

        function updateCorrespondence(corr, idx) {
            PublicationService.updateCorrespondence(corr)
                .then(function (response) {
                    vm.correspondences[idx] = response.data;
                });
        }
    }

}());