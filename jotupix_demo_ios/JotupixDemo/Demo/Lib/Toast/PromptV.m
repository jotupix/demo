//
//  PromptV.m
//  CoolLED1248
//
//  Created by 君同 on 2023/5/4.
//  Copyright © 2023 Haley. All rights reserved.
//

#import "PromptV.h"

@interface PromptV ()<UIGestureRecognizerDelegate>


@property (nonatomic, copy) PromptVSureBlock block;

@end

@implementation PromptV

- (instancetype)initWithSureBlock:(PromptVSureBlock)block
{
    self = [super initWithFrame:[UIScreen mainScreen].bounds];
    if (self) {
       _block = block;
       
       [self initSubView];
       
//       [self initData];
    }
    return self;
}

- (void)initSubView
{
    UIView *coverView = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
    coverView.backgroundColor = [UIColor colorWithWhite:0 alpha:0.4];
    [self addSubview:coverView];
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(bgClick)];
    tapGesture.delegate = self;
    [coverView addGestureRecognizer:tapGesture];
    
    UIView *containView = [[UIView alloc] initWithFrame:CGRectMake(([UIScreen mainScreen].bounds.size.width - 315)*0.5, 200, 315, 317)];
    containView.backgroundColor = [UIColor whiteColor];
    containView.layer.cornerRadius = 5;
    [coverView addSubview:containView];
    
    UILabel *titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 16, 315, 25)];
    self.titleLabel = titleLabel;
    titleLabel.textColor = [UIColor blackColor];
    titleLabel.text = @"Tips";
    titleLabel.font = [UIFont boldSystemFontOfSize:18];
    titleLabel.textAlignment = NSTextAlignmentCenter;
    [containView addSubview:titleLabel];
    
    UITextView *contentTV = [[UITextView alloc] initWithFrame:CGRectMake(30, 55, 315 - 60, 180)];
    contentTV.textColor = [UIColor blackColor];
    contentTV.font = [UIFont systemFontOfSize:14];
    [containView addSubview:contentTV];
    self.contentTV = contentTV;
    
    UIButton *sureBtn = [[UIButton alloc] initWithFrame:CGRectMake(31, 249, 252, 44)];
    sureBtn.backgroundColor = [UIColor grayColor];
    sureBtn.layer.cornerRadius = 22;
    [sureBtn setTitle:@"Sure" forState:UIControlStateNormal];
    [sureBtn setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [sureBtn addTarget:self action:@selector(sureClick) forControlEvents:UIControlEventTouchUpInside];
    [containView addSubview:sureBtn];
}

- (void)showWTitle:(NSString *)title Content:(NSString *)content
{
    self.titleLabel.text = title;
    self.contentTV.text = content;
    
    [[UIApplication sharedApplication].keyWindow addSubview:self];
}

#pragma mark - btn click events
- (void)bgClick
{
    [self endEditing:YES];
}

- (void)sureClick
{
    if (self.block) {
        self.block();
    }
    [self hide];
}

- (void)show
{
    [[UIApplication sharedApplication].keyWindow addSubview:self];
}

- (void)hide
{
    [self removeFromSuperview];
}

@end
