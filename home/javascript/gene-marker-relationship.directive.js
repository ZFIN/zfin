;(function () {
    angular
        .module('app')
        .directive('geneMarkerRelationship', geneMarkerRelationship);

    function geneMarkerRelationship() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/gene-marker-relationship.directive.html',
            scope: {
                markerId: '@',
                edit: '@',
                markerAbbrev: '@',
                relationshipType:   '@'
                },
            controller: GeneMarkerRelationshipController,
            controllerAs: 'mkrreln',
            bindToController: true
        };

        return directive;
    }

    GeneMarkerRelationshipController.$inject = [$window,'MarkerService'];
    function GeneMarkerRelationshipController($window,MarkerService) {
        var mkrreln = this;
        mkrreln.newGene = '';
        mkrreln.newRelationship = '';
        mkrreln.openAddNewRelationship = openAddNewRelationship;
        mkrreln.close = close;
        mkrreln.addNewRelationship = addNewRelationship;
        mkrreln.removeRelationship = removeRelationship;
        mkrreln.editRelationship = editRelationship;
        mkrreln.addAttribution = addAttribution;
        mkrreln.removeAttribution = removeAttribution;
        mkrreln.grpname = "GENEDOM";
        mkrreln.errorMessage = '';
        init();

        function init() {

            MarkerService.getRelationshipsForEdit(mkrreln.markerId)
                
                .then(function (relationships) {
                    mkrreln.relationships = relationships;
                    })
              
                .catch(function (error) {
                    console.error(error);
                });
           
        }
        
        function openAddNewRelationship() {
            MarkerService.openModalPopup('new-relationship-modal');
        }
        function addNewRelationship() {
            if (!mkrreln.newRelationship) {
                mkrreln.errorMessage = 'Relationship cannot be empty.';
            } else if (!mkrreln.newGene) {
                mkrreln.errorMessage = 'Partner gene cannot be empty.';
            } else if (!mkrreln.newAttribution) {
                mkrreln.errorMessage = 'Reference cannot be empty.';
            } else {
                mkrreln.processing = true;
                var first = {zdbID: mkrreln.markerId};
                var second = {name: mkrreln.newGene};
                //var reln="gene contains small segment"
                MarkerService.addRelationship(first, second, mkrreln.newRelationship, mkrreln.newAttribution)
                    .then(function (relationship) {
                         mkrreln.relationships.unshift(relationship);
                        mkrreln.newGene = '';
                        mkrreln.newAttribution = '';
                        mkrreln.errorMessage='';
                        close();
                    })
                    .catch(function (error) {
                        mkrreln.errorMessage = error.data.message;
                    })
                    .finally(function () {
                    });
            }
        }

        function removeRelationship(relationship, index) {
            MarkerService.removeRelationship(relationship)
                .then(function () {
                    mkrreln.relationships.splice(index, 1);
                })
                .catch(function (error) {
                    mkrreln.error(error);
                });
        }

        function editRelationship(relationship) {
            // MarkerService.getAttributionForRelationship(relationship)
         MarkerService.openModalPopup('relationship-attribution-modal');
           
        }

        function addAttribution(pubId) {
            return MarkerService.addRelationshipAttribution(mkrreln.editing, pubId)
                .then(function (relationship) {
                    mkrreln.editing.Attributions = relationship.Attributions;
                });
        }

        function removeAttribution(Attribution, index) {
            return MarkerService.removeRelationshipAttribution(mkrreln.editing, Attribution)
                .then(function () {
                    mkrreln.editing.Attributions.splice(index, 1);
                });
        }
        
        function close() {
        
            mkrreln.errorMessage = '';
            mkrreln.newGene = '';
            mkrreln.newAttribution = '';
            MarkerService.closeModal();
        }
    }


}())