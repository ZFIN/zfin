require('imagesloaded');
const jQueryBridget = require('jquery-bridget');
const Masonry = require('masonry-layout');
// make Masonry a jQuery plugin
jQueryBridget('masonry', Masonry, window.$);
