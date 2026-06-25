<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@ page import="java.io.*, java.util.*, java.text.*, com.uniinformation.utils.*, com.uniinformation.webcore.*" %>
<%
	SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper(request, response);
	String versionTag = "?version=" + sessionHelper.getVersionId();
	//versionTag+=new Date().getTime();  //for dev only, force refresh js/css file
	
	String jsHome = "zkassets/js";
	if (StringUtils.isNotBlank(request.getParameter("jshome"))){
		jsHome = request.getParameter("jshome").toString().trim();
	}
	String cssHome = "zkassets/css";
	if (StringUtils.isNotBlank(request.getParameter("csshome"))){
		cssHome = request.getParameter("csshome").toString().trim();
	}
	String cssMobileHome = "zkassets/cssMobile";
	if (StringUtils.isNotBlank(request.getParameter("cssmobilehome"))){
		cssMobileHome = request.getParameter("cssmobilehome").toString().trim();
	}
	
	//convert load parameters into hashset for load ondemand
	String loadArr[] = request.getParameterValues("load");
	HashSet<String> loadHS = new HashSet<String>();
	if (loadArr != null){
		loadHS.addAll(Arrays.asList(loadArr));
	}
	
	int changingDelay = 1000;
	try{
		if (StringUtils.isNotBlank(request.getParameter("changingdelay"))){
			int changingDelayTmp = Integer.parseInt(request.getParameter("changingdelay").toString().trim());
			if (changingDelayTmp < 300){
				changingDelayTmp = 300;
			}
			if (changingDelayTmp > 3000){
				changingDelayTmp = 3000;
			}
			changingDelay = changingDelayTmp;
		}
		//UniLog.log1("DEBUG: changingDelay:" + changingDelay);
	}
	catch(Exception ex){
		UniLog.log1("error:" + ex.getMessage());
	}
%>
<!-- load icon -->
<%
	if (sessionHelper.useJxFormG2()){
%>
<link rel="icon" type="image/png" href="images/logo/g2_logo_32x32.png" sizes="32x32">
<%
	} else {
%>
<link rel="icon" type="image/png" href="images/icons/zkweb/090-edit-32x32.png" sizes="32x32">
<%
}
%>

<!-- for electron integration -->
<%
    //for electron integration, need to remove module before loading jquery lib
	if (sessionHelper.getAllowElectronIntegration()){
%>
<script>
	if (typeof require !== "undefined" && typeof module === 'object') {
		console.log('is electron');
		window.nodeRequire = require;
		delete window.require;
		delete window.exports;
		delete window.module;
	}
</script>
<%
	}
%>

	
</script>

<!-- load js -->
<script type="text/javascript" src="<%=jsHome%>/jquery-3.1.1.min.js<%=versionTag%>"></script>
<script type="text/javascript" src="<%=jsHome%>/jquery.sidr.js<%=versionTag%>"></script>
<script type="text/javascript" src="<%=jsHome%>/mobileUtil.js<%=versionTag%>"></script>
<script>
	mobileUtil.muInit(<%=sessionHelper.isMobile()%>);
</script>
<script type="text/javascript" src="<%=jsHome%>/notify.js<%=versionTag%>"></script>
<script type="text/javascript" src="<%=jsHome%>/zkprint.js<%=versionTag%>"></script>
<script type="text/javascript" src="<%=jsHome%>/jquery-ui-1.12.1.min.js<%=versionTag%>"></script>
<script type="text/javascript" src="<%=jsHome%>/signature_pad.2.3.2.min.js<%=versionTag%>"></script>
<script type="text/javascript" src="<%=jsHome%>/pdfobject-2.0.min.js<%=versionTag%>"></script>
<script type="text/javascript" src="<%=jsHome%>/zkAdvSearch.js<%=versionTag%>"></script>

<!-- load static css -->
<link rel="stylesheet" href="<%=cssHome%>/colors-3.0.2.min.css<%=versionTag%>"/>
<link rel="stylesheet" href="<%=cssHome%>/bootstrap-4.0.0.min.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/zkbibs.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/font-awesome-4.7.0/css/font-awesome.min.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/flaticon/flaticon-bes.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/flaticon/flaticon-dtmb.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/flaticon/flaticon-ec.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/flaticon/flaticon-ld.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/sidr.iceblue.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/sidr.dropdown.css<%=versionTag%>">
<link rel="stylesheet" href="<%=cssHome%>/mfb-0.12.css<%=versionTag%>"/>
<link rel="stylesheet" href="<%=cssHome%>/jquery-ui-1.12.1.min.css<%=versionTag%>"/>


<%
	//init FCM
	if (sessionHelper.getAllowFCM()){
%>
		<script type="text/javascript" src="https://www.gstatic.com/firebasejs/8.3.0/firebase-app.js"></script>
		<!--  
		<script type="text/javascript" src="https://www.gstatic.com/firebasejs/8.3.0/firebase-analytics.js"></script>
		-->
		<script type="text/javascript" src="https://www.gstatic.com/firebasejs/8.3.0/firebase-messaging.js"></script>
		<script type="text/javascript" src="<%=jsHome%>/zkfcm.js<%=versionTag%>"></script>
		<script>
			zkFCM.init();
		</script>
<%
	}
%>

<%
	//load vis ondemand
	if (loadHS.contains("all") || loadHS.contains("vis")){
%>
		<script type="text/javascript" src="<%=jsHome%>/visUtil.js<%=versionTag%>"></script>
<%
	}
%>

<%
	//load chartjs ondemand (tmp bypass)
	if (true || loadHS.contains("all") || loadHS.contains("cjs")){
%>
		<script type="text/javascript" src="<%=jsHome%>/Chart.bundle.min.js<%=versionTag%>"></script>
		<script type="text/javascript" src="<%=jsHome%>/cjsUtil.js<%=versionTag%>"></script>
<%
	}
%>

<%
	//load pivot ondemand (tmp bypass)
	if (true || loadHS.contains("all") || loadHS.contains("pivot")){
%>
		<script type="text/javascript" src="<%=jsHome%>/pivot-2.23.min.js<%=versionTag%>"></script>
		<script type="text/javascript" src="<%=jsHome%>/pivot/export_renderers.min.js<%=versionTag%>"></script>
		<script type="text/javascript" src="<%=jsHome%>/pivot/zkbi_renderers.js<%=versionTag%>"></script>
		<link rel="stylesheet" href="<%=cssHome%>/pivot-2.23.min.css<%=versionTag%>"/>
<%
	}
%>


<%
	//load dropzone ondemand (tmp bypass)
	if (true || loadHS.contains("all") || loadHS.contains("dz")){
%>
		<script type="text/javascript" src="<%=jsHome%>/dropzone-5.5.0.min.js<%=versionTag%>"></script>
		<link rel="stylesheet" href="<%=cssHome%>/dropzone-5.5.0.css<%=versionTag%>"/>
<%
	}
%>

<%
	//load shepherd ondemand
	if (sessionHelper.getAllowTour() || loadHS.contains("all") || loadHS.contains("shepherd")){
%>
		<script type="text/javascript" src="<%=jsHome%>/popper.js<%=versionTag%>"></script>
		<script type="text/javascript" src="<%=jsHome%>/shepherd.js<%=versionTag%>"></script>
		<link rel="stylesheet" href="<%=cssHome%>/shepherd-theme-arrows.css<%=versionTag%>"/>
<%
	}
%>

<%
	//load fullcalendar ondemand
	if (loadHS.contains("all") || loadHS.contains("fc")){
%>
		<link href="js/fullcalendar/core/main.css<%=versionTag%>" rel="stylesheet" />
		<link href="js/fullcalendar/daygrid/main.css<%=versionTag%>" rel="stylesheet" />
		<link href="js/fullcalendar/timegrid/main.css<%=versionTag%>" rel="stylesheet" />
		<link href="js/fullcalendar/list/main.css<%=versionTag%>" rel="stylesheet" />
		<script src="js/fullcalendar/core/main.js<%=versionTag%>"></script>
		<script src="js/fullcalendar/interaction/main.js<%=versionTag%>"></script>
		<script src="js/fullcalendar/daygrid/main.js<%=versionTag%>"></script>
		<script src="js/fullcalendar/timegrid/main.js<%=versionTag%>"></script>
		<script src="js/fullcalendar/list/main.js<%=versionTag%>"></script>
		
		<script src="js/fcUtil.js<%=versionTag%>"></script>
		<script src="js/moment-2.26.0.min.js<%=versionTag%>"></script>
		<link rel="stylesheet" href="<%=cssHome%>/jquery-ui-1.12.1.min.css<%=versionTag%>"/>
		
		<link href="css/jquery.datetimepicker-2.5.21.min.css<%=versionTag%>" rel="stylesheet" />
		<script src="js/jquery.datetimepicker-2.5.21.full.min.js<%=versionTag%>"></script>
		<script>
		mobileUtil.muLoadCss("<%=request.getParameter("mode")%>", "<%=cssHome%>/fc.css<%=versionTag%>", "<%=cssMobileHome%>/fc.css<%=versionTag%>");
		</script>
<%
	}
%>

<%
	//load sweetalert2
	if (true || loadHS.contains("all") || loadHS.contains("sa")){
%>
		<script src="<%=jsHome%>/sweetalert2-9.14.3.all.min.js<%=versionTag%>"></script>
		<script>
		function sa_ok(p_json){
			var json = { icon:'success', title:'Success', text:''};
			if (p_json){
				//Object.keys(p_json).forEach(key => json[key] = p_json[key]);
				Object.keys(p_json).forEach(function(key){ json[key] = p_json[key]});
			}
			Swal.fire(json);
		}
		function sa_fail(p_json){
			var json = { icon:'error', title:'Error', text:'Message'};
			if (p_json){
				Object.keys(p_json).forEach(function(key){ json[key] = p_json[key]});
			}
			Swal.fire(json);
		}
		function sa_warn(p_json){
			var json = { icon:'warning', title:'Warning', text:'Message'};
			if (p_json){
				Object.keys(p_json).forEach(function(key){ json[key] = p_json[key]});
			}
			Swal.fire(json);
		}
		function sa_info(p_json){
			var json = { icon:'info', title:'Information', text:'Message'};
			if (p_json){
				Object.keys(p_json).forEach(function(key){ json[key] = p_json[key]});
			}
			Swal.fire(json);
		}
		function sa_question(p_json){
			var json = { icon:'question', title:'Question', text:'Message'};
			if (p_json){
				Object.keys(p_json).forEach(function(key){ json[key] = p_json[key]});
			}
			Swal.fire(json);
		}
		function sa_info(p_json){
			var json = { icon:'info', title:'Information', text:'Message'};
			if (p_json){
				Object.keys(p_json).forEach(function(key){ json[key] = p_json[key]});
			}
			Swal.fire(json);
		}
		</script>
<%
	}
%>

<%
	//load pickr
	if (loadHS.contains("all") || loadHS.contains("pickr")){
%>
		<link href="js/pickr/themes/classic.min.css<%=versionTag%>" rel="stylesheet" />
		<link href="js/pickr/themes/monolith.min.css<%=versionTag%>" rel="stylesheet" />
		<link href="js/pickr/themes/nano.min.css<%=versionTag%>" rel="stylesheet" />
		<script src="js/pickr/pickr.min.js<%=versionTag%>"></script>

		<script src="js/pickrUtil.js<%=versionTag%>"></script>
<%
	}
%>


<%
	//load Logger.js on demand
	if (loadHS.contains("all") || loadHS.contains("syslog") || sessionHelper.getAllowSyslog()){
%>
		<script type="text/javascript" src="<%=jsHome%>/Logger.js<%=versionTag%>"></script>
<%
	}
%>


<%
	//load select2
	//if (loadHS.contains("all") || loadHS.contains("s2")){  }
	if (sessionHelper.getAllowS2Listbox()){
%>
		<link href="<%=jsHome%>/select2/css/select2.css<%=versionTag%>" rel="stylesheet" />
		<script type="text/javascript" src="<%=jsHome%>/select2/js/select2.js<%=versionTag%>"></script>
<%
	}
%>


<%
	//load zkdevice
	if (loadHS.contains("all") || loadHS.contains("zkdevice")){
%>
	<script type="text/javascript" src="<%=jsHome%>/zkdevice.js<%=versionTag%>"></script>
<%
	}
%>


<%
	//load jquery-contextmenu
	if (sessionHelper.isOpenPageIframe()){
%>
		<script type="text/javascript" src="<%=jsHome%>/jQuery-contextMenu/jquery.contextMenu.js<%=versionTag%>"></script>
		<script type="text/javascript" src="<%=jsHome%>/jQuery-contextMenu/jquery.ui.position.min.js<%=versionTag%>"></script>
		<link rel="stylesheet" href="<%=jsHome%>/jQuery-contextMenu/jquery.contextMenu.min.css<%=versionTag%>"> 
<%
	}
%>

<%
	//211125 bootstrap conflict with jqueryui tooltip. 
	//possible workaround: rename tooltip to uitooltip. e.g. $.widget.bridge('uitooltip', $.ui.tooltip)
	
	//load bootstrap-iconpicker
	if (loadHS.contains("all") || loadHS.contains("bsip")){
%>
		<link href="js/bootstrap-iconpicker/css/bootstrap-iconpicker.min.css<%=versionTag%>" rel="stylesheet" />
		<script src="js/bootstrap.bundle-4.0.0.min.js"></script>
		<script src="js/bootstrap-iconpicker/js/bootstrap-iconpicker.bundle.min.js<%=versionTag%>"></script>
<%
	}
