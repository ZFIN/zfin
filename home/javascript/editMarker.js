var editMarker = angular.module('editMarker', []);

editMarker.controller('EditController', ['$attrs', '$scope', function ($attrs, $scope) {
    var eControl = this;
    $scope.editMode = false;

    eControl.editMarker = function () {
        $scope.editMode = true;
        $("#viewMarker").show();
        $("#editMarker").hide();
    };

    eControl.viewMarker = function () {
        $scope.editMode = false;
        $("#viewMarker").hide();
        $("#editMarker").show();
    };
}]);

