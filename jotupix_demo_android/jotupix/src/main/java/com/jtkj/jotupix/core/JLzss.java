package com.jtkj.jotupix.core;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class JLzss {
    private static final String TAG = JLzss.class.getSimpleName();

    public static byte[] encode(byte[] input) {
        byte[] result = lzssCompress(input);
        return result;
    }

    private static final int N = 512;
    private static final int F = 18;
    private static final int THRESHOLD = 2;
    private static final int NIL = N;

    private static int textsize = 0;
    private static int codesize = 0;
    private static int printcount = 0;

    private static byte[] enbuffer = new byte[N + F - 1];
    private static int match_position = 0;
    private static int match_length = 0;
    private static int[] lson = new int[N + 1];
    private static int[] rson = new int[N + 257];
    private static int[] dad = new int[N + 1];

    public static void initTree() {
        int i = 0;

        for (i = N + 1; i <= N + 256; i++) {
            rson[i] = NIL;
        }

        for (i = 0; i < N; i++) {
            dad[i] = NIL;
        }
    }

    public static void insertNode(int r) {
        int i = 0, p = 0, cmp = 0;

        cmp = 1;
        p = N + 1 + Byte.toUnsignedInt(enbuffer[r]);
        rson[r] = lson[r] = NIL;
        match_length = 0;

        for (; ; ) {
            if (cmp >= 0) {
                if (rson[p] != NIL) {
                    p = rson[p];
                } else {
                    rson[p] = r;
                    dad[r] = p;
                    return;
                }
            } else {
                if (lson[p] != NIL) {
                    p = lson[p];
                } else {
                    lson[p] = r;
                    dad[r] = p;
                    return;
                }
            }

            for (i = 1; i < F; i++) {
                cmp = Byte.toUnsignedInt(enbuffer[r + i]) - Byte.toUnsignedInt(enbuffer[p + i]);
                if (cmp != 0) {
                    break;
                }
            }

            if (i > match_length) {
                match_position = p;

                match_length = i;
                if (match_length >= F) {
                    break;
                }
            }
        }

        dad[r] = dad[p];
        lson[r] = lson[p];
        rson[r] = rson[p];
        dad[lson[p]] = r;
        dad[rson[p]] = r;

        if (rson[dad[p]] == p) {
            rson[dad[p]] = r;
        } else {
            lson[dad[p]] = r;
        }

        dad[p] = NIL;
    }

    public static void deleteNode(int p) {
        int q = 0;

        if (dad[p] == NIL) {
            return;
        }

        if (rson[p] == NIL) {
            q = lson[p];
        } else if (lson[p] == NIL) {
            q = rson[p];
        } else {
            q = lson[p];

            if (rson[q] != NIL) {
                do {
                    q = rson[q];
                } while (rson[q] != NIL);

                rson[dad[q]] = lson[q];
                dad[lson[q]] = dad[q];
                lson[q] = lson[p];
                dad[lson[p]] = q;
            }

            rson[q] = rson[p];
            dad[rson[p]] = q;
        }

        dad[q] = dad[p];

        if (rson[dad[p]] == p) {
            rson[dad[p]] = q;
        } else {
            lson[dad[p]] = q;
        }

        dad[p] = NIL;
    }

    public static byte[] lzssCompress(byte[] data) {
        int i, len, r, s, last_match_length, code_buf_ptr;
        byte c = 0, mask = 0;
        byte[] code_buf = new byte[17];
        int currEncodeIndex = 0;
        int encodeDataLen = data.length;
        List<Byte> resultBuffer = new ArrayList<Byte>();

        textsize = 0;
        codesize = 0;
        printcount = 0;

        initTree();

        code_buf[0] = 0;
        code_buf_ptr = mask = 1;
        s = 0;
        r = N - F;

        for (i = s; i < r; i++) {
            enbuffer[i] = 0;
        }

        for (len = 0; len < F && currEncodeIndex < encodeDataLen; len++, currEncodeIndex++) {
            enbuffer[r + len] = data[currEncodeIndex];
        }

        textsize = len;
        if (textsize == 0) {
            return null;
        }

        for (i = 1; i <= F; i++) {
            insertNode(r - i);
        }

        insertNode(r);

        do {
            if (match_length > len) {
                match_length = len;
            }

            if (match_length <= THRESHOLD) {
                match_length = 1;
                code_buf[0] |= mask;
                code_buf[code_buf_ptr++] = enbuffer[r];
            } else {
                code_buf[code_buf_ptr++] = (byte) (match_position & 0xFF);
                code_buf[code_buf_ptr++] = (byte) (((match_position >>> 4) & 0xf0) | (match_length - (THRESHOLD + 1)));
            }

            mask <<= 1;
            if ((mask & 0xFF) == 0) {
                for (i = 0; i < code_buf_ptr; i++) {
                    resultBuffer.add(code_buf[i]);
                }

                codesize += code_buf_ptr;
                code_buf[0] = 0;
                code_buf_ptr = mask = 1;
            }

            last_match_length = match_length;

            for (i = 0; i < last_match_length && currEncodeIndex < encodeDataLen; i++, currEncodeIndex++) {
                deleteNode(s);

                c = data[currEncodeIndex];

                enbuffer[s] = c;

                if (s < F - 1) {
                    enbuffer[s + N] = c;
                }

                s = (s + 1) & (N - 1);
                r = (r + 1) & (N - 1);
                insertNode(r);
            }

            textsize += i;
            if (textsize > printcount) {
                printcount += 1024;
            }

            while (i++ < last_match_length) {
                deleteNode(s);
                s = (s + 1) & (N - 1);
                r = (r + 1) & (N - 1);
                if (--len > 0) {
                    insertNode(r);
                }
            }
        } while (len > 0);

        if (code_buf_ptr > 1) {
            for (i = 0; i < code_buf_ptr; i++) {
                resultBuffer.add(code_buf[i]);
            }
            codesize += code_buf_ptr;
        }

        Log.i(TAG, "In : " + textsize);
        Log.i(TAG, "Out: " + codesize);
        Log.i(TAG, "Out/In: " + (double) codesize / textsize); // Compression ratio

        byte[] result = new byte[resultBuffer.size()];
        for (i = 0; i < resultBuffer.size(); i++) {
            result[i] = (byte) resultBuffer.get(i).intValue();
        }

        return result;
    }
}
