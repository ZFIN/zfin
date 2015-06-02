/**
 * Angular app for pub tracker
 */

angular.module('pubTrackingApp', [])
    .controller('PubTrackingNotesController', ['$http', '$attrs', function ($http, $attrs) {
        var pubNotes = this;
        pubNotes.notes = [];
        pubNotes.newNote = "";
        pubNotes.user = $attrs.loggedInUser;
        pubNotes.pub = $attrs.pubZdbId;

        pubNotes.addNote = function () {
            $http.post('/action/publication/' + pubNotes.pub + '/notes', {
                "zdbID": null,
                "text": pubNotes.newNote,
                "date": null,
                "curator": null
            }).success(function (data) {
                pubNotes.notes.unshift(data);
                pubNotes.newNote = "";
            });
        };

        pubNotes.editNote = function (note) {
            $http.post('/action/publication/notes/' + note.zdbID, {
                "zdbID": null,
                "text": note.text,
                "date": null,
                "curator": null
            }).success(function () {
                note.editing = false;
            });
        };

        pubNotes.deleteNote = function (note) {
            $http.delete('/action/publication/notes/' + note.zdbID)
                .success(function () {
                    var idx = pubNotes.notes.indexOf(note);
                    pubNotes.notes.splice(idx, 1);
                });
        };

        $http.get('/action/publication/' + pubNotes.pub + '/notes')
            .success(function (data) {
                pubNotes.notes = data;
            })
            .error(function (data) {
                console.log(data);
            });

    }]);