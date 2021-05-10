;(function () {
    angular
        .module('app')
        .factory('FieldErrorService', FieldErrorService);

    function FieldErrorService() {
        return {
            processErrorResponse: processErrorResponse
        };

        function processErrorResponse(response) {
            var errors = {};
            errors.$isGeneric = !response.data.fieldErrors.length;
            response.data.fieldErrors.forEach(function (error) {
                if (!errors.hasOwnProperty(error.field)) {
                    errors[error.field] = [];
                }
                errors[error.field].push(error.message);
            });
            return errors;
        }
    }
}());