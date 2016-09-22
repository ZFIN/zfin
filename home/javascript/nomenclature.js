var nomenApp = angular.module('nomenclature', []);

nomenApp.controller('NomenclatureController', ['$http', '$attrs', '$scope', '$window', function ($http, $attrs, $scope, $window) {
    var nomenController = this;

    nomenController.nomenID;
    nomenController.reason;
    nomenController.comments;
    nomenController.publicationID;
    nomenController.errorMessage;
    nomenController.geneNameOrAbbreviation = null;
    nomenController.hasGeneEdit = false;
    nomenController.fieldName;
    nomenController.showAttribution = false;
    nomenController.publicationDtoList = [];
    nomenController.newAttribution;
    nomenController.newAlias;
    nomenController.aliasID;
    nomenController.previousNameList = [];


    $scope.reasonList = $window.reasonList;
    $scope.markerID = $window.markerID;

    nomenController.updateNomenclature = function () {
        nomenController.errorMessage = '';
        var parameters = {
            'comments': nomenController.comments,
            'reason': nomenController.reason
        };
//            alert('Field Name: ' + nomenController.fieldName);
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
                    $('#nomenclature-modal').hide();
                    nomenController.fetchPreviousNameList();
                })
                .catch(function (error) {
                    nomenController.errorMessage = error.data.message;
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
                nomenController.fetchPreviousNameList();
            })
            .catch(function (error) {
                alert("error")
                nomenController.errorMessage = error.data.message;
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
                nomenController.errorMessage = '';
            })
            .catch(function (error) {
                nomenController.errorMessage = error.data.message;
            });
    };

    nomenController.addAliasAttribution = function () {
        $http.post('/action/marker/alias/addAttribution/' + nomenController.nomenID, nomenController.publicationID)
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
                nomenController.errorMessage = '';
            })
            .catch(function (error) {
                nomenController.errorMessage = error.data.message;
            });
    };

    nomenController.createNewAlias = function () {
        var parameters = {
            'newAlias': nomenController.newAlias,
            'attribution': nomenController.newAttribution
        };
        $http.post('/action/marker/' + markerID + '/addAlias/', parameters)
            .then(function (list) {
                //alert("success")
                $("#alias-modal").hide();
                nomenController.fetchPreviousNameList();
            })
            .catch(function (error) {
                nomenController.errorMessage = error.data.message;
            });
    };

    nomenController.deleteAlias = function (aliasZdbID) {
        $http.delete('/action/marker/' + markerID + '/remove-alias/' + aliasZdbID)
            .then(function (list) {
                nomenController.fetchPreviousNameList();
            })
            .catch(function (error) {
                alert('Error')
            });
        $("#alias-modal").hide();
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
                nomenController.generalError = 'Could not fetch attributions';
            });
    };

    nomenController.fetchAliasAttributions = function () {
        $http.get('/action/marker/alias/attributions/' + nomenController.nomenID)
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
                nomenController.generalError = 'Could not fetch attributions';
            });
    };

    nomenController.fetchPreviousNameList = function () {
        $http.get('/action/marker/previous-name-list/' + markerID)
            .then(function (list) {
                nomenController.previousNameList = [];
                for (data in list.data) {
                    //alert("Obje " + list.data[data].alias);
                    nomenController.previousNameList.push({
                        alias: list.data[data].pureAliasName,
                        aliasZdbID: list.data[data].aliasZdbID,
                        attributionLink: list.data[data].linkWithAttribution,
                    })
                }
                //alert('Success kl')
                $("#previousNameListOriginal").hide();
            })
            .catch(function (error) {
                nomenController.generalError = 'Could not fetch attributions';
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

    nomenController.deleteAliasAttribution = function (publicationID) {
        $http.delete('/action/marker/alias/deleteAttribution/' + nomenController.nomenID + '/' + publicationID)
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
        $('#nomenclature-modal')
            .modal({
                escapeClose: true,
                clickClose: true,
                showClose: true,
                fadeDuration: 100
            })
            .on($.modal.AFTER_CLOSE, function () {
            });
    }

    function openAliasPopup() {
        $('#alias-modal')
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
        nomenController.errorMessage = '';
        $("#nomenclature-modal").show();
        //openEditorPopup();
    };

    nomenController.openAddNewPreviousNameEditor = function () {
        //alert("zdb ID: "+ID);
        nomenController.newAlias = '';
        $("#alias-modal").show();
        //openAliasPopup();
    };

    nomenController.editAttribution = function (ID, name) {
        //alert("name: " + name);
        nomenController.nomenID = ID;
        nomenController.newAlias = name;
        nomenController.errorMessage = '';
        $("#alias-attribution-modal").show();
        nomenController.fetchAliasAttributions(ID);
        //openAliasPopup();
    };

    nomenController.closeAliasEditor = function () {
        $("#alias-modal").hide();
    };

    nomenController.cancelDeleteAlias = function () {
        $.modal.close();
    };

    nomenController.closeGeneEditor = function () {
        $("#nomenclature-modal").hide();
    };

    nomenController.closeAliasAttributionEditor = function () {
        $("#alias-attribution-modal").hide();
        nomenController.publicationDtoList = [];
        nomenController.fetchPreviousNameList();
    };

    nomenController.confirmDeleteAlias = function (aliasID, alias) {
        nomenController.newAlias = alias;
        nomenController.aliasID = aliasID;
        $('#delete-modal')
            .modal({
                escapeClose: false,
                clickClose: false,
                showClose: false,
                fadeDuration: 100
            })
            .on($.modal.AFTER_CLOSE, function () {
            });
    }


}]);


nomenApp.filter('unsafe', function ($sce) {
    return function (val) {
        return $sce.trustAsHtml(val);
    };
});

