;
(function () {
    angular
        .module('app')
        .factory('MarkerService', MarkerService);

    MarkerService.$inject = ['$http'];
    function MarkerService($http) {
        return {
            getSuppliers: getSuppliers,
            addSupplier: addSupplier,
            removeSupplier: removeSupplier,
            getAliases: getAliases,
            addAlias: addAlias,
            addAliasReference: addAliasReference,
            removeAliasReference: removeAliasReference
        };

        function returnResponseData(response) {
            return response.data;
        }

        function getSuppliers(markerId) {
            return $http.get('/action/marker/' + markerId + '/suppliers')
                .then(returnResponseData);
        }

        function addSupplier(markerId, supplier) {
            return $http.post('/action/marker/' + markerId + '/suppliers', supplier)
                .then(returnResponseData);
        }

        function removeSupplier(markerId, supplier) {
            return $http.delete('/action/marker/' + markerId + '/suppliers/' + supplier.zdbID);
        }

        function getAliases(markerId) {
            return $http.get('/action/marker/' + markerId + '/aliases')
                .then(returnResponseData);
        }

        function addAlias(markerId, name, pubId) {
            var alias = {
                alias: name,
                references: [{zdbID: pubId}]
            };
            return $http.post('/action/marker/' + markerId + '/aliases', alias)
                .then(returnResponseData);
        }

        function addAliasReference(alias, pubId) {
            return $http.post('/action/marker/alias/' + alias.zdbID + '/references', {zdbID: pubId})
                .then(returnResponseData);
        }

        function removeAliasReference(alias, reference) {
            return $http.delete('/action/marker/alias/' + alias.zdbID + '/references/' + reference.zdbID);
        }
    }
}());