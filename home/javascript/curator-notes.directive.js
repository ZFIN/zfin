;(function () {
    angular
        .module('app')
        .directive('curatorNotes', curatorNotes);

    function curatorNotes() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/curator-notes.directive.html',
            scope: {
                markerId: '@',
                edit: '@'
            },
            controller: CuratorNotesController,
            controllerAs: 'cn',
            bindToController: true
        };

        return directive;
    }

    CuratorNotesController.$inject = [$window, 'MarkerService'];
    function CuratorNotesController($window, MarkerService) {

        var cn = this;

        cn.openAddNewCuratorNotes = openAddNewCuratorNotes;
        cn.openEditCuratorNotes= openEditCuratorNotes;
        cn.openDeleteCuratorNotes = openDeleteCuratorNotes;
        cn.addCuratorNote = addCuratorNote;
        cn.editCuratorNote = editCuratorNote;
        cn.deleteCuratorNote = deleteCuratorNote;
        cn.close = close;

        cn.noteText = '';
        cn.existingNoteText = '';

        cn.curatorNotes = [];

        cn.ind = 0;

        cn.curatorNote;

        cn.errorMessage;

        init();

        function init() {
            cn.errorMessage = '';

            MarkerService.getNotes(cn.markerId)
                .then(function (notes) {
                    notes.forEach(function (note) {
                        if (note.noteEditMode === 'PRIVATE') {
                            note.collapsable = false;
                            cn.curatorNotes.push(note);
                        }
                    });
                })
                .catch(function (error) {
                    console.error(error);
                    cn.errorMessage = 'Unable to display curator notes. Please try again later.';
                });
        }

        function openAddNewCuratorNotes() {
            cn.noteText = '';
            MarkerService.openModalPopup('new-curator-note-modal');
        }

        function addCuratorNote() {
            var newNote = {
                noteData: cn.noteText,
                noteEditMode: "PRIVATE"
            };

            if (!cn.noteText) {
                cn.errorMessage = 'Curator note cannot be empty.';
            } else {
                MarkerService.addCuratorNote(cn.markerId, newNote)
                    .then(function (note) {
                        cn.curatorNotes.unshift(note);
                        cn.noteText = '';
                    })
                    .catch(function (error) {
                        console.error(error);
                        cn.errorMessage = 'Unable to add the note. Please try again later.';
                    })
                    .finally(function () {
                    });
                close();

            }
        }

        function openEditCuratorNotes(obj, ind) {
            cn.noteText = obj.noteData;
            cn.existingNoteText = obj.noteData;
            cn.curatorNote = obj;
            cn.ind = ind;
            MarkerService.openModalPopup('edit-curator-note-modal');
        }

        function editCuratorNote() {
            if (!cn.noteText) {
                cn.errorMessage = 'Curator note cannot be empty.';
            } else if (cn.existingNoteText === cn.noteText) {
                cn.errorMessage = 'No change with the curator note detected.';
            } else {
                MarkerService.updateCuratorNote(cn.markerId, cn.curatorNote, cn.noteText)
                    .then(function (note) {
                        cn.curatorNotes[cn.ind] = note;
                    })
                    .catch(function (error) {
                        console.error(error);
                        cn.errorMessage = 'Unable to save the note. Please try again later.';
                    })
                    .finally(function () {
                    });
                cn.existingNoteText = '';
                close();
            }
        }

        function openDeleteCuratorNotes(obj, ind) {
            cn.noteText = obj.noteData;
            cn.curatorNote = obj;
            cn.ind = ind;
            MarkerService.openModalPopup('delete-curator-note-modal');
        }

        function deleteCuratorNote() {
            MarkerService.deleteCuratorNote(cn.markerId, cn.curatorNote)
                .then(function () {
                    cn.curatorNotes.splice(cn.ind, 1);
                    close();
                })
                .catch(function (error) {
                    console.error(error);
                    cn.errorMessage = 'Unable to delete the note. Please try again later.';
                });
        }

        function close() {
            cn.noteText = "";
            cn.errorMessage = '';
            MarkerService.closeModal();
        }
    }
}());