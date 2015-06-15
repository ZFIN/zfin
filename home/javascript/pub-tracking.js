/**
 * Angular app for pub tracker
 */

angular.module('pubTrackingApp', [])

    .filter('timeago', function () {
        // from https://gist.github.com/rodyhaddad/5896883

        //time: the time
        //local: compared to what time? default: now
        //raw: wheter you want in a format of '5 minutes ago', or '5 minutes'
        return function (time, local, raw) {
            if (!time) return 'never';

            if (!local) {
                (local = Date.now())
            }

            if (angular.isDate(time)) {
                time = time.getTime();
            } else if (typeof time === 'string') {
                time = new Date(time).getTime();
            }

            if (angular.isDate(local)) {
                local = local.getTime();
            }else if (typeof local === 'string') {
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

    .controller('PubTrackingController', ['$http', '$filter', '$attrs', function ($http, $filter, $attrs) {
        var pubTrack = this,
            previousNote = '',
            pubId = $attrs.zdbId;
        pubTrack.topics = [];
        pubTrack.status = null;
        pubTrack.notes = [];
        pubTrack.newNote = '';
        pubTrack.warnings = [];
        pubTrack.correspondences = [];

        var getTopics = function() {
            $http.get('/action/publication/' + pubId + '/topics')
                .success(function (data) {
                    pubTrack.topics = data;
                });
        };

        var getStatus = function () {
            $http.get('/action/publication/' + pubId + '/status')
                .success(function (data) {
                    pubTrack.status = data;
                });
        };

        var getNotes = function () {
            $http.get('/action/publication/' + pubId + '/notes')
                .success(function (data) {
                    pubTrack.notes = data;
                });
        };

        var getCorrespondences = function () {
            $http.get('/action/publication/' + pubId + '/correspondences')
                .success(function (data) {
                    pubTrack.correspondences = data;
                });
        };

        var addOrUpdateTopic = function (topic, idx) {
            var post;
            if (topic.zdbID) {
                post = $http.post('/action/publication/topics/' + topic.zdbID, topic);
            } else {
                post = $http.post('/action/publication/' + pubId + '/topics', topic)
            }
            post.success(function (data) {
                pubTrack.topics[idx] = data;
            });
            return post;
        };

        var updateStatus = function () {
            var post = $http.post('/action/publication/' + pubId + '/status', pubTrack.status);
            post.success(function (data) {
                pubTrack.status = data;
            });
            return post;
        };

        var updateCorrespondence = function (corr, idx) {
            var post = $http.post('/action/publication/correspondences/' + corr.id, corr);
            post.success(function (data) {
                    pubTrack.correspondences[idx] = data;
                });
            return post;
        };

        // not private because it gets called from notif_frame.apg
        pubTrack._addNote = function (txt) {
            var post = $http.post('/action/publication/' + pubId + '/notes', { text: txt });
            post.success(function (data) {
                pubTrack.notes.unshift(data);
            });
            return post;
        };

        pubTrack.toggleTopicDataFound = function (topic, idx) {
            topic.dataFound = !topic.dataFound;
            addOrUpdateTopic(topic, idx);
        };

        pubTrack.openTopic = function(topic, idx) {
            topic.openedDate = Date.now();
            topic.closedDate = null;
            topic.dataFound = true;
            addOrUpdateTopic(topic, idx);
        };

        pubTrack.closeTopic = function(topic, idx) {
            topic.closedDate = Date.now();
            topic.dataFound = true;
            addOrUpdateTopic(topic, idx);
        };

        pubTrack.unopenTopic = function (topic, idx) {
            topic.openedDate = null;
            addOrUpdateTopic(topic, idx);
        };

        pubTrack.isNewTopic = function(topic) {
            return !topic.openedDate && !topic.closedDate;
        };

        pubTrack.isOpenTopic = function(topic) {
            return topic.openedDate && !topic.closedDate;
        };

        pubTrack.isClosedTopic = function(topic) {
            return topic.closedDate;
        };

        pubTrack.indexPub = function () {
            pubTrack.status.indexed = true;
            pubTrack.status.indexedDate = Date.now();
            updateStatus()
                .success(function () {
                    pubTrack._addNote('Indexed paper');
                });
        };

        pubTrack.unindexPub = function () {
            pubTrack.status.indexed = false;
            pubTrack.status.indexedDate = null;
            updateStatus()
                .success(function () {
                    pubTrack._addNote('Un-indexed paper');
                });
        };

        pubTrack.validateForClose = function () {
            $http.post('/action/publication/' + pubId + '/validate', {})
                .success(function (data) {
                    if (typeof data.warnings !== 'undefined' && data.warnings !== null && data.warnings.length > 0) {
                        pubTrack.warnings = data.warnings;
                    } else {
                        pubTrack.closePub();
                    }
                });
        };

        pubTrack.closePub = function () {
            var noteToAdd = pubTrack.hasTopics() ?
                'Closed paper' : 'Upon review, this publication contains no information currently curated by ZFIN';
            pubTrack.status.closedDate = Date.now();
            updateStatus()
                .success(function () {
                    pubTrack.hideWarnings();
                    pubTrack._addNote(noteToAdd);
                    getTopics();
                });
        };

        pubTrack.reopenPub = function () {
            pubTrack.status.closedDate = null;
            updateStatus()
                .success(function () {
                    pubTrack._addNote('Reopened paper')
                });
        };

        pubTrack.hideWarnings = function () {
            pubTrack.warnings = [];
        };

        pubTrack.getTopicStatus = function (topic) {
            if (pubTrack.isOpenTopic(topic)) {
                return 'Opened ' + $filter('timeago')(topic.openedDate);
            } else if (pubTrack.isClosedTopic(topic)) {
                return 'Closed';
            } else {
                return '';
            }
        };

        pubTrack.addNote = function () {
            pubTrack._addNote(pubTrack.newNote)
                .success(function () {
                    pubTrack.newNote = '';
                });
        };

        pubTrack.beginEditingNote = function (note) {
            previousNote = note.text;
            note.editing = true;
        };

        pubTrack.cancelEditingNote = function (note) {
            note.text = previousNote;
            note.editing = false;
        };

        pubTrack.editNote = function (note) {
            $http.post('/action/publication/notes/' + note.zdbID, { text: note.text })
                .success(function () {
                    note.editing = false;
                });
        };

        pubTrack.deleteNote = function (note) {
            $http.delete('/action/publication/notes/' + note.zdbID)
                .success(function () {
                    var idx = pubTrack.notes.indexOf(note);
                    pubTrack.notes.splice(idx, 1);
                });
        };

        pubTrack.hasTopics = function () {
            return pubTrack.topics.some(function (t) { return t.dataFound; });
        };

        pubTrack.allCorrespondencesClosed = function () {
            return pubTrack.correspondences.length == 0 || pubTrack.correspondences[0].closedDate;
        };

        pubTrack.newCorrespondence = function () {
            $http.post('/action/publication/' + pubId + '/correspondences', {})
                .success(function (data) {
                    pubTrack.correspondences.unshift(data);
                });
        };

        pubTrack.closeCorrespondence = function (replied, corr, idx) {
            corr.closedDate = Date.now();
            corr.replyReceived = replied;
            updateCorrespondence(corr, idx);
        };

        pubTrack.reopenCorrespondence = function (corr, idx) {
            corr.closedDate = null;
            updateCorrespondence(corr, idx);
        };

        pubTrack.deleteCorrespondence = function (corr, idx) {
            $http.delete('/action/publication/correspondences/' + corr.id)
                .success(function () {
                    pubTrack.correspondences.splice(idx, 1);
                });
        };

        getTopics();
        getStatus();
        getNotes();
        getCorrespondences();

    }]);