;
(function () {
    angular
        .module('app')
        .factory('PublicationService', PublicationService);

    PublicationService.$inject = ['$http'];
    function PublicationService($http) {
        return {
            getTopics              : getTopics,
            addTopic               : addTopic,
            updateTopic            : updateTopic,
            getStatus              : getStatus,
            updateStatus           : updateStatus,
            getNotes               : getNotes,
            addNote                : addNote,
            updateNote             : updateNote,
            deleteNote             : deleteNote,
            getCorrespondences     : getCorrespondences,
            addCorrespondence      : addCorrespondence,
            updateCorrespondence   : updateCorrespondence,
            deleteCorrespondence   : deleteCorrespondence,
            getPublicationDetails  : getPublicationDetails,
            getCuratedEntities     : getCuratedEntities,
            validatePubForClose    : validatePubForClose,
            sendAuthorNotification : sendAuthorNotification
        };

        function getTopics(id) {
            return $http.get('/action/publication/' + id + '/topics');
        }

        function addTopic(id, topic) {
            return $http.post('/action/publication/' + id + '/topics', topic);
        }

        function updateTopic(topic) {
            return $http.post('/action/publication/topics/' + topic.zdbID, topic);
        }

        function getStatus(id) {
            return $http.get('/action/publication/' + id + '/status');
        }

        function updateStatus(status) {
            return $http.post('/action/publication/' + status.pubZdbID + '/status', status);
        }

        function getNotes(id) {
            return $http.get('/action/publication/' + id + '/notes');
        }

        function addNote(id, txt) {
            return $http.post('/action/publication/' + id + '/notes', {text: txt});
        }

        function updateNote(note) {
            return $http.post('/action/publication/notes/' + note.zdbID, {text: note.text});
        }

        function deleteNote(note) {
            return $http.delete('/action/publication/notes/' + note.zdbID);
        }

        function getCorrespondences(id) {
            return $http.get('/action/publication/' + id + '/correspondences');
        }

        function addCorrespondence(id) {
            return $http.post('/action/publication/' + id + '/correspondences', {});
        }

        function updateCorrespondence(correspondence) {
            return $http.post('/action/publication/correspondences/' + correspondence.id, correspondence);
        }

        function deleteCorrespondence(corr) {
            return $http.delete('/action/publication/correspondences/' + corr.id);
        }

        function getPublicationDetails(id) {
            return $http.get('/action/publication/' + id + '/details');
        }

        function getCuratedEntities(id) {
            return $http.get('/action/publication/' + id + '/curatedEntities');
        }

        function validatePubForClose(id) {
            return $http.post('/action/publication/' + id + '/validate', {});
        }

        function sendAuthorNotification(id, recipients, message) {
            return $http.post('/action/publication/' + id + '/notification', {
                recipients: recipients,
                message: message
            });
        }
    }

}());