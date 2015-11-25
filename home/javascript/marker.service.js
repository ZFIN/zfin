;(function() {
    angular
        .module('app')
        .factory('MarkerService', MarkerService);

    MarkerService.$inject = ['$http'];
    function MarkerService($http) {
        return {
            getSuppliers: getSuppliers,
            addSupplier: addSupplier,
            removeSupplier: removeSupplier
        };

        function getSuppliers(markerId) {
            return $http.get('/action/marker/' + markerId + '/suppliers')
                .then(function (response) {
                    return response.data;
                });
        }

        function addSupplier(markerId, supplier) {
            return $http.post('/action/marker/' + markerId + '/suppliers', supplier)
                .then(function (response) {
                    return response.data;
                });
        }

        function removeSupplier(markerId, supplier) {
            return $http.delete('/action/marker/' + markerId + '/suppliers/' + supplier.zdbID);
        }
    }
}());