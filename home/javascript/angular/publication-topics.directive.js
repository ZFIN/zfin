;(function () {
    angular
        .module('app')
        .directive('publicationTopics', publicationTopics);

    function publicationTopics() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/publication-topics.directive.html',
            scope: {
                pubId: '@',
                topics: '='
            },
            controller: PublicationTopicsController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PublicationTopicsController.$inject = ['PublicationService', 'ZfinUtils'];
    function PublicationTopicsController(PublicationService, ZfinUtils) {
        var vm = this;

        vm.toggleTopicDataFound = toggleTopicDataFound;
        vm.getTopicStatus = getTopicStatus;
        vm.isNewTopic = isNewTopic;
        vm.isOpenTopic = isOpenTopic;
        vm.isClosedTopic = isClosedTopic;
        vm.openTopic = openTopic;
        vm.closeTopic = closeTopic;
        vm.unopenTopic = unopenTopic;

        function toggleTopicDataFound(topic, idx) {
            topic.dataFound = !topic.dataFound;
            addOrUpdateTopic(topic, idx);
        }

        function getTopicStatus(topic) {
            if (vm.isOpenTopic(topic)) {
                return 'Opened ' + ZfinUtils.timeago(topic.openedDate);
            } else if (vm.isClosedTopic(topic)) {
                return 'Closed';
            } else {
                return '';
            }
        }

        function isNewTopic(topic) {
            return !topic.openedDate && !topic.closedDate;
        }

        function isOpenTopic(topic) {
            return topic.openedDate && !topic.closedDate;
        }

        function isClosedTopic(topic) {
            return topic.closedDate;
        }

        function openTopic(topic, idx) {
            topic.openedDate = Date.now();
            topic.closedDate = null;
            topic.dataFound = true;
            addOrUpdateTopic(topic, idx);
        }

        function closeTopic(topic, idx) {
            topic.closedDate = Date.now();
            topic.dataFound = true;
            addOrUpdateTopic(topic, idx);
        }

        function unopenTopic(topic, idx) {
            topic.openedDate = null;
            addOrUpdateTopic(topic, idx);
        }

        function addOrUpdateTopic(topic, idx) {
            var request;
            if (topic.zdbID) {
                request = PublicationService.updateTopic(topic);
            } else {
                request = PublicationService.addTopic(vm.pubId, topic);
            }
            request.then(function (response) {
                vm.topics[idx] = response.data;
            });
        }
    }
}());