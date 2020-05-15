#import <Foundation/Foundation.h>
#import <React/RCTLog.h>

#import "RnUnifiedPushEmitter.h"

@implementation RNUnifiedPushEmitter

RCT_EXPORT_MODULE();


- (NSArray<NSString *> *)supportedEvents {
    return @[@"onDefaultMessage"];
}


- (void)startObserving
{
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(sendEvent:)
                                               name:@"event-emitted"
                                              object:nil
   ];
}

- (void)stopObserving
{
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}


- (void)sendEvent:(NSNotification *)notification {
  RCTLogInfo(@"send %@", notification);
    [self sendEventWithName:@"onDefaultMessage" body:notification.userInfo[@"aps"][@"alert"][@"body"]];
}

+ (void)emitEvent:(NSDictionary*) payload
{
    RCTLogInfo(@"emitEvent %@", payload);
  [[NSNotificationCenter defaultCenter] postNotificationName:@"event-emitted"
                                                      object:self
                                                    userInfo:payload
   ];
}

@end
