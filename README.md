# @aerogear/aerogear-reactnative-push

## Getting started

`$ npm install @aerogear/aerogear-reactnative-push --save`

## Native Project Configuration

The ReactNative Unified Push Client requires configuration of the native projects to consume push messages. Please refer to your platform's documentation or the [Unified Push documentation](http://aerogear.github.io/aerogear-unifiedpush-server/) for details.

## Usage Examples

There are two parts to using the library inside of your application. Your application must register itself with UnifiedPush and then your application will be able to handle messages.

### Registration

The client SDK must be provided with the variant information and, in the case of Android, the `senderID`. These values may be provided together as in the example below, or you may wish to use [platform specific code](https://reactnative.dev/docs/platform-specific-code). Once you have provided these values, the RNUnifiedPush instance is ready to register your application with UnifiedPush.

You will want to run this process at the boot of your application, and if your user changes their alias or subscribed categories.

```javascript
import RNUnifiedPush from '@aerogear/aerogear-reactnative-push';

const ups = new RNUnifiedPush();

ups.init(//Initialization provides the UPS address and variant secrets
{ 
pushServerURL: "http://10.1.10.51:9999/",
ios: {
    variantID: "91c039f9-d657-49cd-b507-cb78bea786e3",
    variantSecret: "4b7fd0b4-58b5-46e8-80ef-08a6b8d449cd"
}, 
android: {
    senderID: "557802659713",
    variantID: "c046f7b6-cf86-4ae6-9eb0-bb39104ea38b",
    variantSecret: "4907e063-cd39-477f-8938-2d817eabff97"
}
}).then(//Registration connects your device to UnifiedPush
() => {
    return ups.register({
    alias:"rnAlias",
    categories:["cat1", "cat2"]
    })
}).then(()=> this.onRegister && this.onRegister();  //You are now ready to receive push messages, inform your app
).catch(//Always catch your errors
(err) => console.log("Err", err));

```

### Handling Messages

RNUnifiedPush instances receive messages from the native platform the application is running on. The library passes these messages largely as is and the developer is responsible for ensuring that their application handles both iOS and Android messages. Do this by checking for the `aps` property of the message object, or by [platform specific code](https://reactnative.dev/docs/platform-specific-code). The following example uses different code files for Android and iOS to define our handler, and then it registers to a UnifiedPush server using our RNUnifiedPush instance.

*handler.android.js*
```javascript
//Extract the message text and add it to a state variable in a React application
export default function(message) { this.setState({messages: [...this.state.messages, message.alert] })}
```

*handler.ios.js*
```javascript
//Extract the message text and add it to a state variable in a React application
export default function(message) {this.setState({messages: [...this.state.messages, message.aps.alert.body] })};

```

*app.js*
```javascript
    import handler from './handler';
    /*
        SNIPPING imports and registration
    */
    ups.registerMessageHandler(handler.bind(this));  //.bind(this) allows `this` to work in the above examples
```
## Cookbook application
There is a full example of this application in our [cookbook repository](https://github.com/aerogear/unifiedpush-cookbook/react-native/push)
