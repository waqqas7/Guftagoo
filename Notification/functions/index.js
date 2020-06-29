'use strict'




const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);




exports.sendNotification = functions.database.ref('/Notifications/{receiver_user_id}/{notification_id}')
.onWrite((data, context) =>
{
 const receiver_user_id = context.params.receiver_user_id;
 const notification_id = context.params.notification_id;




 console.log('We have a notification to send to :' , receiver_user_id);




 if (!data.after.val())
 {
  console.log('A notification has been deleted :' , notification_id);
  return null;
 }




 const sender_user_id = admin.database().ref(`/Notifications/${receiver_user_id}/${notification_id}/from`).once('value');
 return sender_user_id.then(result =>
  {
   const senderId = result.val();
   console.log('notification from:' ,senderId);
   const userQuery = admin.database().ref(`/Users/${senderId}/name`).once('value');
   return userQuery.then(result =>
    {
     const userName = result.val();
console.log('username:' ,userName);
  const DeviceToken = admin.database().ref(`/Users/${receiver_user_id}/device_token`).once('value');


 return DeviceToken.then(result =>
 {
  const token_id = result.val();


  const payload =
  {
   notification:
   {
    title: "New Chat Request",
    body: `${userName} wants to connect with you.`,
    icon: "default"
   }
  };


  return admin.messaging().sendToDevice(token_id, payload)
  .then(response =>
   {
    console.log('This was a notification feature.');
   });
 });
    });
  });
});