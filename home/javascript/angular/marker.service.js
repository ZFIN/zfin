;(function () {
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
            getRelationshipTypes: getRelationshipTypes,
            getRelationships: getRelationships,
            getRelationshipsForEdit: getRelationshipsForEdit,
            addRelationship: addRelationship,
            addGeneRelationship: addGeneRelationship,
            removeRelationship: removeRelationship,
            addRelationshipReference: addRelationshipReference,
            removeRelationshipReference: removeRelationshipReference,
            getNotes: getNotes,
            updatePublicNote: updatePublicNote,
            addCuratorNote: addCuratorNote,
            updateCuratorNote: updateCuratorNote,
            deleteCuratorNote: deleteCuratorNote,
            getLinks: getLinks,
            getLinkDatabases: getLinkDatabases,
            addLink: addLink,
            removeLink: removeLink,
            addLinkReference: addLinkReference,
            removeLinkReference: removeLinkReference,
            openModalPopup: openModalPopup,
            closeModal: closeModal,
            validateReference: validateReference
        };

        function returnResponseData(response) {
            return response.data;
        }

        // === SUPPLIERS ===

        function getSuppliers(markerId) {
            return $http.get('/action/marker/' + markerId + '/suppliers')
                .then(returnResponseData);
        }

        function addSupplier(markerId, supplierName) {
            var supplier = {name: supplierName};
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


        function getRelationshipTypes(markerId,interacts) {
            return $http.get('/action/marker/'+ markerId + '/relationshipTypes?interacts=' +interacts)
                .then(returnResponseData)
        }

        function getRelationships(markerId) {
            return $http.get('/action/marker/' + markerId + '/relationships')
                .then(returnResponseData);
        }

        function getRelationshipsForEdit(markerId,interacts) {
            return $http.get('/action/marker/' + markerId + '/relationshipsForEdit?interacts=' +interacts)
                .then(returnResponseData);
        }

        function addRelationship(first, second, type, pubId) {
            var relationship = {
                "markerRelationshipType": { name: type },
                "firstMarker": first,
                "secondMarker": second,
                "references": [{"zdbID": pubId}]
            };
            return $http.post('/action/marker/relationship', relationship)
                .then(returnResponseData);
        }

        function addGeneRelationship(first, second, type, pubId) {
            var relationship = {
                "markerRelationshipType": { name: type },
                "firstMarker": first,
                "secondMarker": second,
                "references": [{"zdbID": pubId}]
            };
            return $http.post('/action/marker/gene-relationship', relationship)
                .then(returnResponseData);
        }

        function removeRelationship(relationshipId) {
            return $http.delete('/action/marker/relationship/' + relationshipId);
        }

        function addRelationshipReference(relationshipId, pubId) {
            return $http.post('/action/marker/relationship/' + relationshipId + '/references', {zdbID: pubId})
                .then(returnResponseData);
        }

        function removeRelationshipReference(relationshipId, referenceId) {
            return $http.delete('/action/marker/relationship/' + relationshipId  + '/references/' + referenceId);
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

        // === LINKS ===

        function getLinks(markerId, group) {
            return $http.get('/action/marker/' + markerId + '/links?group=' + group)
                .then(returnResponseData);
        }

        function getLinkDatabases(group) {
            return $http.get('/action/marker/link/databases?group=' + group)
                .then(returnResponseData)
        }

        function addLink(markerId, fdbId, accession, pubId) {
            var link = {
                referenceDatabaseZdbID: fdbId,
                accession: accession,
                references: [{zdbID: pubId}]
            };
            return $http.post('/action/marker/' + markerId + '/links', link)
                .then(returnResponseData);
        }

        function removeLink(link) {
            return $http.delete('/action/marker/link/' + link.dblinkZdbID);
        }

        function addLinkReference(link, pubId) {
            return $http.post('/action/marker/link/' + link.dblinkZdbID + '/references', {zdbID: pubId})
                .then(returnResponseData);
        }

        function removeLinkReference(link, reference) {
            return $http.delete('/action/marker/link/' + link.dblinkZdbID + '/references/' + reference.zdbID);
        }
        
        function openModalPopup(element) {
            $('#' + element)
                .modal({
                    escapeClose: true,
                    clickClose: true,
                    showClose: true,
                    fadeDuration: 100
                })
                .on($.modal.AFTER_CLOSE, function () {
                });
        }

        function closeModal() {
            $.modal.close();
        }

        function validateReference(pubID) {
            return $http.post('/action/marker/link/reference/' + pubID + '/validate', {});
        }
    }
}());