;(function () {
    angular
        .module('app')
        .directive('publicationAuthorNotif', publicationAuthorNotif);

    function publicationAuthorNotif() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/publication-author-notif.directive.html',
            scope: {
                pubId: '@',
                curatorFirst: '@',
                curatorLast: '@',
                curatorEmail: '@',
                notes: '='
            },
            controller: PublicationAuthorNotifController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PublicationAuthorNotifController.$inject = ['$sce', 'PublicationService'];
    function PublicationAuthorNotifController($sce, PublicationService) {
        var vm = this;

        vm.loading = false;
        vm.editing = false;
        vm.previewing = false;
        vm.sendSuccess = false;
        vm.sendError = false;
        vm.recipients = [];
        vm.authors = [];
        vm.additionalRecipients = '';
        vm.salutation = 'Dear';
        vm.names = '';
        vm.intro = 'I am pleased to report that information about your paper has been entered ' +
            'into ZFIN, the Zebrafish Information Network.';
        vm.pubReference = '';
        vm.pubLink = '';
        vm.dataNote = 'Please notify me if you have corrections, comments about the data associated with your paper:';
        vm.customNote = '';
        vm.zfinDescription = 'ZFIN is The Zebrafish Information Network, a centralized community ' +
            'resource for zebrafish genetic, genomic, and developmental data. We ' +
            'encourage you to share this message with your co-authors and appreciate ' +
            'any feedback that you are able to offer. Community input is vital to our ' +
            'success and value as a public resource. If you have corrections, comments, ' +
            'or additional data that you would like to submit to ZFIN, please contact me.';
        vm.signOff = 'Thank you';
        vm.sender =
        {
            name: vm.curatorFirst + ' ' + vm.curatorLast,
            group: 'Scientific Curation Group',
            email: vm.curatorEmail
        };
        vm.address = [
            'Zebrafish Information Network',
            '5291 University of Oregon',
            'Eugene, Oregon, USA 97403-5291'
        ];
        vm.curatedData = [];

        vm.hasNoRecipients = hasNoRecipients;
        vm.editNotification = editNotification;
        vm.previewNotification = previewNotification;
        vm.cancelNotificationEditing = cancelNotificationEditing;
        vm.generateDataLinks = generateDataLinks;
        vm.generateNotification = generateNotification;
        vm.sendNotification = sendNotification;
        vm.cancelNotificationPreview = cancelNotificationPreview;

        activate();

        function activate() {
            PublicationService.getPublicationDetails(vm.pubId)
                .then(function (response) {
                    function hasEmail(author) {
                        return author.email;
                    }

                    response.data.registeredAuthors.forEach(function (author) {
                        author.send = true;
                    });

                    vm.authors = response.data.registeredAuthors.filter(hasEmail);
                    vm.pubReference = response.data.citation;
                    vm.pubLink = '/' + response.data.zdbID;
                });
        }

        function hasNoRecipients() {
            return !vm.authors.some(function (a) { return a.send; }) && !vm.additionalRecipients;
        }

        function editNotification() {
            vm.loading = true;
            vm.sendSuccess = false;
            vm.sendError = false;

            var sendAuthors = vm.recipients = vm.authors.filter(function (a) { return a.send; });

            vm.recipients = sendAuthors.map(function(a) { return a.email; });
            vm.noteText = 'Notified authors: ' + sendAuthors.map(function (a) { return a.display; }).join(', ');

            var additional = vm.additionalRecipients.split(/[,\s]+/);
            if (additional.length > 0 && additional[0]) {
                vm.recipients = vm.recipients.concat(additional);
                if (sendAuthors.length > 0) {
                    vm.noteText += ', ';
                }
                vm.noteText += additional.join(', ');
            }

            PublicationService.getCuratedEntities(vm.pubId)
                .then(function (resp) {
                    vm.curatedData = resp.data;
                    vm.editing = true;
                })
                .finally(function () {
                    vm.loading = false;
                });
        }

        function cancelNotificationEditing() {
            vm.editing = false;
        }

        function previewNotification() {
            vm.editing = false;
            vm.previewing = true;
        }

        function cancelNotificationPreview () {
            vm.editing = true;
            vm.previewing = false;
        }

        function generateDataLinks() {
            if (!vm.curatedData) {
                return;
            }
            return $sce.trustAsHtml('<ul>' +
              vm.curatedData
                .map(function (link) {
                    var item = '<a href="http://' + window.location.hostname + link.path + '">' + link.label + '</a>';
                    if (link.count) {
                        item += ' (' + link.count + ')';
                    }
                    return '<li>' + item + '</li>';
                }).join('') +
              '</ul>');
        }

        function generateNotification() {
            var notif =
                '<p>' + vm.salutation + ' ' + vm.names + ',</p>' +
                '<p>' + vm.intro + '</p>' +
                '<p><a href="http://' + window.location.hostname + vm.pubLink + '">' + vm.pubReference + '</a></p>' +
                '<p>' + vm.dataNote + '</p>' +
                $sce.valueOf(vm.generateDataLinks());
            if (vm.customNote) {
                notif += '<p>' + vm.customNote + '</p>';
            }
            notif +=
                '<p>' + vm.zfinDescription + '</p>' +
                '<p>' + vm.signOff + ',</p>' +
                '<p>' + vm.sender.name + '<br>' +
                vm.sender.group + '<br>' +
                vm.sender.email + '</p>' +
                '<p>' + vm.address[0] + '<br>' +
                vm.address[1] + '<br>' +
                vm.address[2] + '</p>';
            return $sce.trustAsHtml(notif);
        }

        function sendNotification() {
            vm.loading = true;
            PublicationService.sendAuthorNotification(vm.pubId, vm.recipients, $sce.valueOf(vm.generateNotification()))
                .then(function () {
                    vm.editing = false;
                    vm.previewing = false;
                    vm.sendSuccess = true;
                    vm.sendError = false;
                    addNote(vm.noteText);
                })
                .catch(function () {
                    vm.sendSuccess = false;
                    vm.sendError = true;
                })
                .finally(function () {
                    vm.loading = false;
                });
        }

        function addNote(text) {
            PublicationService.addNote(vm.pubId, text)
                .then(function (response) {
                    vm.notes.unshift(response.data);
                });
        }

    }
}());