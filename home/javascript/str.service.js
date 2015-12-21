;(function() {
    angular
        .module('app')
        .factory('STRService', STRService);

    STRService.$inject = ['$http'];
    function STRService($http) {
        return {
            getStrDetails: getStrDetails,
            saveStrDetails: saveStrDetails
        };

        function returnResponseData(response) {
            return response.data;
        }

        function getStrDetails(id) {
            return $http.get('/action/str/' + id + '/details')
                .then(returnResponseData);
        }

        function saveStrDetails(id, details) {
            return $http.post('/action/str/' + id + '/details', details)
                .then(returnResponseData);
        }
    }
}());