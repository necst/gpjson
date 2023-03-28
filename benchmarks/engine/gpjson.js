var engine;

function init(dir) {
    baseDir = dir;
    engine = Polyglot.eval('gpjson', "GJ");
    engine.buildKernels();
}

// $.user.lang
function queryTT1(dataset) {
    result = engine.query(dataset, ["$.user.lang"], true, true);
    return result;
}

// {$.user.lang, $.lang}
function queryTT2(dataset) {
    result = engine.query(dataset, ["$.user.lang", "$.lang"], true, true);
    return result;
}

// $.user.lang[?(@ == 'nl')]
function queryTT3(dataset) {
    result = engine.query(dataset, ["$.user.lang[?(@ == 'nl')]"], true, true);
    return result;
}

// $.user.lang[?(@ == 'en')]
function queryTT4(dataset) {
    result = engine.query(dataset, ["$.user.lang[?(@ == 'en')]"], true, true);
    return result;
}

// {$.bestMarketplacePrice.price, $.name}
function queryWM(dataset) {
    result = engine.query(dataset, ["$.bestMarketplacePrice.price", "$.name"], true, true);
    return result;
}

// $.categoryPath[1:3].id
function queryBB(dataset) {
    result = engine.query(dataset, ["$.categoryPath[1:3].id"], true, true);
    return result;
}

function count(result) {
    var count = 0;
    for (let q = 0; q < result.length; q++) {
        for (let i = 0; i < result[q].length; i++) {
            for (let j = 0; j < result[q][i].length; j++) {
                if (result[q][i][j] != null)
                    count += 1;
            }
        }
    }
    return count;
}

module.exports = { 
    init, count, queryTT1, queryTT2, queryTT3, queryTT4, queryWM, queryBB
}
