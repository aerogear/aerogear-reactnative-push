#import "RnUnifiedPush.h"
#import "RNUnifiedPushEmitter.h"
#import <AGDeviceRegistration.h>
#import <React/RCTLog.h>

static NSData* _deviceToken;
static NSDictionary* _config;
static RCTPromiseResolveBlock _resolve;
static RCTPromiseRejectBlock _reject;
static RCTResponseSenderBlock _messageHandler;
static NSUserDefaults * _preferences;


@implementation RnUnifiedPush

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(register: (NSDictionary*)config 
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    @try {
        [RnUnifiedPush registerToUPS :config resolver:resolve rejecter:reject];
    }@catch(NSException *e) {
        reject( @"RN-IOS", [NSString stringWithFormat:@" error in register2%@", e.reason], nil);
    }
}

RCT_EXPORT_METHOD(initialize: (NSDictionary*)config 
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    @try{
        _preferences = [NSUserDefaults standardUserDefaults];
        _config = config;
        if (_deviceToken != nil) {
            [RnUnifiedPush saveParams :resolve rejecter:reject];
        } else {
            _resolve = resolve;
            _reject = reject;
        }
    }@catch(NSException *e) {
        reject( @"RN-IOS", @"init error", nil);
    }
}


+ (void)didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    @try{
        _deviceToken = deviceToken;
        RCTLogInfo(@"getting token %@", deviceToken);
        if (_resolve != nil) {
            [RnUnifiedPush saveParams :_resolve rejecter:_reject];
        }
    }@catch(NSException *e) {
        RCTLogInfo(@"Sending push to didRegisterForRemoteNotificationsWithDeviceToken %@", e.reason);
        
        _reject( @"RN-IOS", @"didRegisterForRemoteNotificationsWithDeviceToken error", nil);
    }
}

+ (void)didReceiveRemoteNotification:(NSDictionary *)userInfo {
    RCTLogInfo(@"Sending push to Emitter %@", userInfo);
    [RNUnifiedPushEmitter emitEvent:userInfo];
}


+ (void)saveParams: (RCTPromiseResolveBlock)resolve
          rejecter:(RCTPromiseRejectBlock)reject {
    @try{
        NSDictionary* iosConfig = [_config objectForKey:@"ios"];
        
        if (iosConfig == nil) {
            iosConfig = [_config objectForKey:@"ios_token"];
        }
        
        if (iosConfig == nil) {
            reject( @"RN-IOS", @"iosConfig was nil", nil);
            return;
        }
        
        if ([_config objectForKey:@"pushServerURL"] == nil) {
            reject( @"RN-IOS", @"pushServerURL was nil", nil);
            return;
        }
        
        if ([iosConfig objectForKey:@"variantID"] == nil) {
            reject( @"RN-IOS", @"variantID was nil", nil);
            return;
        }
        if ([iosConfig objectForKey:@"variantSecret"] == nil) {
            reject( @"RN-IOS", @"variantSecret was nil", nil);
            return;
        }
        
        [_preferences setObject:_config[@"pushServerURL"] forKey:@"pushServerURL"];
        [_preferences setObject:iosConfig[@"variantID"] forKey:@"variantID"];
        [_preferences setObject:iosConfig[@"variantSecret"] forKey:@"variantSecret"];
        [_preferences synchronize];
        
        resolve(@[[NSNull null]]);
    }@catch(NSException *e) {
        RCTLogInfo(@"Sending push to didReceiveRemoteNotification %@", e.reason);
        reject( @"RN-IOS", @"didReceiveRemoteNotification error", nil);
    }
    
}


+ (void)registerToUPS: (NSDictionary*)config 
             resolver:(RCTPromiseResolveBlock)resolve
             rejecter:(RCTPromiseRejectBlock)reject {
    @try{
        if ([_preferences objectForKey:@"pushServerURL"] == nil)
        {
            reject( @"RN-IOS", @"pushServerURL was nil, have you called initialize?", nil);
            return;
        }
        
        NSString *url =  (NSString*)[_preferences objectForKey:@"pushServerURL"];
        NSString *variantID =  (NSString*)[_preferences objectForKey:@"variantID"];
        NSString *variantSecret =  (NSString*)[_preferences objectForKey:@"variantSecret"];
        
        AGDeviceRegistration *d = [[AGDeviceRegistration alloc] initWithServerURL:[NSURL URLWithString:url]];
        
        [d registerWithClientInfo:^(id<AGClientDeviceInformation> clientInfo) {
            
            [clientInfo setDeviceToken:_deviceToken];
            [clientInfo setVariantID:variantID];
            [clientInfo setVariantSecret:variantSecret];
            
            UIDevice *currentDevice = [UIDevice currentDevice];
            // set some 'useful' hardware information params
            [clientInfo setOperatingSystem:[currentDevice systemName]];
            [clientInfo setOsVersion:[currentDevice systemVersion]];
            [clientInfo setDeviceType:[currentDevice model]];
            
            if ([config objectForKey:@"alias"] != nil) {
                [clientInfo setAlias:(NSString*)[config objectForKey:@"alias"]];
            }
            if ([config objectForKey:@"categories"] != nil) {
                NSArray *categories = [config objectForKey:@"categories"];
                [clientInfo setCategories:categories];
            }
            
        } success:^{
            NSLog(@"RN-IOS => UnifiedPush Server registration worked");
            NSLog(@"RN-IOS => Invoking callback");
            resolve(@[[NSNull null]]);
            NSLog(@"RN-IOS => Callback invoked");
        } failure:^(NSError * err) {
            reject( @"no_events", @"Error registering",err);
        }];
    }@catch(NSException *e) {
        RCTLogInfo(@"registerToUPS %@", e.reason);
        reject( @"RN-IOS", @"registerToUPS error", nil);
    }
}

@end

