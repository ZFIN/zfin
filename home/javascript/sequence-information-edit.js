;(function () {
    angular.module('app', [])
        .controller('sequenceInfoCtrl', ['$scope', '$http', '$sce', function ($scope, $http, $sce) {
            var seqInfoCtrl = this;

            seqInfoCtrl.linkDisplays = [];
            seqInfoCtrl.newDatabase = '';
            seqInfoCtrl.errorMessage = "";
            seqInfoCtrl.errorDb = "";
            seqInfoCtrl.errorAcc = "";
            seqInfoCtrl.errorRef = "";
            seqInfoCtrl.newAccession = '';
            seqInfoCtrl.newReference = '';
            seqInfoCtrl.accessionEdit = '';
            seqInfoCtrl.referenceEdit = '';
            seqInfoCtrl.seqenceInfo = null;

            $scope.initiate = initiate;
            $scope.getSequences = getSequences;
            $scope.getDatabases = getDatabases;

            seqInfoCtrl.close = close;

            function initiate(markerZdbId) {
                $scope.markerId = markerZdbId;
                getSequences();
                getDatabases();
            }

            function getSequences() {
                getLinks($scope.markerId,"marker linked sequence")
                    .then(function (links) {
                        seqInfoCtrl.linkDisplays = links;
                        var previousDataType = seqInfoCtrl.linkDisplays[0].dataType;
                        for (i = 0; i < seqInfoCtrl.linkDisplays.length; i++) {
                            seqInfoCtrl.linkDisplays[i].referenceDatabaseDisplay = seqInfoCtrl.linkDisplays[i].referenceDatabaseName
                                                                                 + " - "
                                                                                 + seqInfoCtrl.linkDisplays[i].dataType;
                            if (i > 0 && seqInfoCtrl.linkDisplays[i].dataType === previousDataType) {
                                seqInfoCtrl.linkDisplays[i].dataType = "";
                            } else {
                                previousDataType = seqInfoCtrl.linkDisplays[i].dataType;
                            }
                            seqInfoCtrl.linkDisplays[i].refLink = $sce.trustAsHtml(seqInfoCtrl.linkDisplays[i].attributionLink);
                        }
                    })
                    .catch(function (error) {
                        console.error(error);
                    });
            }

            function getDatabases() {
                getLinkDatabases("dblink adding on marker-edit")
                    .then(function (databases) {
                        seqInfoCtrl.databases = [];
                        for (var i in databases) {
                                databases[i].label = databases[i].name + " - " + databases[i].type;
                                seqInfoCtrl.databases.push(databases[i]);
                        }
                    })
                    .catch(function (error) {
                        console.error(error);
                    });
            }

            seqInfoCtrl.addSequenceInfo = function() {
                if (validated(1)) {
                    addLink($scope.markerId, seqInfoCtrl.newDatabase, seqInfoCtrl.newAccession.toUpperCase(), seqInfoCtrl.newReference, null)
                        .then(function (link) {
                            getSequences();
                            if (!seqInfoCtrl.errorRef) {
                                close();
                            }
                        })
                        .catch(function (error) {
                            seqInfoCtrl.errorMessage = error.data.message;
                        });
                } else {
                }
            };

            seqInfoCtrl.deleteSeqenceInfo = function() {
                removeLink(seqInfoCtrl.seqenceInfo)
                    .then(function () {
                        getSequences();
                        close();
                    })
                    .catch(function (error) {
                        seqInfoCtrl.errorMessage = error.data.message;
                    })
                    .finally(function () {
                        getSequences();
                        close();
                    });
            };

            seqInfoCtrl.addAttribution = function() {
                if (!seqInfoCtrl.referenceEdit) {
                    seqInfoCtrl.errorAcc = '';
                    seqInfoCtrl.errorMessage = 'Reference cannot be empty.';
                } else {
                    addLinkReference(seqInfoCtrl.seqenceInfo, seqInfoCtrl.referenceEdit)
                        .then(function (link) {
                            seqInfoCtrl.seqenceInfo.references = link.references;
                            seqInfoCtrl.referenceEdit = '';
                            seqInfoCtrl.errorMessage = '';
                            seqInfoCtrl.errorDb = '';
                            seqInfoCtrl.errorAcc = '';
                            seqInfoCtrl.errorRef = '';
                            getSequences();
                        }).catch(function (error) {
                            seqInfoCtrl.errorMessage = error.data.message;
                        });
                }
            };

            seqInfoCtrl.deleteAttribution = function(ind) {
                removeLinkReference(seqInfoCtrl.seqenceInfo, seqInfoCtrl.seqenceInfo.references[ind])
                    .then(function () {
                        seqInfoCtrl.seqenceInfo.references.splice(ind, 1);
                        seqInfoCtrl.errorMessage = '';
                        seqInfoCtrl.errorDb = '';
                        seqInfoCtrl.errorAcc = '';
                        seqInfoCtrl.errorRef = '';
                        getSequences();
                    }).catch(function (error) {
                        seqInfoCtrl.errorMessage = error.data.message;
                    });
            };

            seqInfoCtrl.updateSequenceInfo = function() {
                if (validated(0)) {
                    seqInfoCtrl.references = seqInfoCtrl.seqenceInfo.references;
                    removeLink(seqInfoCtrl.seqenceInfo)
                        .then(function () {
                            addLink($scope.markerId, seqInfoCtrl.seqenceInfo.referenceDatabaseZdbID,
                                    seqInfoCtrl.accessionEdit.toUpperCase(), seqInfoCtrl.seqenceInfo.references[0].zdbID, seqInfoCtrl.seqenceInfo.length)
                                .then(function (link) {
                                    seqInfoCtrl.seqenceInfo = link;
                                    var referenceIds = [];
                                    for(var i = 0; i < seqInfoCtrl.references.length; i++) {
                                        referenceIds.push(seqInfoCtrl.references[i].zdbID);
                                    }
                                    if (referenceIds.length > 1) {
                                        var pubID = "";
                                        for(var i = 1; i < referenceIds.length; i++) {
                                            pubID = referenceIds[i];
                                            addLinkReference(seqInfoCtrl.seqenceInfo, pubID)
                                                .then(function (seq) {
                                                    seqInfoCtrl.seqenceInfo = seq;
                                                    seqInfoCtrl.seqenceInfo.references = seq.references;
                                                    seqInfoCtrl.newReference = '';
                                                }).catch(function (error) {
                                                    seqInfoCtrl.errorAcc = error.data.message;
                                                }).finally(function () {
                                                    getSequences();
                                                });
                                        }
                                    }
                                    getSequences();
                                    close();
                                })
                                .catch(function (error) {
                                    seqInfoCtrl.errorAcc = error.data.message;
                                });
                        })
                        .catch(function (error) {
                            seqInfoCtrl.errorAcc = error.data.message;
                        });
                }
            };

            seqInfoCtrl.openAddSequenceInfo = function() {
                openModalPopup('new-sequence-information-modal');
            };

            seqInfoCtrl.openDeleteSequenceInfo = function(obj) {
                seqInfoCtrl.seqenceInfo = obj;
                openModalPopup('delete-sequence-info-modal');
            };

            seqInfoCtrl.openUpdateSequenceInfo = function(obj) {
                seqInfoCtrl.seqenceInfo = obj;
                seqInfoCtrl.accessionEdit = obj.accession;
                seqInfoCtrl.newDatabase = obj.referenceDatabaseZdbID;
                openModalPopup('update-sequence-information-modal');
            };

            function getLinks(markerId, group) {
                return $http.get('/action/marker/' + markerId + '/links?group=' + group)
                    .then(returnResponseData).catch(function (error) {
                        seqInfoCtrl.errorMessage = error.data.message;
                    });
            }

            function getLinkDatabases(group) {
                return $http.get('/action/marker/link/databases?group=' + group)
                    .then(returnResponseData).catch(function (error) {
                        seqInfoCtrl.errorMessage = error.data.message;
                    });
            }

            function addLink(markerId, fdbId, accession, pubId, len) {
                var link = {
                    referenceDatabaseZdbID: fdbId,
                    accession: accession,
                    references: [{zdbID: pubId}],
                    length: len
                };
                return $http.post('/action/marker/' + markerId + '/links', link)
                    .then(returnResponseData).catch(function (error) {
                        seqInfoCtrl.errorMessage = error.data.message;
                    });
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

            function returnResponseData(response) {
                return response.data;
            }

            function openModalPopup(element) {
                $('#' + element)
                    .modal({
                        escapeClose: true,
                        clickClose: true,
                        showClose: true,
                        fadeDuration: 100
                    });
            }

            function close() {
                seqInfoCtrl.errorMessage = '';
                seqInfoCtrl.errorDb = '';
                seqInfoCtrl.errorAcc = '';
                seqInfoCtrl.errorRef = '';
                seqInfoCtrl.newDatabase = '';
                seqInfoCtrl.newAccession = '';
                seqInfoCtrl.accessionEdit = '';
                seqInfoCtrl.newReference = '';
                seqInfoCtrl.referenceEdit = '';
                seqInfoCtrl.seqenceInfo = null;
                $.modal.close();
            }

            function validated(checkRef) {
                seqInfoCtrl.errorMessage = '';
                seqInfoCtrl.errorDb = '';
                seqInfoCtrl.errorAcc = '';
                seqInfoCtrl.errorRef = '';
                if (!checkRef) {
                    seqInfoCtrl.newAccession = seqInfoCtrl.accessionEdit;
                }
                if (!seqInfoCtrl.newDatabase) {
                    seqInfoCtrl.errorDb = 'Database cannot be empty.';
                    return false;
                } else if (!seqInfoCtrl.newAccession) {
                    seqInfoCtrl.errorAcc = 'Accession cannot be empty.';
                    return false;
                } else if (/\s/g.test(seqInfoCtrl.newAccession)) {
                    seqInfoCtrl.errorAcc = 'Cannot have whitespace.';
                    return false;
                } else if (checkRef && !seqInfoCtrl.newReference) {
                    seqInfoCtrl.errorRef = 'Reference cannot be empty.';
                    return false;
                } else if ( isGenBank() &&
                    !seqInfoCtrl.newAccession.charAt(0).match(/[a-z]/i) ) {
                    seqInfoCtrl.errorAcc = 'GenBank acc starts with letter';
                    return false;
                } else if (checkRef && seqInfoCtrl.newReference) {
                    validateReference(seqInfoCtrl.newReference)
                        .then(function (response) {
                            if (response.data.errors.length > 0) {
                                seqInfoCtrl.errorRef = response.data.errors[0];
                            }
                        }).catch(function (error) {
                            seqInfoCtrl.errorMessage = error.data.message;
                        });
                    return (seqInfoCtrl.errorRef === "");
                }

                if (!checkRef) {
                    seqInfoCtrl.newAccession = '';
                }

                return true;
            }

            function isGenBank() {
                if (!seqInfoCtrl.newAccession) {
                    return false;
                } else if (seqInfoCtrl.newDatabase === 'ZDB-FDBCONT-040412-37' ||
                    seqInfoCtrl.newDatabase === 'ZDB-FDBCONT-040412-36') {
                    return true;
                } else {
                    return false;
                }
            }

            function validateReference(pubID) {
                return $http.post('/action/marker/link/reference/' + pubID + '/validate', {});
            }

        }]);

}());