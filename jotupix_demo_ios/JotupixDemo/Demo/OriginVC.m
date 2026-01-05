//
//  OriginVC.m
//  SDKTest
//
//  Created by go on 12/5/25.
//

#import "OriginVC.h"
#import "DeviceVC.h"

@interface OriginVC ()

@end

@implementation OriginVC

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initNavigationBar];
}

- (void)initNavigationBar
{
    UIButton *leftBtn = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 40, 40)];
    [leftBtn setImage:[UIImage imageNamed:@"BluetoothIcon"] forState:UIControlStateNormal];
    [leftBtn addTarget:self action:@selector(leftBtnClick) forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem *leftItem = [[UIBarButtonItem alloc] initWithCustomView:leftBtn];
    self.navigationItem.leftBarButtonItem = leftItem;
}

-(void)leftBtnClick{
    DeviceVC *deviceVC = [[DeviceVC alloc] init];
    [self.navigationController pushViewController:deviceVC animated:NO];
}


@end
