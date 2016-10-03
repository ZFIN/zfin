;
(function () {
    angular
        .module('app')
        .factory('PublicationService', PublicationService);

    PublicationService.$inject = ['$http', 'ZfinUtils'];
    function PublicationService($http, zf) {

        var locationCache = null;

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
            sendAuthorNotification : sendAuthorNotification,
            getStatuses            : getStatuses,
            getLocations           : getLocations,
            getPriorities          : getPriorities,
            getCurators            : getCurators,
            searchPubStatus        : searchPubStatus,
            statusNeedsOwner       : statusNeedsOwner,
            statusNeedsLocation    : statusNeedsLocation,
            statusHasPriority      : statusHasPriority,
            getFiles               : getFiles,
            getFileTypes           : getFileTypes
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

        function getStatuses() {
            return $http.get('/action/publication/statuses');
        }

        function getLocationsAndPriorities() {
            if (!locationCache) {
                locationCache = $http.get('/action/publication/locations');
            }
            return locationCache;
        }

        function getLocations() {
            return getLocationsAndPriorities()
                .then(function (response) {
                    var responseCopy = angular.copy(response);
                    responseCopy.data = responseCopy.data.filter(function (item) {
                        return item.role === 'CURATOR';
                    });
                    return responseCopy;
                });
        }

        function getPriorities() {
            return getLocationsAndPriorities()
                .then(function (response) {
                    var responseCopy = angular.copy(response);
                    responseCopy.data = responseCopy.data.filter(function (item) {
                        return item.role === 'INDEXER';
                    });
                    return responseCopy;
                });
        }

        function getCurators() {
            return $http.get('/action/publication/curators');
        }

        function searchPubStatus(opts) {
            return $http.get('/action/publication/search-status', {params: opts});
        }

        function statusNeedsOwner(status) {
            var type = zf.get(status, 'type');
            return type === 'CURATING' || type === 'WAIT';
        }

        function statusNeedsLocation(status) {
            return zf.get(status, 'type') === 'READY_FOR_CURATION';
        }

        function statusHasPriority(status) {
            return zf.get(status, 'type') === 'READY_FOR_INDEXING';
        }

        function getFiles(id) {
            return $http.get('/action/publication/' + id + '/files');
        }

        function getFileTypes() {
            return $http.get('/action/publication/file-types');
        }
    }

}());