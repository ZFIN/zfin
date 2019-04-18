const path = require('path');

module.exports = {
    context: path.resolve(__dirname, 'home/javascript'),
    entry: {
        'vendor-common': './vendor-common.js',
        'zfin-common': './zfin-common.js',
    },
    output: {
        path: path.resolve(process.env.TARGETROOT, 'home/javascript/dist'),
        filename: '[name].bundle.js',
        publicPath: 'javascript/dist'
    },
};
