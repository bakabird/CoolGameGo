var fs = require('fs');
const { argv } = require('process');
var SparkMD5 = require("spark-md5");
// console.log(argv);
if (argv.length > 2) {
    const path = argv[2]
    var md5 = SparkMD5.ArrayBuffer.hash(fs.readFileSync(path))
    console.log(path);
    console.log(md5);
}