%>

<%
	//load reportproblem
	if (/*loadHS.contains("all") || loadHS.contains("reportproblem") */ sessionHelper.getAllowReportProblem()){
%>
		<script type="text/javascript" src="<%=jsHome%>/html2canvas-1.4.0.js<%=versionTag%>"></script>
<%
	}
%>

<%
	//load tui.image-editor ondemand
	if (/*loadHS.contains("all") || loadHS.contains("timge")*/ sessionHelper.getAllowReportProblem()){
%>
        <link href="js/tui.image-editor/libs/tui-color-picker-2.2.7.min.css<%=versionTag%>" rel="stylesheet">
        <link href="js/tui.image-editor/tui-image-editor-3.15.2.min.css<%=versionTag%>" rel="stylesheet">
        <script type="text/javascript" src="js/tui.image-editor/libs/tui-code-snippet-1.5.2.min.js<%=versionTag%>"></script>
        <script type="text/javascript" src="js/tui.image-editor/libs/tui-color-picker-2.2.7.min.js<%=versionTag%>"></script>
        <script type="text/javascript" src="js/tui.image-editor/libs/panZoom.js<%=versionTag%>"></script>
        <script type="text/javascript" src="js/tui.image-editor/tui-image-editor-3.15.2.min.js<%=versionTag%>"></script>
<%
	}
%>


<%
	//240226 load mermaid
	if (loadHS.contains("all") || loadHS.contains("merm")){
%>
		<script src="js/mermaid-10.8.0.min.js<%=versionTag%>"></script>
<%
	}
%>


<%
	if (request.getParameter("skipdefaultcss") == null || !request.getParameter("skipdefaultcss").trim().toUpperCase().equals("Y")){
%>
<script>
	console.log('commoninit called');
</script>
<%
	}
%>

<%
	if (request.getParameter("less") != null){
%>	
<link rel="stylesheet/less" type="text/css" href="<%=request.getParameter("less")%>" />
<script>
  less = {
    env: "development",
    async: false,
    fileAsync: false,
    poll: 1000,
    functions: {},
    dumpLineNumbers: "comments",
    relativeUrls: true
  };
</script>
<script src="<%=jsHome%>/less.min.js<%=versionTag%>" data-env="development" ></script>
<%
	}
%>
<%
	if (request.getParameter("css") != null){
%>	
	<script>
		mobileUtil.muLoadCss(null, "<%=request.getParameter("css")%><%=versionTag%>");
	</script>
<%
	}
%>

<!-- load theme to override default style -->
<%@include file="themeInit.html"%>

