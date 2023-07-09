var gulp = require('gulp');
let tinypng = require('gulp-tinypng-with-cache-rdd-mod')

const resImgs = __dirname + '/assets/resources/Imgs'
const resParticle = __dirname + '/assets/resources/particle'
const resUI = __dirname + '/UI_ori'
const destUI = __dirname + '/assets/resources/UI'
const settingPath = __dirname + "/settings"
const apiKeyList = [
    // 'XgNgkoyWbdIZd8OizINMjX2TpxAd_Gp3', // 无效 key
    // 'IAl6s3ekmONUVMEqWZdIp1nV2ItJLyPC', // 有效 key
    '5Y4qLp4G9WzGmzXpNXynPb9M1RNFK46Q',
    'P6L0DV5RJMgNG9Cx9NwcMtbQF05BW3GQ',
    '4L3sZFvkrpr3XgwdbrJ0T5vfyLTHcYlf',
    'VHkkqPC97cGPrk5SkHz5r9JXZXcvMWLS',
]

function goodluck(path) {
    return [
        path + '/**/*.png',
        path + '/**/*.jpg',
        path + '/**/*.jpeg',
    ]
}

/**压缩 fairyGUI 打包出的图片 */
gulp.task('tinypng_UI', function () {
    return gulp.src([
        resUI + "/**/*",
    ], {
        // base: './', // 对文件使用相路径，为了后面覆盖源文件
        nodir: true, // 忽略文件夹
    })
        .pipe(tinypng({
            apiKeyList,
            reportFilePath: settingPath + '/tinypngReportUI.json', // 不设置，则不进行日志记录
            md5RecordFilePath: settingPath + '/tinypngMd5RecordUI.json', // 不设置，则不进行缓存过滤
            recordSourceMd5: true,
            minCompressPercentLimit: 10, // 默认值为零，最小压缩百分比限制，为保证图片质量，当压缩比例低于该值时，保持源文件，避免过分压缩，损伤图片质量
            createMd5FormOrigin: false, // 不进行压缩操作，只生成现有图片的 md5 信息，并作为缓存。用于「初次项目接入」及手动清理冗余的「图片md5信息」
        }))
        .pipe(gulp.dest(destUI, { overwrite: true })) // 覆写原文件
})

/**压缩 resource 中的图片 */
gulp.task('tinypng_Others', function (cb) {
    console.log("Do Nothings.")
    cb()
    return;
    return gulp.src([
        ...goodluck(resImgs),
        ...goodluck(resParticle),
    ], {
        base: './', // 对文件使用相路径，为了后面覆盖源文件
        nodir: true, // 忽略文件夹
    })
        .pipe(tinypng({
            apiKeyList,
            reportFilePath: settingPath + '/tinypngReportOthers.json', // 不设置，则不进行日志记录
            md5RecordFilePath: settingPath + '/tinypngMd5RecordOthers.json', // 不设置，则不进行缓存过滤
            recordSourceMd5: false,
            minCompressPercentLimit: 10, // 默认值为零，最小压缩百分比限制，为保证图片质量，当压缩比例低于该值时，保持源文件，避免过分压缩，损伤图片质量
            createMd5FormOrigin: false, // 不进行压缩操作，只生成现有图片的 md5 信息，并作为缓存。用于「初次项目接入」及手动清理冗余的「图片md5信息」
        }))
        .pipe(gulp.dest('./', { overwrite: true })) // 覆写原文件
})

