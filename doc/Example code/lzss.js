// 数据压缩

const { Debug } = require("../../utils/debug");

const N = 512;       // 缓冲区长度即字典区，一定要为2的次方，一般定义为4096，越长压缩比越高，但是由于设备RAM空间有限，大小为512
const F = 18;         // 最大的输出长度 
const THRESHOLD = 2;         // 最小的数据长度要大于THRESHOLD，即=THRESHOLD+1
const NIL = N;

var textsize = 0;
var codesize = 0;
var printcount = 0;

var enbuffer = new Uint32Array(N + F - 1); // 压缩解压所要用到的缓冲区
var match_position = 0;
var match_length = 0;
var lson = new Uint32Array(N+1);
var rson = new Uint32Array(N+257);
var dad = new Uint32Array(N+1);

/**
 * 初始化二叉树 lson为左叶子节点，rson为右子节点，dad为父节点
 */
function InitTree() {
    var  i = 0;

    for (i = N + 1; i <= N + 256; i++)
    {
        rson[i] = NIL;
    }

    for (i = 0; i < N; i++)
    {
        dad[i] = NIL;
    }
}

/**
 * 插入一个节点
 * @param {Number} r 
 */
function InsertNode(r) {
    var i=0, p=0, cmp=0;
    var key = r;
    
    cmp = 1;
    p = N + 1 + enbuffer[key];
    rson[r] = lson[r] = NIL;
    match_length = 0;

    for (; ; )
    {
        if (cmp >= 0)
        {
            if (rson[p] != NIL)
            {
                p = rson[p];
            }
            else
            {
                rson[p] = r;
                dad[r] = p;
                return;
            }
        }
        else
        {
            if (lson[p] != NIL)
            {
                p = lson[p];
            }
            else
            {
                lson[p] = r;
                dad[r] = p;
                return;
            }
        }

        for (i = 1; i < F; i++)
        {
            cmp = enbuffer[key+i] - enbuffer[p + i];
            if (cmp != 0)
            {
                break;
            }
        }

        if (i > match_length)
        {
            match_position = p;
            if ((match_length = i) >= F)
            {
                break;
            }
        }
    }

    dad[r] = dad[p];
    lson[r] = lson[p];
    rson[r] = rson[p];
    dad[lson[p]] = r;
    dad[rson[p]] = r;

    if (rson[dad[p]] == p)
    {
        rson[dad[p]] = r;
    }
    else
    {
        lson[dad[p]] = r;
    }

    dad[p] = NIL;
}

/**
 * 删除一个节点
 * @param {Number} p 
 */
function DeleteNode(p) {
    var q = 0;

    if (dad[p] == NIL)
    {
        return;
    }

    if (rson[p] == NIL)
    {
        q = lson[p];
    }
    else if (lson[p] == NIL)
    {
        q = rson[p];
    }
    else
    {
        q = lson[p];

        if (rson[q] != NIL)
        {
            do
            {
                q = rson[q];
            }
            while (rson[q] != NIL);

            rson[dad[q]] = lson[q];
            dad[lson[q]] = dad[q];
            lson[q] = lson[p];
            dad[lson[p]] = q;
        }

        rson[q] = rson[p];
        dad[rson[p]] = q;
    }

    dad[q] = dad[p];

    if (rson[dad[p]] == p)
    {
        rson[dad[p]] = q;
    }
    else
    {
        lson[dad[p]] = q;
    }

    dad[p] = NIL;
}

/**
 * 压缩一段数据
 * @param {ArrayBuffer} data 
 * 返回: 压缩后的数据 ArrayBuffer
 */
function encode(data) {
    var  i, c, len, r, s, last_match_length, code_buf_ptr;
    var code_buf = new Uint8Array(17);
    var mask = 0;
    var currEncodeIndex = 0; // 当前处理的数据位置
    var buffer = new Uint8Array(data);
    var encodeDataLen = buffer.length; // 压缩数据的原始长度
    var resultBuffer = []; // 压缩结果输出

    textsize = 0;
    codesize = 0;
    printcount = 0;

    InitTree();

    code_buf[0] = 0;
    code_buf_ptr = mask = 1;
    s = 0;
    r = N - F;

    for (i = s; i < r; i++)
    {
        enbuffer[i] = 0;
    }

    for (len = 0; len < F && currEncodeIndex < encodeDataLen; len++, currEncodeIndex++)
    {
        enbuffer[r + len] = buffer[currEncodeIndex];
    }

    textsize = len;
    if (textsize == 0)
    {
        return;
    }

    for (i = 1; i <= F; i++)
    {
        InsertNode(r - i);
    }

    InsertNode(r);

    do
    {
        if (match_length > len)
        {
            match_length = len;
        }

        if (match_length <= THRESHOLD)
        {
            match_length = 1;
            code_buf[0] |= mask;
            code_buf[code_buf_ptr++] = enbuffer[r];
        }
        else
        {
            code_buf[code_buf_ptr++] = (match_position & 0xFF);
            code_buf[code_buf_ptr++] = (((match_position >>> 4) & 0xf0) | (match_length - (THRESHOLD + 1))); // >>>为不带符号的右移
        }

        // 状态标志flag只有一个字节，8bit
        mask <<= 1;
        if ((mask & 0xFF) == 0)
        {
            for (i = 0; i < code_buf_ptr; i++)
            {
                resultBuffer.push(code_buf[i]);
            }

            codesize += code_buf_ptr;
            code_buf[0] = 0;
            code_buf_ptr = mask = 1;
        }

        last_match_length = match_length;

        for (i = 0; i < last_match_length && currEncodeIndex < encodeDataLen; i++, currEncodeIndex++)
        {
            DeleteNode(s);

            c = buffer[currEncodeIndex];

            enbuffer[s] = c;

            if (s < F - 1)
            {
                enbuffer[s + N] = c;
            }

            s = (s + 1) & (N - 1);
            r = (r + 1) & (N - 1);
            InsertNode(r);
        }

        textsize += i;
        if (textsize > printcount)
        {
            printcount += 1024;
        }

        while (i++ < last_match_length)
        {
            DeleteNode(s);
            s = (s + 1) & (N - 1);
            r = (r + 1) & (N - 1);
            if (--len) {
                InsertNode(r);
            }
        }
    } while (len > 0);

    if (code_buf_ptr > 1)
    {
        for (i = 0; i < code_buf_ptr; i++)
        {
            resultBuffer.push(code_buf[i]);
        }

        codesize += code_buf_ptr;
    }

    console.log("In : ", textsize);
    console.log("Out: ", codesize);
    console.log("Out/In: ", codesize / textsize); // 压缩比

    return new Uint8Array(resultBuffer);
}

/**
 * 解压一段数据
 * @param {ArrayBuffer} data 
 * 返回: 解压后的数据 ArrayBuffer
 */
function decode(data) {

}

// 压缩之前的数据
var TEST_BUFFER = [1, 2, 3, 4, 2, 3, 4, 2, 3, 4 , 2, 3, 4 , 2, 3, 4, 5, 20, 8, 10];
// 压缩之后的数据
var TEST_RESULT_BUFFER = [239,1,2,3,4,239,25,5,20,8,1,10]; 

function test() {
    var data = new Uint8Array(TEST_BUFFER);
    var rData = encode(data.buffer);

    console.log(rData);
}

module.exports = {
    encode,
    decode,
    test
}