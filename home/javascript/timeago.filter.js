;(function () {
    angular
        .module('app')
        .filter('timeago', timeago);

    timeago.$inject = ['ZfinUtils'];
    function timeago(zf) {
        return function (input, raw) {
            return zf.timeago(input, undefined, raw);
        }
    }
}());