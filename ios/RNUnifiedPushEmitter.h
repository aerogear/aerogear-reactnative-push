#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#ifndef RNUnifiedPushEmitter_h
#define RNUnifiedPushEmitter_h

@interface RNUnifiedPushEmitter : RCTEventEmitter <RCTBridgeModule>

+ (void)emitEvent:(NSDictionary*) payload;

@end

#endif /* RNUnifiedPushEmitter_h */
