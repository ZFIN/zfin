angular.module('app')
    .controller('EditController', ['$attrs', '$scope', function ($attrs, $scope) {
        var eControl = this;
        $scope.editMode = false;

        eControl.editMarker = function () {
            $scope.editMode = true;
        };

        eControl.viewMarker = function () {
            $scope.editMode = false;
        };
    }]);

