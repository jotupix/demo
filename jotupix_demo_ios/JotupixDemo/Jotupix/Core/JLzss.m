// JLzss.m
#import "JLzss.h"

@interface JLzss () {
    uint8_t  _workBuffer[LZSS_N + LZSS_F - 1];
    uint32_t _matchPos;
    uint32_t _matchLength;
    uint16_t _lson[LZSS_N + 1];
    uint16_t _rson[LZSS_N + 257];
    uint16_t _dad[LZSS_N + 1];
}
@end

@implementation JLzss

#pragma mark - Tree Operations

- (void)initTree {
    for (int i = LZSS_N + 1; i <= LZSS_N + 256; i++) {
        _rson[i] = LZSS_NIL;
    }
    for (int i = 0; i < LZSS_N; i++) {
        _dad[i] = LZSS_NIL;
    }
}

- (void)insertNode:(uint32_t)r {
    uint32_t i, p;
    int cmp = 1;

    p = LZSS_N + 1 + _workBuffer[r];
    _rson[r] = _lson[r] = LZSS_NIL;
    _matchLength = 0;

    while (1) {
        if (cmp >= 0) {
            if (_rson[p] != LZSS_NIL) {
                p = _rson[p];
            } else {
                _rson[p] = r;
                _dad[r] = p;
                return;
            }
        } else {
            if (_lson[p] != LZSS_NIL) {
                p = _lson[p];
            } else {
                _lson[p] = r;
                _dad[r] = p;
                return;
            }
        }

        for (i = 1; i < LZSS_F; i++) {
            cmp = _workBuffer[r + i] - _workBuffer[p + i];
            if (cmp != 0) break;
        }

        if (i > _matchLength) {
            _matchPos = p;
            _matchLength = i;
            if (i >= LZSS_F) break;
        }
    }

    _dad[r] = _dad[p];
    _lson[r] = _lson[p];
    _rson[r] = _rson[p];
    _dad[_lson[p]] = r;
    _dad[_rson[p]] = r;

    if (_rson[_dad[p]] == p)
        _rson[_dad[p]] = r;
    else
        _lson[_dad[p]] = r;

    _dad[p] = LZSS_NIL;
}

- (void)deleteNode:(uint32_t)p {
    if (_dad[p] == LZSS_NIL) return;

    uint32_t q;

    if (_rson[p] == LZSS_NIL) {
        q = _lson[p];
    }
    else if (_lson[p] == LZSS_NIL) {
        q = _rson[p];
    }
    else {
        q = _lson[p];
        if (_rson[q] != LZSS_NIL) {
            do {
                q = _rson[q];
            } while (_rson[q] != LZSS_NIL);

            _rson[_dad[q]] = _lson[q];
            _dad[_lson[q]] = _dad[q];
            _lson[q] = _lson[p];
            _dad[_lson[p]] = q;
        }
        _rson[q] = _rson[p];
        _dad[_rson[p]] = q;
    }

    _dad[q] = _dad[p];

    if (_rson[_dad[p]] == p)
        _rson[_dad[p]] = q;
    else
        _lson[_dad[p]] = q;

    _dad[p] = LZSS_NIL;
}

#pragma mark - Encode

- (NSData *)encode:(const uint8_t *)pu8Data length:(uint32_t)u32Len {

    NSMutableData *outData = [NSMutableData data];

    uint8_t code_buf[17];
    int code_buf_ptr = 1;
    uint8_t mask = 1;

    _matchPos = _matchLength = 0;

    [self initTree];

    uint32_t readIndex = 0;

    for (int i = 0; i < LZSS_N - LZSS_F; i++) {
        _workBuffer[i] = 0;
    }

    int s = 0;
    int r = LZSS_N - LZSS_F;

    int len = 0;
    for (; len < LZSS_F && readIndex < u32Len; len++, readIndex++) {
        _workBuffer[r + len] = pu8Data[readIndex];
    }

    if (len == 0) return outData;

    for (int i = 1; i <= LZSS_F; i++) {
        [self insertNode:(r - i)];
    }
    [self insertNode:r];

    do {
        if (_matchLength > len) _matchLength = len;

        if (_matchLength <= LZSS_THRESHOLD) {
            _matchLength = 1;
            code_buf[0] |= mask;
            code_buf[code_buf_ptr++] = _workBuffer[r];
        } else {
            code_buf[code_buf_ptr++] = (uint8_t)_matchPos;
            code_buf[code_buf_ptr++] = (uint8_t)(((_matchPos >> 4) & 0xF0)
                | (_matchLength - (LZSS_THRESHOLD + 1)));
        }

        mask <<= 1;

        if (mask == 0) {
            [outData appendBytes:code_buf length:code_buf_ptr];
            code_buf[0] = 0;
            code_buf_ptr = 1;
            mask = 1;
        }

        int last = _matchLength;

        int i;
        for (i = 0; i < last && readIndex < u32Len; i++, readIndex++) {

            [self deleteNode:s];

            int c = pu8Data[readIndex];
            _workBuffer[s] = c;

            if (s < LZSS_F - 1)
                _workBuffer[s + LZSS_N] = c;

            s = (s + 1) & (LZSS_N - 1);
            r = (r + 1) & (LZSS_N - 1);

            [self insertNode:r];
        }

        while (i++ < last) {
            [self deleteNode:s];
            s = (s + 1) & (LZSS_N - 1);
            r = (r + 1) & (LZSS_N - 1);
            if (--len) [self insertNode:r];
        }

    } while (len > 0);

    if (code_buf_ptr > 1) {
        [outData appendBytes:code_buf length:code_buf_ptr];
    }

    return outData;
}

@end
