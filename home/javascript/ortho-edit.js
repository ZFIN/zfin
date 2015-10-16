angular
    .module('app', [])
    .directive('orthoEdit', orthoEdit)
    .directive('pubDisplay', pubDisplayDirective)
    .filter('pub', pubDisplayFilter);

var namedPubs = [
    {
        zdbID: 'ZDB-PUB-030905-1',
        display: 'Ortho Curation Pub'
    }
];

function zdbIdToDisplay(value) {
    var out = value;
    namedPubs.forEach(function (namedPub) {
        if (namedPub.zdbID === value) {
            out = namedPub.display;
        }
    });
    return out;
}

function displayToZdbId(value) {
    var out = value;
    namedPubs.forEach(function (namedPub) {
        if (namedPub.display === value) {
            out = namedPub.zdbID;
        }
    });
    return out;
}

function pubDisplayDirective() {
    var directive = {
        restrict: 'A',
        require: 'ngModel',
        link: link
    };

    function link(scope, element, attrs, modelCtrl) {
        modelCtrl.$parsers.push(displayToZdbId);
        modelCtrl.$formatters.push(zdbIdToDisplay);
    }

    return directive;
}

function pubDisplayFilter() {
    return zdbIdToDisplay;
}

function orthoEdit() {
    var directive = {
        restrict: 'EA',
        templateUrl: '/javascript/orthoedit.directive.html', // todo: this is totally the wrong location for this file
        scope: {
            gene: '@'
        },
        controller: OrthoEditController,
        controllerAs: 'vm',
        bindToController: true,
        link: link
    };

    function link(scope, el) {
        function hideOptions (container, opts, event) {
            if (!container.has(event.target).length) {
                opts.hide();
                angular.element(document).off('click', hideOptions);
            }
        }

        el.find('.list-select').each(function () {
            var container = $(this);
            var opts = container.find('.list-select-options');
            var text = container.find('.list-select-text');

            if (!container.hasClass('keep-open')) {
                opts.on('click', 'li', function (event) {
                    event.stopPropagation();
                    opts.hide();
                });
            }

            opts.hide();

            text.on('click', function () {
                opts.show();
                angular.element(document).on('click', hideOptions.bind(null, container, opts));
            });
        });
    }

    return directive;
}

