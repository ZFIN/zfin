angular.module('nomenclature', [])

    .controller('NomenclatureController', ['$http', '$attrs', '$scope', '$window', function ($http, $attrs, $scope, $window) {
        var nomenController = this;

        nomenController.nomenID;
        nomenController.reason;
        nomenController.comments;
        nomenController.publicationID;
        nomenController.geneNameOrAbbreviation = null;
        nomenController.hasGeneEdit = false;
        nomenController.fieldName;
        nomenController.showAttribution = false;
        nomenController.publicationDtoList = [];

        $scope.reasonList = $window.reasonList;

        nomenController.updateNomenclature = function () {
//            alert('Field Name: ' + nomenController.fieldName);
            var parameters = {
                'comments': nomenController.comments,
                'reason': nomenController.reason,
            };
            if (nomenController.fieldName == 'Nomenclature') {
                $http.post('/action/nomenclature/update/' + nomenController.nomenID, parameters)
                    .then(function (success) {
                        location.reload();
                    })
                    .catch(function (error) {

                    });
            } else {
                if (nomenController.fieldName == 'Gene Name') {
                    parameters.name = nomenController.geneNameOrAbbreviation;
                } else if (nomenController.fieldName == 'Gene Symbol') {
                    parameters.abbreviation = nomenController.geneNameOrAbbreviation;
                }
                $http.post('/action/marker/edit/' + nomenController.nomenID, parameters)
                    .then(function (success) {
                        if (nomenController.fieldName == 'Gene Name') {
                            $("#markerName").text(parameters.name)
                        } else if (nomenController.fieldName == 'Gene Symbol') {
                            $("#markerAbbreviation").text(parameters.abbreviation)
                        }
                        $('#evidence-modal').modal('hide');
                        $('#markerName').click();
                    })
                    .catch(function (error) {

                    });

            }
        };

        nomenController.updateGeneName = function () {
            var parameters = {
                'comments': nomenController.comments,
                'reason': nomenController.reason,
                'name': nomenController.geneNameOrAbbreviation
            };
            $http.post('/action/marker/edit/' + nomenController.nomenID, parameters)
                .then(function (success) {
                    location.reload();
                })
                .catch(function (error) {

                });
        };

        nomenController.updateGeneAbbreviation = function () {
            $http.post('/action/marker/edit/' + nomenController.nomenID, parameters)
                .then(function (success) {
                    location.reload();
                })
                .catch(function (error) {

                });
        };

        nomenController.updateMarkerHistory = function () {
            var parameters = {
                'comments': nomenController.comments,
                'reason': nomenController.reason
            };
            $http.post('/action/nomenclature/update/' + nomenController.nomenID, parameters)
                .then(function (success) {
                    location.reload();
                })
                .catch(function (error) {

                });
        };

        nomenController.reload = function () {
            location.reload();
        };

        nomenController.addAttribution = function () {
            $http.post('/action/nomenclature/addAttribution/' + nomenController.nomenID, nomenController.publicationID)
                .then(function (list) {
                    nomenController.publicationDtoList = [];
                    for (data in list.data) {
                        //alert("Obje " + list.data[data].zdbID);
                        nomenController.publicationDtoList.push({
                            zdbID: list.data[data].zdbID,
                            title: list.data[data].title
                        })
                    }
                    nomenController.publicationID = '';
                })
                .catch(function (error) {
                    alert('Error')
                });
        };

        nomenController.fetchAttributions = function () {
            $http.get('/action/nomenclature/attributions/' + nomenController.nomenID)
                .then(function (list) {
                    for (data in list.data) {
                        //alert("Obje " + list.data[data].zdbID);
                        nomenController.publicationDtoList.push({
                            zdbID: list.data[data].zdbID,
                            title: list.data[data].title
                        })
                    }
                    //alert(publicationDtoList[0].zdbID)
                })
                .catch(function (error) {
                    vm.generalError = 'Could not fetch attributions';
                });
        };


        nomenController.deleteAttribution = function (publicationID) {
            $http.delete('/action/nomenclature/deleteAttribution/' + nomenController.nomenID + '/' + publicationID)
                .then(function (list) {
                    nomenController.publicationDtoList = [];
                    for (data in list.data) {
                        //alert("Obje " + list.data[data].zdbID);
                        nomenController.publicationDtoList.push({
                            zdbID: list.data[data].zdbID,
                            title: list.data[data].title
                        })
                    }
                })
                .catch(function (error) {
                    vm.generalError = 'Could not delete attribution';
                });
        };


        nomenController.openNomenclatureEditor = function (ID, comments, reason) {
            nomenController.nomenID = ID;
            nomenController.comments = comments;
            nomenController.reason = reason;
            nomenController.showAttribution = true;
            nomenController.hasGeneEdit = false;
            nomenController.fieldName = 'Nomenclature';
            nomenController.fetchAttributions();
            openEditorPopup();
        };

        function openEditorPopup() {
            $('#evidence-modal')
                .modal({
                    escapeClose: true,
                    clickClose: true,
                    showClose: true,
                    fadeDuration: 100
                })
                .on($.modal.AFTER_CLOSE, function () {
                });
        }

        nomenController.openGeneEditor = function (ID, name, type) {
            nomenController.nomenID = ID;
            nomenController.fieldName = type;
            nomenController.geneNameOrAbbreviation = name;
            nomenController.hasGeneEdit = true;
            openEditorPopup();
        }

    }]);


