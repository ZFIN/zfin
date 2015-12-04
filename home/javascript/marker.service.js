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
            removeAlias: removeAlias,
            addAliasReference: addAliasReference,
            removeAliasReference: removeAliasReference,
            getRelationships: getRelationships,
            addRelationship: addRelationship,
            removeRelationship: removeRelationship,
            addRelationshipReference: addRelationshipReference,
            removeRelationshipReference: removeRelationshipReference,
            getNotes: getNotes,
            updatePublicNote: updatePublicNote,
            addCuratorNote: addCuratorNote,
            updateCuratorNote: updateCuratorNote,
            deleteCuratorNote: deleteCuratorNote
        };

        function returnResponseData(response) {
            return response.data;
        }

        // === SUPPLIERS ===

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

        // === ALIASES ===

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

        function removeAlias(alias) {
            return $http.delete('/action/marker/alias/' + alias.zdbID);
        }

        function addAliasReference(alias, pubId) {
            return $http.post('/action/marker/alias/' + alias.zdbID + '/references', {zdbID: pubId})
                .then(returnResponseData);
        }

        function removeAliasReference(alias, reference) {
            return $http.delete('/action/marker/alias/' + alias.zdbID + '/references/' + reference.zdbID);
        }

        // === RELATIONSHIPS ===

        function getRelationships(markerId) {
            return $http.get('/action/marker/' + markerId + '/relationships')
                .then(returnResponseData);
        }

        function addRelationship(first, second, type, pubId) {
            var relationship = {
                "relationship": type,
                "first": first,
                "second": second,
                "references": [{"zdbID": pubId}]
            };
            return $http.post('/action/marker/relationship', relationship)
                .then(returnResponseData);
        }

        function removeRelationship(relationship) {
            return $http.delete('/action/marker/relationship/' + relationship.zdbID);
        }

        function addRelationshipReference(alias, pubId) {
            return $http.post('/action/marker/relationship/' + alias.zdbID + '/references', {zdbID: pubId})
                .then(returnResponseData);
        }

        function removeRelationshipReference(alias, reference) {
            return $http.delete('/action/marker/relationship/' + alias.zdbID + '/references/' + reference.zdbID);
        }

        // === NOTES ===

        function getNotes(markerId) {
            return $http.get('/action/marker/' + markerId + '/notes')
                .then(returnResponseData);
        }

        function updatePublicNote(markerId, note) {
            return $http.post('/action/marker/' + markerId + '/public-note', note)
                .then(returnResponseData);
        }

        function addCuratorNote(markerId, note) {
            return $http.post('/action/marker/' + markerId + '/curator-notes', note)
                .then(returnResponseData);
        }

        function updateCuratorNote(markerId, note, newNote) {
            note.noteData = newNote;
            return $http.post('/action/marker/' + markerId + '/curator-notes/' + note.zdbID, note)
                .then(returnResponseData);
        }

        function deleteCuratorNote(markerId, note) {
            return $http.delete('/action/marker/' + markerId + '/curator-notes/' + note.zdbID);
        }
    }
}());