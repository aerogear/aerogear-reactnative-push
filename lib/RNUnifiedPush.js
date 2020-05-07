import {NativeModules,NativeEventEmitter} from "react-native";

const NativeRnUnifiedPush = NativeModules.RnUnifiedPush;
const RNUnifiedPushEmitter = NativeModules.RNUnifiedPushEmitter;

// const { RNUnifiedPushEmitter } = NativeModules;

const eventEmitter = new NativeEventEmitter(RNUnifiedPushEmitter);
//
// const subscription = eventEmitter.addListener(
//   'onUPSPushNotificationReceived',
//   (msg) => console.log('RN Event received:', msg),
// );

export class RNUnifiedPush {
  constructor() {
    this.subscription = eventEmitter.addListener(
      'onDefaultMessage',
      (msg) => this.messageHandler && this.messageHandler(msg)
    )
  }

  init(config, successCallback, errorCallback) {
    console.log('init', config, NativeRnUnifiedPush.init);
    NativeRnUnifiedPush.initialize(config, (err, res) => {
      console.log('JS PUSH => Initialized', {err, res});
      if (err) return errorCallback(err);
      return successCallback(res);
    })
  }

  registerMessageHandler(messageHandler) {
    this.messageHandler = messageHandler;
  }

  unregisterMessageHandler() {
    this.messageHandler = null;
  }
}