/**
 * Angular app for pub tracker
 */

angular.module('pubTrackingApp', [])

    .config(['$locationProvider', function ($locationProvider) {
        $locationProvider.html5Mode(true);
    }])

    .factory('pubId', ['$location', function ($location) {
        var match = $location.path().match("action/publication/([^/]+)/track");
        return match ? match[1] : "";
    }])

    .filter("timeago", function () {
        // from https://gist.github.com/rodyhaddad/5896883

        //time: the time
        //local: compared to what time? default: now
        //raw: wheter you want in a format of "5 minutes ago", or "5 minutes"
        return function (time, local, raw) {
            if (!time) return "never";

            if (!local) {
                (local = Date.now())
            }

            if (angular.isDate(time)) {
                time = time.getTime();
            } else if (typeof time === "string") {
                time = new Date(time).getTime();
            }

            if (angular.isDate(local)) {
                local = local.getTime();
            }else if (typeof local === "string") {
                local = new Date(local).getTime();
            }

            if (typeof time !== 'number' || typeof local !== 'number') {
                return;
            }

            var
                offset = Math.abs((local - time) / 1000),
                span = [],
                MINUTE = 60,
                HOUR = 3600,
                DAY = 86400,
                WEEK = 604800,
                MONTH = 2629744,
                YEAR = 31556926,
                DECADE = 315569260;

            if (offset <= MINUTE)              span = [ '', raw ? 'now' : 'less than a minute' ];
            else if (offset < (MINUTE * 60))   span = [ Math.round(Math.abs(offset / MINUTE)), 'min' ];
            else if (offset < (HOUR * 24))     span = [ Math.round(Math.abs(offset / HOUR)), 'hr' ];
            else if (offset < (DAY * 7))       span = [ Math.round(Math.abs(offset / DAY)), 'day' ];
            else if (offset < (WEEK * 52))     span = [ Math.round(Math.abs(offset / WEEK)), 'week' ];
            else if (offset < (YEAR * 10))     span = [ Math.round(Math.abs(offset / YEAR)), 'year' ];
            else if (offset < (DECADE * 100))  span = [ Math.round(Math.abs(offset / DECADE)), 'decade' ];
            else                               span = [ '', 'a long time' ];

            span[1] += (span[0] === 0 || span[0] > 1) ? 's' : '';
            span = span.join(' ');

            if (raw === true) {
                return span;
            }
            return (time <= local) ? span + ' ago' : 'in ' + span;
        }
    })

    .controller('PubTrackingTopicsController', ['$http', '$filter', 'pubId', function ($http, $filter, pubId) {
        var pubTopics = this,
            previousNote = "";
        pubTopics.topics = [];
        pubTopics.status = null;
        pubTopics.notes = [];
        pubTopics.newNote = "";
        pubTopics.warnings = [];

        var addOrUpdateTopic = function (topic, idx) {
            if (topic.zdbID) {
                $http.post('/action/publication/topics/' + topic.zdbID, topic)
                    .success(function (data) {
                        pubTopics.topics[idx] = data;
                    });
            } else {
                $http.post('/action/publication/' + pubId + '/topics', topic)
                    .success(function (data) {
                        pubTopics.topics[idx] = data;
                    });
            }
        };

        var updateStatus = function (status) {
            $http.post('/action/publication/' + pubId + '/status', status)
                .success(function (data) {
                    pubTopics.status = data;
                });
        };

        pubTopics.toggleDataFound = function (topic, idx) {
            topic.dataFound = !topic.dataFound;
            addOrUpdateTopic(topic, idx);
        };

        pubTopics.open = function(topic, idx) {
            topic.openedDate = Date.now();
            topic.closedDate = null;
            topic.dataFound = true;
            addOrUpdateTopic(topic, idx);
        };

        pubTopics.close = function(topic, idx) {
            topic.closedDate = Date.now();
            topic.dataFound = true;
            addOrUpdateTopic(topic, idx);
        };

        pubTopics.unopen = function (topic, idx) {
            topic.openedDate = null;
            addOrUpdateTopic(topic, idx);
        };

        pubTopics.isNew = function(topic) {
            return !topic.openedDate && !topic.closedDate;
        };

        pubTopics.isOpen = function(topic) {
            return topic.openedDate && !topic.closedDate;
        };

        pubTopics.isClosed = function(topic) {
            return topic.closedDate;
        };

        pubTopics.indexPub = function () {
            pubTopics.status.indexed = true;
            pubTopics.status.indexedDate = Date.now();
            updateStatus(pubTopics.status);

            $http.post('/action/publication/' + pubId + '/notes', { text: 'Indexed paper' })
                .success(function (data) {
                    pubTopics.notes.unshift(data);
                });
        };

        pubTopics.unindexPub = function () {
            pubTopics.status.indexed = false;
            pubTopics.status.indexedDate = null;
            updateStatus(pubTopics.status);

            $http.post('/action/publication/' + pubId + '/notes', { text: 'Un-indexed paper' })
                .success(function (data) {
                    pubTopics.notes.unshift(data);
                });
        };

        pubTopics.closePub = function () {
            $http.post('/action/publication/' + pubId + '/validate')
                .success(function (data) {
                    if (typeof data.warnings != "undefined" && data.warnings != null && data.warnings.length > 0) {
                        pubTopics.warnings = data.warnings;
                    } else {
                        pubTopics.closeWithoutWarning();
                    }
                });
        };

        pubTopics.closeWithoutWarning = function () {
            var noteToAdd;
            pubTopics.status.closedDate = Date.now();
            if (pubTopics.hasTopics()) {
                noteToAdd = "Closed paper";
            } else {
                noteToAdd = "Upon review, this publication contains no information currently curated by ZFIN"
            }
            updateStatus(pubTopics.status);
            pubTopics.hideWarnings();
            $http.post('/action/publication/' + pubId + '/notes', { text: noteToAdd })
                .success(function (data) {
                    pubTopics.notes.unshift(data);
                });
        };

        pubTopics.hideWarnings = function () {
            pubTopics.warnings = [];
        };

        pubTopics.getStatus = function (topic) {
            if (pubTopics.isOpen(topic)) {
                return "Opened " + $filter("timeago")(topic.openedDate);
            } else if (pubTopics.isClosed(topic)) {
                return "Closed";
            } else {
                return "";
            }
        };

        pubTopics.addNote = function () {
            $http.post('/action/publication/' + pubId + '/notes', {
                "zdbID": null,
                "text": pubTopics.newNote,
                "date": null,
                "curator": null
            }).success(function (data) {
                pubTopics.notes.unshift(data);
                pubTopics.newNote = "";
            });
        };

        pubTopics.beginEditing = function (note) {
            previousNote = note.text;
            note.editing = true;
        };

        pubTopics.cancelEditing = function (note) {
            note.text = previousNote;
            note.editing = false;
        };

        pubTopics.editNote = function (note) {
            $http.post('/action/publication/notes/' + note.zdbID, {
                "zdbID": null,
                "text": note.text,
                "date": null,
                "curator": null
            }).success(function () {
                note.editing = false;
            });
        };

        pubTopics.deleteNote = function (note) {
            $http.delete('/action/publication/notes/' + note.zdbID)
                .success(function () {
                    var idx = pubTopics.notes.indexOf(note);
                    pubTopics.notes.splice(idx, 1);
                });
        };

        pubTopics.hasTopics = function () {
            return pubTopics.topics.some(function (t) {
                return t.dataFound;
            });
        };

        $http.get('/action/publication/' + pubId + '/topics')
            .success(function (data) {
                pubTopics.topics = data;
            });
        $http.get('/action/publication/' + pubId + '/status')
            .success(function (data) {
                pubTopics.status = data;
            });

        $http.get('/action/publication/' + pubId + '/notes')
            .success(function (data) {
                pubTopics.notes = data;
            })
            .error(function (data) {
                console.log(data);
            });

    }]);