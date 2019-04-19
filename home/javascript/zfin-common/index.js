require('./autocompletify');

const headerMenu = require('exports-loader?hdrGetCookie,hdrSetCookie!./header-menu');
window.hdrGetCookie = headerMenu.hdrGetCookie;
window.hdrSetCookie = headerMenu.hdrSetCookie;

require('./form-reset');

window.processPopupLinks = require('exports-loader?processPopupLinks!./popups');

require('./your-input-welcome');
require('./list-collapse');
require('./figure-gallery-resize.jquery');
require('./gbrowse-image');
require('./tabbify');
require('./table-collapse');
require('./multirow-table');