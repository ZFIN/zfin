;(function () {
    angular
        .module('app')
        .factory('FigureService', FigureService);

    FigureService.$inject = ['$http'];
    function FigureService($http) {
        return {
            getFigures: getFigures,
            addFigure: addFigure,
            deleteFigure: deleteFigure
        };

        function getFigures(pubId) {
            return $http.get('/action/publication/' + pubId + '/figures');
        }

        function addFigure(pubId, label, caption, fileList) {
            var form = new FormData();
            form.set('label', label);
            form.set('caption', caption);
            angular.forEach(fileList, function (file) {
                form.append('files', file);
            });
            return $http.post('/action/publication/' + pubId + '/figures', form, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            });
        }

        function deleteFigure(fig) {
            return $http({
                url: '/action/figure/' + fig.zdbId,
                method: 'DELETE',
                transformResponse: undefined
            });
        }
    }
}());