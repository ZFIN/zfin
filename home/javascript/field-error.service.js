;(function() {
    angular
        .module('app')
        .factory('FieldErrorService', FieldErrorService);

    function FieldErrorService() {
        return {
            clearErrors: clearErrors,
            processErrorResponse: processErrorResponse
        };

        function clearErrors() {
            return {
                isGeneric: false,
                fields: {}
            };
        }

        function processErrorResponse(response) {
            var errors = clearErrors;
            errors.isGeneric = !response.data.fieldErrors.length;
            response.data.fieldErrors.forEach(function (error) {
                if (!errors.fields.hasOwnProperty(error.field)) {
                    errors.fields[error.field] = [];
                }
                errors.fields[error.field].push(error.message);
            });
            return errors;
        }
    }
}());