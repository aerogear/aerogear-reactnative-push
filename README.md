# @aerogear/aerogear-reactnative-push

## Getting started

`$ npm install @aerogear/aerogear-reactnative-push --save`

## Configuration

The ReactNative Unified Push Client requires configuration of the native projects to consume push messages. Please refer to your platform's documentation or the [Unified Push documentation](http://aerogear.github.io/aerogear-unifiedpush-server/) for details.

## Usage Example
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
    "alias":"rnAlias",
    "categories":["cat1", "cat2"]
    })
}).then(()=>{//You are now ready to receive push messages, inform your app
    if(this.onRegister)this.onRegister(); 
}).catch(//Always catch your errors
(err) => {
    console.log("Err", err)
});

ups.registerMessageHandler(//A message handler responds to messages 
    (message)=>{
        console.log("You have receieved a background push message." + JSON.stringify(message));
    };
);
```

## Cookbook application
There is a full example of this application in our [cookbook repository](https://github.com/aerogear/unifiedpush-cookbook/react-native/push)