OrthoEditController.$inject = ['$http', '$q'];
function OrthoEditController($http, $q) {
    var vm = this;

    vm.orthologs = [];
    vm.ncbiGeneNumber = '';
    vm.ncbiError = '';
    vm.note = {};
    vm.noteText = '';
    vm.noteEditing = false;

    vm.codes = [];
    vm.pubs = [
        {
            zdbID: 'ZDB-PUB-030905-1'
        }
    ];

    vm.evidenceCodeError = '';
    vm.evidencePublicationError = '';
    vm.evidencePublicationWarning = '';
    vm.noteError = '';

    vm.modalOrtholog = {};
    vm.modalEvidence = {};
    vm.modalEvidenceIndex = -1;

    vm.addOrtholog = addOrtholog;
    vm.confirmDeleteOrtholog = confirmDeleteOrtholog;
    vm.cancelDelete = cancelDelete;
    vm.deleteOrtholog = deleteOrtholog;

    vm.addEvidence = addEvidence;
    vm.cancelEvidence = cancelEvidence;
    vm.saveEvidence = saveEvidence;
    vm.editEvidence = editEvidence;
    vm.deleteEvidence = deleteEvidence;

    vm.editNote = editNote;
    vm.cancelNoteEdit = cancelNoteEdit;
    vm.saveNoteEdit = saveNoteEdit;

    vm.selectPub = selectPub;
    vm.checkPub = checkPub;

    activate();

    function activate() {
        $q
            .all([
                $http.get('/action/gene/' + vm.gene + '/orthologs'),
                $http.get('/action/ortholog/evidence-codes')
            ])
            .then(function(resp) {
                vm.codes = resp[1].data;
                vm.orthologs = resp[0].data;
                vm.orthologs.forEach(function (ortholog) {
                    var evidenceDisplayMap = {};
                    ortholog.evidenceSet.forEach(function (e) {
                        var pubUsed = vm.pubs.find(function (pub) {
                            return pub.zdbID === e.publication.zdbID;
                        });

                        if (!pubUsed) {
                            vm.pubs.push(e.publication);
                        }

                        if (evidenceDisplayMap[e.publication.zdbID] === undefined) {
                            var evidenceDisplay = new EvidenceDisplay(vm.codes);
                            evidenceDisplay.publication = e.publication;
                            evidenceDisplayMap[e.publication.zdbID] = evidenceDisplay;
                        }

                        evidenceDisplayMap[e.publication.zdbID].toggleCode(e.evidenceCode);
                    });
                    ortholog.evidenceMap = evidenceDisplayMap;
                });
            })
            .catch(function(error) {
                console.error(error);
            });

        $http.get('/action/gene/' + vm.gene + '/orthology-note')
            .then(function(resp) {
                vm.note = resp.data;
                vm.noteText = vm.note.note;
            })
            .catch(function(error) {
                vm.noteError = 'Couldn\'t retrieve orthology note';
                console.error(error);
            });
    }

    function addOrtholog() {
        var alreadyAdded = vm.orthologs.some(function(existing) {
            return existing.ncbiOtherSpeciesGeneDTO.id === vm.ncbiGeneNumber;
        });

        if (alreadyAdded) {
            vm.ncbiError = 'Ortholog already added';
            return;
        }

        $http.post('/action/gene/' + vm.gene + '/ortholog/ncbi/' + vm.ncbiGeneNumber)
            .then(function(resp) {
                var newOrtholog = resp.data;
                newOrtholog.evidenceMap = {};
                vm.orthologs.push(newOrtholog);
                vm.ncbiGeneNumber = '';
            })
            .catch(function(error) {
                vm.ncbiError = error.data.message;
            });
    }

    function confirmDeleteOrtholog(ortholog) {
        vm.modalOrtholog = ortholog;
        $('#delete-modal')
            .modal({
                escapeClose: false,
                clickClose: false,
                showClose: false,
                fadeDuration: 100
            })
            .on($.modal.AFTER_CLOSE, function() {
                vm.modalOrtholog = undefined;
            });
    }

    function deleteOrtholog() {
        var idx = vm.orthologs.indexOf(vm.modalOrtholog);
        $http.delete('/action/gene/' + vm.gene + '/ortholog/' + vm.modalOrtholog.zdbID)
            .then(function (resp) {
                vm.orthologs.splice(idx, 1);
                $.modal.close();
            })
            .catch(function (error) {
                alert('PANIC! There was an error!')
            });
    }

    function cancelDelete() {
        $.modal.close();
    }

    function addEvidence(ortholog) {
        openEvidenceModal(ortholog, new EvidenceDisplay(vm.codes));
    }

    function saveEvidence() {
        if (!vm.modalEvidence.publication.zdbID) {
            vm.evidencePublicationError = 'Select or enter a publication';
            return;
        }

        if (!vm.modalEvidence.publication.zdbID.match(/^ZDB-PUB-\d{6}-\d+/)) {
            vm.evidencePublicationError = 'Not a valid ZDB-PUB ID';
        }

        if (!vm.modalEvidence.anySelected()) {
            vm.evidenceCodeError = 'Select at least one evidence code';
            return;
        }

        var pubID = vm.modalEvidence.publication.zdbID;
        var payload = {
            'publicationID': pubID,
            'orthologID': vm.modalOrtholog.zdbID,
            'evidenceCodeList': vm.modalEvidence.asArray()
        };
        $http.post('/action/gene/' + vm.gene + '/ortholog/evidence', payload)
            .then(function (resp) {
                vm.modalOrtholog.evidenceMap[pubID] = angular.copy(vm.modalEvidence);
                $.modal.close();
            })
            .catch(function (error) {
                console.log('Error!', error);
            });
    }

    function cancelEvidence() {
        $.modal.close();
    }

    function editEvidence(ortholog, evidence, index) {
        vm.modalEvidenceIndex = index;
        openEvidenceModal(ortholog, angular.copy(evidence));
    }

    function deleteEvidence(ortholog, evidence) {
        var pubID = evidence.publication.zdbID;
        var payload = {
            'publicationID': pubID,
            'orthologID': ortholog.zdbID,
            'evidenceCodeList': []
        };
        $http.post('/action/gene/' + vm.gene + '/ortholog/evidence', payload)
            .then(function (resp) {
                delete ortholog.evidenceMap[pubID];
            })
            .catch(function (error) {
                console.log('Error!', error);
            });
    }

    function editNote() {
        vm.noteText = vm.note.note;
        vm.noteEditing = true;
    }

    function cancelNoteEdit() {
        vm.noteEditing = false;
        vm.noteError = '';
    }

    function saveNoteEdit() {
        $http.post('/action/gene/' + vm.gene + '/orthology-note', { note: vm.noteText })
            .then(function (resp) {
                vm.note = resp.data;
                vm.noteEditing = false;
                vm.noteError = '';
            })
            .catch(function (error) {
                vm.noteError = error.data.message;
                console.log('error saving note', error);
            });
    }

    function openEvidenceModal(ortholog, evidence) {
        vm.modalOrtholog = ortholog;
        vm.modalEvidence = evidence;

        $('#evidence-modal')
            .modal({
                escapeClose: false,
                clickClose: false,
                showClose: false,
                fadeDuration: 100
            })
            .on($.modal.AFTER_CLOSE, function() {
                vm.modalOrtholog = undefined;
                vm.modalEvidence = undefined;
                vm.modalEvidenceIndex = -1;
                vm.evidencePublicationWarning = false;
            });
    }

    function selectPub(pub) {
        vm.modalEvidence.publication.zdbID = pub.zdbID;
        checkPub();
    }

    function checkPub() {
        vm.evidencePublicationError = '';
        var pubID = makeZdb(vm.modalEvidence.publication.zdbID);
        vm.modalEvidence.publication.zdbID = pubID;
        var existingEvidence = vm.modalOrtholog.evidenceMap[pubID];
        if (existingEvidence) {
            vm.modalEvidence = angular.copy(existingEvidence);
            vm.evidencePublicationWarning = true;
        } else {
            vm.modalEvidence.clearAllCodes();
            vm.evidencePublicationWarning = false;
        }
    }

    function makeZdb(value) {
        var out = value;
        if (out.match(/^\d{6}-\d+$/)) {
            out = 'ZDB-PUB-' + out;
        }
        return out;
    }

}

function EvidenceDisplay(codes) {
    this.publication = {
        zdbID: ''
    };

    this.codes = angular.copy(codes).map(function (code) {
        code.selected = false;
        return code;
    });
}

EvidenceDisplay.prototype.toggleCode = function(evidenceCode) {
    this.codes.forEach(function (code) {
        if (code.code === evidenceCode) {
            code.selected = !code.selected;
        }
    });
};

EvidenceDisplay.prototype.clearAllCodes = function() {
    this.codes.forEach(function (code) {
        code.selected = false;
    });
};

EvidenceDisplay.prototype.anySelected = function() {
    return this.codes.some(function(code) {
        return code.selected;
    });
};

EvidenceDisplay.prototype.asArray = function() {
    return this.codes
        .filter(function(c) { return c.selected; })
        .map(function(c) { return c.code; });
};

EvidenceDisplay.prototype.asList = function() {
    return this.asArray().join(', ');
};