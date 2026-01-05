#pragma once
#include <vector>
#include <cstdint>
#include <stdexcept> // for std::out_of_range

class JByteWriter
{
public:
    // Output byte buffer
    std::vector<uint8_t> buffer;

    // Write 1 byte
    inline void put_u8(uint8_t v)
    {
        buffer.push_back(v);
    }

    // Write 2 bytes in big-endian order
    inline void put_u16(uint16_t v)
    {
        buffer.push_back((v >> 8) & 0xFF);
        buffer.push_back((v >> 0) & 0xFF);
    }

    // Write 4 bytes in big-endian order
    inline void put_u32(uint32_t v)
    {
        buffer.push_back((v >> 24) & 0xFF);
        buffer.push_back((v >> 16) & 0xFF);
        buffer.push_back((v >> 8) & 0xFF);
        buffer.push_back((v >> 0) & 0xFF);
    }

    // Write raw byte array
    inline void put_bytes(const uint8_t* data, size_t len)
    {
        buffer.insert(buffer.end(), data, data + len);
    }

    // Write bytes from an existing vector
    inline void put_bytes(const std::vector<uint8_t>& data)
    {
        buffer.insert(buffer.end(), data.begin(), data.end());
    }

    // Append repeated byte values
    inline void put_repeat(uint8_t value, size_t count)
    {
        buffer.insert(buffer.end(), count, value);
    }

    // Insert uint32_t at specified position (big-endian)
    inline void insert_u32(size_t pos, uint32_t value) {
        if (pos > buffer.size()) {
            throw std::out_of_range("ByteWriter::insert_u32 position out of range");
        }
        buffer.insert(buffer.begin() + pos, {
            static_cast<uint8_t>((value >> 24) & 0xFF),
            static_cast<uint8_t>((value >> 16) & 0xFF),
            static_cast<uint8_t>((value >> 8) & 0xFF),
            static_cast<uint8_t>((value >> 0) & 0xFF)
            });
    }

    inline size_t size() {
        return buffer.size();
    }
};
