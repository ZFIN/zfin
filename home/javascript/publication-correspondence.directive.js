;(function () {
    angular
        .module('app')
        .directive('publicationCorrespondence', publicationCorrespondence);

    function publicationCorrespondence() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/publication-correspondence.directive.html',
            scope: {
                pubId: '@',
                curatorId: '@',
                curatorEmail: '@'
            },
            controller: PublicationCorrespondenceController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PublicationCorrespondenceController.$inject = ['PublicationService'];
    function PublicationCorrespondenceController(PublicationService) {
        var vm = this;

        vm.authors = [];
        vm.newEmail = null;

        vm.correspondences = [];

        vm.allCorrespondencesClosed = allCorrespondencesClosed;
        vm.newCorrespondence = newCorrespondence;
        vm.deleteCorrespondence = deleteCorrespondence;
        vm.reopenCorrespondence = reopenCorrespondence;
        vm.closeCorrespondence = closeCorrespondence;
        vm.emailList = emailList;

        vm.openSendForm = openSendForm;
        vm.openResponseForm = openResponseForm;
        vm.closeForm = closeForm;
        vm.sendMessage = sendMessage;

        activate();

        function activate() {
            PublicationService.getPublicationDetails(vm.pubId)
                .then(function (response) {
                    vm.authors = response.data.registeredAuthors.filter(function (r) { return r.email; });
                });
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

        function emailList(recipients) {
            return recipients.map(function (r) { return r.email; }).join(', ');
        }

        function openSendForm() {
            vm.newEmail = {
                outgoing: true,
                additionalTo: '',
                from: {zdbID: vm.curatorId, email: vm.curatorEmail},
                subject: '',
                message: ''
            };
        }

        function openResponseForm() {
            vm.newEmail = {
                outgoing: false,
                to: vm.curatorEmail,
                from: '',
                subject: '',
                message: ''
            };
        }

        function closeForm() {
            vm.newEmail = null;
        }

        function sendMessage() {
            vm.newEmail.to = vm.authors
                .filter(function (a) { return a.send; })
                .concat(vm.newEmail.additionalTo
                    .split(/[,;\s]+/)
                    .filter(function (e) { return e.length; })
                    .map(function (e) { return {email: e}; })
                );
            PublicationService.addCorrespondence(vm.pubId, vm.newEmail)
                .then(function (response) {
                    vm.correspondences.unshift(response.data);
                    closeForm();
                });
        }
    }

}());