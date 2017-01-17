/**
 * IntertabEventService
 *
 * This service exposes methods for communicating between browser tabs or windows.
 */
;(function () {
    angular
        .module('app')
        .factory('IntertabEventService', IntertabEventService);

    IntertabEventService.$inject = ['$window'];
    function IntertabEventService($window) {
        return {
            receiveEvents: receiveEvents,
            fireEvent: fireEvent
        };

        ////////////

        /**
         * Register an event handler callback for the given event name. The callback will be called with the same
         * argument as the underlying storage event (https://developer.mozilla.org/en-US/docs/Web/Events/storage).
         * Note that no event is generated when fireEvent is called by the same tab.
         */
        function receiveEvents(name, callback) {
            $window.addEventListener('storage', function (evt) {
                if (evt.key !== name || !evt.newValue) {
                    return;
                }
                callback.call(this, evt);
            });
        }

        /**
         * Fire the event with the given name in other tabs and windows.
         */
        function fireEvent(name) {
            $window.localStorage.setItem(name, Math.random());
            $window.localStorage.removeItem(name);
        }
    }
}());