//
//  RNUnifiedPushEmitter.h
//  RnUnifiedPush
//
//  Created by Massimiliano Ziccardi on 01/05/2020.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

#ifndef RNUnifiedPushEmitter_h
#define RNUnifiedPushEmitter_h

@interface RNUnifiedPushEmitter : RCTEventEmitter <RCTBridgeModule>

+ (void)emitEvent:(NSDictionary*) payload;

@end

#endif /* RNUnifiedPushEmitter_h */