<script>

	
	//experimental function. append browserwinid to url. 
	//know issue: page reloaded; need to handle recursion; need to handle login
	function checkAndFixBrowserWinId() {
		const newUrl = replaceUrlParam(window.location.href, 'bwid', getBrowserWinId());
		//console.log("orgUrl:"+window.location.href);
		//console.log("newUrl:"+newUrl);
		if (window.location.href != newUrl){
			window.location.replace(newUrl);
			//window.history.pushState("", "", newUrl);  //for change url without reload
		}
	}
	function replaceUrlParam(url, paramName, paramValue) {
		try {
			if (paramValue == null) {
				paramValue = '';
			}
			var pattern = new RegExp('\\b(' + paramName + '=).*?(&|#|$)', 'g');
			if (url.search(pattern) >= 0) {
				return url.replace(pattern, '$1' + paramValue + '$2');
			}
			url = url.replace(/[?#]$/, '');
			return url + (url.indexOf('?') > 0 ? '&' : '?') + paramName + '=' + paramValue;
		} catch (err) {
			console.log('error:' + err);
			return url;
		}
	}
	function getBrowserWinId() {
		if (!window.name || !window.name.startsWith("ZKBIBWID")) {
			window.name = "ZKBIBWID-" + new Date().getTime();
		}
		//console.log('getBrowserWinId() name:' + window.name);
		return window.name;
	}
	function debugLog(str) {
		if (typeof Logger !== "undefined") {
			Logger.open();
			Logger.print('[DEBUG] ' + str);
		} else {
			console.log('[DEBUG] ' + str);
		}
	}
	Date.prototype.yyyymmdd = function(p_sep) {
		var mm = this.getMonth() + 1; // getMonth() is zero-based
		var dd = this.getDate();
		return [ this.getFullYear(), (mm > 9 ? '' : '0') + mm,
				(dd > 9 ? '' : '0') + dd ].join(p_sep);
	};

	function getY4MD(p_date, p_sep, p_fmt) {
		var mm = p_date.getMonth() + 1; // getMonth() is zero-based
		var dd = p_date.getDate();
		
		//230224 quickfix for date fmt dd/MM/yyyy. should enhanance to support any format later
    	if (typeof p_fmt !== "undefined" && p_fmt == 'dd/MM/yyyy'){
			return [ (dd > 9 ? '' : '0') + dd, (mm > 9 ? '' : '0') + mm, p_date.getFullYear() ].join(p_sep);
    	}
    	else{
    		//default fmt
			return [ p_date.getFullYear(), (mm > 9 ? '' : '0') + mm, (dd > 9 ? '' : '0') + dd ].join(p_sep);
    	}
	};

	function copyToClipboard(value) { //TODO: generic function move to js file
		var tempInput = document.createElement("textarea");
		tempInput.style = "position: absolute; left: -1000px; top: -1000px";
		//jq(tempInput).val(value);
		$(tempInput).val(value);
		document.body.appendChild(tempInput);
		tempInput.select();
		/*
		//210503 try to fix ios cannot copy link, not work
		if (navigator.userAgent.match(/ipad|ipod|iphone/i)) {
			console.log('is ios');
			const editable = tempInput.contentEditable;
			tempInput.contentEditable = true;
			const range = document.createRange();
			range.selectNodeContents(tempInput);
			const sel = window.getSelection();
			sel.removeAllRanges();
			sel.addRange(range);
			tempInput.setSelectionRange(0, 999999);
			tempInput.contentEditable = editable;
		}
		else {
			console.log('non ios');
			tempInput.select();
		}
		*/
		
		document.execCommand("copy");
		document.body.removeChild(tempInput);
	}
	function openNewTab(url){
		//window.location.href = url;
		if (navigator.userAgent.match(/ipad|ipod|iphone/i)) {
			window.location.href = url;
		}
		else{
			window.open(url, '_blank').focus();
		}
	}
	function closeTab(){
		console.log('closetab');
		window.close();
	}
	function getURLParam(url, name) {
		return (RegExp(name + '=' + '(.+?)(&|$)').exec(url) || [ , null ])[1];
	}
	/*
	//this function need to enhance
	function getMainComp(){
	   	var comp = null;
	   	comp = zk.Widget.$('$zkbibrowser');
	   	if (comp == null){
	   		comp = zk.Widget.$('$winMain');
	   	}
	   	if (comp == null){
	   		comp = zk.Widget.$('$zkCustomMenu');
	   	}
	   	return(comp);
		
	}
	 */
	 
	//alias
	function getRootComp() {
		return getMainComp();
	}
	//guess the main component
	function getMainComp() {
		var comp = null;
		//first win
		comp = zk.Widget.$('@window');
		
		//first div
		if (comp == null) {
			comp = zk.Widget.$('@div');
		}
		return (comp);
	}
	var processUpdateTranslate = function(e) {
		if (!e.shiftKey) {
			return;
		}
		var page_id = $(e.currentTarget).data('pageid');
		console.log('page_id=' + page_id);
		var comp = getMainComp();
		if (comp && page_id) {
			console.log("target comp:" + comp.id);
			var dataJson = new Object();
			dataJson.key = page_id.toUpperCase();
			dataJson.type = "MENU";
			dataJson.defaultValue = "";
			zAu.send(new zk.Event(comp, "onUpdateTranslate", JSON
					.stringify(dataJson), {
				toServer : true
			}));
		} else {
			$.notify("User is not allowed to modify special label.", {
				className : "warn",
				globalPosition : "bottom right",
				autoHideDelay : 5000
			});
		}
		e.preventDefault();
	}

	$(document).ready( function() {
		
		
		<%
 			//handle alt click update translation
 			if (sessionHelper.getAllowUpdateTranslate()){
 		%>
		 		$('div#sidr div.dropdownlink > span').click(function(e){
			 		processUpdateTranslate(e);
			  	});
		 		$('div#sidr a > div > span').click(function(e){
			 		processUpdateTranslate(e);
			  	});
	 	<%
 			}
 			else{
	 	%>
	 	<%
	 			UniLog.log("not allow update translate");
 			}
	 	%>
			
		$(document).on('keydown', function ( e ) {
			//TODO: should obtain key list from server side
			//esc
		    if (e.keyCode == 27) {
	        	var zkComp = zk.Widget.$('$zkbibrowser');  //get component by id
	        	if (zkComp !== null && zAu !== null){
	        		zAu.send(new zk.Event(zkComp, "onCustomEsc", null, {toServer:true}));	        		
	        	}		      	
		    }
			/*
			//enter
		    if (e.keyCode == 13) {
	        	var zkComp = zk.Widget.$('$zkbibrowser');  //get component by id
	        	if (zkComp !== null && zAu !== null){
	        		zAu.send(new zk.Event(zkComp, "onCustomEnter", null, {toServer:true}));	        		
	        	}
		    }
			*/
			//alt
			if (!e.altKey){
				return;
			}
			if (e.which === 0){
				return;
			}
			
			//toggle menu
	    	if (e.which == 'M'.charCodeAt()){
           		if (typeof zkToggleSidr === "function"){ 
           			zkToggleSidr();
		      		e.preventDefault();
           		}
		      	return;
		   	}
			
			//allow alt+d
	    	if (e.which == 'D'.charCodeAt()){ 
	    		return;
	    	}
			
			//set value = today
		    if (e.which == 'T'.charCodeAt()){
		    	if (zk !== null && zk.currentFocus !== null && zk.currentFocus.className !== undefined && (zk.currentFocus.className.includes('Datebox') || zk.currentFocus.className.includes('Bandbox') || zk.currentFocus.className.includes('Textbox'))){
					var n = zk.currentFocus.getInputNode ? zk.currentFocus.getInputNode() : zk.currentFocus.$n(); //remark $n - textbox,datebox,etc, getInputNode - bandbox, etc 
					if (jq.nodeName(n, 'input')){ //if is input element
						//zk.Widget.$(jq(n)).setValue(getY4MD(new Date(),'/'));
						zk.Widget.$(jq(n)).$n('real').value = getY4MD(new Date(),'/');  //190718: fix js tz is not a function bug
						zk.Widget.$(jq(n)).fireOnChange();
					}
		    	}
		        e.preventDefault();
		    	return;
		    }
			//set value = previous/next date
		    if (e.which == 'P'.charCodeAt() || e.which == 'N'.charCodeAt()){
		    	if (zk !== null && zk.currentFocus !== null && zk.currentFocus.className !== undefined && (zk.currentFocus.className.includes('Datebox') || zk.currentFocus.className.includes('Bandbox') || zk.currentFocus.className.includes('Textbox'))){
					var n = zk.currentFocus.getInputNode ? zk.currentFocus.getInputNode() : zk.currentFocus.$n(); //remark $n - textbox,datebox,etc, getInputNode - bandbox, etc 
					if (jq.nodeName(n, 'input')){ //if is input element
						//var timeValue = (new Date(zk.Widget.$(jq(n)).getValue())).getTime();
						var timeValue = (new Date(zk.Widget.$(jq(n)).$n('real').value)).getTime();  //190718: fix js tz is not a function bug
						if (isNaN(timeValue)){
							timeValue = (new Date()).getTime();
						}
						var dayOffset = 86400000;
						if (e.which == 'P'.charCodeAt()){
							dayOffset = -86400000;
						}
						//zk.Widget.$(jq(n)).setValue(getY4MD(new Date(timeValue + dayOffset),'/'));
						zk.Widget.$(jq(n)).$n('real').value = getY4MD(new Date(timeValue + dayOffset),'/');  //190718: fix js tz is not a function bug
						zk.Widget.$(jq(n)).fireOnChange();
					}
		    	}
		        e.preventDefault();
		    	return;
		    }
		    
			//send back to server
		    if ((e.which >= '0'.charCodeAt() && e.which <= '9'.charCodeAt()) || (e.which >= 'A'.charCodeAt() && e.which <= 'Z'.charCodeAt())) {
	        	var zkComp = zk.Widget.$('$zkbibrowser');  //get component by id
	        	if (zkComp !== null && zAu !== null){
	        		zAu.send(new zk.Event(zkComp, "onCustomAltDown", e.which, {toServer:true}));
	        	}
		        e.preventDefault();
		    }
		});
		
		
		//adjust changing delay (instant) from 350ms to 1000ms
    	if (typeof zk !== "undefined"){
			zk.afterLoad("zul.inp", function () {
				console.log('after load zul.inp');
				//zul.inp.InputWidget.onChangingDelay=1000;
				zul.inp.InputWidget.onChangingDelay=<%=changingDelay%>;
			});
		
			
			<% 
				//setup custom datebox if required
				if (sessionHelper.getAllowCustomDatebox()){ 
			%> 
					customDateboxSetup(); 
			<% 
				} 
			%>
			
			//add floating effect for the quick search div
			zk.afterMount(function () {
				console.log('after mount');
				//set inputmode
				zkbiInputModeRefresh(200);
				
				//send desktop event for notify startup
				startupData = {};
				startupData.devicetype = "unknown";
			    if (typeof android !== "undefined"){
					startupData.devicetype = "android";
		        }
			    else if (typeof webkit !== "undefined"){
					startupData.devicetype = "ios";
			    }
			    else if (typeof flutterJsChannel !== 'undefined'){
					startupData.devicetype = "flutter";
			    }
				zkbiSend('onStartup',startupData);
				
    			var nsmark = $('.zkbi-nscrollout-mark70');
    			if (nsmark.length > 0){
    				//console.log("mark exist add scroll event");
					$(document).scroll(function(){
	      				//console.log("scrollTop:"+$(this).scrollTop());
				        if ($(this).scrollTop() > 70) {
				        	//console.log("add class");
				        	nsmark.addClass('zkbi-nscrollout');
				        } 
				        else {
				        	//console.log("remove class");
				        	nsmark.removeClass('zkbi-nscrollout');
				        }
					});
    			}
    			
    			/*
    			//test 
   			    console.log($('.z-grid'));
   			    $('.z-grid').formNavigation();
   			    */
   			    
   			    
   			    //experimental211112 immediate show tooltip. probably has side effect as the scope is too large
    			//$(document).tooltip({show: {effect:"none", delay:0}});
    			//$('.z-button').tooltip({show: {effect:"none", show:0, hide:0}});
    			var allowTooltip = true;
    			if (allowTooltip && !mobileUtil.muIsMobile()){
	    			$('div.z-window, div.sidemenu-container').children().tooltip({
	    			      position: {
	    			        my: "center bottom-12",
	    			        at: "center top",
	    			        using: function(position,feedback) {
	    			          $(this).css( position );
	    			          $("<div>")
	    			            .addClass( "ttarrow" )
	    			            .addClass( feedback.vertical )
	    			            .addClass( feedback.horizontal )
	    			            .appendTo( this );
	    			        }
	    			      },
	    			      show: {
	    			    	  effect:"none", show:0, hide:0, delay:500
	   			    	  },
	    			      open: function (event, ui) {
	    			    	  //211124 fix tooltip not dismiss when click button bug.
	    			    	  var orgEl = event.originalEvent.target;
	    			    	  var loopCnt = 0;
	    			          var timer = setInterval(function () {
	    			        	  //console.log('is hover:' + orgEl.matches(':hover'));
	   			        		  //if no more hover, close it
	    			        	  if (!orgEl.matches(':hover')){  
	    			        		 clearInterval(timer);
	    			                 $(ui.tooltip).hide(100);
	    			        	  }
	    			          }, 1000);
	    			      },
	    			});
    			}
    			
				<%
					if (sessionHelper.getAllowListboxHoverEffect()){
 				%>
    					listboxHoverEffect();
    			<%
 					}
    			%>
    			
    			
			}); //afterMount end
		}
	});
	
    function listboxHoverEffect(){
  	    console.log('listboxHoverEffect setup')
    	$(document).on('mouseenter','div.zkbi-main-listbox table td, div.zkbi-main-listbox table th',function() {
    		//console.log('hover');
    	    $('table td:nth-child(' + ($(this).index() + 1) + ')').addClass('zkbi-hover-col');
    	    $('table th:nth-child(' + ($(this).index() + 1) + ')').addClass('zkbi-hover-col');
    	});
    	$(document).on('mouseleave','div.zkbi-main-listbox table td, div.zkbi-main-listbox table th',function(){
    		//console.log('unhover');
    	    $('table td:nth-child(' + ($(this).index() + 1) + ')').removeClass('zkbi-hover-col');
    	    $('table th:nth-child(' + ($(this).index() + 1) + ')').removeClass('zkbi-hover-col');
    	});
    	$(document).on('mouseenter','div.zkbi-main-listbox table tr',function(){
    		//console.log('hover');
    		$(this).addClass('zkbi-hover-row');
    	});
    	$(document).on('mouseleave','div.zkbi-main-listbox table tr',function(){
    		//console.log('unhover');
    		$(this).removeClass('zkbi-hover-row');
    	});
    }
    
	function startBasicTour(){
		tour = new Shepherd.Tour({
		  defaults: {
		    classes: 'shepherd-theme-arrows',
	        showCancelLink: true,
		    scrollTo: true
		  }
		});

		tour.addStep('tour', {
		  title: 'Basic Tour (1/3)',
		  text: ['<b>Master Listbox</b>', 'Record Detail - Click record icon or double click to open record detail', 'Single Column Sort - Click column header', 'Multi Column Sort - Click Column header option -> Sort Multicolumn', '...' ],
		  attachTo: '.basictour_s1 bottom',
		  buttons: [
		            {
		              text: 'Next',
		              action: tour.next
		            },
		          ]
		});
		tour.addStep('tour', {
		  title: 'Basic Tour (2/3)',
		  text: ['<b>Query Bar</b>', 'Advanced Search - Open Advnaced Search Panel', 'Width by Data - Adjust column width based on data length', '...'],
		  attachTo: '.basictour_s2 bottom',
		  buttons: [
		            {
		              text: 'Back',
		              action: tour.back
		            },
		            {
		              text: 'Next',
		              action: tour.next
		            },
		          ]
		});
		tour.addStep('tour', {
		  title: 'Basic Tour (3/3)',
		  
		  text: ['<b>Action Bar</b>',
		         '<button onclick="tour.complete();zAu.send(new zk.Event(zk.Widget.$(\'$zkbibrowser\') , \'onCustomAltDown\', \'A\'.charCodeAt(), {toServer:true}));" class="z-button"><img class=\'z-button-image\' src=\'/pmsdemo/images/icons/zkweb/038-file-4-25x25.png\'/>&nbsp;Add</button> - Add new record',
		         '<button onclick="tour.complete();zAu.send(new zk.Event(zk.Widget.$(\'$zkbibrowser\') , \'onCustomAltDown\', \'E\'.charCodeAt(), {toServer:true}));" class="z-button"><img class=\'z-button-image\' src=\'/pmsdemo/images/icons/zkweb/019-export-25x25.png\'/>&nbsp;Export</button> - Export selected records in excel format',
		         ],
		  
		  attachTo: '.basictour_s3 top',
		  buttons: [
		            {
			              text: 'Back',
			              action: tour.back
		            },
		            {
		              text: 'Done',
		              action: tour.next
		            }
		          ]
		});

		tour.start();
	}
	var pivottpl ;
	var utils = $.pivotUtilities;
	function startDataAnalysis(id, rendName, aggregatestr,vals,data, cols, rows) {
		//var $container = $('#' + id);
		var $container = $('#' + jq(id).attr('id'));
		var renderers = $.extend(
				$.pivotUtilities.renderers,
	            $.pivotUtilities.export_renderers,
	            $.pivotUtilities.zkbi_renderers,
	            $.pivotUtilities.plotly_renderers
	            );
		var renderer ;

		/*
		if(aggregatestr == "")
			myaggregate = utils.aggregators["Count"]();
		else
			myaggregate = utils.aggregators["Sum"]([aggregatestr]);
		*/
//		myaggregate = utils.aggregators[aggregatestr](vals);

//		myaggregate = utils.aggregators[aggregatestr](["Sales Qty"]);
		myaggregate = utils.aggregators[aggregatestr](vals);

		if(rendName == 'ZKBI')
			renderer =  $.pivotUtilities.zkbi_renderers["ZKBI"];
		else
			renderer =  $.pivotUtilities.renderers["Table"];
		pivottpl = $.pivotUtilities.aggregatorTemplates;
		$container.pivot(data, {
            renderer: renderer,
	        aggregator: myaggregate,
//	        aggregator: aggregate,
//	        aggregator: daaggregate(["Sales Qty"]),
//	        aggregator: utils.aggregators["Sum"](["Sales Qty"]),
            cols: cols, 
            rows: rows,
            rendererOptions: { zkOpt: {compId: id} }
		});
		/*
		function insertLinks() {
			$container.find('.pvtAxisContainer.pvtRows')
	        	.prepend('<a style="display:block" href="javascript:addFieldsToColumn()">Add Fields to Column</a>'
	        			+ '<a style="display:block" href="javascript:addFieldsToRow()">Add Fields to Row</a>'
	        			+ '<a style="display:block" href="javascript:clearColumn()">Clear Column</a>'
	        			+ '<a style="display:block" href="javascript:clearRow()">Clear Row</a>');
		}
		*/
		function insertLinks() {
			$container.find('.pvtAxisContainer.pvtRows')
	        	.prepend(
	        			'<a style="display:block" href="javascript:tozkbi()">Back to ZKBI</a>'
	        			+ '<a style="display:block" href="javascript:clearColumn()">Clear Column</a>'
	        			+ '<a style="display:block" href="javascript:clearRow()">Clear Row</a>');
		}
		insertLinks();
		function getConfig() {
			return $container.data("pivotUIOptions");
		}
		function restore(config) {
			$container.pivotUI(data, config, true);
			insertLinks();
		}
		tozkbi = function() {
				console.log(id);
	        	var zkComp = zk.Widget.$(id);  //get component by id
	        	if (zkComp !== null && zAu !== null){
	        		console.log("sending event");
	        		zAu.send(new zk.Event(zkComp, "onDataAnalysis", null, {toServer:true}));	        		
	        	}		      	
		}
		/*
		addFieldsToColumn = function() {
			var config = getConfig();
			config.cols = config.cols.concat(config.rows);
			config.rows = [];
			$.each(cols.concat(rows), function(i, o){
				if ($.inArray(o, config.cols) < 0)
					config.cols.push(o);
			});
			restore(config);
		};
		addFieldsToRow = function() {
			var config = getConfig();
			config.rows = config.rows.concat(config.cols);
			config.cols = [];
			$.each(rows.concat(cols), function(i, o){
				if ($.inArray(o, config.rows) < 0)
					config.rows.push(o);
			});
			restore(config);
		};
		*/
		clearColumn = function() {
			var config = getConfig();
			config.cols = [];
			restore(config);
		};
		clearRow = function() {
			var config = $container.data("pivotUIOptions");
			config.rows = [];
			restore(config);
		};
	}
	function startDataAnalysisUI(id, rendName, aggregates,data, cols, rows) {
		//var $container = $('#' + id);
		var $container = $('#' + jq(id).attr('id'));
		var renderers = $.extend(
				$.pivotUtilities.renderers,
	            $.pivotUtilities.export_renderers,
	            $.pivotUtilities.zkbi_renderers,
	            $.pivotUtilities.plotly_renderers
	            );
		pivottpl = $.pivotUtilities.aggregatorTemplates;
		$container.pivotUI(data, {
			renderers: renderers,
//	        aggregators: aggregates,
            cols: cols, 
            rows: rows,
            rendererName: rendName,
            rendererOptions: { zkOpt: {compId: id} }
		});
		function insertLinks() {
       		console.log("haha0");
       		console.log($container.find('.pvtAxisContainer'));
       		console.log("haha1");
//			$container.find('.pvtAxisContainer.pvtRows')
			$container.find('.pvtAxisContainer.pvtUnused')
	        	.prepend(
	        			'<a style="display:block" href="javascript:tozkbi(\'close\')" align="center">Close</a>'
	        			+ '<a style="display:block" href="javascript:tozkbi(\'save\')" align="center">Save and Close</a>'
	        			+ '<a style="display:block" href="javascript:clearColumn()" align="center">Clear Column</a>'
	        			+ '<a style="display:block" href="javascript:clearRow()" align="center">Clear Row</a>');
		}
		insertLinks();
		function getConfig() {
			return $container.data("pivotUIOptions");
		}
		function restore(config) {
			$container.pivotUI(data, config, true);
			insertLinks();
		}
		tozkbi = function(cmd) {
				console.log(id);
	        	var zkComp = zk.Widget.$(id);  //get component by id
	        	if (zkComp !== null && zAu !== null){
	        		console.log("sending event");
					var rtn = {'cmd':cmd,'config':getConfig()};
	        		zAu.send(new zk.Event(zkComp, "onDataAnalysis", rtn , {toServer:true}));	        		
	        	}		      	
		}
		/*
		addFieldsToColumn = function() {
			var config = getConfig();
			config.cols = config.cols.concat(config.rows);
			config.rows = [];
			$.each(cols.concat(rows), function(i, o){
				if ($.inArray(o, config.cols) < 0)
					config.cols.push(o);
			});
			restore(config);
		};
		addFieldsToRow = function() {
			var config = getConfig();
			config.rows = config.rows.concat(config.cols);
			config.cols = [];
			$.each(rows.concat(cols), function(i, o){
				if ($.inArray(o, config.rows) < 0)
					config.rows.push(o);
			});
			restore(config);
		};
		*/
		clearColumn = function() {
			var config = getConfig();
			config.cols = [];
			restore(config);
		};
		clearRow = function() {
			var config = $container.data("pivotUIOptions");
			config.rows = [];
			restore(config);
		};
	}
	
	function startDataAnalysisYY(id, data, cols, rows, aggregate) {
//		var $container = $('#' + id);
//		var $container = $('#' + jq('$'+id).attr('id'));
		var $container = $('#' + jq(id).attr('id'));
		/*
		var renderers = $.extend($.pivotUtilities.renderers 
	            ,$.pivotUtilities.plotly_renderers 
				);
		*/
		var renderers = $.extend($.pivotUtilities.renderers 
	            ,$.pivotUtilities.zkbi_renderers
	            ,$.pivotUtilities.export_renderers
	            );
		pivottpl = $.pivotUtilities.aggregatorTemplates;
//		console.log(aggregate);
//		console.log(JSON.stringify(aggregate));
		$container.pivotUI(data, {
			renderers: renderers,
	           cols: cols, rows: rows
	           , aggregators: aggregate
	           /*
	           aggregators: {
                   "Number of Records":      function() { return tpl.count()() },
		    	   "Total Qty": function() { return tpl.sum()(["Confirmed Qty"])}
               }
	           */
		});
		function insertLinks() {
			$container.find('.pvtAxisContainer.pvtRows')
	        	.prepend('<a style="display:block" href="javascript:addFieldsToColumn()">Add Fields to Column</a>'
	        			+ '<a style="display:block" href="javascript:addFieldsToRow()">Add Fields to Row</a>'
	        			+ '<a style="display:block" href="javascript:clearColumn()">Clear Column</a>'
	        			+ '<a style="display:block" href="javascript:getData()">Get Data</a>'
	        			+ '<a style="display:block" href="javascript:clearRow()">Clear Row</a>');
		}
		insertLinks();
		function getConfig() {
			return $container.data("pivotUIOptions");
		}
		function restore(config) {
			$container.pivotUI(data, config, true);
			insertLinks();
		}
		addFieldsToColumn = function() {
			var config = getConfig();
			config.cols = config.cols.concat(config.rows);
			config.rows = [];
			$.each(cols.concat(rows), function(i, o){
				if ($.inArray(o, config.cols) < 0)
					config.cols.push(o);
			});
			restore(config);
		};
		addFieldsToRow = function() {
			var config = getConfig();
			config.rows = config.rows.concat(config.cols);
			config.cols = [];
			$.each(rows.concat(cols), function(i, o){
				if ($.inArray(o, config.rows) < 0)
					config.rows.push(o);
			});
			restore(config);
		};
		clearColumn = function() {
			var config = getConfig();
			config.cols = [];
			restore(config);
		};
		clearRow = function() {
			var config = $container.data("pivotUIOptions");
			config.rows = [];
			restore(config);
		};
		{
			//alert(id);
				console.log(id);
	        	var zkComp = zk.Widget.$('$'+id);  //get component by id
	        	if (zkComp !== null && zAu !== null){
	        		console.log("sending event");
	        		zAu.send(new zk.Event(zkComp, "onDataAnalysis", null, {toServer:true}));	        		
	        	}		      	
		}
		haha = function() {
			console.log("HAHA called 1");
			console.log(renderers);
			console.log("HAHA called 2");
			console.log($container);
			console.log("HAHA called 3");
			var config = getConfig();
			console.log(config);
//			console.log(config.aggregators);
//			console.log(aggregate);
		};
		
		getData = function() {
			console.log("Get Data");
//			pd = $container.data("pivotUIOptions").renderers;
			pd = $container.renderer;
			console.log(pd);
		}
		
		
	}
	
	function clearDropzoneFiles(p_jsDivId) {
		if ($(p_jsDivId).length && $(p_jsDivId).data('dropzone'))
			$(p_jsDivId).data('dropzone').removeAllFiles();
	}

	function addDropzone(p_jsDivId, p_removeAfterUpload, p_autoFiling, p_acceptedFileList, p_showRemoveLink){
		var jsDivId = 'div#jsDropzone';
		var removeAfterUpload = false;
		var autoFiling = false;
		var acceptedFileList = null; //e.g. 'application/pdf,image/jpeg,image/png,image/gif';
		var showRemoveLink = false;
    	if (typeof p_jsDivId !== "undefined"){
    		jsDivId = p_jsDivId;
    	}
    	if (typeof p_removeAfterUpload !== "undefined"){
    		removeAfterUpload = p_removeAfterUpload;
    	}
    	if (typeof p_autoFiling !== "undefined"){
    		autoFiling = p_autoFiling;
    	}
    	if (typeof p_acceptedFileList !== "undefined"){
    		acceptedFileList = p_acceptedFileList;
    	}
    	if (typeof p_showRemoveLink !== "undefined"){
    		showRemoveLink = p_showRemoveLink;
    	}
        if ($("#jsDropzoneLogTA").length){
        	 $("#jsDropzoneLogTA").attr('readonly', 'readonly');
        }
		if( $(jsDivId).length){
			  var dropzone = new Dropzone(jsDivId, {
			  url: "dropzone",
			  method:"post",
			  paramName:"file",
			  autoProcessQueue:true,
			  //parallelUploads: 5,
			  parallelUploads: 1,
			  thumbnailHeight: 100,
			  thumbnailWidth: 100,
			  maxFilesize: 256,
			  filesizeBase: 1000,
			  addRemoveLinks: showRemoveLink,
			  acceptedFiles: acceptedFileList,
			  init: function() {
				//call before file send
			    this.on("sending", function(file, xhr, formData) {  
				    console.log('sending name:' + file.name +" file:"+file);
				    //append uuid to form post
                       formData.append("uuid", 'dropzone-' + file.upload.uuid);
                       formData.append("autoFiling", autoFiling);
			    });
				
					
			    this.on("success", function(file, responseText) {
				    console.log('success name:' + file.name +" file:"+ file+" response:" + responseText);
				    if (removeAfterUpload){
				    	this.removeFile(file);
				    }
				    if ( $("#jsDropzoneLogTA").length) {
			    		var newLineTag = "";
			    		if (responseText != null && !/\r|\n/.exec(responseText)){ //check str contain newline
			    			newLineTag = "\n";
			    		}
				    	$("#jsDropzoneLogTA").append("OK:\t"+responseText + newLineTag);
			    	  	$("#jsDropzoneLogTA").scrollTop($("#jsDropzoneLogTA")[0].scrollHeight);
				    }
			    });
			    this.on("error", function(file, responseText) {
			    	console.log("error name:" + file.name + "response:" + responseText);
				    if ( $("#jsDropzoneLogTA").length) {
			    		var newLineTag = "";
			    		if (responseText != null && !/\r|\n/.exec(responseText)){ //check str contain newline
			    			newLineTag = "\n";
			    		}
				    	$("#jsDropzoneLogTA").append("FAIL:\t"+ responseText + newLineTag);
			    	  	$("#jsDropzoneLogTA").scrollTop($("#jsDropzoneLogTA")[0].scrollHeight);
				    }
			    });
		        
			    this.on("complete", function(file) {
				    console.log('complete name:' + file.name +" file:"+ file);
           			var zkComp = zk.Widget.$('$zkDropzone');
					if (zkComp !== null && zAu !== null && zk !== null){ 
						zAu.send(new zk.Event(zkComp, "onDropzoneAdd", 'dropzone-' + file.upload.uuid, {toServer:true}));
					}
			    });
		        
		        //call when file removed
			    this.on("removedfile", function(file){
				    console.log('removedfile name:' + file.name +" file:"+file);
           			var zkComp = zk.Widget.$('$zkDropzone');
					if (zkComp !== null && zAu !== null && zk !== null){ 
						zAu.send(new zk.Event(zkComp, "onDropzoneDelete", 'dropzone-' + file.upload.uuid, {toServer:true}));
					}
			  	});
			  }
			});
		  	$(jsDivId).data('dropzone', dropzone);
		}
	}
	function closePdfDialog(p_dlgId) {
      	var zkComp = zk.Widget.$('$' + p_dlgId);
  		if (zkComp !== null && zAu !== null && zk !== null)
    		zAu.send(new zk.Event(zkComp, "onCancel", null, {toServer:true}));
      	else
    	  	console.log("comp or zAu is null, skip register event");
	}
	function embedPdfObject(p_link, p_container, p_dlgId) {
		if (! jq('$' + p_container).length){
			console.log("not exist:" + p_container);
			return;
		}
		var $pdf = $('#' + jq('$' + p_container).attr('id')); 
		var options = {
			pdfOpenParams: {
    			pageMode: 'none',
    			scaleValue: 'page-fit',
    			disableFullscreen: true,
				disableOpenFile: true,
				disableViewBookmark: true,
		    	disableDownload: true,
		    	disablePrinting: true,
		    	disableCloseDialog: typeof p_dlgId === 'undefined',
		    	closeDialogFun: typeof p_dlgId !== 'undefined' ? 'closePdfDialog("'+p_dlgId+'")' : null
			},
			forcePDFJS: true,
			PDFJS_URL: "js/pdfjs/web/viewer.html",
			fallbackLink: '<p>This browser does not support inline PDFs, please click this <a href="' + p_link + '">link</a> to download</p>'
		};
		PDFObject.embed(p_link, $pdf, options);
	}
	function showEditing(p_enable){
		if (p_enable !== 'undefined' && p_enable){
			$( ".zkbi-editing-msg" ).css("display","initial");
		}
		else{
			$( ".zkbi-editing-msg" ).css("display","none");
		}
	}
	function abmClick(p_parentId, p_btnId, p_liId){
		if (p_liId){
			var listItem = $('#' + p_liId);
			listItem.addClass("abm_item_busy");
			setTimeout(function() {
			    listItem.removeClass('abm_item_busy');
			}, 1000);
		}
       	var zkComp = zk.Widget.$('$' + p_parentId );
      	if (zkComp !== null && zAu !== null){
			zAu.send(new zk.Event(zkComp, "onABMClick", "" + p_btnId, {toServer:true}));
      	}
	}
	function abmHideToggle(){
		if ($( ".mfb-component--br" ).css("display") === "block"){
			$( ".mfb-component--br" ).css("display","none");
		}
		else{
			$( ".mfb-component--br" ).css("display","block");
		}
	}
	function abmHide(){
		$( ".mfb-component--br" ).css("display","none");
	}
	function testLargeData(){
		var cnt=0;
	  	//cnt=209700; //2M ok
		//cnt=209715; //fail. default post limitation maxPostSize:2097152 
		cnt=500000; //5M
		var text = [];
		for (i=0; i<cnt; i++){
			text.push( i % 2 == 0 ? "0000000000" : "1111111111");
		}
		var data = text.join("");
		console.log("len: " + data.length);
        zAu.send(new zk.Event(zk.Widget.$('$zkbibrowser') , 'onTestLargeData', data, {toServer:true}));
	}
	
	function loadCssOne(p_cssFile){
		console.log("load " + p_cssFile);
		var cssTag = document.createElement("link");
		cssTag.setAttribute("rel", "stylesheet");
		cssTag.setAttribute("type", "text/css");
		cssTag.setAttribute("href", p_cssFile);
		document.getElementsByTagName("head")[0].appendChild(cssTag);
	}
	function loadJsOne(p_jsFile){
		console.log("load " + p_jsFile);
		var jsElement = document.createElement('script');
		jsElement.src = p_jsFile;
		document.head.appendChild(jsElement);
	}
	function initPhotoSwipe(){
		if (initPhotoSwipe.called){ //avoid it call
			return;
		} 
		initPhotoSwipe.called = true;
		loadJsOne("js/photoswipe/photoswipe.min.js");
		//loadJsOne("js/photoswipe/photoswipe-ui-default.min.js");
		loadJsOne("js/photoswipe/photoswipe-ui-default.js");
		loadCssOne("js/photoswipe/photoswipe.css");
		loadCssOne("js/photoswipe/default-skin/default-skin.css");
	}
	function initSwiper(){
		if (initSwiper.called){ //avoid it call
			return;
		} 
		initSwiper.called = true;
		loadJsOne("js/swiper-4.5.1.min.js");
		loadCssOne("css/swiper-4.5.1.min.css");
		loadCssOne("css/swiper-common.css");
	}
	/*function showPhotoSwipe(){
		console.log("showPhotoSwipe called");  //prototype function, for testing only, need to rewrite
		initPhotoSwipe();
    	if (typeof PhotoSwipe === "undefined"){
    		 setTimeout(function() { showPhotoSwipe() }, 100);
    		 return;
    	}
		var pswpElement = document.querySelectorAll('.pswp')[0];
		
		//photo should obtain from parameter
		var items = [
		    {
		        src: 'https://placekitten.com/600/400',
		        w: 600,
		        h: 400
		    },
		    {
		        src: 'https://placekitten.com/1200/900',
		        w: 1200,
		        h: 900
		    }
		];
		var options = {
		    index: 0
		};

		var gallery = new PhotoSwipe( pswpElement, PhotoSwipeUI_Default, items, options);
		gallery.init();
	}*/
	function showPhotoSwipe(swiperSelector, gallerySelector, pswpSelector, inputItems){
		console.log("showPhotoSwipe called");
		initSwiper();
		initPhotoSwipe();
    	if (typeof Swiper === "undefined" || typeof PhotoSwipe === "undefined"){
    		setTimeout(function() { showPhotoSwipe(swiperSelector, gallerySelector, pswpSelector, inputItems) }, 100);
    		return;
    	}
		/* 1 of 2 : SWIPER ################################### */
    	$.each(inputItems, function(i, item){
	      	var imgSize = item["thumbnailDataSize"].split("x");
    		var $li = $('<li class="swiper-slide" itemprop="associatedMedia" itemscope itemtype="http://schema.org/ImageObject"></li>')
    		var $a = $('<a title="click to zoom-in" href="'+item.photoSrc+'" itemprop="contentUrl" data-size="'+item.photoDataSize+'"></a>');
    		var $img = $('<img src="'+item.thumbnailSrc+'" itemprop="thumbnail" alt="Image description xxx" />');
    		$a.append($img);
    		$li.append($a);
    		$(gallerySelector).append($li);
    		var imgWidth = parseInt(imgSize[0]);
    		var imgHeight = parseInt(imgSize[1]);
    		var rw = $li.width() / imgWidth;
    		var rh = $li.height() / imgHeight;
    		if (rw <= rh) {
    			$img.css('width', '100%');
    			$img.css('height', 'auto');
    		} else {
    			$img.css('height', '100%');
    			$img.css('width', 'auto');
    		}
    	});
		var mySwiper = new Swiper(swiperSelector, {
			// If swiper loop is true set photoswipe counterEl: false (line 175 her)
			loop: true,
			/* slidesPerView || auto - if you want to set width by css like flickity.js layout - in this case width:80% by CSS */
			slidesPerView: "auto",
			spaceBetween: 7,
			centeredSlides: true,
			// If we need pagination
			pagination: {
				el: ".swiper-pagination",
			    clickable: true,
			    renderBullet: function(index, className) {
			      return '<span class="' + className + '">' + (index + 1) + "</span>";
			    }
			},
		   	// Navigation arrows
			navigation: {
			    nextEl: '.swiper-button-next',
			    prevEl: '.swiper-button-prev',
			}
		});

		// 2 of 2 : PHOTOSWIPE #######################################
	  	// parse slide data (url, title, size ...) from DOM elements
		// (children of gallerySelector)
		var parseThumbnailElements = function(el) {
	    	var thumbElements = el.childNodes,
	      		numNodes = thumbElements.length,
	      		items = [],
	      		figureEl,
	      		linkEl,
	      		size,
	      		item;
	    	for (var i = 0; i < numNodes; i++) {
	      		figureEl = thumbElements[i]; // <figure> element
		      	// include only element nodes
	      		if (figureEl.nodeType !== 1) {
	        		continue;
	      		}
	      		linkEl = figureEl.children[0]; // <a> element
		      	size = linkEl.getAttribute("data-size").split("x");
		      	// create slide object
		      	item = {
		      	  	src: linkEl.getAttribute("href"),
		      	  	w: parseInt(size[0], 10),
		      	  	h: parseInt(size[1], 10)
		      	};
		      	if (figureEl.children.length > 1) {
		      	  	// <figcaption> content
		      	  	item.title = figureEl.children[1].innerHTML;
		      	}
		      	if (linkEl.children.length > 0) {
		      	  	// <img> thumbnail element, retrieving thumbnail url
		      	  	item.msrc = linkEl.children[0].getAttribute("src");
		      	}
		      	item.el = figureEl; // save link to element for getThumbBoundsFn
		      	items.push(item);
	    	}
	    	return items;
	  	};

  		// find nearest parent element
  		var closest = function closest(el, fn) {
    		return el && (fn(el) ? el : closest(el.parentNode, fn));
  		};

	  	// triggers when user clicks on thumbnail
		var onThumbnailsClick = function(e) {
	    	e = e || window.event;
	    	e.preventDefault ? e.preventDefault() : (e.returnValue = false);
	    	var eTarget = e.target || e.srcElement;
	    	// find root element of slide
	    	var clickedListItem = closest(eTarget, function(el) {
	    	  	return el.tagName && el.tagName.toUpperCase() === "LI";
	    	});
	    	if (!clickedListItem) {
	    	  	return;
	    	}
	    	// find index of clicked item by looping through all child nodes
	    	// alternatively, you may define index via data- attribute
	    	var clickedGallery = clickedListItem.parentNode,
	    	  	childNodes = clickedListItem.parentNode.childNodes,
		    	numChildNodes = childNodes.length,
		    	nodeIndex = 0,
		    	index;
	    	for (var i = 0; i < numChildNodes; i++) {
				if (childNodes[i].nodeType !== 1) {
					continue;
				}
				if (childNodes[i] === clickedListItem) {
					index = nodeIndex;
		    	    break;
	    	  	}
	    	  	nodeIndex++;
	    	}
	    	if (index >= 0) {
	    	  	// open PhotoSwipe if valid index found
	    	  	openPhotoSwipe(index, clickedGallery);
	    	}
	    	return false;
	  	};
  		// parse picture index and gallery index from URL (#&pid=1&gid=2)
	  	var photoswipeParseHash = function() {
    		var hash = window.location.hash.substring(1),
	    	  	params = {};
	    	if (hash.length < 5) {
	      		return params;
	    	}
	    	var vars = hash.split("&");
	    	for (var i = 0; i < vars.length; i++) {
	    	  	if (!vars[i]) {
	    	    	continue;
	    	  	}
	    	  	var pair = vars[i].split("=");
	    	  	if (pair.length < 2) {
	    	  	  	continue;
	    	  	}
	    	  	params[pair[0]] = pair[1];
	    	}
	    	if (params.gid) {
	    	  	params.gid = parseInt(params.gid, 10);
	    	}
	    	return params;
	  	};

  		var openPhotoSwipe = function(index, galleryElement, disableAnimation, fromURL) {
    		var pswpElement = document.querySelectorAll(pswpSelector)[0], gallery, options, items;
		    items = parseThumbnailElements(galleryElement);
    		// define options (if needed)
    		options = {
      			/* "showHideOpacity" uncomment this If dimensions of your small thumbnail don't match dimensions of large image */
      			//showHideOpacity:true,
      			// Buttons/elements
      			closeEl: true,
      			captionEl: true,
      			fullscreenEl: true,
      			zoomEl: true,
      			shareEl: true,
      			counterEl: false,
      			arrowEl: true,
      			preloaderEl: true,
      			// define gallery index (for URL)
      			galleryUID: galleryElement.getAttribute("data-pswp-uid"),
      			getThumbBoundsFn: function(index) {
        			// See Options -> getThumbBoundsFn section of documentation for more info
        			var thumbnail = items[index].el.getElementsByTagName("img")[0], // find thumbnail
          				pageYScroll = window.pageYOffset || document.documentElement.scrollTop,
          				rect = thumbnail.getBoundingClientRect();
        			return { x: rect.left, y: rect.top + pageYScroll, w: rect.width };
      			}
    		};
    		// PhotoSwipe opened from URL
    		if (fromURL) {
      			if (options.galleryPIDs) {
        			// parse real index when custom PIDs are used
        			// http://photoswipe.com/documentation/faq.html#custom-pid-in-url
        			for (var j = 0; j < items.length; j++) {
          				if (items[j].pid == index) {
            				options.index = j;
            				break;
          				}
        			}
      			} else {
        			// in URL indexes start from 1
        			options.index = parseInt(index, 10) - 1;
      			}
    		} else {
      			options.index = parseInt(index, 10);
    		}
    		// exit if index not found
    		if (isNaN(options.index)) {
      			return;
    		}
		    if (disableAnimation) {
      			options.showAnimationDuration = 0;
    		}
    		// Pass data to PhotoSwipe and initialize it
    		gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);
    		/* EXTRA CODE (NOT FROM THE CORE) - UPDATE SWIPER POSITION TO THE CURRENT ZOOM_IN IMAGE (BETTER UI) */
    		// photoswipe event: Gallery unbinds events
    		// (triggers before closing animation)
    		gallery.listen("unbindEvents", function() {
      			// This is index of current photoswipe slide
      			var getCurrentIndex = gallery.getCurrentIndex();
      			// Update position of the slider
      			mySwiper.slideTo(getCurrentIndex, false);
    		});
    		/*// create variable that will store real size of viewport
    		var realViewportWidth,
    		    useLargeImages = false,
    		    firstResize = true,
    		    imageSrcWillChange;
    		// beforeResize event fires each time size of gallery viewport updates
    		gallery.listen('beforeResize', function() {
    			console.log('beforeResize ' + gallery.getCurrentIndex() + ',' + gallery.viewportSize.x + ',' + gallery.viewportSize.y + ',' + window.devicePixelRatio);
    		});
    		// gettingData event fires each time PhotoSwipe retrieves image source & size
    		gallery.listen('gettingData', function(index, item) {
    			console.log('gettingData ' + index + ',' + item);
    		});*/

    		gallery.init();
  		};

  		// loop through all gallery elements and bind events
		var galleryElements = document.querySelectorAll(gallerySelector);
		for (var i = 0, l = galleryElements.length; i < l; i++) {
		    galleryElements[i].setAttribute("data-pswp-uid", i + 1);
		    galleryElements[i].onclick = onThumbnailsClick;
		}
		// Parse URL and open gallery if it contains #&pid=3&gid=1
		var hashData = photoswipeParseHash();
		if (hashData.pid && hashData.gid) {
		    openPhotoSwipe(hashData.pid, galleryElements[hashData.gid - 1], true, true);
  		}
	}

	function showTuiImageEditor(editorSelector, imageSrc, imageSelector){
		const myTheme = {
			"header.display": "none"
			/*"common.bisize.width": "0px",
		  	"common.bisize.height": "0px",
		  	"loadButton.display":"None", 
		  	"downloadButton.display":"None"*/
		};
		const menuList = ['draw', 'shape', 'text'];
		const isLandBody = document.body.clientWidth >= document.body.clientHeight;
		var cssMaxWidth, cssMaxHeight;
		if ($.support.touch)
            cssMaxWidth = $(editorSelector).width() - (isLandBody ? 64 : 0);
		else
            cssMaxWidth = $(editorSelector).width() - (isLandBody ? 260 : 0);
        cssMaxHeight = Number.MAX_VALUE;
        const imageEditor = new tui.ImageEditor(editorSelector, {
            includeUI: {
				loadImage: {
					path: imageSrc,
                    name: 'SampleImage'
                },
               	//theme: blackTheme, // or whiteTheme
                theme: myTheme,
                menu: menuList,
                initMenu: $.support.touch ? null : 'draw',
                menuBarPosition: isLandBody ? 'left' : 'bottom'
            },
            cssMaxWidth: cssMaxWidth,
            cssMaxHeight: cssMaxHeight,
            usageStatistics: false
        });
        /*window.onresize = function() {
            imageEditor.ui.resizeEditor();
        }*/
        //if ($.support.touch)
        //	new IEditorPanZoom(imageEditor).enable(editorSelector);

        const $editMenu = $(editorSelector).find('.tui-image-editor-menu');
		const $helpMenu = $(editorSelector).find('.tui-image-editor-help-menu');
		const $editorCanvasContainer = $(editorSelector).find('.tui-image-editor-wrap .tui-image-editor-canvas-container');

		$helpMenu.find('.tui-image-editor-item')
				.filter('.tie-btn-zoomIn,.tie-btn-zoomOut,.tie-btn-hand,.tie-btn-history,.tie-btn-delete,.tie-btn-deleteAll,:not(.help)').hide();
		
		if (isLandBody) {
			$helpMenu.removeClass('right');
			$helpMenu.addClass('left');
			$helpMenu.height('130px');
			$helpMenu.css({ 'top': 'calc(50% - 60px)', 'z-index' : '99', 'background-color' : 'transparent' });
			$editMenu.css('padding-top', '150px');
			$(editorSelector).find('.tui-image-editor-main-container .tui-image-editor-main').css('top', '0');
			$(editorSelector).find('.tui-image-editor-main .tui-image-editor-submenu').css('width', 'auto');
		}
		else {
			$helpMenu.removeClass('top');
			$helpMenu.addClass('bottom');
			$helpMenu.width('156px');
			$helpMenu.css({ 'left': 'calc(50% - 80px)', 'z-index' : '99', 'background-color' : 'transparent' });
			$editMenu.css('padding-left', '180px');
			$(editorSelector).find('.tui-image-editor-main-container .tui-image-editor-main').css('top', '0');
			$(editorSelector).find('.tui-image-editor-main .tui-image-editor-submenu').css('height', 'auto');
			$(editorSelector).find('.tui-image-editor-main .tui-image-editor-submenu>div').css('padding-bottom', '0');
			$(editorSelector).find('.tui-image-editor').css('padding-bottom', '91px');
		}
		$(editorSelector).find('.tui-image-editor-main .tui-image-editor-submenu .tui-image-editor-submenu-style').css('opacity', '.90');
		
		if ($.support.touch) {
			//handle scroll event for mobile device
			const $maskDiv = $('<div style="position:absolute;width:100%;height:100%;"></div>');
			$maskDiv.appendTo($editorCanvasContainer);
        	setTimeout(function() {
        		for (var i in menuList) {
        			const menuName = menuList[i];
					imageEditor.ui._buttonElements[menuName].addEventListener('click', function() {
						if (imageEditor.ui.submenu)
							$maskDiv.hide();
						else
							$maskDiv.show();
					});
        		}
        	}, 150);
		}


        if (imageSelector) {
        	jq(imageSelector).data('setTuiImageSrcFunc', function(){
        		jq(imageSelector).attr('src', imageEditor.toDataURL({format: 'jpeg'}));
        	});
        	jq(imageSelector).data('toggleOffAllButtonFunc', function(){
        		for (var i in menuList) {
        			const menuName = menuList[i];
					if (imageEditor.ui.submenu === menuName) {
						$(imageEditor.ui._buttonElements[menuName]).click();
						break;
					}
        		}
        	});
        }
	}

	//firebase cloud messaging
	function obtainFCMToken() {
		console.log('obtainFCMToken called');
		try {
			if (typeof android !== "undefined") {
				//android
				android.obtainFCMToken();
			} 
			else if (typeof webkit !== "undefined") {
				//ios
				webkit.messageHandlers.callbackHandler.postMessage("obtainFCMToken");
			} 
			else {
				//web
			}
		} 
		catch (err){
			debugLog('error:' + err)
		}
	}
	function obtainFCMTokenReturn(p_json) {
		console.log('obtainFCMTokenReturn called:' + p_json);
		//send back the json to server side using event or broadcast
	}
	
	/*
	//send desktop event
	//obsoleted, replaced by root component event
	function zkbiSend(evname, evdata){
		//console.log('dtid:' + zk.Desktop.$().id + " zAu:" +zAu); //obtain dtid
		if (!zAu || !zk) {
			console.log("zk not available");
			return;
		}
		if (!evdata) {
			evdata = {};
		}
		evdata.dtid = zk.Desktop.$().id;
		evdata.bwid = getBrowserWinId();
		zAu.send(new zk.Event(zk.Desktop.$(), evname, evdata));
	}
	*/
	function zkbiSend(evname, evdata){
		//console.log('dtid:' + zk.Desktop.$().id + " zAu:" +zAu); //obtain dtid
		if (!zAu || !zk) {
			console.log("zk not available");
			return;
		}
		const mainComp = getMainComp();
		if (!mainComp){
			console.log('mainComp is null, skip ' + evname);
			return;
		}
		if (!evdata) {
			evdata = {};
		}
		evdata.dtid = zk.Desktop.$().id;
		evdata.bwid = getBrowserWinId();
		zAu.send(new zk.Event(mainComp, evname, evdata));
	}

	//idle control
	let zkbiIdleCtrl = (function() {
		const fDebug = false;
		var intervalDur = 60000; //60s
		var idleCnt = 0;
		var maxIdle = 3600000; //1hr
		var idleFn = null;
		var idleTimerHandle = null;
		var eventCnt = 0;
		var setIdleCntBcFn = null;
		
		let init = function(p_maxIdle, p_interval, p_idleFn, p_setIdleCntBcFn) {
			if (fDebug) console.log('zkbiIdleCtrl init:%d %d', p_maxIdle, p_interval);
			idleCnt = 0;
			eventCnt = 0;
			
			if (p_maxIdle){
				maxIdle = p_maxIdle;
			}
			if (p_interval) {
				intervalDur = p_interval < 1000 ? 1000 : p_interval;
			}
			idleFn = p_idleFn;
			setIdleCntBcFn = p_setIdleCntBcFn;
			start();
		}
		let stop = function(){
			if (fDebug) console.log('zkbiIdleCtrl stop');
			if (idleTimerHandle){
				clearInterval(idleTimerHandle);
			}
			$(document).off('mousemove.zkbi');
			$(document).off('keypress.zkbi');
		}
		let start = function(){
			if (!idleFn){
				console.log('init first');
				return;
			}
			stop();
			if (fDebug) console.log('zkbiIdleCtrl start');
			idleTimerHandle = setInterval(idleTimer, intervalDur);
			$(document).on('mousemove.zkbi', function(event) {
				eventCnt++;
			});
			$(document).on('keypress.zkbi', function(event) {
				eventCnt++
			});
		}
		let setIdleCnt = function(p_idleCnt){
			if (fDebug) console.log('zkbiIdleCtrl setIdleCnt:' + p_idleCnt);
			if (typeof p_idleCnt !== 'number') {
				console.log('invalid idleCnt, set as default');
				idleCnt = 0;
			}
			else{
				idleCnt = p_idleCnt;
			}
		}
		let idleTimer = function() {
			if (fDebug) console.log('idleTimer idleCnt:%d eventCnt:%d', idleCnt, eventCnt);
			if (eventCnt > 0){
				try{
					if (typeof setIdleCntBcFn === 'function'){
						setIdleCntBcFn();
					}
				}
				catch(err) {
					console.log(err);
				}
				idleCnt = 0;
				eventCnt = 0;
				return;
			}
			idleCnt++;
			if ((idleCnt * intervalDur) >= maxIdle) {
				console.log('trigger idleFn');
				if (idleFn) {
					idleFn();
				}
				idleCnt = 0;
			}
		}
		return { init:init, setIdleCnt:setIdleCnt, stop:stop, start:start };
	})();
	
	
	var zkbiBc = (function() {
		const fDebug = false;
		var channel = null;
		var mode = null;
		var bcType = null;
		var bcUUID = genUUID();
		if (fDebug) {
			console.log('bcUUID=' + bcUUID);
		}
		function init() {
			if (bcType){
				console.log('already init');
				return;
			}
			try {
				console.log('init bc');
				channel = new BroadcastChannel('zkbiBc');
				channel.onmessage = receive;
				bcType = 'ch';
				if (fDebug) console.log('init ok: bcType:%s', bcType);
				return true;
			} catch (err) {
				channel = null;
				console.log('bc not supported:' + err);
			}
			
			//localStorage polyfill
			try{
				localStorage.setItem('zkbimsg', '');
		        localStorage.removeItem('zkbimsg');
		    	$(window).on('storage.zkbi', receive);
				bcType = 'ls';
				if (fDebug) console.log('init ok: bcType:%s', bcType);
		    	return true;
			}
			catch (err){
				console.log('storage no supported:' + err);
			}
			return false;
		}
		function genUUID(){
			return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {var r = Math.random()*16|0,v=c=='x'?r:r&0x3|0x8;return v.toString(16);});
		}
		function receive(ev, p_selfExe) {
			try {
				if (fDebug) console.log('receive called: bctype:%s', bcType);
				if (!ev){
					console.log('invalid event');
					return;
				}
				var jsonStr = null;
				if (bcType == 'ch' && ev.data) { //for channel
					if (fDebug) console.log('receive: ev:%s ev.data:%s',  ev, ev.data);
					if (ev.data){
						jsonStr = ev.data;
					}
					else{
						if (fDebug){
							console.log('no data, action ignore');
						}
						return;
					}
				}
				else if (bcType == 'ls'){
					if (!ev.originalEvent) {
						return;
					}
					if (fDebug) {
						console.log('receive: key:%s newValue:%s data:%s',  ev.originalEvent.key, ev.originalEvent.newValue, ev.data);
					}
					if (ev.originalEvent.key == 'zkbimsg' && ev.originalEvent.newValue){ //for storage
						jsonStr = ev.originalEvent.newValue;
					}
					else{
						console.log('no data, action ignore');
						return; 
					}
				}
				else if (ev.data){
					jsonStr = ev.data;
				}
				if (!jsonStr){
					console.log('invalid jsonStr');
					return;
				}
				if (fDebug) console.log('jsonStr:%s', jsonStr);
				var json = JSON.parse(jsonStr);
				if (fDebug){
					console.log('action:%s sameUUID:%s selfExe:%s', json.action, (json.senderUUID == bcUUID), p_selfExe);
				}
				
				//200824: avoid safari send bc msg to it own window. fix fail to set default homepage. 
				if (json.senderUUID == bcUUID && p_selfExe == undefined && !p_selfExe){
					return;
				}
				if (json.senderFullContextPath != getFullContextPath()){
					if (fDebug) console.log('ignore cross context bcmsg');
					return;
				}
				if (fDebug) console.log('process same context bcmsg');
				
				switch (json.action) {
				case 'redirect':
					//changeUrl(json.data)
					changeUrl(appendURLParam(json.data,'selfexe=' + (p_selfExe ? 'Y' : 'N')));
					break;
				case 'redirectWithHistory':
					if (!json.data) {
						console.log('skip redirect');
						return;
					}
					changeUrl(json.data, true)
					break;
				case 'setIdleCnt':
					try{
						zkbiIdleCtrl.setIdleCnt(json.data);
					}
					catch(err){
						console.log(err);
					}
					break;
				case 'reloadCurrent':
					reloadCurrent(true);
					break;
				case 'reloadCurrentNonActive':
					if (fDebug){
						console.log('hasFocus:%s', document.hasFocus());
					}
					if (!document.hasFocus()){
						reloadCurrent(true);
					}
					break;
				default:
					console.log('action not supported:' + json.action);
				}
			} catch (err) {
				console.log(err);
			}
		}
		function close() {
			if (bcType == 'ch'){
				if (!channel){
					console.log('already close');
					return;
				}
				console.log('close bc');
				channel.close();
				channel = null;
			}
			else if (bcType == 'ls'){
				$(window).off('storage.zkbi');
			}
			bcType = null;
		}
		function isValid(){
			return bcType == null ? false : true;
		}
		function send(p_json, p_selfExe) {
			//allow to fire selfexe even channel not available
			if (bcType == null && !p_selfExe) {
				return;
			}
			
			if (!p_json) {
				console.log('no data');
				return;
			}
			p_json.senderUUID = bcUUID;
			p_json.senderFullContextPath = getFullContextPath();
			var jsonStr = JSON.stringify(p_json);
			
			try {
				if (bcType == 'ch') {
					if (fDebug) console.log('send bcType:%s data:%s', bcType, jsonStr);
					channel.postMessage(jsonStr);
				} 
				else if (bcType == 'ls') {
					if (fDebug) console.log('send bcType:%s data:%s', bcType, jsonStr);
		 			localStorage.setItem('zkbimsg', jsonStr);
		 			localStorage.removeItem('zkbimsg');
				}
				else {
					console.log('bc not available. skip send');
				}
			} catch (err) {
				console.log('send error:' + err);
			}
			if (p_selfExe) {
				console.log('selfexec');
    			//setTimeout(function() { receive({ data : jsonStr }); }, 100);  //andrew200617 tmp fix, active window execute last
    			
    			//simulate a receive event
    			var msg = {};
    			if (bcType == 'ch' || bcType == null){
    				msg.data = jsonStr;
					receive(msg,true);
    			}
    			else if (bcType == 'ls'){
    				msg = {};
    				msg.originalEvent = {};
    				msg.originalEvent.key = 'zkbimsg';
    				msg.originalEvent.newValue = jsonStr;
					receive(msg,true);
    			}
    			else{
    				console.log('bcType:%s not supported', bcType);
    			}
    			
			}
		}
		return {
			init : init,
			send : send,
			close : close,
			isValid : isValid,
		};
	})();
