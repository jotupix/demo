// JProtocol.h
#import <Foundation/Foundation.h>
#import <stdint.h>
#import "JDefine.h"

#define JPROTOCOL_RX_PARSE_BUFF_SIZE   256     // Buffer size for parsed RX data
#define JPROTOCOL_TX_BUFF_SIZE         181     // TX buffer size 

@protocol IJParseCallback <NSObject>
@required
/// Called when a full protocol frame has been parsed.
/// pu8Data: pointer to parsed payload
/// len: length of parsed data in bytes
- (void)onParseComplete:(const uint8_t *)data length:(uint32_t)len;
@end


@protocol IJSend <NSObject>
@required
/// Send raw bytes through underlying transport (e.g., BLE/UART/TCP)
- (int)send:(const uint8_t *)data length:(uint32_t)len;
@end


@interface JProtocol : NSObject

/// Initialize protocol with a sender and a parse-complete callback
- (void)init:(id<IJSend>)sender callback:(id<IJParseCallback>)callback;

/// Pack and send a data frame according to protocol format
- (int)send:(const uint8_t *)pu8Data length:(uint32_t)len;

/// Parse incoming raw bytes; triggers callback when a complete frame is received
- (void)parse:(const uint8_t *)pu8Data length:(uint32_t)len;

@end
