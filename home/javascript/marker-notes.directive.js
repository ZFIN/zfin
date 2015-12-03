
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
                id: '@'
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

        vm.resetPublicNote = resetPublicNote;
        vm.savePublicNote = savePublicNote;

        activate();

        function activate() {
            MarkerService.getNotes(vm.id)
                .then(function(notes) {
                    notes.forEach(function(note) {
                        if (note.noteEditMode === 'PRIVATE') {
                            vm.curatorNotes.push(note);
                        } else if (note.noteEditMode === 'PUBLIC') {
                            vm.publicNote = note;
                            resetPublicNote();
                        }
                    });
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

        function resetPublicNote() {
            vm.newPublicNote = vm.publicNote.noteData;
        }

        function savePublicNote() {
            // todo: move this to MarkerService?
            var newNote = vm.publicNote;
            newNote.noteData = vm.newPublicNote;
            MarkerService.updatePublicNote(vm.id, newNote)
                .then(function(note) {
                    vm.publicNote = note;
                })
                .catch(function(error) {
                    console.error(error);
                });
        }

    }
}());