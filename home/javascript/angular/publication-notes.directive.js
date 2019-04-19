;(function () {
    angular
        .module('app')
        .directive('publicationNotes', publicationNotes);

    function publicationNotes() {
        var directive = {
            restrict: 'EA',
            templateUrl: '/templates/publication-notes.directive.html',
            scope: {
                pubId: '@',
                notes: '='
            },
            controller: PublicationNotesController,
            controllerAs: 'vm',
            bindToController: true
        };

        return directive;
    }

    PublicationNotesController.$inject = ['PublicationService'];
    function PublicationNotesController(PublicationService) {
        var vm = this;
        var previousNote = '';

        vm.newNote = '';

        vm.addNote = addNote;
        vm.beginEditingNote = beginEditingNote;
        vm.deleteNote = deleteNote;
        vm.editNote = editNote;
        vm.cancelEditingNote = cancelEditingNote;

        function addNote() {
            PublicationService.addNote(vm.pubId, vm.newNote)
                .then(function (response) {
                    vm.notes.unshift(response.data);
                    vm.newNote = '';
                });
        }

        function beginEditingNote(note) {
            previousNote = note.text;
            note.editing = true;
        }

        function deleteNote(note, idx) {
            PublicationService.deleteNote(note)
                .then(function () {
                    vm.notes.splice(idx, 1);
                });
        }

        function editNote(note) {
            PublicationService.updateNote(note)
                .then(function () {
                    note.editing = false;
                });
        }

        function cancelEditingNote(note) {
            note.text = previousNote;
            note.editing = false;
        }
    }

}());