#pragma once

#include <stdint.h>
#include <vector>

enum class JColor : uint16_t
{
    Red = 0x0F00,
    Green = 0x00F0,
    Blue = 0x000F,
    Yellow = 0x0FF0,
    Cyan = 0x00FF,
    Purple = 0x0F0F,
    White = 0x0FFF,
};