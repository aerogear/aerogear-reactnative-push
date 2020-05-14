import {NativeModules,NativeEventEmitter, AppRegistry} from "react-native";

const NativeRnUnifiedPush = NativeModules.RnUnifiedPush;
const RNUnifiedPushEmitter = NativeModules.RNUnifiedPushEmitter;
const nativeEventEmitter = new NativeEventEmitter(RNUnifiedPushEmitter);


AppRegistry.registerHeadlessTask('onBackgroundMessage', 
 ()=> async (alert) => { nativeEventEmitter.emit("onDefaultMessage", alert)}
);

export class RNUnifiedPush {
  constructor() {
    this.subscription = nativeEventEmitter.addListener(
      'onDefaultMessage',
      (msg) => this.messageHandler && this.messageHandler(msg),
    );
  }

  init(config) {
    return NativeRnUnifiedPush.initialize(config);
  }

  register(config) {
    return NativeRnUnifiedPush.register(config);
  }

  registerMessageHandler(messageHandler) {
    this.messageHandler = messageHandler;
  }

  unregisterMessageHandler() {
    this.messageHandler = null;
  }
}
