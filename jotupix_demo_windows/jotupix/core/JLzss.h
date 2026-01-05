#pragma once

#include <vector>
#include "JDefine.h"

/*
    Do not modify the following parameters.
*/
#define LZSS_N           512       
#define LZSS_F           18        
#define LZSS_THRESHOLD   2  
#define LZSS_NIL         LZSS_N

class JLzss
{
public:
    /**
     * @brief Compress input data using the LZSS algorithm.
     *
     * This function performs LZSS encoding on a given buffer and returns the
     * compressed result as a vector of bytes. The implementation keeps the
     * output compatible with the classic 1989 LZSS specification.
     *
     * @param pu8Data Pointer to the input data buffer.
     * @param u32Len  Length of the input data in bytes.
     * @return std::vector<uint8_t> The compressed output data.
     */
    std::vector<uint8_t> Encode(const uint8_t *pu8Data, uint32_t u32Len);

private:
    uint8_t m_au8WorkBuffer[LZSS_N + LZSS_F - 1];
    uint32_t m_u32MatchPos;
    uint32_t m_u32MatchLength;
    uint16_t m_au16Lson[LZSS_N + 1];
    uint16_t m_au16Rson[LZSS_N + 257];
    uint16_t m_au16Dad[LZSS_N + 1];

    void InitTree();
    void InsertNode(uint32_t r);
    void DeleteNode(uint32_t p);
};

