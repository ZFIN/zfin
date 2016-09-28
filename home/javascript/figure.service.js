;(function () {
    angular
        .module('app')
        .factory('FigureService', FigureService);

    FigureService.$inject = ['$http'];
    function FigureService($http) {
        return {
            getFigures: getFigures,
            addFigure: addFigure,
            updateFigure: updateFigure,
            deleteFigure: deleteFigure,
            addImage: addImage,
            deleteImage: deleteImage
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

        function updateFigure(fig) {
            return $http.post('/action/figure/' + fig.zdbId, fig);
        }

        function deleteFigure(fig) {
            return $http({
                url: '/action/figure/' + fig.zdbId,
                method: 'DELETE',
                transformResponse: undefined
            });
        }

        function addImage(fig, file) {
            var form = new FormData();
            form.set('file', file);
            return $http.post('/action/figure/' + fig.zdbId + '/images', form, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            });
        }

        function deleteImage(img) {
            return $http({
                url: '/action/image/' + img.zdbId,
                method: 'DELETE',
                transformResponse: undefined
            });
        }
    }
}());