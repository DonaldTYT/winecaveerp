if (typeof mobileUtil !== "undefined"){
	console.log("already load mobileUtil");
}
else{
	var mobileUtil = (function() {
		var fDebug = false;
		var fMobile;
		
		/*
		//obsoleted221207 should obtain the mode from sessionhelper
		function muGetParam(name) {
		    var url = window.location.href;
		    name = name.replace(/[\[\]]/g, "\\$&");
		    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"), results = regex.exec(url);
		    if (!results) return null;
		    if (!results[2]) return '';
		    return decodeURIComponent(results[2].replace(/\+/g, " "));
		}
		function muIsMobile(){
			var mode = muGetParam("mode");
			if (mode != null && mode == "pc") return(false);
			if (mode != null && mode == "mobile") return(true);
			return(navigator.appVersion.indexOf("Mobile") > -1 ? true : false);
		}
		*/
		function muIsMobile(){
			//obtain from setup
			if (typeof fMobile !== "undefined"){
				if (fDebug) console.log('muIsMobile return:' + fMobile);
				return fMobile;
			}
			console.log('fMobile is undefined. run init first.');
			return false;
		}
		function muInit(p_isMobileFlag){
			fMobile = p_isMobileFlag;
		}
		function muLoadCss(p_mode, p_fileNormal, p_fileMobile){
			var file = null;
			if (p_mode == "auto" || !p_mode || p_mode == "null"){
				if (p_fileMobile && muIsMobile()){
					file = p_fileMobile;
				}
				else if(p_fileNormal){
					file = p_fileNormal;
				}
			}
			else if (p_mode == "mobile" && p_fileMobile){
				file = p_fileMobile;
			}
			else{
				file = p_fileNormal;
			}
			
			if (file != null){
				if (fDebug) console.log("muLoadCss:" + file);
				var cssTag = document.createElement("link");
			    cssTag.setAttribute("rel", "stylesheet");
			    cssTag.setAttribute("type", "text/css");
			    cssTag.setAttribute("href", file);
			    document.getElementsByTagName("head")[0].appendChild(cssTag);
			}
			else{
				if (fDebug) console.log("muLoadCss ignore:("+p_mode+","+p_fileNormal+","+p_fileMobile+")");
			}
		}
		function muSetViewPort(p_scale){
			var scale = 1;
			if (p_scale && p_scale != "null"){
				scale = p_scale;
			}
			if (fDebug) console.log("scale:"+scale+" "+ document.documentElement.clientWidth +"x"+ document.documentElement.clientHeight  +", " + window.screen.width + "x"+window.screen.height+"x"+window.devicePixelRatio);
			if (!muIsMobile()) {
				return;
			}
			var metaTag=document.createElement('meta');
			metaTag.name = "viewport";
			metaTag.id = "viewport";
			metaTag.content = "width=device-width, initial-scale="+scale+", maximum-scale="+(scale*3);
			document.getElementsByTagName('head')[0].appendChild(metaTag);
		}
		function muChangeMode(p_mode){
			var url = window.location.href;
			
		    var pattern = new RegExp('\\b('+'mode'+'=).*?(&|#|$)');
		    if (url.search(pattern)>=0) {
		        url = url.replace(pattern,'$1' + p_mode + '$2');
		    }
		    else{
		    	url = url.replace(/[?#]$/,'');
		    	url = url + (url.indexOf('?')>0 ? '&' : '?') + 'mode' + '=' + p_mode;	
		    }
			window.location.href = url;
		}
		
		return {
			muInit : muInit,
			muIsMobile : muIsMobile,
			muLoadCss : muLoadCss,
			muSetViewPort : muSetViewPort,
			muChangeMode : muChangeMode,
		};
	})();
}