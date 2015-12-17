
;(function() {
    angular
        .module('app')
        .filter('trusted_html', trustedHtml)
        .directive('markerNotes', markerNotes);

    trustedHtml.$inject = ['$sce'];
    function trustedHtml($sce) {
        return function(text) {
            return $sce.trustAsHtml(text);
        };
    }

    function markerNotes() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/javascript/marker-notes.directive.html',
            scope: {
                id: '@',
                userId: '@'
            },
            controller: MarkerNotesController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    MarkerNotesController.$inject = ['MarkerService'];
    function MarkerNotesController(MarkerService) {

        var vm = this;

        vm.newPublicNote = '';
        vm.newCuratorNote = '';

        vm.publicNote = null;
        vm.curatorNotes = [];
        vm.processing = false;
        vm.errors = {};

        vm.editPublicNote = editPublicNote;
        vm.cancelEditPublicNote = cancelEditPublicNote;
        vm.savePublicNote = savePublicNote;
        vm.addCuratorNote = addCuratorNote;
        vm.editCuratorNote = editCuratorNote;
        vm.cancelEditCuratorNote = cancelEditCuratorNote;
        vm.saveCuratorNote = saveCuratorNote;
        vm.deleteCuratorNote = deleteCuratorNote;

        activate();

        function activate() {
            MarkerService.getNotes(vm.id)
                .then(function(notes) {
                    notes.forEach(function(note) {
                        if (note.noteEditMode === 'PRIVATE') {
                            vm.curatorNotes.push(note);
                        } else if (note.noteEditMode === 'PUBLIC') {
                            vm.publicNote = note;
                            cancelEditPublicNote();
                        }
                    });
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function editPublicNote() {
            vm.publicNote.editing = true;
        }

        function cancelEditPublicNote() {
            vm.newPublicNote = vm.publicNote.noteData;
            vm.publicNote.editing = false;
        }

        function savePublicNote() {
            // todo: move this to MarkerService?
            var newNote = vm.publicNote;
            newNote.noteData = vm.newPublicNote;
            vm.publicNote.processing = true;
            MarkerService.updatePublicNote(vm.id, newNote)
                .then(function(note) {
                    vm.publicNote = note;
                })
                .catch(function(error) {
                    console.error(error);
                    vm.publicNote.error = 'Unable to save note. Please try again later.';
                })
                .finally(function() {
                    vm.publicNote.processing = false;
                });
        }

        function addCuratorNote() {
            // todo: move this to MarkerService?
            var newNote = {
                noteData: vm.newCuratorNote,
                noteEditMode: "PRIVATE"
            };
            vm.processing = true;
            MarkerService.addCuratorNote(vm.id, newNote)
                .then(function(note) {
                    vm.curatorNotes.unshift(note);
                    vm.newCuratorNote = '';
                    vm.errors = {};
                })
                .catch(function(error) {
                    console.error(error);
                    vm.errors.curator = 'Unable to save note. Please try again later.';
                })
                .finally(function() {
                    vm.processing = false;
                })
        }

        function editCuratorNote(note) {
            note.editing = true;
            note.editText = note.noteData;
        }

        function cancelEditCuratorNote(note) {
            note.editing = false;
        }

        function saveCuratorNote(note, index) {
            note.processing = true;
            MarkerService.updateCuratorNote(vm.id, note, note.editText)
                .then(function(note) {
                    vm.curatorNotes[index] = note;
                })
                .catch(function(error) {
                    console.error(error);
                    note.error = 'Unable to save note. Please try again later.';
                })
                .finally(function() {
                    note.processing = false;
                })
        }

        function deleteCuratorNote(note, index) {
            MarkerService.deleteCuratorNote(vm.id, note)
                .then(function() {
                    vm.curatorNotes.splice(index, 1);
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

    }
}());