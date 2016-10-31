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
                edit: '@'
            },
            controller: OtherMarkersController,
            controllerAs: 'om',
            bindToController: true
        };

        return directive;
    }

    OtherMarkersController.$inject = [$window, $sce, 'MarkerService'];
    function OtherMarkersController($window, $sce, MarkerService) {
        var om = this;

        om.links = [];
        om.databases = [];
        om.otherLink = null;
        om.ind = 0;
        om.errorMessage = '';
        om.newDatabase = '';
        om.newAccession = '';
        om.newReference = '';
        om.references = [];

        om.openAddOtherMarkerLink = openAddOtherMarkerLink;
        om.addOtherMarkerLink = addOtherMarkerLink;
        om.openEditAttribution = openEditAttribution;
        om.addAttribution = addAttribution;
        om.deleteAttribution = deleteAttribution;
        om.openDeleteOtherMarkerLink = openDeleteOtherMarkerLink;
        om.deleteOtherMarkerLink = deleteOtherMarkerLink;
        om.close = close;

        init();

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

        }

        function openAddOtherMarkerLink() {
             MarkerService.openModalPopup('new-other-marker-link-modal');
        }

        function addOtherMarkerLink() {
            if (!om.newDatabase) {
                om.errorMessage = 'Database cannot be empty.';
            } else if (!om.newAccession) {
                om.errorMessage = 'Accession number cannot be empty.';
            } else if (!om.newReference) {
                om.errorMessage = 'Reference cannot be empty.';
            } else {
                MarkerService.addLink(om.markerId, om.newDatabase, om.newAccession, om.newReference)
                    .then(function (link) {
                        om.links.unshift(link);
                        close();
                    })
                    .catch(function (error) {
                       om.errorMessage = error.data.message;
                    })
                    .finally(function () {
                    });
            }
        }

        function openEditAttribution(obj, ind) {
            om.otherLink = obj;
            om.ind = ind;
            MarkerService.openModalPopup('other-marker-attribution-modal');
        }

        function addAttribution() {
            if (!om.newReference) {
                om.errorMessage = 'Attribution/reference cannot be empty.';
            } else {
                 MarkerService.addLinkReference(om.otherLink, om.newReference)
                    .then(function (link) {
                        om.otherLink.references = link.references;
                        om.newReference = '';
                        om.errorMessage = '';
                    }).catch(function (error) {
                        om.errorMessage = error.data.message;
                    });
            }
        }

        function deleteAttribution(ind) {
            MarkerService.removeLinkReference(om.otherLink, om.otherLink.references[ind])
                .then(function () {
                    om.otherLink.references.splice(ind, 1);
                    om.errorMessage = '';
                 }).catch(function (error) {
                     om.errorMessage = error.data.message;
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
                    om.errorMessage = error.data.message;
                });
        }

        function close() {
            om.errorMessage = '';
            om.newDatabase = '';
            om.newAccession = '';
            om.newReference = '';
            om.references = [];
            om.ind = 0;
            om.otherLink = null;
            MarkerService.closeModal();
        }
    }
}());