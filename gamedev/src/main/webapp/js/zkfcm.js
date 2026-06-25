if (typeof zkFCM !== "undefined"){
	console.log("already load zkfcm");
}
else{
	console.log("load zkfcm");
	var zkFCM = (function() {
		let loadJS = function (p_jsFile){
			console.log("load " + p_jsFile);
			var jsElement = document.createElement('script');
			jsElement.src = p_jsFile;
			document.head.appendChild(jsElement);
		}
		
		var fInit = -1;  //-1 not yet init, 0 initializing, 1 initialized
		let init = function() {
			if (fInit >= 0) {
				console.log('already init zkfcm');
				return;
			}
			console.log('init zkfcm');
			
			fInit = 0;
			/*
			//load external js
			loadJS('https://www.gstatic.com/firebasejs/8.3.0/firebase-app.js');
			loadJS('https://www.gstatic.com/firebasejs/8.3.0/firebase-analytics.js');
			loadJS('https://www.gstatic.com/firebasejs/8.3.0/firebase-messaging.js');
			*/
			
			
			var int = setInterval(function() {
				//make sure firebase is ready
			    if (typeof firebase == 'undefined'  || 
			    	typeof firebase.initializeApp == 'undefined' || 
			    	//typeof firebase.analytics == 'undefined' ||
			    	typeof firebase.messaging == 'undefined'
			    		){
			    	console.log('waiting for firebase');
			    	return;
			    }
			    clearInterval(int);
			    
				firebase.initializeApp({
					apiKey: "AIzaSyAWTWu05SujtI1QbHDoJW3Mo24sXIpKfXI",
					authDomain: "pmsmobile-f62b4.firebaseapp.com",
					databaseURL: "https://pmsmobile-f62b4.firebaseio.com",
					projectId: "pmsmobile-f62b4",
					storageBucket: "pmsmobile-f62b4.appspot.com",
					messagingSenderId: "946303863654",
					appId: "1:946303863654:web:5a31f24cb289aeb89516a7",
					measurementId: "G-PS2QE836F5"
				});
				//firebase.analytics();
				
				
				const messaging = firebase.messaging();
				
				if ('serviceWorker' in navigator) {
					//let swjs = 'js/firebase-messaging-sw.js';
					let swjs = 'js/zkfcm-sw.js?version=1';
					navigator.serviceWorker.register(swjs).then((registration) => {
						console.log('sw scope: ', registration.scope);
						messaging.useServiceWorker(registration);
				
						
						//this block of code probably can move out this block
						messaging.requestPermission()
						.then(function () {
							console.log("Notification permission granted.");
							return messaging.getToken()
						})
						.then(function(token) {
							console.log("token %s", token);
						})
						.catch(function (err) {
							console.log("Unable to get permission to notify.", err);
						});
						messaging.onMessage(function(payload) {
							console.log("zkfcm message received. ", payload);
						});
						
						
					});
				}

				fInit = 1;
			    

			    // the rest of the code
			}, 500);
		}
		return {
			init: init,
		};
	})();
	
}