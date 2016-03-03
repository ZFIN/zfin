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

    .directive('selectAllList', ['$sce', function ($sce) {

        function SelectAllListController() {
            var vm = this;

            vm.toggleAll = toggleAll;
            vm.updateAllSelected = updateAllSelected;
            vm.safeItemLabel = safeItemLabel;

            function toggleAll() {
                vm.items.forEach(function (i) {
                    i.selected = vm.allSelected;
                });
            }

            function updateAllSelected() {
                vm.allSelected = vm.items.every(function(i) {
                    return i.selected;
                });
            }

            function safeItemLabel(locals) {
                return $sce.trustAsHtml(vm.itemLabel(locals));
            }
        }

        function link(scope) {
            var unwatch = scope.$watch('vm.items', function(newValue) {
                if (newValue) {
                    scope.vm.updateAllSelected();
                    unwatch();
                }
            });
        }

        var template =
            '<ul class="list-unstyled">' +
            '  <li ng-show="vm.items.length > 0">' +
            '    <div class="checkbox">' +
            '      <label>' +
            '        <input type="checkbox" ng-model="vm.allSelected" ng-change="vm.toggleAll()">' +
            '        <b>{{ vm.allLabel }}</b>' +
            '      </label>' +
            '    </div>' +
            '  </li>' +
            '  <li ng-repeat="item in vm.items">' +
            '    <div class="checkbox">' +
            '      <label>' +
            '        <input type="checkbox" ng-model="item.selected" ng-change="vm.updateAllSelected()">' +
            '        <span ng-bind-html="vm.safeItemLabel({item: item})">' +
            '      </label>' +
            '    </div>' +
            '  </li>' +
            '</ul>';

        var directive = {
            restrict: 'EA',
            template: template,
            scope: {
                items: '=',
                allLabel: '@',
                itemLabel: '&'
            },
            controller: SelectAllListController,
            controllerAs: 'vm',
            bindToController: true,
            link: link
        };

        return directive;
    }])

    .controller('PubTrackingController', ['$http', '$filter', '$attrs', '$sce', '$location', function ($http, $filter, $attrs, $sce, $location) {
        var pubTrack = this;
        var previousNote = '';
        var pubId = $attrs.zdbId;

        pubTrack.publication = null;
        pubTrack.topics = [];
        pubTrack.status = null;
        pubTrack.notes = [];
        pubTrack.newNote = '';
        pubTrack.warnings = [];
        pubTrack.correspondences = [];

        pubTrack.notification = {
            loading: false,
            editing: false,
            previewing: false,
            sendSuccess: false,
            sendError: false,
            recipients: [],
            authors: [],
            additionalRecipients: '',
            salutation: 'Dear',
            names: '',
            intro: 'I am pleased to report that information about your paper has been entered ' +
                   'into ZFIN, the Zebrafish Model Organism Database.',
            pubReference: '',
            dataNote: 'Genes and mutants associated with your paper are listed on the publication ' +
                      'page and are also appended at the end of this message.',
            customNote: '',
            zfinDescription: 'ZFIN is the zebrafish model organism database, a centralized community ' +
                             'resource for zebrafish genetic, genomic, and developmental data. We ' +
                             'encourage you to share this message with your co-authors and appreciate ' +
                             'any feedback that you are able to offer. Community input is vital to our ' +
                             'success and value as a public resource. If you have corrections, comments, ' +
                             'or additional data that you would like to submit to ZFIN, please contact me.',
            signOff: 'Thank you',
            sender: {
                name: $attrs.curatorFirst + ' ' + $attrs.curatorLast,
                group: 'Scientific Curation Group',
                email: $attrs.curatorEmail
            },
            address: [
                'Zebrafish Model Organism Database',
                '5291 University of Oregon',
                'Eugene, Oregon, USA 97403-5291'
            ],
            curatedData: {}
        };

        // TODO: more graceful error handling would be nice
        var genericFailure = function () {
            alert('Oops! There was a problem communicating with the server. Please try again later. If the problem ' +
                'persists, get in touch with a developer.')
        };

        var getTopics = function() {
            $http.get('/action/publication/' + pubId + '/topics')
                .success(function (data) {
                    pubTrack.topics = data;
                })
                .error(genericFailure);
        };

        var getStatus = function () {
            $http.get('/action/publication/' + pubId + '/status')
                .success(function (data) {
                    pubTrack.status = data;
                })
                .error(genericFailure);
        };

        var getNotes = function () {
            $http.get('/action/publication/' + pubId + '/notes')
                .success(function (data) {
                    pubTrack.notes = data;
                })
                .error(genericFailure);
        };

        var getCorrespondences = function () {
            $http.get('/action/publication/' + pubId + '/correspondences')
                .success(function (data) {
                    pubTrack.correspondences = data;
                })
                .error(genericFailure);
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
            post.error(genericFailure);
            return post;
        };

        var updateStatus = function () {
            var post = $http.post('/action/publication/' + pubId + '/status', pubTrack.status);
            post.success(function (data) {
                pubTrack.status = data;
            });
            post.error(genericFailure);
            return post;
        };

        var updateCorrespondence = function (corr, idx) {
            var post = $http.post('/action/publication/correspondences/' + corr.id, corr);
            post.success(function (data) {
                pubTrack.correspondences[idx] = data;
            });
            post.error(genericFailure);
            return post;
        };

        var addNote = function (txt) {
            var post = $http.post('/action/publication/' + pubId + '/notes', { text: txt });
            post.success(function (data) {
                pubTrack.notes.unshift(data);
            });
            post.error(genericFailure);
            return post;
        };

        var getPublicationDetails = function () {
            $http.get('/action/publication/' + pubId + '/details')
                .success(function (data) {
                    function hasEmail(author) {
                        return author.email;
                    }

                    data.registeredAuthors.forEach(function (author) {
                        author.send = true;
                    });
                    pubTrack.publication = data;

                    pubTrack.notification.authors = data.registeredAuthors.filter(hasEmail);
                    pubTrack.notification.pubReference = data.citation;
                })
                .error(genericFailure);
        };

        var getCuratedEntities = function () {
            return $http.get('/action/publication/' + pubId + '/curatedEntities')
                .then(function (resp) {
                    function byType(types) {
                        var typeArray = [].concat(types);
                        return function(val) {
                            return typeArray.indexOf(val.type) >= 0;
                        }
                    }

                    angular.forEach(resp.data, function(data) {
                        angular.forEach(data, function(datum) {
                            datum.selected = true;
                        })
                    });

                    pubTrack.notification.curatedData = resp.data;
                    pubTrack.notification.curatedData.genes = resp.data.markers.filter(byType('GENE'));
                    pubTrack.notification.curatedData.strs = resp.data.markers.filter(byType(['MRPHLNO', 'CRISPR', 'TALEN']));
                    pubTrack.notification.curatedData.antibodies = resp.data.markers.filter(byType('ATB'));
                })
                .catch(genericFailure);
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
                })
                .error(genericFailure);
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
                })
                .error(genericFailure);
        };

        pubTrack.deleteNote = function (note) {
            $http.delete('/action/publication/notes/' + note.zdbID)
                .success(function () {
                    var idx = pubTrack.notes.indexOf(note);
                    pubTrack.notes.splice(idx, 1);
                })
                .error(genericFailure);
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
                })
                .error(genericFailure);
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
                })
                .error(genericFailure);
        };

        pubTrack.editNotification = function () {
            pubTrack.notification.loading = true;
            pubTrack.notification.sendSuccess = false;
            pubTrack.notification.sendError = false;

            var sendAuthors = pubTrack.notification.recipients = pubTrack.notification.authors
                .filter(function (a) { return a.send; });

            pubTrack.notification.recipients = sendAuthors.map(function(a) { return a.email; });
            pubTrack.notification.noteText = 'Notified authors: ' + sendAuthors.map(function (a) { return a.display; }).join(', ');

            var additional = pubTrack.notification.additionalRecipients.split(/[,\s]+/);
            if (additional.length > 0 && additional[0]) {
                pubTrack.notification.recipients = pubTrack.notification.recipients.concat(additional);
                if (sendAuthors.length > 0) {
                    pubTrack.notification.noteText += ', ';
                }
                pubTrack.notification.noteText += additional.join(', ');
            }

            getCuratedEntities()
                .then(function () {
                    pubTrack.notification.editing = true;
                })
                .finally(function () {
                    pubTrack.notification.loading = false;
                })
        };

        pubTrack.cancelNotificationEditing = function () {
            pubTrack.notification.editing = false;
        };

        pubTrack.previewNotification = function () {
            pubTrack.notification.editing = false;
            pubTrack.notification.previewing = true;
        };

        pubTrack.cancelNotificationPreview = function () {
            pubTrack.notification.editing = true;
            pubTrack.notification.previewing = false;
        };

        pubTrack.hasNoRecipients = function () {
            return !pubTrack.notification.authors.some(function (a) { return a.send; }) &&
                !pubTrack.notification.additionalRecipients;
        };

        pubTrack.generateNotification = function () {
            function isSelected(e) {
                return e.selected;
            }

            function appendSection(entities, label, formatter) {
                if (entities && entities.some(isSelected)) {
                    notif += '<p><b>' + label + '</b><br>';
                    entities.forEach(function (e) {
                        appendEntityLink(e, formatter);
                    });
                    notif += '</p>'
                }
            }

            function nameOnly(e) {
                return e.name;
            }

            function genoName(e) {
                return '<i>' + e.name + '</i>';
            }

            function nameAndAbbrev(e) {
                return e.name + '(<i>' + e.abbreviation + '</i>)';
            }

            function appendEntityLink(e, formatter) {
                if (e.selected) {
                    notif += '<a href="http://' + $location.host() + '/' + e.zdbID + '">' + formatter(e) + '</a><br>';
                }
            }

            var notif =
                '<p>' + pubTrack.notification.salutation + ' ' + pubTrack.notification.names + ',</p>' +
                '<p>' + pubTrack.notification.intro + '</p>' +
                '<p><a href="http://' + $location.host() + '/' + pubTrack.publication.zdbID + '">' + pubTrack.notification.pubReference + '</a></p>' +
                '<p>' + pubTrack.notification.dataNote + '</p>';
            if (pubTrack.notification.customNote) {
                notif += '<p>' + pubTrack.notification.customNote + '</p>';
            }
            notif +=
                '<p>' + pubTrack.notification.zfinDescription + '</p>' +
                '<p>' + pubTrack.notification.signOff + ',</p>' +
                '<p>' + pubTrack.notification.sender.name + '<br>' +
                pubTrack.notification.sender.group + '<br>' +
                pubTrack.notification.sender.email + '</p>' +
                '<p>' + pubTrack.notification.address[0] + '<br>' +
                pubTrack.notification.address[1] + '<br>' +
                pubTrack.notification.address[2] + '</p>';

            appendSection(pubTrack.notification.curatedData.genes, 'Genes', nameAndAbbrev);
            appendSection(pubTrack.notification.curatedData.strs, 'Sequence Targeting Reagents', nameOnly);
            appendSection(pubTrack.notification.curatedData.antibodies, 'Antibodies', nameOnly);

            if (pubTrack.notification.curatedData.expressionGenes && pubTrack.notification.curatedData.expressionGenes.some(isSelected)) {
                notif += '<p><a href="http://' + $location.host() + '/action/figure/all-figure-view/' + pubId + '"><b>Curated Gene Expression</b></a><br>';
                pubTrack.notification.curatedData.expressionGenes.forEach(function (g) {
                    if (g.selected) {
                        notif += nameAndAbbrev(g) + '<br>';
                    }
                });
                notif += '</p>';
            }

            appendSection(pubTrack.notification.curatedData.genotypes, 'Genotypes', genoName);

            return $sce.trustAsHtml(notif);
        };

        pubTrack.sendNotification = function () {
            pubTrack.notification.loading = true;
            $http.post('/action/publication/' + pubId + '/notification', {
                recipients: pubTrack.notification.recipients,
                message: $sce.valueOf(pubTrack.generateNotification())
            }).then(function () {
                pubTrack.notification.editing = false;
                pubTrack.notification.previewing = false;
                pubTrack.notification.sendSuccess = true;
                pubTrack.notification.sendError = false;
                addNote(pubTrack.notification.noteText);
            }).catch(function () {
                pubTrack.notification.sendSuccess = false;
                pubTrack.notification.sendError = true;
            }).finally(function () {
                pubTrack.notification.loading = false;
            });
        };

        getPublicationDetails();
        getTopics();
        getStatus();
        getNotes();
        getCorrespondences();

    }]);