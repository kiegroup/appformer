const path = require("path");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const CircularDependencyPlugin = require("circular-dependency-plugin");

module.exports = {
    mode: "production",
    entry: {
        index: "./src/index.tsx"
    },
    externals: {
        'appformer-js': {
            root: 'AppFormer', //indicates global variable
            commonjs: 'appformer-js',
            commonjs2: 'appformer-js',
            amd: 'appformer-js'
        },
        react: {
            root: "React", //indicates global variable
            commonjs: "react",
            commonjs2: "react",
            amd: "react"
        },
        "react-dom": {
            root: "ReactDOM", //indicates global variable
            commonjs: "react-dom",
            commonjs2: "react-dom",
            amd: "react-dom"
        }
    },
    output: {
        path: path.resolve(__dirname, "./dist"),
        filename: "index.js",
        library: "UberfireShowcaseReactComponents",
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
        extensions: [".tsx", ".ts", ".js", ".jsx"],
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
