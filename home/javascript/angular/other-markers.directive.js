;(function () {
    angular
        .module('app')
        .directive('otherMarkers', otherMarkers);

    function otherMarkers() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/other-markers.directive.html',
            scope: {
                markerId: '@',
                edit: '='
            },
            controller: OtherMarkersController,
            controllerAs: 'om',
            bindToController: true
        };

        return directive;
    }

    OtherMarkersController.$inject = ['$sce', 'MarkerService'];
    function OtherMarkersController($sce, MarkerService) {
        var om = this;
        om.links = [];
        om.databases = [];
        om.otherLink = null;
        om.ind = 0;
        om.errorMessage = '';
        om.errorRef = '';
        om.newDatabase = '';
        om.newAccession = '';
        om.newReference = '';
        om.openAddOtherMarkerLink = openAddOtherMarkerLink;
        om.addOtherMarkerLink = addOtherMarkerLink;
        om.openEditAttribution = openEditAttribution;
        om.addAttribution = addAttribution;
        om.deleteAttribution = deleteAttribution;
        om.openDeleteOtherMarkerLink = openDeleteOtherMarkerLink;
        om.deleteOtherMarkerLink = deleteOtherMarkerLink;
        om.close = close;

        function init() {
            MarkerService.getLinks(om.markerId,"other marker pages")
                .then(function (links) {
                    om.links = links;
                    for (i = 0; i < om.links.length; i++) {
                        om.links[i].refLink = $sce.trustAsHtml(om.links[i].attributionLink);
                    }
                })
                .catch(function (error) {
                    console.error(error);
                });

            MarkerService.getLinkDatabases("other marker pages")
                .then(function (databases) {
                    om.databases = databases;
                })
                .catch(function (error) {
                    console.error(error);
                });
        };

        this.$onInit = function () {
            init();
        };

        function openAddOtherMarkerLink() {
             MarkerService.openModalPopup('new-other-marker-link-modal');
        }

        function addOtherMarkerLink() {
            if (!om.newDatabase) {
                om.errorMessage = 'Database cannot be empty.';
            } else if (!om.newAccession) {
                om.errorMessage = 'Accession number cannot be empty.';
            } else if (publicationIdValidated()) {
                MarkerService.addLink(om.markerId, om.newDatabase, om.newAccession, om.newReference)
                    .then(function (link) {
                        init();
                        close();
                    })
                    .catch(function (error) {
                        setErrorMessageFromResponse(error);
                    });
            }
        }

        function openEditAttribution(obj, ind) {
            om.otherLink = obj;
            om.ind = ind;
            MarkerService.openModalPopup('other-marker-attribution-modal');
        }

        function addAttribution() {
            if (publicationIdValidated()) {
                 MarkerService.addLinkReference(om.otherLink, om.newReference)
                    .then(function (link) {
                        om.otherLink.references = link.references;
                        om.newReference = '';
                        om.errorMessage = '';
                        init();
                    }).catch(function (error) {
                        setErrorMessageFromResponse(error);
                    });
            }
        }

        function deleteAttribution(ind) {
            MarkerService.removeLinkReference(om.otherLink, om.otherLink.references[ind])
                .then(function () {
                    om.otherLink.references.splice(ind, 1);
                    om.errorMessage = '';
                    init();
                 }).catch(function (error) {
                    setErrorMessageFromResponse(error);
                 });;
        }

        function openDeleteOtherMarkerLink(obj, ind) {
            om.otherLink = obj;
            om.ind = ind;
            MarkerService.openModalPopup('delete-other-marker-link-modal');
        }

        function deleteOtherMarkerLink() {
            MarkerService.removeLink(om.otherLink)
                .then(function () {
                    om.links.splice(om.ind, 1);
                    close();
                })
                .catch(function (error) {
                    setErrorMessageFromResponse(error);
                    init();
                    close();
                })
                .finally(function () {
                    init();
                    close();
                });
        }

        function close() {
            om.errorMessage = '';
            om.newDatabase = '';
            om.newAccession = '';
            om.newReference = '';
            om.errorRef = '';
            om.ind = 0;
            om.otherLink = null;
            MarkerService.closeModal();
        }
        
        function publicationIdValidated() {
            om.errorRef = "";
            om.errorMessage = "";
            if (!om.newReference) {
                om.errorMessage = 'Attribution/reference cannot be empty.';
                return false;
            } 
            MarkerService.validateReference(om.newReference)
                .then(function (response) {
                    if ( response.data &&  response.data.errors && response.data.errors.length > 0) {
                        om.errorRef = response.data.errors[0];
                    }
                }).catch(function (error) {
                    setErrorMessageFromResponse(error);
                });
            return (om.errorRef === "");
        }

        /**
         * Sets the error message from the response.
         * This function should only be called if there is an error.
         * It will set the om.errorMessage to the error message from the response.
         * @param error
         */
        function setErrorMessageFromResponse(error) {
            let msg = error && error.data && error.data.message;
            if (msg) {
                om.errorMessage = msg;
            } else {
                om.errorMessage = 'An error occurred.';
            }
        }
    }
}());