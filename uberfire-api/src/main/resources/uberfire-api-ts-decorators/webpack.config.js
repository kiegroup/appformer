const path = require("path");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const CircularDependencyPlugin = require("circular-dependency-plugin");

module.exports = {
    mode: "production",
    entry: {
        index: "./src/index.ts"
    },
    externals: [
        "@kiegroup-ts-generated/uberfire-api"
    ],
    output: {
        path: path.resolve(__dirname, "./dist"),
        filename: "index.js",
        library: "UberfireApiDecorators",
        libraryTarget: "umd",
        umdNamedDefine: true
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                loader: "ts-loader",
                options: {
                    configFile: path.resolve("./tsconfig.json")
                }
            }
        ]
    },
    resolve: {
        extensions: [".tsx", ".ts", ".js"],
        modules: [path.resolve("./node_modules"), path.resolve("./src")]
    },
    plugins: [
        new CleanWebpackPlugin(["dist"]),
        new CircularDependencyPlugin({
            exclude: /node_modules/, // exclude detection of files based on a RegExp
            failOnError: false, // add errors to webpack instead of warnings
            cwd: process.cwd() // set the current working directory for displaying module paths
        })
    ]
};
