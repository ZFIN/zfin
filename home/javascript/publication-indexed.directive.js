;(function () {
  angular
    .module('app')
    .directive('publicationIndexed', publicationIndexed);

  function publicationIndexed() {
    var directive = {
      restrict: 'EA',
      template: '' +
        '<form>' +
        '    <label>' +
        '        <input type="checkbox" ng-model="vm.indexed.indexed" ng-change="vm.handleToggle()" ng-show="!vm.saving">' +
        '        <span ng-show="vm.saving"><i class="fas fa-spinner fa-spin"></i></span>' +
        '        Indexed\n' +
        '    </label>' +
        '    <div class="text-danger">{{vm.error}}</div>' +
        '</form>',
      scope: {
        pubId: '@',
        curator: '='
      },
      controller: PublicationIndexedController,
      controllerAs: 'vm',
      bindToController: true
    };

    return directive;
  }

  PublicationIndexedController.$inject = ['PublicationService', 'IntertabEventService'];
  function PublicationIndexedController(PublicationService, IntertabEventService) {
    var vm = this;

    vm.indexed = null;
    vm.saving = false;
    vm.error = '';

    vm.handleToggle = handleToggle;

    activate();

    function activate() {
      PublicationService.getIndexed(vm.pubId)
        .then(function (response) {
          vm.indexed = response.data;
        });
    }

    function handleToggle() {
      vm.saving = true;
      vm.indexed.indexer = vm.curator;
      PublicationService.updateIndexed(vm.indexed)
        .then(function (response) {
          vm.error = '';
          vm.indexed = response.data;
          IntertabEventService.fireEvent('pub-status-update');
        })
        .catch(function () {
          vm.error = 'Something went wrong. Try again later.';
        })
        .finally(function () {
          vm.saving = false;
        });
    }
  }
}());