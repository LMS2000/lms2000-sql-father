// This file is generated by Umi automatically
// DO NOT CHANGE IT MANUALLY!
// Created by Umi Plugin

export interface IConfigFromPlugins {
clientLoader?: {

}
title?: string
styles?: any[]
scripts?: any[]
routes?: {

}[]
presets?: string[]
plugins?: string[]
npmClient?: ("pnpm" | "tnpm" | "cnpm" | "yarn" | "npm")
mountElementId?: string
metas?: any[]
links?: any[]
history?: {
type?: ("browser" | "hash" | "memory")
}
headScripts?: any[]
conventionRoutes?: {
base?: string
exclude?: any[]
}
base?: string
writeToDisk?: boolean
theme?: {

}
targets?: {

}
svgr?: {

}
svgo?: ({

} | boolean)
styleLoader?: {

}
srcTranspiler?: ("babel" | "esbuild" | "swc" | "none")
sassLoader?: {

}
runtimePublicPath?: {

}
purgeCSS?: {

}
publicPath?: string
proxy?: ({

} | any[])
postcssLoader?: {

}
outputPath?: string
mfsu?: ({
cacheDirectory?: string
chainWebpack?: (() => any)
esbuild?: boolean
exclude?: any[]
include?: string[]
mfName?: string
runtimePublicPath?: boolean
strategy?: ("eager" | "normal")
} | boolean)
mdx?: {
loader?: string
loaderOptions?: {

}
}
manifest?: {

}
lessLoader?: {

}
jsMinifierOptions?: {

}
jsMinifier?: ("esbuild" | "swc" | "terser" | "uglifyJs" | "none")
inlineLimit?: number
ignoreMomentLocale?: boolean
https?: {

}
hash?: boolean
forkTSChecker?: {

}
fastRefresh?: boolean
extraPostCSSPlugins?: any[]
extraBabelPresets?: any[]
extraBabelPlugins?: any[]
extraBabelIncludes?: string[]
externals?: ({

} | string | (() => any))
esm?: {

}
devtool?: (string | boolean)
depTranspiler?: ("babel" | "esbuild" | "swc" | "none")
define?: {

}
deadCode?: {

}
cssMinifierOptions?: {

}
cssMinifier?: ("cssnano" | "esbuild" | "parcelCSS" | "none")
cssLoaderModules?: {

}
cssLoader?: {

}
copy?: any[]
chainWebpack?: (() => any)
cacheDirectoryPath?: string
babelLoaderCustomize?: string
autoprefixer?: {

}
autoCSSModules?: boolean
alias?: {

}
crossorigin?: (boolean | {
include?: {

}[]
})
esmi?: {
cdnOrigin?: string
shimUrl?: string
}
favicons?: string[]
mock?: {
exclude?: string[]
include?: string[]
}
polyfill?: {
imports?: string[]
}
routePrefetch?: {

}
ssr?: {
serverBuildPath?: string
platform?: string
}
terminal?: {

}
tmpFiles?: boolean
lowImport?: {
libs?: any[]
css?: string
}
vite?: {

}
apiRoute?: {
platform?: string
}
monorepoRedirect?: (boolean | {
srcDir?: string[]
exclude?: {

}[]
})
clickToComponent?: {
editor?: string
}
verifyCommit?: {
scope?: string[]
allowEmoji?: boolean
}
access?: ({

} | boolean)
analytics?: ({

} | boolean)
antd?: ({
configProvider?: {

}
dark?: boolean
compact?: boolean
import?: boolean
style?: ("less" | "css")
} | boolean)
dva?: ({
extraModels?: string[]
immer?: {

}
} | boolean)
initialState?: ({
loading?: string
} | boolean)
layout?: ({

} | boolean)
model?: ({
extraModels?: string[]
} | boolean)
moment2dayjs?: ({
preset?: string
plugins?: any[]
} | boolean)
request?: ({
dataField?: ""
} | boolean)
locale?: ({
default?: string
useLocalStorage?: boolean
baseNavigator?: boolean
title?: boolean
antd?: boolean
baseSeparator?: string
} | boolean)
qiankun?: ({
slave?: {

}
master?: {

}
externalQiankun?: boolean
} | boolean)
tailwindcss?: ({

} | boolean)
}
