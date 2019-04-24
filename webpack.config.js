const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = {
    context: path.resolve(__dirname, 'home/javascript'),
    entry: {
        angular: './angular/index.js',
        bootstrap: './bootstrap.js',
        curation: './curation/index.js',
        'jquery-ui': './jquery-ui/index.js',
        profiles: './profiles/index.js',
        search: './search/index.js',
        'vendor-common': './vendor-common.js',
        'zfin-common': './zfin-common/index.js',
    },
    output: {
        path: path.resolve(process.env.TARGETROOT, 'home/javascript/dist'),
        filename: '[name].bundle.js',
        publicPath: 'javascript/dist'
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                ],
            },
            {
                test: /\.(woff2?|ttf|eot|svg|gif|png)$/,
                use: {
                    loader: 'url-loader',
                    options: {
                        limit: 8192
                    }
                }
            },
        ],
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: '[name].bundle.css',
        }),
    ],
};
