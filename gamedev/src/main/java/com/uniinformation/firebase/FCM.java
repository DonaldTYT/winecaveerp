package com.uniinformation.firebase;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.auth.oauth2.GoogleCredentials;
//import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
//import com.uniinformation.utils.DateUtil;
//import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;

public class FCM {
	//private final FirebaseApp firebaseApp;
	/***
	 * 
	 * @param p_accountIS - the private key obtain from firebase.google.com
	 * @param p_databaseUrl - database url obtain from firebase.google.com (optional)
	 * @throws Exception
	 */
	public FCM(InputStream p_accountIS, String p_databaseUrl) throws Exception{
		/*
		//FirebaseOptions.Builder builder = new FirebaseOptions.Builder();
		FirebaseOptions.Builder builder = new FirebaseOptions.Builder();
		builder.setCredentials(GoogleCredentials.fromStream(p_accountIS));
		UniLog.log1("set credentials ok");
		if (StringUtils.isNotBlank(p_databaseUrl)) {
			builder.setDatabaseUrl(p_databaseUrl);
			UniLog.log1("set databaseurl ok");
		}
		firebaseApp = FirebaseApp.initializeApp(builder.build());
		*/
		FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(p_accountIS))
				.setDatabaseUrl(p_databaseUrl)
				.build();
		
		//firebaseApp = FirebaseApp.initializeApp(options);
		if (FirebaseApp.getApps().isEmpty()) {  //fix FirebaseApp name [DEFAULT] already exists
			FirebaseApp.initializeApp(options);
		}
	}
	public FCM(InputStream p_accountIS) throws Exception{
		this(p_accountIS, null);
	}
	/***
	 * Send notification message to web/android/ios/unity client via firebase cloud messaging
	 * If browser offline, it will store in firebase and deliver when browser online.
	 * 
	 * web remark:
	 * - user need to accept notification, after accepted, it can generate a token. 
	 * - The token will be changed after clear browser cache.
	 * - fcm only run over https or localhost
	 * - Chrome cannot display message when closed. (allow browser run in background mode may help, but it's not a easy task)
	 * - Edge can display message even it was closed
	 * 
	 * @param p_destTokens - destination token
	 * @param p_title - notification title
	 * @param p_content - notification content
	 * @param p_dataMap - for custom data attribute
	 * @param p_urgent - allow message display in sleeping mode
	 * @return  ok count. message registered to google network only, not yet deliver to client machine.
	 * @throws FirebaseMessagingException
	 */
	public int sendMsg(List<String> p_destTokens, String p_title, String p_content, Map<String,String> p_dataMap, boolean p_urgent) throws FirebaseMessagingException {
		//TODO handle TTL/expiration
		MulticastMessage.Builder builder = MulticastMessage.builder();
		if (p_dataMap != null) {
			builder.putAllData(p_dataMap);
		}
		
		//230308 allow message display in sleeping mode
		if (p_urgent) {
			builder.setAndroidConfig(AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build());
		}
		
		builder.setNotification(
				Notification.builder().setTitle(p_title == null ? "no title" : p_title)
					.setBody(p_content == null ? "no content" : p_content)
					.build());
		MulticastMessage message = builder.addAllTokens(p_destTokens).build();
		BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
		UniLog.log1("ok:%d fail:%d", response.getSuccessCount(), response.getFailureCount());
		return response.getSuccessCount();
	}
	public static void main(String args[]) throws Exception{
		FCM fcm = new FCM(new FileInputStream("/tmp/hellovoice-71e71-firebase-adminsdk-pxavc-2e1b452377.json"));
		fcm.sendMsg(
				Arrays.asList(
						//"fuIALfxWRKahfJBjWng_2k:APA91bHZ8Y-CdxSoqVwUC-4qY4wtFh2aeYVJ6keziAYsOvDO_ePtrqfuRQkYkYJlZqesj8jnXMeaE2ZYrq66khT4YmIzJ4vUN9LoTI0uByI1WYLkuBxMLQaGq9pnOIgGHiSB-U-frzig" //emu
						"fBcUOlO-RbOqthROO8kuVn:APA91bFpc5e-grJrc6X43vHJfaw1LH9LXjkymiI7eycsJfOpaNC2gFaXtvoDWHDQuRaZHmEnDJWesm5vFwnyV-m7TjWzonVHDVlPQ8uHr1wwm7J1D-ikKs70SVnrq_CCUvMftun9EU54"  //real
						), 
				"my title " + new Date(), 
				"my content\nHello how are you?\n" + new Date().toString() , 
				null,
				true);
	}
}
