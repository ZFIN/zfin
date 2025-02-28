const path = require('path');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const WebpackAssetsManifest = require('webpack-assets-manifest');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const { EnvironmentPlugin } = require('webpack');
const webpack = require('webpack');

const isProd = process.env.NODE_ENV === 'production';

const config = {
    context: path.resolve(__dirname, 'home/javascript'),
    devtool: isProd ? false : 'eval-cheap-module-source-map',
    mode: isProd ? 'production' : 'development',
    entry: {
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
        filename: '[name].[contenthash].js',
        publicPath: '/dist/'
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env']
                    }
                }
            },
            {
                test: /\.tsx?$/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env', '@babel/preset-react', '@babel/preset-typescript']
                    }
                },
                exclude: /node_modules/
            },
            {
                test: /\.(scss|css)$/,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    {
                        loader: 'sass-loader',
                        options: {
                            additionalData: `$primary: ${process.env.PRIMARY_COLOR || 'null'};`
                        }
                    }
                ]
            },
            {
                //old method was supposed to inline any assets smaller than 8k as base64 data-uris
                //new method: https://stackoverflow.com/a/67514465
                test: /\.(woff2?|ttf|eot|svg|gif|png)$/,
                type: 'asset/resource'
            },
        ]
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js']
    },
    plugins: [
        new CleanWebpackPlugin(),
        new MiniCssExtractPlugin({
            filename: '[name].bundle.[contenthash].css'
        }),
        new webpack.BannerPlugin({ //Add UTF-8 charset to top of style css file
            banner: '@charset "UTF-8";',
            raw: true,
            test: /\.bundle\.\w+\.css$/
        }),
        new WebpackAssetsManifest({
            output: '../asset-manifest.json', // relative to output.path
            publicPath: true,
        }),
        new HtmlWebpackPlugin(),
        // expose certain environment variables via process.env
        new EnvironmentPlugin([
            'NODE_ENV',
            'WIKI_HOST',
            'ZFIN_ADMIN'
        ]),
    ],
    optimization: {
        minimize: isProd,
        minimizer: [new TerserPlugin(), new CssMinimizerPlugin()],
    },
    // jquery is loaded via CDN to make bootstrap 4 play nicely with inline script tags
    externals: {
        jquery: 'jQuery'
    },
};


module.exports = config;
