//
//  JotuPixTick.h
//  Converted from jotupix_tick.h
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

// same logic as original C macros
#define time_after(a, b)        ((int)(b) - (int)(a) < 0)
#define time_before(a, b)       time_after(b, a)

#define time_after_eq(a, b)     ((int)(a) - (int)(b) >= 0)
#define time_before_eq(a, b)    time_after_eq(b, a)

#define time_get_next_tick(s, t) ((s) + (t))

NS_ASSUME_NONNULL_END
