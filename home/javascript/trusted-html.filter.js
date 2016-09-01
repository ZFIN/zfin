;(function () {
    angular
        .module('app')
        .filter('trustedHtml', trustedHtml);

    trustedHtml.$inject = ['$sce'];
    function trustedHtml($sce) {
        return function (text) {
            return $sce.trustAsHtml(text);
        };
    }
}());