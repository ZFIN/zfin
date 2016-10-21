;(function () {
    angular
        .module('app')
        .directive('publicNote', publicNote);

    function publicNote() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/public-note.directive.html',
            scope: {
                markerId: '@',
                edit: '@'
            },
            controller: PublicNotesController,
            controllerAs: 'pn',
            bindToController: true
        };

        return directive;
    }

    PublicNotesController.$inject = [$window, 'MarkerService'];
    function PublicNotesController($window, MarkerService) {

        var pn = this;

        pn.publicNote;
        pn.noteText;
        pn.existingNoteText;
        pn.errorMessage;

        pn.openAddNewPublicNote = openAddNewPublicNote;
        pn.openEditNote = openEditNote;
        pn.openDeleteNote = openDeleteNote;
        pn.addNote = addNote;
        pn.editNote = editNote;
        pn.deleteNote = deleteNote;
        pn.close = close;

        init();

        function init() {
            pn.greeting = 'Hello, U';
            pn.noteText = '';

            MarkerService.getNotes(pn.markerId)
                .then(function (notes) {
                    notes.forEach(function (note) {
                        if (note.noteEditMode === 'PUBLIC') {
                            pn.publicNote = note;
                            pn.noteText = note.noteData;;
                            pn.existingNoteText = note.noteData;
                        }
                    });
                })
                .catch(function (error) {
                    console.error(error);
                    pn.errorMessage = 'Unable to display the note. Please try again later.';
                });
        }

        function openAddNewPublicNote() {
            pn.noteText = '';
            MarkerService.openModalPopup('new-public-note-modal');
        }

        function openEditNote() {
            MarkerService.openModalPopup('edit-public-note-modal');
        }

        function openDeleteNote() {
            MarkerService.openModalPopup('delete-public-note-modal');
        }

        function addNote() {
            if (!pn.noteText) {
                pn.errorMessage = 'The note cannot be empty.';
            } else {
                var newNote = pn.publicNote;
                newNote.noteData = pn.noteText;
                saveNote(newNote);
            }
        }

        function editNote() {
            if (pn.existingNoteText === pn.noteText) {
                pn.errorMessage = 'No change with the note detected.';
            } else {
                var newNote = pn.publicNote;
                newNote.noteData = pn.noteText;
                saveNote(newNote);
            }
        }

        function deleteNote() {
            var newNote = pn.publicNote;
            newNote.noteData = '';
            saveNote(newNote);
        }

        function close() {
            pn.errorMessage = "";
            MarkerService.closeModal();
        }

        function saveNote(newerNote) {
            MarkerService.updatePublicNote(pn.markerId, newerNote)
                .then(function (note) {
                    pn.publicNote = note;
                })
                .catch(function (error) {
                    console.error(error);
                    pn.errorMessage = 'Unable to save the note. Please try again later.';
                })
                .finally(function () {
                });
            close();
        }
    }
}());