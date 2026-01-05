/**
 * @file    jotupix_config.h
 * @brief   Configuration File
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#ifndef __JOTUPIX_CONFIG_H__
#define __JOTUPIX_CONFIG_H__

/*
	This maximum value can be obtained from the pkt-max-size field in the device information.
	The default value is 1024, and it cannot exceed pkt-max-size.
    If RAM is insufficient, this value can be reduced.
*/
//#define PACKET_PAYLOAD_SIZE       1024

/*
    Program compression is not supported by default.
    The compression module requires 4.2KB of RAM, and in embedded systems, it is inefficient and not cost-effective.
*/
#define SUPPORT_COMPRESS          0

#endif //__JOTUPIX_CONFIG_H__