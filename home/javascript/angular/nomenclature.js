angular.module('app').controller('NomenclatureController', ['$http', '$attrs', '$scope', '$window', function ($http, $attrs, $scope, $window) {
    var nomenController = this;

    nomenController.nomenID;
    nomenController.geneName;
    nomenController.geneAbbreviation;
    nomenController.reason;
    nomenController.comments;
    nomenController.publicationID;
    nomenController.errorMessage;
    nomenController.geneNameOrAbbreviation;
    nomenController.hasGeneEdit = false;
    nomenController.fieldName;
    nomenController.showAttribution = false;
    nomenController.publicationDtoList = [];
    nomenController.newAttribution;
    nomenController.newAlias;
    nomenController.aliasID;
    nomenController.previousNameList = [];
    $scope.editMode;

    $scope.reasonList = $window.reasonList;
    $scope.markerID = $window.markerID;

    $scope.$watch("editMode", function () {
        // reset values
        nomenController.reset();
    });

    nomenController.reset = function () {
        nomenController.reason = '';
        nomenController.comments = '';
    };

    $scope.init = function (name, abbreviation) {
        nomenController.geneName = name;
        nomenController.geneAbbreviation = abbreviation;
    };


    nomenController.updateNomenclature = function () {
        nomenController.errorMessage = '';
        var parameters = {
            'comments': nomenController.comments,
            'reason': nomenController.reason
        };
        if (nomenController.fieldName == 'Nomenclature') {
            $http.post('/action/nomenclature/update/' + nomenController.nomenID, parameters)
                .then(function (success) {
                    location.reload();
                })
                .catch(function (error) {

                });
        } else {
            if (nomenController.geneNameOrAbbreviation == nomenController.geneNameOrAbbreviationOrg) {
                nomenController.errorMessage = "No change in name or symbol detected."
                return;
            }

            if (nomenController.fieldName == 'Gene Name') {
                parameters.name = nomenController.geneNameOrAbbreviation;
            } else if (nomenController.fieldName == 'Gene Symbol') {
                parameters.abbreviation = nomenController.geneNameOrAbbreviation;
            }
            $http.post('/action/marker/edit/' + nomenController.nomenID, parameters)
                .then(function (success) {
                    if (nomenController.fieldName == 'Gene Name') {
                        $("#markerName").text(parameters.name)
                        nomenController.geneName = nomenController.geneNameOrAbbreviation;
                    } else if (nomenController.fieldName == 'Gene Symbol') {
                        $('body').find("[geneSymbol='']").text(parameters.abbreviation)
                        nomenController.fetchPreviousNameList();
                        nomenController.geneAbbreviation = nomenController.geneNameOrAbbreviation;
                    }
                    nomenController.closeModal();
                })
                .catch(function (error) {
                    nomenController.errorMessage = error.data.message;
                });

        }
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
                nomenController.fetchPreviousNameList();
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
                    nomenController.publicationDtoList.push({
                        zdbID: list.data[data].zdbID,
                        title: list.data[data].title
                    })
                }
                nomenController.publicationID = '';
                nomenController.errorMessage = '';
                nomenController.fetchPreviousNameList();
            })
            .catch(function (error) {
                nomenController.errorMessage = error.data.message;
            });
    };

    nomenController.createNewAlias = function () {
        nomenController.errorMessage = '';
        if (!nomenController.newAlias) {
            nomenController.errorMessage = 'No alias provided';
            return;
        }

        var parameters = {
            'newAlias': nomenController.newAlias,
            'attribution': nomenController.newAttribution
        };
        $http.post('/action/marker/' + markerID + '/addAlias/', parameters)
            .then(function (list) {
                //alert("success")
                nomenController.closeModal();
                nomenController.fetchPreviousNameList();
                nomenController.newAlias = '';
                nomenController.newAttribution = ''
            })
            .catch(function (error) {
                nomenController.errorMessage = error.data.message;
            });
    };

    nomenController.deleteAlias = function (aliasZdbID) {
        $http.delete('/action/marker/' + markerID + '/remove-alias/' + aliasZdbID)
            .then(function (list) {
                nomenController.closeModal();
                nomenController.fetchPreviousNameList();
            })
            .catch(function (error) {
                nomenController.errorMessage = error.data.message;
            });
    };

    nomenController.fetchAttributions = function () {
        nomenController.publicationDtoList = [];
        $http.get('/action/nomenclature/attributions/' + nomenController.nomenID)
            .then(function (list) {
                for (data in list.data) {
                    nomenController.publicationDtoList.push({
                        zdbID: list.data[data].zdbID,
                        title: list.data[data].title
                    })
                }
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
                nomenController.generalError = 'Could not delete attribution';
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
                nomenController.fetchPreviousNameList();
            })
            .catch(function (error) {
                nomenController.generalError = 'Could not delete attribution';
            });
    };


    nomenController.openNomenclatureEditor = function (ID, reason, index) {
        nomenController.nomenID = ID;
        nomenController.comments = $('#data-comments-' + index).html();
        nomenController.reason = reason;
        nomenController.showAttribution = true;
        nomenController.hasGeneEdit = false;
        nomenController.fieldName = 'Nomenclature';
        nomenController.fetchAttributions();
        openModalPopup('nomenclature-modal');
    };

    function openModalPopup(element) {
        //alert('popup: '+element);
        return $('#' + element)
            .modal({
                escapeClose: true,
                clickClose: true,
                showClose: true,
                fadeDuration: 100
            });
    }

    nomenController.openGeneEditor = function (ID, name, type) {
        nomenController.nomenID = ID;
        nomenController.fieldName = type;
        nomenController.geneNameOrAbbreviation = name;
        nomenController.geneNameOrAbbreviationOrg = name;
        nomenController.hasGeneEdit = true;
        nomenController.errorMessage = '';
        openModalPopup('nomenclature-modal');
    };

    nomenController.openAddNewPreviousNameEditor = function () {
        //alert("zdb ID: "+ID);
        nomenController.newAlias = '';
        openModalPopup('alias-modal');
    };

    nomenController.editAttribution = function (ID, name) {
        nomenController.nomenID = ID;
        nomenController.newAlias = name;
        nomenController.errorMessage = '';
        nomenController.fetchAliasAttributions(ID);
        openModalPopup('alias-attribution-modal').on($.modal.AFTER_CLOSE, function () {
            nomenController.closeAliasAttributionEditor();
        });
    };

    nomenController.closeModal = function () {
        $.modal.close();
    };

    nomenController.closeAliasAttributionEditor = function () {
        nomenController.closeModal();
        nomenController.publicationDtoList = [];
        nomenController.fetchPreviousNameList();
    };

    nomenController.confirmDeleteAlias = function (aliasID, alias) {
        nomenController.aliasID = aliasID;
        nomenController.newAlias = alias;
        openModalPopup('delete-modal');
    };

}]);

$(document).ready(function () {
    $("#alias-modal").on('shown.bs.modal', function () {
        $("[data-modalfocus]", this).focus();
    });
});