<%
	if (sessionHelper.getAllowJSBroadcastChannel()) {
%>		
		zkbiBc.init();
<%
	}
%>		
	function reloadCurrent(p_force) {
		try{
			if (p_force){
				window.onbeforeunload = null; //clear editing block
			}
			location.reload();
		}
		catch (err){
			console.log('err:' + err);
		}
	}
	function changeUrl(url, withHistory) {
		if (!url) {
			console.log('no url');
			return;
		}
		window.onbeforeunload = null; //clear editing block
		if (!withHistory) {
			//alert(url);
			try {
				window.location.replace(url);
				return;
			} catch (err) {
			}
		}
		try {
			//when withHistory or replace not supported
			window.location.href = url;
			return;
		} catch (err) {
		}
		try {
			//for support edge
			window.location = url;
			return;
		} catch (err) {
			console.log('err:' + err);
		}
	}

	//andrew200616 hotfix cannot eval goBack 
	function goBack() {
	    window.history.back();
	}
	
	
	var zkbis2 = (function() {
		let setup = function(p_uuid, p_multiple, p_tags, p_placeholder, p_allowClear, p_allowListenResizeEvent) {
			//console.log('setup:'+ p_uuid +" comp:" + zk.Widget.$('#' + p_uuid) + " id:" + (zk.Widget.$('#' + p_uuid) == null ? "na" : zk.Widget.$('#' + p_uuid).id) );
   			//const comp = zk.Widget.$('#' + p_uuid);  //210511 pass const comp obj to inner function will trigger Cannot read property 'desktop' of null bug. probably a zk bug
   			const selectEvent = function(e) {
				//console.log('selectevent:'+ p_uuid +" comp:" + zk.Widget.$('#' + p_uuid) + " id:" + (zk.Widget.$('#' + p_uuid) == null ? "na" : zk.Widget.$('#' + p_uuid).id) );
   				const comp = zk.Widget.$('#' + p_uuid);
				const selectedItemIds = [];
				const removeItemIds = [];
				const tagItemValues = [];
				$(e.target).find('option').each(function(i, opt){
					if (opt.selected) {
						if (opt.id)
							selectedItemIds.push(opt.id);
						else {
							tagItemValues.push(opt.value);
							$(opt).remove();
						}
					}
					else if ($(opt).attr('data-select2-tag') && opt.id)
						removeItemIds.push(opt.id);
				});
				zAu.send(new zk.Event(comp, 'onSelect2Select', {listboxId: comp.id, selectedItemIds: selectedItemIds, removeItemIds: removeItemIds, tagItemValues: tagItemValues}, {toServer:true}));
   			};
   			opts = {};
   			if (typeof p_multiple !== "undefined"){
   				opts.multiple = p_multiple;
			}
   			if (typeof p_tags !== "undefined"){
   				opts.tags = p_tags;
			}
    		if (typeof p_placeholder !== "undefined" && p_placeholder){
   				opts.placeholder = p_placeholder;
    		}
    		if (typeof p_allowClear !== "undefined"){
    			opts.allowClear = p_allowClear;
    		}
    		
    		//change the matcher
    		opts.matcher = customMatcher;
    		
    		//console.log(opts);
    		var $uuid = $('#'+p_uuid);
    		if ($uuid.data('setupSelect2Status') == 'Y') {
    			if ($uuid.data('resizeObserver') && $uuid.data('observeResizeElement'))
    				$uuid.data('resizeObserver').unobserve($uuid.data('observeResizeElement'));
				$uuid.select2('destroy')
					.off('select2:open')
					.off('select2:close')
					.off('select2:select')
					.off('select2:unselect')
					.off('select2:clear')
					.off('change')
					.data('setupSelect2Status', '');
    		}
			$uuid.select2(opts)
						.on('select2:open', function(e){
							console.log('open', e);
						})
						.on('select2:close', function(e){
							console.log('close', e);
						})
						.on('select2:select', function(e){
							console.log('select', e);
							selectEvent(e);
						})
						.on('select2:unselect', function(e){
							console.log('unselect', e);
							selectEvent(e);
						})
						.on('select2:clear', function(e){
							console.log('clear', e);
						})
						.on('change', function(e){
							console.log('change', e);
						})
						.data('setupSelect2Status', 'Y')
						;

			//handle select2 resize event
    		if (p_allowListenResizeEvent !== "undefined" && p_allowListenResizeEvent && typeof ResizeObserver === 'function') {
				const resizeObserver = new ResizeObserver(function(entries) {
					for (var i in entries) {
						var entry = entries[i];
						if ($(entry.target).hasClass('select2')) {
            				var zkComp = zk.Widget.$('#' + p_uuid);
							if (zkComp !== null && zAu !== null && zk !== null)
								zAu.send(new zk.Event(zkComp, "onResize", {width: entry.contentRect.width, height: entry.contentRect.height}, {toServer:true}));
						}
					}
				});
				const observeEle = $uuid.closest('div').find('.select2')[0];
				resizeObserver.observe(observeEle);
				$uuid.data('resizeObserver', resizeObserver);
				$uuid.data('observeResizeElement', observeEle);
			}
		}
		let destroyAll = function() {
			$('select').each(function() {
    			if ($(this).data('select2')) {
					console.log('destroy', this);
    				$(this).select2('destroy');
    			}
			});
		};
		function customMatcher(params, data) {
		    params.term = params.term || '';
			//console.log('params.term:' + params.term);
			//console.log('data.text:' + data.text);
			
			//match from beginning
			if (params.term.startsWith("=")){
		    	if (data.text.toUpperCase().indexOf(params.term.substring(1).toUpperCase()) == 0) {
		    	    return data;
		    	}
			}
			else{
		    	if (data.text.toUpperCase().indexOf(params.term.toUpperCase()) >= 0) {
		    	    return data;
		    	}
			}
			return false;
		}	
		return { setup:setup, destroyAll:destroyAll };
		
	})();
	
	//change inputmode based on css class 
	function zkbiInputModeRefresh(p_delay){
	    //remark: numeric/decimal - not all device show minus key (including iphone), so don't apply the inputmode to all element
		//$("input.z-intbox").attr('inputmode','numeric');
		//$("input.z-decimalbox").attr('inputmode','decimal');
		//$("input.z-doublebox").attr('inputmode','decimal');
       	setTimeout(function(){ 
			$("input.zkbi-im-decimal").attr('inputmode','decimal');
			$("input.zkbi-im-numeric").attr('inputmode','numeric');
			$("input.zkbi-im-none").attr('inputmode','none');
			$("input.zkbi-im-tel").attr('inputmode','tel');
			$("input.zkbi-im-search").attr('inputmode','search');
			$("input.zkbi-im-email").attr('inputmode','email');
			$("input.zkbi-im-url").attr('inputmode','url');
			$("span.zkbi-im-search.z-bandbox > input").attr('inputmode','search');
       		}, p_delay ? p_delay : 0);
	}
	
	//gen unique component id
	var zkbiUID=0;
	function zkbiGetUID(p_prefix){
		zkbiUID++;
		var uid = 'zkbiuid-' + zkbiUID;
		if (p_prefix){
			return p_prefix +'-' + uid;
		}
		else{
			return uid;
		}
	}
	
	//TODO: allow user to arrange the window
	//TODO: allow window resize
	var zkbiIframeIdx=0;
	function zkbiOpenIframe(p_title, p_url, p_width, p_height){
		const minimizedWidth = 120;
		const defaultXOffset = 30;
		const defaultYOffset = 30;
		var defaultWinWidth = 600;
		var defaultWinHeight = 400;
		if (p_width) defaultWinWidth = p_width;
		if (p_height) defaultWinHeight = p_height;
		var bodyWidth = $('body').outerWidth();
		var bodyHeight = $('body').outerHeight();

		var winId = 'zkbi-iframe-' + zkbiIframeIdx++;
		var winWidth = (defaultWinWidth > bodyWidth) ? bodyWidth : defaultWinWidth;
		var winHeight = (defaultWinHeight > bodyHeight) ? bodyHeight : defaultWinHeight;
		var posRight = (bodyWidth > winWidth) ? ((50 + (defaultXOffset * zkbiIframeIdx)) % (bodyWidth - winWidth)) : 0;
		var posBottom = (bodyHeight > winHeight) ? ((50 + (defaultYOffset * zkbiIframeIdx)) % (bodyHeight - winHeight)) : 0;
		
		var curWin = $("<div class='zkbi-iframe' style='position: fixed;z-index:1001;" +
						"display:flex;display:-webkit-flex;flex-direction:column;-webkit-flex-direction:column;" +
						"'></div>").attr('id', winId)
						//.css('bottom',posBottom).css('right',posRight).css('width',winWidth).css('height',winHeight);
						.css('top',bodyHeight-posBottom-winHeight).css('left',bodyWidth-posRight-winWidth).css('width',winWidth).css('height',winHeight);
		console.log('id=' + curWin.attr('id'));
		curWin.append(
			'<div class="z-window" style="flex-shrink:0;-webkit-flex-shrink:0;border-top-left-radius:5px !important; border-top-right-radius:5px !important; width:100%; background:green; border:0px !important">' +
			'<span class="zkbi-iftitle" style="overflow:hidden;color:#FFF !important; padding-left:4px !important;float:left">'+p_title+'</span>' +
			'<span title="close" class="zkbi-ifbtn-close" style="margin-left:2px; cursor:pointer; color:#FFF !important; float:right; padding-right:5px !important;"><i class="fa fa-times" style="margin-top:4px"></i></span>' +
			'<span title="maximize" class="zkbi-ifbtn-max" style="margin-left:2px; cursor:pointer; color:#FFF !important; float:right; padding-right:5px !important;"><i class="fa fa-expand" style="margin-top:4px"></i></span>' +
			'<span title="minimize" class="zkbi-ifbtn-min" style="margin-left:2px; cursor:pointer; color:#FFF !important; float:right; padding-right:5px !important;"><i class="fa fa-minus" style="margin-top:8px"></i></span>' +
		    '</div>' +
			'<div style="flex-grow:1;-webkit-flex-grow:1;">' +
			'<div class="zkbi-ifmask" style="display:none;position:absolute;left:0;top:0;right:0;bottom:0"></div>' +
			'<iframe style="width:100%;height:100%;border:0px;border-bottom-left-radius:5px;border-bottom-right-radius:5px;" src="'+ p_url +'"></iframe>' +
		    '</div>'
		);
		$("body").append(curWin);

		var collectAllWindows = function(isSortDesc) {
			var oWins = [];
			var j = 0;
	    	for (var i = 0; i < zkbiIframeIdx; i++) {
	    		var win = $('#zkbi-iframe-' + i);
				if (win.length > 0)
					oWins.push({index: j++, win: win, zindex: win.css('z-index')});
	    	}
	    	oWins = oWins.sort(function(l, r) {
	    		return ((l.zindex == r.zindex) ? (l.index - r.index) : (l.zindex - r.zindex)) * (isSortDesc ? -1 : 1);
    		});
	    	var wins = [];
	    	$.each(oWins, function(i, o) {
				wins.push(o.win);
	    	});
	    	return wins;
		};

		var showAllWindowsMask = function(isShow) {
			$.each(collectAllWindows(false), function(i, win){
				var $ifmask = win.find('.zkbi-ifmask');
				if (isShow)
					$ifmask.show();
				else
					$ifmask.hide();
			});
		}

    	//Bring window to front when click title bar
    	var bringWindowToFront = function() {
	    	var curZIndex = curWin.css('z-index');
	    	var otherZIndex = 0;
	    	for (var i = 0; i < zkbiIframeIdx; i++) {
	    		var id = 'zkbi-iframe-' + i;
	    		var $id = $('#' + id);
				if (id != winId && $id.length > 0)
					otherZIndex = Math.max(otherZIndex, $id.css('z-index'));
	    	}
	    	if (curZIndex <= otherZIndex)
				curWin.css('z-index', otherZIndex + 1);
    	};
	    curWin.find('.z-window').click(function(e){
	    	console.log('z-window click');
			bringWindowToFront();
	    });
		$(iframeWindow).focus(function(){
	    	console.log('iframewindow focus');
			bringWindowToFront();
		});
		bringWindowToFront();
		
		//drag
	    curWin.draggable({
			iframeFix: false,
			opacity: 0.5,
			stack: '.zkbi-iframe',
			'start':function(event,ui){
				$(this).css({'right':'auto','bottom':'auto'});
				showAllWindowsMask(true);
			},
	    	'stop':function(){
				showAllWindowsMask(false);
	    	}
	    });

		//resize
	    curWin.resizable({
	        handles: "all",
	        autoHide: true,
    	  	'start':function(event,ui){
				showAllWindowsMask(true);
				bringWindowToFront();
    		},
	    	'stop':function(){
				showAllWindowsMask(false);
	    	}
	    });  //need jquery-ui.css

	    //message channel
	    var msgChannel = new MessageChannel();
	    var chConnected = false;
	    var chPort = msgChannel.port1;
	    chPort.onmessage = function(e){
	    	console.log('port1 receive message:' + e.data);
	    	switch (e.data) {
	    		case 'connectedIFrame':
					chConnected = true;
	    			break;
	    		case 'closeWindow':
	    			curWin.remove();
	    			break;
	    	}
        };
		var iframeWindow = curWin.find('iframe')[0].contentWindow;
		curWin.find('iframe').on('load', function(){
			//post message connect iframe
			iframeWindow.postMessage('connectIFrame','*',[msgChannel.port2]);
		});
	    curWin.find('.zkbi-ifbtn-close').click(function(){ 
	    	if (chConnected)
				chPort.postMessage('doClose');
	    	else
	    		$(this).parent().parent().remove();
    	});

	    //minimized button, maximized button, normal button
	    var lastPos = { newMode: 'normal', top: '', left: '', width: '', height: '', zindex: '', overflow: '', backToNormal: function(){
			//zkbiFreeMinSlot(curWin.data('minslot'));
    		curWin.draggable('enable');
    		curWin.resizable('enable');
    		curWin.find('.zkbi-iftitle').css('max-width','none');
    		lastPos.mode = lastPos.newMode;
    		lastPos.newMode = 'normal';
    		curWin.css({'top': lastPos.top, 'left': lastPos.left, 'width': lastPos.width, 'height': lastPos.height, /*'z-index': lastPos.zindex,*/ 'overflow': lastPos.overflow }	);
	    }, minimizeWindow: function() {
			var allWins = collectAllWindows(false);
			var winPoss = [];
			$.each(allWins, function(j, win){
				var winLastPos = win.data('lastPos');
				if (winLastPos.newMode == 'min')
					winPoss.push({right: win.css('right'), bottom: win.css('bottom'), width: win.width(), height: win.height()});
			});
			var bodyWidth = $('body').outerWidth();
			var width = minimizedWidth;
			var lastRight = 5 + (width + 1);
			var right;
			for (var i = 0; ; i++) {
				right = 5 + (width + 1) * i;
				if (right + width > bodyWidth) {
					right = lastRight;
					break;
				}
				var flag = false;
				$.each(winPoss, function(j, pos){
					var r = parseInt(pos.right);
					if (right < r + pos.width && right + width > r) {
						flag = true;
						return false;
					}
				});
				if (!flag)
					break;
				lastRight = right;
			}
			//var minSlot = zkbiGetMinSlot();
			//curWin.data('minslot',minSlot);
    		curWin.draggable( 'disable');
    		curWin.resizable( 'disable');
    		curWin.find('.zkbi-iftitle').css('max-width','40px');
    		lastPos.mode = lastPos.newMode;
    		lastPos.newMode = 'min';
    		//curWin.css({'top':'auto','left':'auto','bottom':5, 'right':5+(minimizedWidth+1)*minSlot, 'width':minimizedWidth+'px', 'height':'25px', 'overflow':'hidden'});
    		curWin.css({'top':'auto','left':'auto','bottom':5, 'right':right, 'width':width+'px', 'height':'25px', 'overflow':'hidden'});
	    }, maximizeWindow: function(){
			//zkbiFreeMinSlot(curWin.data('minslot'));
   			curWin.draggable('enable');
   			curWin.resizable('enable');
   			curWin.find('.zkbi-iftitle').css('max-width','none');
    		lastPos.mode = lastPos.newMode;
    		lastPos.newMode = 'max';
    		curWin.css({'top':0, 'left':0, 'width':'100%', 'height':'100%'});
	    }, saveLastPos: function(){
    		lastPos.top = curWin.css('top');
    		lastPos.left = curWin.css('left');
    		lastPos.width = curWin.css('width');
    		lastPos.height = curWin.css('height');
    		lastPos.zindex = curWin.css('z-index');
    		lastPos.overflow = curWin.css('overflow');
	    }};
		curWin.data('lastPos', lastPos);
	    curWin.find('.zkbi-ifbtn-max').click(function(e){ 
	    	if (lastPos.newMode == 'min')
				lastPos.maximizeWindow();
	    	else if (lastPos.newMode == 'normal'){
				lastPos.saveLastPos();
				lastPos.maximizeWindow();
	    	}
	    	else
	    		lastPos.backToNormal();
	    } );
	    curWin.find('.zkbi-ifbtn-min').click(function(e){ 
	    	if (lastPos.newMode == 'max')
				lastPos.minimizeWindow();
	    	else if (lastPos.newMode == 'normal'){
				lastPos.saveLastPos();
				lastPos.minimizeWindow();
	    	}
	    	else if (lastPos.mode == 'max')
				lastPos.maximizeWindow();
			else
    			lastPos.backToNormal();
	    });

    	
		//arrange iframe windows
		var tileWindows = function(isHorizontal) {
			bodyWidth = $('body').outerWidth();
			bodyHeight = $('body').outerHeight();
			var allWins = collectAllWindows(true);
	    	if (allWins.length == 1) {
  				var win = allWins[0];
				var winLastPos = win.data('lastPos');
				if (winLastPos.newMode != 'max')
					win.find('.zkbi-ifbtn-max').click();
		   	}
			else if (allWins.length > 1) {
				var colCount = Math.floor(Math.sqrt(allWins.length));
			   	var dRowCount = Math.floor(allWins.length / colCount);
	    		var rowCount = dRowCount;
			    var lCount = allWins.length % colCount;
			    var rowNum = 0;
			    var colNum = 0;
			    var winss = [];
			    var wins = [];
			    for (var i = 0; i < allWins.length; i++) {
					wins.push(allWins[i]);
			    	if (++rowNum >= rowCount) {
						winss.push(wins);
						wins = [];
						if (i == allWins.length - 1)
							break;
						rowNum = 0;
						colNum++;
			    		rowCount = dRowCount;
			    		if (colNum >= colCount - lCount)
							rowCount++;
			    	}
			    }
			    if (wins.length > 0)
					winss.push(wins);
			    //console.log('colCount:' + colCount + ',dRow1Count:' + dRowCount + ',lCount:' + lCount + ',rowCount:' + rowCount);
			    if (isHorizontal) {
			    	var winHeight = bodyHeight / colCount;
					for (var i = 0; i < winss.length; i++) {
						var winWidth = bodyWidth / winss[i].length;
						for (var j = 0; j < winss[i].length; j++) {
	    					var win = winss[i][j];
							var winLastPos = win.data('lastPos');
							if (winLastPos.newMode != 'normal')
								winLastPos.backToNormal();
							win.css({width: winWidth, height: winHeight, top: (i * winHeight), left: (j * winWidth)});
						}
					}
			    }
			    else {
			    	var winWidth = bodyWidth / colCount;
					for (var i = 0; i < winss.length; i++) {
						var winHeight = bodyHeight / winss[i].length;
						for (var j = 0; j < winss[i].length; j++) {
	    					var win = winss[i][j];
							var winLastPos = win.data('lastPos');
							if (winLastPos.newMode != 'normal')
								winLastPos.backToNormal();
							win.css({width: winWidth, height: winHeight, left: (i * winWidth), top: (j * winHeight)});
						}
					}
			    }
			}
		};
	    $.contextMenu({
	        selector: '#' + winId + ' .z-window',
	        items: {
	        	tileHorizontalWindows: {name: 'Side by side (horizontal)', callback: function(key, opt){ 
					tileWindows(true);
	        	}},
	        	tileVerticalWindows: {name: 'Side by side (vertical)', callback: function(key, opt){ 
					tileWindows(false);
	        	}},
	        	cascadeWindows: {name: 'Cascade Windows', callback: function(key, opt){ 
					bodyWidth = $('body').outerWidth();
					bodyHeight = $('body').outerHeight();
     				var allWins = collectAllWindows(false);
					winWidth = (defaultWinWidth > bodyWidth) ? bodyWidth : defaultWinWidth;
					winHeight = (defaultWinHeight > bodyHeight) ? bodyHeight : defaultWinHeight;
					$.each(allWins, function(i, win){
						var winLastPos = win.data('lastPos');
						if (winLastPos.newMode != 'normal')
							winLastPos.backToNormal();
		    			var left = (bodyWidth > defaultXOffset) ? ((i * defaultXOffset) % (bodyWidth - defaultXOffset)) : 0;
		    			var top = (bodyHeight > defaultYOffset) ? ((i * defaultYOffset) % (bodyHeight - defaultYOffset)) : 0;
		    			win.css({width: winWidth, height: winHeight, left: left, top: top});
					});
	        	}},
	        	showDesktop: {name: 'Show desktop', callback: function(key, opt){ 
     				var allWins = collectAllWindows(true);
					$.each(allWins, function(i, win){
						var winLastPos = win.data('lastPos');
						if (winLastPos.newMode != 'min')
    						win.find('.zkbi-ifbtn-min').click();
					});
	        	}},
	        }
	    });
	}
	
	function changeLargeScale(p_large){
		var url = window.location.href;
		
	    var pattern = new RegExp('\\b('+'large'+'=).*?(&|#|$)');
	    if (url.search(pattern)>=0) {
	        url = url.replace(pattern,'$1' + p_large + '$2');
	    }
	    else{
	    	url = url.replace(/[?#]$/,'');
	    	url = url + (url.indexOf('?')>0 ? '&' : '?') + 'large' + '=' + p_large;	
	    }
		window.location.href = url;
	}
	
	/*var minMap = new Map();
	function zkbiGetMinSlot(){
		for (i=0; i<10; i++){
			//console.log('check:%d value:%s',i,minMap.get(i));
			if (!minMap.get(i)){
				minMap.set(i,true);
				return i;
			}
		}
		return 10;
	}
	function zkbiFreeMinSlot(p_idx){
		minMap.set(p_idx,false);
	}*/
	function getFullContextPath(){
		try {
	    	return('<%=request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()%>');
		}
		catch(err){
			return ('');
		}
	}
	
	function appendURLParam(url, param){
		try{
	    	var newurl = url;
	    	newurl += (newurl.split('?')[1] ? '&':'?') + param;
	    	return newurl;
		}
		catch(err){
			console.log('appendparam error');
			return (url);
		}
	}
	function customDateboxSetup(){
		
	}
	function customDateboxSetupXX(){
		try{
			console.log('setup custom datebox called');
	        zk.afterLoad('zul.db', function () {
	            var _xRenderer = {};
	            zk.override(zul.db.Renderer, _xRenderer, {
		            titleHTML: function (wgt, out, localizedSymbols) {
			            _xRenderer.titleHTML.apply(this, arguments); //call the original method
			            //console.log(wgt._view);
			            //console.log(wgt);
			            var uuid = wgt.uuid,
			               view = wgt._view,
			               text = wgt.getZclass() + '-ctrler';
			
			            //if(view == 'day') { }
		            	out.push('<div style="margin-top:10px;">');
		            	out.push('<button id="', uuid, '-today" class="', text, '"', ' onClick="customDateboxSetValue(this)"', ' >', '<%=sessionHelper.getLabel("Today")%>', '</button>');
		               	out.push('<button id="', uuid, '-monthstart" class="', text, '"', ' onClick="customDateboxSetValue(this)"', ' >', '<%=sessionHelper.getLabel("Month Start")%>', '</button>');
		               	out.push('<button id="', uuid, '-monthend" class="', text, '"', ' onClick="customDateboxSetValue(this)"', ' >', '<%=sessionHelper.getLabel("Month End")%>', '</button>');
		               	out.push('<button id="', uuid, '-clear" class="', text, '"', ' onClick="customDateboxSetValue(this)"', ' >', '<%=sessionHelper.getLabel("Clear")%>', '</button>');
		           		out.push('</div>');
		            }
	            });
	            var _xCalendar = {};
	            zk.override(zul.db.Calendar.prototype, _xCalendar, {
	                onChange : function(e) {
	                    _xCalendar.onChange.apply(this, arguments);
	                    if ("day" == this._view && false !== e.data.shallClose) {
							try {
								var popup, id, bbx;
	                    		(popup = jq(e.target._node).parents('.z-bandbox-popup')[0]) 
	                    		&& (id = popup.id.replace('-pp', '')) 
	                    		&& (bbx = zk.Widget.$('#' + id))
								&& (bbx.setOpen(false));
							}
							catch (err) {
								console.log('setOpen err:' + err);
							}
	                    }
	                }
	            });
			});
		}
		catch (err) {
			console.log('setup err:' + err);
		}
	}
	
	function customDateboxSetValue(btn){
		try {
			var popup = jq(btn).parents('.z-datebox-pp')[0] || jq(btn).parents('.z-datebox-popup')[0] || jq(btn).parents('.z-bandbox-popup')[0],
	        id = popup.id.replace('-pp', ''), dbx = zk.Widget.$('#' + id);
			var fmt = typeof dbx.getFormat !== 'undefined' ? dbx.getFormat() : null;
			//console.log('fmt:' + fmt);
	   		const now = new Date();
	    	if (btn.id.endsWith('-today')){
	    		dbx.getInputNode().value = getY4MD(now,'/',fmt);
	    		dbx.updateChange_();	
	    	}
	    	else if (btn.id.endsWith('-clear')){
	    		dbx.getInputNode().value = '';
	    		dbx.updateChange_();	
	    	}
	      	else if (btn.id.endsWith('-monthstart')){
	    		dbx.getInputNode().value = getY4MD(new Date(now.getFullYear(), now.getMonth(), 1),'/',fmt);
	    		dbx.updateChange_();	
	    	}
	    	else if (btn.id.endsWith('-monthend')){
	    		dbx.getInputNode().value = getY4MD(new Date(now.getFullYear(), now.getMonth() + 1, 0),'/',fmt);
	    		dbx.updateChange_();	
	    	}
			dbx.setOpen(false); //close the popup
		}
		catch (err) {
			console.log('setvalue err:' + err);
		}
	}

	/*function customListboxSetup(){
		try{
			console.log('setup custom listbox called');
	        zk.afterLoad('zul.mesh', function () {
	            var xHeaderWidget = {};
	            zk.override(zul.mesh.HeaderWidget.prototype, xHeaderWidget, {
	            	setFlexSize_: function (a, b) {
	            		const result = xHeaderWidget.setFlexSize_.apply(this, arguments);
	            		try {
	            			var $header = jq(this.$n());
	            			if ($header[0].hasAttribute('refWidth') && result && result.width === 0) { //fix column width is zero bug when hflex=1
	            				const refWidth = parseInt($header.attr('refWidth'));
	            				if (refWidth > 0) {
	                				this._hflexWidth = refWidth;
	        				    	result.width = refWidth;
	            				}
	            			}
	            		}
	            		catch (err) {
							console.log('setWidth err:' + err);
	            		}
	                }
	            });
	        });
		}
		catch (err) {
			console.log('setup err:' + err);
		}
	}*/
	
	function updateLocationURLQuickFilterTags(searchTags, matchMode) {
		const url = new URL(window.location.href);
		const params = new URLSearchParams(url.search);
		params.delete('qf');
		params.delete('qfm');
		if (searchTags && searchTags.length > 0) {
			for (i in searchTags)
				params.append('qf', searchTags[i]);
		}
		if (matchMode && matchMode.toLowerCase() !== 'and')
			params.set('qfm', matchMode);
		url.search = params;
		window.history.pushState({}, '', url.href);
	}
	
	const keepFocusText = (() => {
		let $tb;
		let tmInput;
		let tmFocus1;
		let tmFocus2;
		const delayFullSelect = () => {
			clearInterval(tmInput);
			tmInput = setInterval(() => {
				$tb.select();
			}, 2000);
		};
		const init = (p_uuid) => {
			$tb = $("#" + p_uuid);
			$tb.focus();
			$tb.on("blur", () => {
				clearTimeout(tmFocus1);
				tmFocus1 = setTimeout(() => {
					$tb.focus();
				}, 100);
			});
			$tb.on("input", () => {
				delayFullSelect();
			});
			clearInterval(tmFocus2);
			tmFocus2 = setInterval(() => {
				if (document.activeElement !== $tb[0] && $tb.is(":visible") && $tb.is(":not(:disabled)"))
					$tb.focus();
			}, 1000);
			delayFullSelect();
		};
		const exit = () => {
			clearInterval(tmInput);
			clearTimeout(tmFocus1);
			clearInterval(tmFocus2);
		};
		return {init: init, exit: exit};
	})();
</script>