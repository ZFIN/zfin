const path = require('path');
const webpack = require('webpack');

module.exports = {
    context: path.resolve(__dirname, 'home/javascript'),
    entry: {
        angular: './angular/index.js',
        'vendor-common': './vendor-common.js',
        'zfin-common': './zfin-common.js',
    },
    output: {
        path: path.resolve(process.env.TARGETROOT, 'home/javascript/dist'),
        filename: '[name].bundle.js',
        publicPath: 'javascript/dist'
    },
};
