const path = require('path');
const webpack = require('webpack');

const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCssAssetsPlugin = require('css-minimizer-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const WebpackAssetsManifest = require('webpack-assets-manifest');

const isProd = process.env.NODE_ENV === 'production';

const config = {
    context: path.resolve(__dirname, 'home/javascript'),
    devtool: isProd ? false : 'eval-cheap-module-source-map',
    mode: 'development',
    entry: {
        angular: './angular/index.js',
        bootstrap: './bootstrap/index.js',
        curation: './curation/index.js',
        'jquery-ui': './jquery-ui/index.js',
        profiles: './profiles/index.js',
        search: './search/index.js',
        style: './style/index.js',
        react: './react/index.js',
        'vendor-common': './vendor-common/index.js',
        'zfin-common': './zfin-common/index.js',
    },
    output: {
        path: path.resolve(process.env.TARGETROOT, 'home/dist'),
        filename: '[name].bundle.[contenthash].js',
        publicPath: '/dist/'
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: ['babel-loader'],
            },
            {
                test: /\.s?css$/,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    {
                        loader: 'sass-loader',
                        options: {
                            additionalData: '$primary: ' + (process.env.PRIMARY_COLOR || 'null') + ';',
                        }
                    },
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
            filename: '[name].bundle.[contenthash].css',
        }),
        new WebpackAssetsManifest({
            output: '../asset-manifest.json', // relative to output.path
            publicPath: true,
        }),
        // expose certain environment variables via process.env
        new webpack.EnvironmentPlugin([
            'NODE_ENV',
            'WIKI_HOST',
            'ZFIN_ADMIN',
        ]),
    ],
    // jquery is loaded via CDN to make bootstrap 4 play nicely with inline script tags
    externals: {
        jquery: 'jQuery',
    },
};

if (isProd) {
    config.mode = 'production';
    config.optimization = {
        minimizer: [new TerserPlugin(), new OptimizeCssAssetsPlugin()],
    };
}

module.exports = config;
