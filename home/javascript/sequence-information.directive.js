;(function () {
    angular
        .module('app')
        .directive('sequenceInfoEditLink', sequenceInfoEditLink);

    function sequenceInfoEditLink() {
        var seqInfoEditLink =
            '<a ng-show="edit" style="color: red; font-style: italic; font-weight: bold;" title="Edit sequence information" ng-href="/action/marker/sequence/edit/{{markerId}}">' +
            'Edit' +
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
        si.linkDisplays = [];
        si.databases = [];
        si.ind = 0;
        si.errorMessage = '';
        si.errorDb = '';
        si.errorAcc = '';
        si.errorRef = '';
        si.newDatabase = '';
        si.newAccession = '';
        si.accessionEdit = '';
        si.referenceEdit = '';
        si.newReference = '';
        si.references = [];
        si.seqenceInfo = null;
        si.openAddSequenceInfo = openAddSequenceInfo;
        si.addSequenceInfo = addSequenceInfo;
        si.openDeleteOtherMarkerLink = openDeleteOtherMarkerLink;
        si.deleteSeqenceInfo = deleteSeqenceInfo;
        si.openUpdateSequenceInfo = openUpdateSequenceInfo;
        si.updateSequenceInfo = updateSequenceInfo;
        si.addAttribution = addAttribution;
        si.deleteAttribution = deleteAttribution;
        si.close = close;

        init();

        function init() {
            MarkerService.getLinks(si.markerId,"marker linked sequence")
                .then(function (links) {
                    si.linkDisplays = links;
                    var previousDataType = si.linkDisplays[0].dataType;
                    for (i = 0; i < si.linkDisplays.length; i++) {
                        if (i > 0 && si.linkDisplays[i].dataType === previousDataType) {
                            si.linkDisplays[i].dataType = "";
                        } else {
                            previousDataType = si.linkDisplays[i].dataType;
                        }
                        si.linkDisplays[i].refLink = $sce.trustAsHtml(si.linkDisplays[i].attributionLink);
                    }
                })
                .catch(function (error) {
                    console.error(error);
                });

            MarkerService.getLinkDatabases("dblink adding on marker-edit")
                .then(function (databases) {
                    si.databases = [];
                    for (var i in databases) {
                        if (databases[i].zdbID === 'ZDB-FDBCONT-040412-37' ||    //GenBank (RNA)
                            databases[i].zdbID === 'ZDB-FDBCONT-040412-36' ||    //GenBank (Genomic)
                            databases[i].zdbID === 'ZDB-FDBCONT-040412-42' ||    //GenPept (Polypeptide)
                            databases[i].zdbID === 'ZDB-FDBCONT-040412-47'       //UniProt (Polypeptide)
                        ) {
                            databases[i].label = databases[i].name + " - " + databases[i].type;
                            si.databases.push(databases[i]);
                        }
                    }

                })
                .catch(function (error) {
                    console.error(error);
                });
        }

        function openAddSequenceInfo() {
            MarkerService.openModalPopup('new-sequence-information-modal');
        }

        function addSequenceInfo() {
            if (validated(1)) {
                MarkerService.addLink(si.markerId, si.newDatabase, si.newAccession.toUpperCase(), si.newReference)
                    .then(function (link) {
                        init();
                        close();
                    })
                    .catch(function (error) {
                        si.errorMessage = error.data.message;
                    });
            }
        }

        function openDeleteOtherMarkerLink(obj, ind) {
            si.seqenceInfo = obj;
            si.ind = ind;
            MarkerService.openModalPopup('delete-sequence-info-modal');
        }

        function deleteSeqenceInfo() {
            MarkerService.removeLink(si.seqenceInfo)
                .then(function () {
                    init();
                    close();
                })
                .catch(function (error) {
                    si.errorMessage = error.data.message;
                });
        }

        function openUpdateSequenceInfo(obj, ind) {
            si.seqenceInfo = obj;
            si.accessionEdit = obj.accession;
            si.newDatabase = obj.referenceDatabaseZdbID;
            si.ind = ind;
            MarkerService.openModalPopup('update-sequence-information-modal');
        }

        function updateSequenceInfo() {
            if (validated(0)) {
                si.references = si.seqenceInfo.references;
                MarkerService.removeLink(si.seqenceInfo)
                    .then(function () {
                        MarkerService.addLink(si.markerId, si.seqenceInfo.referenceDatabaseZdbID, si.accessionEdit.toUpperCase(), si.seqenceInfo.references[0].zdbID)
                            .then(function (link) {
                                si.seqenceInfo = link;
                                var referenceIds = [];
                                for(var i = 0; i < si.references.length; i++) {
                                    referenceIds.push(si.references[i].zdbID);
                                }
                                if (referenceIds.length > 1) {
                                    var pubID = "";
                                    for(var i = 1; i < referenceIds.length; i++) {
                                        pubID = referenceIds[i];
                                        MarkerService.addLinkReference(si.seqenceInfo, pubID)
                                            .then(function (seq) {
                                                si.seqenceInfo = seq;
                                                si.seqenceInfo.references = seq.references;
                                                si.newReference = '';
                                            }).catch(function (error) {
                                                si.errorAcc = error.data.message;
                                            }).finally(function () {
                                                init();
                                            });
                                    }
                                }
                                init();
                                close();
                            })
                            .catch(function (error) {
                                si.errorAcc = error.data.message;
                            });
                    })
                    .catch(function (error) {
                        si.errorAcc = error.data.message;
                    });
            }
        }

        function addAttribution() {
            if (!si.referenceEdit) {
                si.errorAcc = '';
                si.errorMessage = 'Reference cannot be empty.';
            } else {
                MarkerService.addLinkReference(si.seqenceInfo, si.referenceEdit)
                    .then(function (link) {
                        si.seqenceInfo.references = link.references;
                        si.referenceEdit = '';
                        si.errorMessage = '';
                        si.errorDb = '';
                        si.errorAcc = '';
                        si.errorRef = '';
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
                    si.errorDb = '';
                    si.errorAcc = '';
                    si.errorRef = '';
                    init();
                }).catch(function (error) {
                    si.errorMessage = error.data.message;
                });
        }

        function close() {
            si.errorMessage = '';
            si.errorDb = '';
            si.errorAcc = '';
            si.errorRef = '';
            si.newDatabase = '';
            si.newAccession = '';
            si.accessionEdit = '';
            si.newReference = '';
            si.referenceEdit = '';
            si.seqenceInfo = null;
            MarkerService.closeModal();
        }

        function isGenBank() {
            if (!si.newAccession) {
                return false;
            } else if (si.newDatabase === 'ZDB-FDBCONT-040412-37' ||
                       si.newDatabase === 'ZDB-FDBCONT-040412-36') {
                return true;
            } else {
                return false;
            }
        }

        function validated(checkRef) {
            si.errorMessage = '';
            si.errorDb = '';
            si.errorAcc = '';
            si.errorRef = '';
            if (!checkRef) {
                si.newAccession = si.accessionEdit;
            }
            if (!si.newDatabase) {
                si.errorDb = 'Database cannot be empty.';
                return false;
            } else if (!si.newAccession) {
                si.errorAcc = 'Accession cannot be empty.';
                return false;
            } else if (/\s/g.test(si.newAccession)) {
                si.errorAcc = 'Cannot have whitespace.';
                return false;
            } else if (checkRef && !si.newReference) {
                si.errorRef = 'Reference cannot be empty.';
                return false;
            } else if ( isGenBank() &&
                        !si.newAccession.charAt(0).match(/[a-z]/i) ) {
                si.errorAcc = 'GenBank acc starts with letter';
                return false;
            } else if (checkRef && si.newReference) {
                MarkerService.validateReference(si.newReference)
                    .then(function (response) {
                        if (response.data.errors.length > 0) {
                            si.errorRef = response.data.errors[0];
                        }
                    }).catch(function (error) {
                        si.errorMessage = error.data.message;
                });
                if(si.errorRef || si.errorMessage) {
                    return false;
                }
            }

            if (!checkRef) {
                si.newAccession = '';
            }

            return true;
        }

    }

}());