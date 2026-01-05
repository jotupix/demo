//
//  PromptV.h
//  CoolLED1248
//
//  Created by 君同 on 2023/5/4.
//  Copyright © 2023 Haley. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void(^PromptVSureBlock)(void);

@interface PromptV : UIView

@property (nonatomic, strong) UILabel *titleLabel;
@property (nonatomic, strong) UITextView *contentTV;

- (instancetype)initWithSureBlock:(PromptVSureBlock)block;

- (void)showWTitle:(NSString *)title Content:(NSString *)content;

- (void)show;

- (void)hide;

@end

NS_ASSUME_NONNULL_END
