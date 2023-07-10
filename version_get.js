const fs = require("fs")
const request = require('request')

function getVersion(url) {
    return new Promise((rso, rje) => {
        request({
            method: 'GET',
            url,
        }, function (err, res, body) {
            if (err) {
                console.error(err)
                rje(err)
                return;
            }
            const rlt = JSON.parse(body)
            rso(rlt.version)
        })
    })
}

function getFileVersion(path) {
    return new Promise((rso, rje) => {
        const body = fs.readFileSync(path);
        const rlt = JSON.parse(body)
        rso(rlt.version);
    })
}

async function main() {
    const relPath = "assets/resources/version.manifest"
    const relUrl = "https://www.bttx199.com/Product/gameasset/tocard/native/release/version.manifest";
    const degPath = "assets/resources/debug/version.manifest"
    const degUrl = "https://www.bttx199.com/Product/gameasset/tocard/native/debug/version.manifest"
    const localRelVer = await getFileVersion(relPath)
    const relVer = await getVersion(relUrl)
    const localDegVer = await getFileVersion(degPath)
    const degVer = await getVersion(degUrl)
    console.log("relUrl " + relUrl)
    console.log("degUrl " + degUrl)
    console.log("relPath " + relPath)
    console.log("degPath " + degPath)
    console.log("---------------------")
    console.log("     | 本地  | 线上  |")
    console.log("---------------------")
    console.log(`发布 | ${localRelVer} | ${relVer} |`)
    console.log("---------------------")
    console.log(`测试 | ${localDegVer} | ${degVer} |`)
    console.log("---------------------")
}

main()