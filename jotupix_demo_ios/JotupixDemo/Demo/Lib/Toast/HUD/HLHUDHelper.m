//
//  FATHelper.m
//  FAT
//
//  Created by 杨涛 on 2018/7/14.
//  Copyright © 2018年 finogeeks. All rights reserved.
//

#import "HLHUDHelper.h"

@implementation HLHUDHelper

#pragma mark - old apis
+ (void)showLoadingWithTitle:(NSString *)title {
    UIWindow *currentWidow = [UIApplication sharedApplication].keyWindow;
    [self showLoadingForView:currentWidow title:title mask:YES];
}

+ (void)showLoadingWithTitle:(NSString *)title detailText:(NSString *)detailText {
    UIWindow *currentWidow = [UIApplication sharedApplication].keyWindow;
    [self showLoadingForView:currentWidow title:title detailText:detailText mask:YES];
}

+ (void)showSuccessWithTitle:(NSString *)title
{
    UIWindow *currentWidow = [UIApplication sharedApplication].keyWindow;
    [self showToastForView:currentWidow title:title image:nil appId:nil icon:@"success" duration:1500 mask:YES];
}

+ (void)showErrorWithTitle:(NSString *)title
{
    UIWindow *currentWidow = [UIApplication sharedApplication].keyWindow;
    [self showToastForView:currentWidow title:title image:nil appId:nil icon:@"error" duration:1500 mask:YES];
}

+ (void)hideHud {
    UIWindow *currentWidow = [UIApplication sharedApplication].keyWindow;
    [self hideHudForView:currentWidow];
}

+ (HLHUD *)currentHud {
    UIWindow *currentWidow = [UIApplication sharedApplication].keyWindow;
    HLHUD *hud = [HLHUD HUDForView:currentWidow];
    return hud;
}

#pragma mark - hud
+ (void)showLoadingForView:(UIView *)view title:(NSString *)title mask:(BOOL)mask {
    [self showLoadingForView:view title:title detailText:nil mask:mask];
}

+ (void)showLoadingForView:(UIView *)view
                     title:(NSString *)title
                detailText:(NSString *)detailText
                      mask:(BOOL)mask {
    if (!view) {
        return;
    }

    // 1.展示之前，先确保hud隐藏
    [HLHUD hideHUDForView:view];
    // 2.创建一个新的hud
    HLHUD *hud = [HLHUD showHUDAddedTo:view mask:mask];
    // 3.设置loading风格
    hud.mode = HLHUDModeIndeterminate;
    // label text.
    hud.textLabel.text = title;
    hud.detailLabel.text = detailText;
    hud.contentColor = [UIColor whiteColor];
}

+ (void)showToastForView:(UIView *)view title:(NSString *)title icon:(NSString *)icon {
    [self showToastForView:view title:title image:nil appId:nil icon:icon duration:1500 mask:NO];
}

+ (void)showToastForView:(UIView *)view
                   title:(NSString *)title
                   image:(NSString *)imagePath
                   appId:(NSString *)appId
                    icon:(NSString *)icon
                duration:(int)duration
                    mask:(BOOL)mask {
    if (!view) {
        return;
    }

    // 1.展示之前，先确保hud隐藏
    [HLHUD hideHUDForView:view];

    float factDuration = duration / 1000.0;

    // 2.创建一个新的hud
    HLHUD *hud = [HLHUD showHUDAddedTo:view mask:mask];
    hud.contentColor = [UIColor whiteColor];

    UIImage *image = nil;
    if (imagePath) {
        
    } else {
        // 2.1. 如果是loading 显示loading效果
        if ([icon isEqualToString:@"loading"]) {
            [self showLoadingForView:view title:title mask:mask];
            HLHUD *hud = [HLHUD HUDForView:view];
            [hud hideAfterDelay:factDuration];
            return;
        }

        if ([icon isEqualToString:@"success"]) {
            image = [UIImage imageNamed:@"ProgressHUD.bundle/success-white.png"];
        } else if ([icon isEqualToString:@"error"]) {
            image = [UIImage imageNamed:@"ProgressHUD.bundle/error-white.png"];
        } else if ([icon isEqualToString:@"none"]) {

        } else {
            image = [UIImage imageNamed:@"ProgressHUD.bundle/success-white.png"];
        }
    }

    if (image) {
        hud.mode = HLHUDModeImageView;
        hud.imageView.image = image;
    } else {
        hud.mode = HLHUDModeText;
        hud.detailLabel.text = title;
    }

    // label text.
    // 如果是HLHUDModeText，textLabel并不会显示
    hud.textLabel.text = title;
    hud.textLabel.adjustsFontSizeToFitWidth = YES;
    [hud hideAfterDelay:factDuration];
}

+ (void)hideHudForView:(UIView *)view {
    if (!view) {
        return;
    }

    [HLHUD hideHUDForView:view];
}

@end
