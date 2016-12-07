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
        vm.processing = false;
        vm.successMessage = '';
        vm.errorMessage = '';

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
        vm.saveReply = saveReply;
        vm.resendMessage = resendMessage;
        vm.openReplyForMessage = openReplyForMessage;
        vm.sendReply = sendReply;
        vm.isValid = isValid;

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
                to: [{zdbID: vm.curatorId, email: vm.curatorEmail}],
                from: {email: ''},
                subject: '',
                message: ''
            };
        }

        function closeForm() {
            vm.newEmail = null;
            vm.authors.forEach(function (a) { a.send = false; });
            vm.successMessage = '';
            vm.errorMessage = '';
        }

        function sendMessage() {
            vm.processing = true;
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
                    vm.successMessage = 'Email successfully sent.';
                })
                .catch(function () {
                    vm.errorMessage = 'Error sending email.';
                })
                .finally(function () {
                    vm.processing = false;
                });
        }

        function saveReply() {
            vm.processing = true;
            PublicationService.addCorrespondence(vm.pubId, vm.newEmail)
                .then(function (response) {
                    vm.correspondences.unshift(response.data);
                    closeForm();
                    vm.successMessage = 'Reply saved.';
                })
                .catch(function () {
                    vm.errorMessage = 'Error saving reply.';
                })
                .finally(function () {
                    vm.processing = false;
                });
        }

        function resendMessage(correspondence) {
            var newMessage = angular.copy(correspondence);
            newMessage.resend = true;
            PublicationService.addCorrespondence(vm.pubId, newMessage)
                .then(function (response) {
                    vm.correspondences.unshift(response.data);
                    vm.successMessage = 'Email successfully sent.';
                })
                .catch(function () {
                    vm.errorMessage = 'Error sending email.';
                });
        }

        function openReplyForMessage(correspondence) {
            vm.newEmail = {
                outgoing: false,
                to: [{zdbID: vm.curatorId, email: vm.curatorEmail}],
                from: {email: emailList(correspondence.to)},
                subject: prependSubject(correspondence.subject),
                message: ''
            };
        }

        function sendReply(correspondence) {
            vm.newEmail = {
                reply: true,
                outgoing: true,
                additionalTo: correspondence.from.email,
                from: {zdbID: vm.curatorId, email: vm.curatorEmail},
                subject: prependSubject(correspondence.subject),
                message: ''
            };
        }

        function prependSubject(subject) {
            if (subject.toLowerCase().substr(0, 3) !== 're:') {
                subject = 'Re: ' + subject;
            }
            return subject;
        }

        function isValid() {
            if (!vm.newEmail) {
                return false;
            }
            if (vm.newEmail.outgoing) {
                if (!vm.authors.some(function (a) { return a.send;}) && !vm.newEmail.additionalTo) {
                    return false;
                }
            } else {
                if (!vm.newEmail.from.email) {
                    return false;
                }
            }
            return vm.newEmail.subject && vm.newEmail.message;
        }
    }

}());