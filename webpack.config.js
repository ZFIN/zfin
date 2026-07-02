const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const WebpackAssetsManifest = require('webpack-assets-manifest');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const { EnvironmentPlugin } = require('webpack');
const webpack = require('webpack');

const fs = require('fs');

const isProd = process.env.NODE_ENV === 'production';

/**
 * Emits stable, un-hashed copies of selected entrypoint bundles, e.g.
 *   style.bundle.<hash>.css  -> style.latest.css
 *   zfin-common.<hash>.js    -> zfin-common.latest.js
 *
 * Statically-served HTML pages (see the zfbook conversion) reference the
 * ".latest" names so their baked-in <head> tags never go stale across deploys.
 * IMPORTANT: the ".latest.*" files must be served with a short/no-cache header
 * (they intentionally share a name across builds) -- the 1-year cache rule on
 * home/dist has a FilesMatch carve-out for them in docker/httpd/conf-local.
 */
class LatestAliasPlugin {
    constructor(entryNames) {
        this.entryNames = entryNames;
    }

    apply(compiler) {
        compiler.hooks.afterEmit.tapAsync('LatestAliasPlugin', (compilation, callback) => {
            const outPath = compilation.outputOptions.path;
            const stats = compilation.getStats().toJson({ all: false, entrypoints: true });
            Object.entries(stats.entrypoints)
                .filter(([name]) => this.entryNames.includes(name))
                .forEach(([name, entrypoint]) => {
                    entrypoint.assets
                        .map(asset => asset.name)
                        .filter(file => /\.(js|css)$/.test(file))
                        .forEach(file => {
                            const ext = path.extname(file);
                            const dest = path.join(outPath, `${name}.latest${ext}`);
                            fs.copyFileSync(path.join(outPath, file), dest);
                        });
                });
            callback();
        });
    }
}

const config = {
    context: path.resolve(__dirname, 'home/javascript'),
    devtool: isProd ? false : 'eval-cheap-module-source-map',
    mode: isProd ? 'production' : 'development',
    entry: {
        bootstrap: './bootstrap/index.js',
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
        publicPath: '/dist/',
        clean: true,
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
        // Stable un-hashed aliases for the bundles referenced by static HTML pages.
        new LatestAliasPlugin(['style', 'vendor-common', 'zfin-common']),
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
