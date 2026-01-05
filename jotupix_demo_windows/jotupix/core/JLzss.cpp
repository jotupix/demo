//#include "pch.h"
#include <string.h>
#include "JLzss.h"

#define TAG  "JLzss"

void JLzss::InitTree()
{
    int  i;

    for (i = LZSS_N + 1; i <= LZSS_N + 256; i++)
    {
        m_au16Rson[i] = LZSS_NIL;
    }

    for (i = 0; i < LZSS_N; i++)
    {
        m_au16Dad[i] = LZSS_NIL;
    }
}

void JLzss::InsertNode(uint32_t r)
{
    uint32_t i, p;
    int cmp;

    cmp = 1;
    p = LZSS_N + 1 + m_au8WorkBuffer[r];
    m_au16Rson[r] = m_au16Lson[r] = LZSS_NIL;

    m_u32MatchLength = 0;

    for (; ; )
    {
        if (cmp >= 0)
        {
            if (m_au16Rson[p] != LZSS_NIL)
            {
                p = m_au16Rson[p];
            }
            else
            {
                m_au16Rson[p] = r;
                m_au16Dad[r] = p;

                return;
            }
        }
        else
        {
            if (m_au16Lson[p] != LZSS_NIL)
            {
                p = m_au16Lson[p];
            }
            else
            {
                m_au16Lson[p] = r;
                m_au16Dad[r] = p;

                return;
            }
        }

        for (i = 1; i < LZSS_F; i++)
        {
            cmp = m_au8WorkBuffer[r + i] - m_au8WorkBuffer[p + i];

            if (cmp != 0)
            {
                break;
            }
        }

        if (i > m_u32MatchLength)
        {
            m_u32MatchPos = p;
            m_u32MatchLength = i;

            if (i >= LZSS_F)
            {
                break;
            }
        }
    }

    m_au16Dad[r] = m_au16Dad[p];
    m_au16Lson[r] = m_au16Lson[p];
    m_au16Rson[r] = m_au16Rson[p];
    m_au16Dad[m_au16Lson[p]] = r;
    m_au16Dad[m_au16Rson[p]] = r;

    if (m_au16Rson[m_au16Dad[p]] == p)
    {
        m_au16Rson[m_au16Dad[p]] = r;
    }
    else
    {
        m_au16Lson[m_au16Dad[p]] = r;
    }

    m_au16Dad[p] = LZSS_NIL;
}

void JLzss::DeleteNode(uint32_t p)
{
    uint32_t q = 0;

    if (m_au16Dad[p] == LZSS_NIL)
    {
        return;
    }

    if (m_au16Rson[p] == LZSS_NIL)
    {
        q = m_au16Lson[p];
    }
    else if (m_au16Lson[p] == LZSS_NIL)
    {
        q = m_au16Rson[p];
    }
    else
    {
        q = m_au16Lson[p];

        if (m_au16Rson[q] != LZSS_NIL)
        {
            do
            {
                q = m_au16Rson[q];
            } while (m_au16Rson[q] != LZSS_NIL);

            m_au16Rson[m_au16Dad[q]] = m_au16Lson[q];
            m_au16Dad[m_au16Lson[q]] = m_au16Dad[q];
            m_au16Lson[q] = m_au16Lson[p];
            m_au16Dad[m_au16Lson[p]] = q;
        }

        m_au16Rson[q] = m_au16Rson[p];
        m_au16Dad[m_au16Rson[p]] = q;
    }

    m_au16Dad[q] = m_au16Dad[p];

    if (m_au16Rson[m_au16Dad[p]] == p)
    {
        m_au16Rson[m_au16Dad[p]] = q;
    }
    else
    {
        m_au16Lson[m_au16Dad[p]] = q;
    }

    m_au16Dad[p] = LZSS_NIL;
}

std::vector<uint8_t> JLzss::Encode(const uint8_t* pu8Data, uint32_t u32Len)
{
    int  i, c, len, r, s, last_match_length, code_buf_ptr;
    uint8_t code_buf[17], mask;
    uint32_t u32RdIndex = 0;

    std::vector<uint8_t> outBuffer;

    m_u32MatchPos = 0;
    m_u32MatchLength = 0;

    InitTree();

    code_buf[0] = 0;
    code_buf_ptr = mask = 1;
    s = 0;
    r = LZSS_N - LZSS_F;

    for (i = s; i < r; i++)
    {
        m_au8WorkBuffer[i] = 0;
    }

    for (len = 0; len < LZSS_F && u32RdIndex < u32Len; len++, u32RdIndex++)
    {
        m_au8WorkBuffer[r + len] = pu8Data[u32RdIndex];
    }

    if (len == 0)
    {
        return outBuffer;
    }

    for (i = 1; i <= LZSS_F; i++)
    {
        InsertNode(r - i);
    }

    InsertNode(r);

    do
    {
        if (m_u32MatchLength > len)
        {
            m_u32MatchLength = len;
        }

        if (m_u32MatchLength <= LZSS_THRESHOLD)
        {
            m_u32MatchLength = 1;
            code_buf[0] |= mask;
            code_buf[code_buf_ptr++] = m_au8WorkBuffer[r];
        }
        else
        {
            code_buf[code_buf_ptr++] = (unsigned char)m_u32MatchPos;
            code_buf[code_buf_ptr++] = (unsigned char)
                (((m_u32MatchPos >> 4) & 0xf0)
                    | (m_u32MatchLength - (LZSS_THRESHOLD + 1)));
        }

        // 状态标志flag只有一个字节，8bit
        mask <<= 1;
        if ((mask & 0xFF) == 0)
        {
            for (i = 0; i < code_buf_ptr; i++)
            {
                outBuffer.push_back(code_buf[i]);
            }

            code_buf[0] = 0;
            code_buf_ptr = mask = 1;
        }

        last_match_length = m_u32MatchLength;

        for (i = 0; i < last_match_length && u32RdIndex < u32Len; i++, u32RdIndex++)
        {
            DeleteNode(s);

            c = pu8Data[u32RdIndex];

            m_au8WorkBuffer[s] = c;

            if (s < LZSS_F - 1)
            {
                m_au8WorkBuffer[s + LZSS_N] = c;
            }

            s = (s + 1) & (LZSS_N - 1);
            r = (r + 1) & (LZSS_N - 1);
            InsertNode(r);
        }

        while (i++ < last_match_length)
        {
            DeleteNode(s);
            s = (s + 1) & (LZSS_N - 1);
            r = (r + 1) & (LZSS_N - 1);
            if (--len) InsertNode(r);
        }
    } while (len > 0);

    if (code_buf_ptr > 1)
    {
        for (i = 0; i < code_buf_ptr; i++)
        {
            //putc(code_buf[i], outfile);
            outBuffer.push_back(code_buf[i]);
        }
    }

    return outBuffer;
}

