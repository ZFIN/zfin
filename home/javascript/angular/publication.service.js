;
(function () {
    angular
        .module('app')
        .factory('PublicationService', PublicationService);

    PublicationService.$inject = ['$http'];
    function PublicationService($http) {

        return {
            getCorrespondences     : getCorrespondences,
            addCorrespondence      : addCorrespondence,
            updateCorrespondence   : updateCorrespondence,
            deleteCorrespondence   : deleteCorrespondence,
            getPublicationDetails  : getPublicationDetails,
            getFiles               : getFiles,
            getFileTypes           : getFileTypes,
            addFile                : addFile,
            deleteFile             : deleteFile
        };

        function getCorrespondences(id) {
            return $http.get('/action/publication/' + id + '/correspondences');
        }

        function addCorrespondence(id, message) {
            return $http.post('/action/publication/' + id + '/correspondences', message);
        }

        function updateCorrespondence(correspondence) {
            return $http.post('/action/publication/correspondences/' + correspondence.id, correspondence);
        }

        function deleteCorrespondence(corr) {
            return $http.delete('/action/publication/correspondences/' + corr.id + '?outgoing=' + corr.outgoing);
        }

        function getPublicationDetails(id) {
            return $http.get('/action/publication/' + id + '/details', {cache: true});
        }

        function getFiles(id) {
            return $http.get('/action/publication/' + id + '/files');
        }

        function getFileTypes() {
            return $http.get('/action/publication/file-types');
        }

        function addFile(pubId, fileType, file) {
            var form = new FormData();
            form.append('fileType', fileType);
            form.append('file', file);
            return $http.post('/action/publication/' + pubId + '/files', form, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            });
        }

        function deleteFile(file) {
            return $http({
                url: '/action/publication/files/' + file.id,
                method: 'DELETE',
                transformResponse: undefined
            });
        }
    }

}());