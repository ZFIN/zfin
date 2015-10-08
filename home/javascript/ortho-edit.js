angular
    .module('app', [])
    .directive('orthoEdit', orthoEdit);

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
                opts.children().on('click', function (event) {
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

OrthoEditController.$inject = ['$http'];
function OrthoEditController($http) {
    var vm = this;

    vm.orthologs = [];
    vm.ncbiGeneNumber = '';
    vm.ncbiError = '';
    vm.note = {};
    vm.noteText = '';
    vm.noteEditing = false;

    vm.pubs = ['Ortho Curation Pub', 'ZDB-PUB-XXXXXX-X', 'ZDB-PUB-YYYYYY-Y', 'ZDB-PUB-ZZZZZZ-Z'];

    vm.codes = [
        {
            abbrev: 'AA',
            full: 'Amino acid sequence comparison'
        },
        {
            abbrev: 'CE',
            full: 'Coincident expression'
        },
        {
            abbrev: 'CL',
            full: 'Conserved genome location'
        },
        {
            abbrev: 'FC',
            full: 'Functional complementation'
        },
        {
            abbrev: 'NT',
            full: 'Nucleotide sequence comparison'
        },
        {
            abbrev: 'PT',
            full: 'Phylogenetic tree'
        },
        {
            abbrev: 'OT',
            full: 'Other'
        }
    ];

    vm.evidenceCodeError = '';
    vm.evidencePublicationError = '';

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

    vm.formatCodes = formatCodes;

    vm.editNote = editNote;
    vm.cancelNoteEdit = cancelNoteEdit;
    vm.saveNoteEdit = saveNoteEdit;

    activate();

    function activate() {
        $http.get('/gene/' + vm.gene + '/orthologs')
            .then(function(resp) {
                vm.orthologs = resp.data.orthologs;
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
                console.error(error);
            });
    }

    function addOrtholog() {
        var alreadyAdded = vm.orthologs.some(function(existing) {
            return existing.id === vm.ncbiGeneNumber;
        });

        if (alreadyAdded) {
            vm.ncbiError = 'Ortholog already added';
            return;
        }

        $http.get('/ncbi/' + vm.ncbiGeneNumber)
            .then(function(resp) {
                var newOrtholog = resp.data;
                newOrtholog.evidence = [];
                vm.orthologs.push(newOrtholog);
                /* TODO: send new ortholog back to server */
                vm.ncbiGeneNumber = '';
            })
            .catch(function(error) {
                vm.ncbiError = 'Couldn\'t find gene with this ID';
            });
    }

    function confirmDeleteOrtholog(ortholog) {
        vm.modalOrtholog = ortholog;
        $('#delete-modal').modal({
            escapeClose: false,
            clickClose: false,
            showClose: false,
            fadeDuration: 100
        })
            .on($.modal.CLOSE, function() {
                vm.modalOrtholog = {};
            });
    }

    function deleteOrtholog(index) {
        var idx = vm.orthologs.indexOf(vm.modalOrtholog);
        /* TODO: send delete to server */
        vm.orthologs.splice(idx, 1);
        $.modal.close();
    }

    function cancelDelete() {
        $.modal.close();
    }

    function addEvidence(ortholog) {
        openEvidenceModal(ortholog, blankEvidence());
    }

    function saveEvidence() {
        if (!vm.modalEvidence.publication) {
            vm.evidencePublicationError = 'Select a publication';
            return;
        }

        var hasSelectedCodes = vm.modalEvidence.codes.some(function(code) {
            return code.selected;
        });

        if (!hasSelectedCodes) {
            vm.evidenceCodeError = 'Select at least one evidence code';
            return;
        }

        if (vm.modalEvidenceIndex < 0) {
            vm.modalOrtholog.evidence.push(angular.copy(vm.modalEvidence));
            /* TODO: call server */
        } else {
            vm.modalOrtholog.evidence.splice(vm.modalEvidenceIndex, 1, angular.copy(vm.modalEvidence));
            /* TODO: call server */
        }

        $.modal.close();
    }

    function cancelEvidence() {
        $.modal.close();
    }

    function editEvidence(ortholog, evidence, index) {
        vm.modalEvidenceIndex = index;
        openEvidenceModal(ortholog, angular.copy(evidence));
    }

    function deleteEvidence(ortholog, evidence, index) {
        /* TODO: server call */
        ortholog.evidence.splice(index, 1);
    }

    function formatCodes(codes) {
        if (!codes) { return; }
        return codes
            .filter(function(c) { return c.selected; })
            .map(function(c) { return c.abbrev; })
            .join(', ');
    }

    function editNote() {
        vm.noteText = vm.note.note;
        vm.noteEditing = true;
    }

    function cancelNoteEdit() {
        vm.noteEditing = false;
    }

    function saveNoteEdit() {
        vm.note.note = vm.noteText;
        vm.note.geneID = vm.gene;
        $http.post('/action/gene/' + vm.gene + '/orthology-note', vm.note)
            .then(function (resp) {
                vm.note = resp.data;
                vm.noteEditing = false;
            })
            .catch(function (error) {
                console.log('error saving note', error);
            });
    }

    function openEvidenceModal(ortholog, evidence) {
        vm.modalOrtholog = ortholog;
        vm.modalEvidence = evidence;

        $('#evidence-modal').modal({
            escapeClose: false,
            clickClose: false,
            showClose: false,
            fadeDuration: 100
        })
            .on($.modal.OPEN, function(event, modal) {
                modal.elm.css('top', '20%');
            })
            .on($.modal.CLOSE, function() {
                vm.modalOrtholog = {};
                vm.modalEvidence = {};
                vm.modalEvidenceIndex = -1;
            });
    }

    function blankEvidence() {
        return {
            publication: '',
            codes: vm.codes.map(function(code) {
                return {
                    abbrev: code.abbrev,
                    full: code.full,
                    selected: false
                };
            })
        };
    }
}
