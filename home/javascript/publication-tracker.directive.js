;(function () {
    angular
        .module('app')
        .directive('publicationTracker', publicationTracker);

    function publicationTracker() {
        var template = '' +
            '<div class="panel panel-default">' +
            '  <div class="panel-heading">' +
            '    <h3 class="panel-title">Status</h3>' +
            '  </div>' +
            '  <div publication-status pub-id="{{vm.pubId}}" curator-id="{{vm.curatorId}}" topics="vm.topics" notes="vm.notes"></div>' +
            '</div>' +
            '<div class="panel panel-default">' +
            '  <div class="panel-heading">' +
            '    <h3 class="panel-title">Topics</h3>' +
            '  </div>' +
            '  <div publication-topics pub-id="{{vm.pubId}}" topics="vm.topics"></div>' +
            '</div>' +
            '<div class="panel panel-default">' +
            '  <div class="panel-heading">' +
            '    <h3 class="panel-title">Notes</h3>' +
            '  </div>' +
            '  <div publication-notes pub-id="{{vm.pubId}}" notes="vm.notes"></div>' +
            '</div>' +
            '<div class="panel panel-default">' +
            '  <div class="panel-heading">' +
            '    <h3 class="panel-title">Contact Authors</h3>' +
            '  </div>' +
            '  <div publication-author-notif pub-id="{{vm.pubId}}" curator-first="{{vm.curatorFirst}}" curator-last="{{vm.curatorLast}}" curator-email="{{vm.curatorEmail}}" notes="vm.notes"></div>' +
            '</div>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                pubId: '@',
                curatorId: '@',
                curatorFirst: '@',
                curatorLast: '@',
                curatorEmail: '@'
            },
            controller: PublicationTrackerController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PublicationTrackerController.$inject = ['PublicationService'];
    function PublicationTrackerController(PublicationService) {
        var vm = this;

        vm.notes = [];
        vm.topics = [];

        activate();

        function activate() {
            PublicationService.getNotes(vm.pubId)
                .then(function (response) {
                    vm.notes = response.data;
                });
            PublicationService.getTopics(vm.pubId)
                .then(function (response) {
                    vm.topics = response.data;
                });
        }
    }

}());