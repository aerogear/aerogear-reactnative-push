#import "RnUnifiedPush.h"
#import "RNUnifiedPushEmitter.h"
#import <AGDeviceRegistration.h>
#import <React/RCTLog.h>

static NSData* _deviceToken;
static NSDictionary* _config;
static RCTResponseSenderBlock _callback;
static RCTResponseSenderBlock _messageHandler;

@implementation RnUnifiedPush

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initialize: (NSDictionary*)config onSuccess: (RCTResponseSenderBlock)callback) {
  _config = config;
  if (_deviceToken != nil) {
    [RnUnifiedPush registerToUPS:callback];
  } else {
    _callback = callback;
  }
}

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
  _deviceToken = deviceToken;
    RCTLogInfo(@"getting token %@", deviceToken);
  if (_callback != nil) {
    [RnUnifiedPush registerToUPS: _callback];
  }
}

+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo {
  RCTLogInfo(@"Sending push to Emitter %@", userInfo);
  [RNUnifiedPushEmitter emitEvent:userInfo];
}

+ (void)registerToUPS: (RCTResponseSenderBlock)callback {
  AGDeviceRegistration *d = [[AGDeviceRegistration alloc] initWithServerURL:[NSURL URLWithString:_config[@"url"]]];
  [d registerWithClientInfo:^(id<AGClientDeviceInformation> clientInfo) {
    [clientInfo setDeviceToken:_deviceToken];
    [clientInfo setVariantID:_config[@"variantId"]];
    [clientInfo setVariantSecret:_config[@"secret"]];
    [clientInfo setAlias:_config[@"alias"]];

    UIDevice *currentDevice = [UIDevice currentDevice];
    // set some 'useful' hardware information params
    [clientInfo setOperatingSystem:[currentDevice systemName]];
    [clientInfo setOsVersion:[currentDevice systemVersion]];
    [clientInfo setDeviceType:[currentDevice model]];
  } success:^{
      NSLog(@"RN-IOS => UnifiedPush Server registration worked");
      NSLog(@"RN-IOS => Invoking callback");
      callback(@[[NSNull null], @"Wow! Done!"]);
      NSLog(@"RN-IOS => Callback invoked");
  } failure:^(NSError * err) {
    NSLog(@"RN-IOS => UnifiedPush Server registration Error: %@", err);
    callback(@[err]);
  }];
}

@end
