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
                edit: '=',
                markerAbbrev: '@',
                interacts: '@'
                },
            controller: GeneMarkerRelationshipController,
            controllerAs: 'mkrreln',
            bindToController: true
        };

        return directive;
    }

    GeneMarkerRelationshipController.$inject = ['MarkerService'];
    function GeneMarkerRelationshipController(MarkerService) {
        var mkrreln = this;
        mkrreln.newGene = '';
        mkrreln.newRelationship = '';
        mkrreln.openAddNewRelationship = openAddNewRelationship;
        mkrreln.close = close;
        mkrreln.addNewRelationship = addNewRelationship;
        mkrreln.openEditAttribution = openEditAttribution;
        mkrreln.addAttribution = addAttribution;
        mkrreln.deleteAttribution = deleteAttribution;
        mkrreln.openDeleteRelationship = openDeleteRelationship;
        mkrreln.deleteRelationship = deleteRelationship;
        mkrreln.errorMessage = '';
        mkrreln.otherLink = null;
        mkrreln.ind = 0;
        init();

        function init() {
            MarkerService.getRelationshipTypes(mkrreln.markerId,mkrreln.interacts)

                .then(function (relationshipTypes) {
                    mkrreln.relationshipTypes = relationshipTypes;
                                  })

                .catch(function (error) {
                    console.error(error);
                });


            MarkerService.getRelationshipsForEdit(mkrreln.markerId,mkrreln.interacts)
                
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
                mkrreln.errorMessage='';
                mkrreln.processing = true;
                var first = {zdbID: mkrreln.markerId};
                var second = {name: mkrreln.newGene};
                //var reln="gene contains small segment"
                MarkerService.addGeneRelationship(first, second, mkrreln.newRelationship, mkrreln.newAttribution)
                    .then(function (relationship) {
                         mkrreln.relationships.unshift(relationship);
                        mkrreln.newGene = '';
                        mkrreln.newAttribution = '';
                        mkrreln.errorMessage='';
                        close();
                        init();
                    })
                    .catch(function (error) {
                                                mkrreln.errorMessage = error.data.message;
                    })
                    .finally(function () {
                    });

            }
        }


        function openEditAttribution(obj, ind) {

            mkrreln.otherLink = obj;
            mkrreln.ind = ind;
            MarkerService.openModalPopup('relationship-attribution-modal');
        }

        function addAttribution() {
            if (!mkrreln.newAttribution) {
                mkrreln.errorMessage = 'Attribution/reference cannot be empty.';
            } else {
                MarkerService.addGeneMarkerRelationshipReference(mkrreln.otherLink, mkrreln.newAttribution)
                    .then(function (relationship) {
                        mkrreln.relationships.attributionZdbIDs = relationship.attributionZdbIDs;
                        mkrreln.newAttribution = '';
                        mkrreln.errorMessage = '';
                        init();
                        close();
                    }).catch(function (error) {
                    mkrreln.errorMessage = error.data.message;
                });
            }
        }

        function deleteAttribution(ind) {

            MarkerService.removeMarkerRelationshipReference(mkrreln.otherLink, mkrreln.otherLink.attributionZdbIDs[ind])
                .then(function () {
                    mkrreln.otherLink.attributionZdbIDs.splice(ind, 1);
                    mkrreln.errorMessage = '';
                    init();
                }).catch(function (error) {
                mkrreln.errorMessage = error.data.message;
            });
        }

        function openDeleteRelationship(obj, ind) {
            mkrreln.otherLink = obj;
            mkrreln.ind = ind;
            MarkerService.openModalPopup('delete-relationship-modal');
        }



        function deleteRelationship() {
            MarkerService.removeRelationship(mkrreln.otherLink)
                .then(function () {
                    mkrreln.relationships.splice(mkrreln.ind, 1);
                    close();
                })
                .catch(function (error) {
                    mkrreln.errorMessage = error.data.message;
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