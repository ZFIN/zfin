require('bootstrap');
require('bootstrap-datepicker');

require('../../css/zfin-bootstrap.scss');
require('bootstrap-datepicker/dist/css/bootstrap-datepicker3.css');

// let jquery-modal have the $.modal() function, bootstrap's moves over to $.bootstrapModal()
$.fn.bootstrapModal = $.fn.modal.noConflict();
$.modal.defaults.modalClass = '';
