angular.module('app')
    .controller('AuthorLinkingController', ['$http', '$filter', '$attrs', 'ZfinUtils', function ($http, $filter, $attrs, zf) {

        var authorLinkCtrl = this;
        authorLinkCtrl.pubZdbId = $attrs.pubZdbId;
        authorLinkCtrl.authors = [];
        authorLinkCtrl.registeredAuthors = [];
        authorLinkCtrl.selectedAuthor = null;
        authorLinkCtrl.errorString = null;


        authorLinkCtrl.loadSuggestedPeople = function(author) {

            $http.get('/action/publication/link-author-suggestions?authorString=' + author.label)
                .success(function(data) {
                    author.suggestions = data;
                    author.loadedSuggestions = true;
                })
                .error(function() {
                    authorLinkCtrl.errorString = "There was an error on the server when fetching author suggestions";
                })

        };

        authorLinkCtrl.selectAuthor = function(author) {
            authorLinkCtrl.selectedAuthor = author;
            if (author.suggestions.length == 0) {
                authorLinkCtrl.loadSuggestedPeople(author);
            }
        };


        authorLinkCtrl.addAuthorByID = function() {
            var person = { zdbID: authorLinkCtrl.authorZdbID };
            authorLinkCtrl.addAuthor(person);
        };

        authorLinkCtrl.addAuthor = function(person) {
            $http.post('/action/publication/' + authorLinkCtrl.pubZdbId + "/addAuthor/" + person.zdbID,[])
                .success(function() {
                    authorLinkCtrl.loadRegisteredAuthors();
                    authorLinkCtrl.errorString = "";
                    authorLinkCtrl.authorZdbID = "";
                    //authorLinkCtrl.registeredAuthors.push(person);
                })
                .error(function() {
                    authorLinkCtrl.errorString = "There was an error on the server when attempting to add the author";
                })
        };

        authorLinkCtrl.removeAuthor = function(person) {
            $http.post('/action/publication/' + authorLinkCtrl.pubZdbId + "/removeAuthor/" + person.zdbID,[])
                .success(function() {
                    authorLinkCtrl.loadRegisteredAuthors();
                    authorLinkCtrl.errorString = "";
                })
                .error(function() {
                    authorLinkCtrl.errorString = "There was an error on the server when attempting to add the author";
                })
        };

        authorLinkCtrl.loadAuthors = function() {
            $http.get('/action/publication/' + authorLinkCtrl.pubZdbId + "/author-strings")
                .success(function(data) {
                    authorLinkCtrl.authorStrings = [];
                    angular.forEach(data, function(authorString) {
                        var author = { label: authorString, suggestions: []};
                        authorLinkCtrl.authors.push(author)
                    });
                })
                .error(function() {
                    authorLinkCtrl.errorString = "There was an error on the server and the registered authors could not be loaded.";
                })

        };

        authorLinkCtrl.loadRegisteredAuthors = function() {
            $http.get('/action/publication/' + authorLinkCtrl.pubZdbId + "/registered-authors")
                .success(function(data) {
                    authorLinkCtrl.registeredAuthors = data;
                    authorLinkCtrl.errorString = "";
                })
                .error(function() {
                    authorLinkCtrl.errorString = "There was an error on the server and the registered authors could not be loaded.";
                })
        };

        authorLinkCtrl.isLinked = function (person) {
            return zf.find(authorLinkCtrl.registeredAuthors, function (reg) {
                return reg.zdbID === person.zdbID;
            });
        };

        authorLinkCtrl.loadRegisteredAuthors();
        authorLinkCtrl.loadAuthors();


}]);


