#import "RnUnifiedPush.h"
#import "RNUnifiedPushEmitter.h"
#import <AGDeviceRegistration.h>
#import <React/RCTLog.h>

static NSData* _deviceToken;
static NSDictionary* _config;
static RCTPromiseResolveBlock _resolve;
static RCTPromiseRejectBlock _reject;
static RCTResponseSenderBlock _messageHandler;

@implementation RnUnifiedPush

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initialize: (NSDictionary*)config 
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
  _config = config;
  if (_deviceToken != nil) {
      [RnUnifiedPush registerToUPS :resolve rejecter:reject];
  } else {
    _resolve = resolve;
    _reject = reject;
  }
}

+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
  _deviceToken = deviceToken;
    RCTLogInfo(@"getting token %@", deviceToken);
  if (_resolve != nil) {
    [RnUnifiedPush registerToUPS :_resolve rejecter:_reject];

  }
}

+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo {
  RCTLogInfo(@"Sending push to Emitter %@", userInfo);
  [RNUnifiedPushEmitter emitEvent:userInfo];
}

+ (void)registerToUPS: (RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject {
  AGDeviceRegistration *d = [[AGDeviceRegistration alloc] initWithServerURL:[NSURL URLWithString:_config[@"pushServerURL"]]];
  [d registerWithClientInfo:^(id<AGClientDeviceInformation> clientInfo) {
    NSDictionary* iosConfig = [_config objectForKey:@"ios"];
    if (iosConfig == nil) {
      iosConfig = [_config objectForKey:@"ios_token"];
    }
    [clientInfo setDeviceToken:_deviceToken];
    [clientInfo setVariantID:iosConfig[@"variantID"]];
    [clientInfo setVariantSecret:iosConfig[@"variantSecret"]];

    UIDevice *currentDevice = [UIDevice currentDevice];
    // set some 'useful' hardware information params
    [clientInfo setOperatingSystem:[currentDevice systemName]];
    [clientInfo setOsVersion:[currentDevice systemVersion]];
    [clientInfo setDeviceType:[currentDevice model]];
  } success:^{
      NSLog(@"RN-IOS => UnifiedPush Server registration worked");
      NSLog(@"RN-IOS => Invoking callback");
      resolve(@[[NSNull null]]);
      NSLog(@"RN-IOS => Callback invoked");
  } failure:^(NSError * err) {
    reject( @"no_events", @"Error registering",err);
  }];
}

@end
