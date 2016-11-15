;(function () {
    angular
        .module('app')
        .directive('sequenceInfoEditLink', sequenceInfoEditLink);

    function sequenceInfoEditLink() {
        var seqInfoEditLink =
            '<br/>' +
            '<a ng-show="edit" style="color: red; font-weight: bold;" title="Edit sequence information" ng-href="/action/marker/sequence/edit/{{markerId}}">' +
            'Edit Sequence Information' +
            '</a>';

        var directive = {
            restrict: 'EA',
            template: seqInfoEditLink,
            scope: {
                markerId: '@',
                edit: '='
            }
        };

        return directive;
    }

    angular
        .module('app')
        .directive('sequenceInformation', sequenceInformation);

    function sequenceInformation() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/sequence-information.directive.html',
            scope: {
                markerId: '@'
            },
            controller: SequenceInformationController,
            controllerAs: 'si',
            bindToController: true
        };

        return directive;
    }

    SequenceInformationController.$inject = ['$sce', 'MarkerService'];
    function SequenceInformationController($sce, MarkerService) {
        var si = this;
        si.links = [];
        si.ind = 0;
        si.errorMessage = '';
        si.seqenceInfo = null;
        si.newReference = '';
        si.openDeleteOtherMarkerLink = openDeleteOtherMarkerLink;
        si.deleteSeqenceInfo = deleteSeqenceInfo;
        si.openEditAttribution = openEditAttribution;
        si.addAttribution = addAttribution;
        si.deleteAttribution = deleteAttribution;
        si.close = close;

        init();

        function init() {
            MarkerService.getLinks(si.markerId,"marker linked sequence")
                .then(function (links) {
                    si.links = links;
                    for (i = 0; i < si.links.length; i++) {
                        si.links[i].refLink = $sce.trustAsHtml(si.links[i].attributionLink);
                    }
                })
                .catch(function (error) {
                    console.error(error);
                });

        }

        function openDeleteOtherMarkerLink(obj, ind) {
            si.seqenceInfo = obj;
            si.ind = ind;
            MarkerService.openModalPopup('delete-sequence-info-modal');
        }

        function deleteSeqenceInfo() {
            MarkerService.removeLink(si.seqenceInfo)
                .then(function () {
                    si.links.splice(si.ind, 1);
                    close();
                })
                .catch(function (error) {
                    si.errorMessage = error.data.message;
                });
        }

        function openEditAttribution(obj, ind) {
            si.seqenceInfo = obj;
            si.ind = ind;
            MarkerService.openModalPopup('sequence-information-attribution-modal');
        }

        function addAttribution() {
            if (!si.newReference) {
                si.errorMessage = 'Attribution/reference cannot be empty.';
            } else {
                MarkerService.addLinkReference(si.seqenceInfo, si.newReference)
                    .then(function (link) {
                        si.seqenceInfo.references = link.references;
                        si.newReference = '';
                        si.errorMessage = '';
                        init();
                    }).catch(function (error) {
                        si.errorMessage = error.data.message;
                    });
            }
        }

        function deleteAttribution(ind) {
            MarkerService.removeLinkReference(si.seqenceInfo, si.seqenceInfo.references[ind])
                .then(function () {
                    si.seqenceInfo.references.splice(ind, 1);
                    si.errorMessage = '';
                    init();
                }).catch(function (error) {
                    si.errorMessage = error.data.message;
                });;
        }

        function close() {
            si.errorMessage = '';
            si.ind = 0;
            si.seqenceInfo = null;
            MarkerService.closeModal();
        }

    }

}());