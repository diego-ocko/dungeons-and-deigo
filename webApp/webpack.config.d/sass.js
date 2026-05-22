// This file is auto-merged into the Webpack config by Kotlin's Gradle plugin
const path = require('path');

// __dirname = build/wasm/packages/dungeons-and-deigo-webApp
const projectRoot = path.resolve(__dirname, '../../../../');
const sassPath = path.resolve(projectRoot, 'webApp/src/wasmJsMain/kotlin/com/dungeonsanddeigo/web/main.scss');

config.module.rules.push({
    test: /\.scss$/,
    use: [
        'style-loader',
        'css-loader',
        'sass-loader'
    ]
});

// Add SCSS as an additional entry point
const mainEntry = config.entry.main;
if (Array.isArray(mainEntry)) {
    mainEntry.unshift(sassPath);
} else {
    config.entry.main = [sassPath, mainEntry];
}
