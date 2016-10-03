angular.module('nomenclature', [])

    .controller('NomenclatureController', ['$http', '$attrs', '$scope', '$window', function ($http, $attrs, $scope, $window) {
        var nomenController = this;

        nomenController.nomenID;
        nomenController.reason;
        nomenController.comments;
        nomenController.publicationID;
        nomenController.errorMessage;
        nomenController.publicationDtoList = [];

        $scope.reasonList = $window.reasonList;

        nomenController.updateNomenclature = function () {
            var parameters = {
                'comments': nomenController.comments,
                'reason': nomenController.reason
            }
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


        nomenController.openEditor = function (ID, reason, index) {
            nomenController.nomenID = ID;
            nomenController.comments = $('#data-comments-' + index).html();
            nomenController.reason = reason;
            nomenController.fetchAttributions();
            $('#evidence-modal')
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


