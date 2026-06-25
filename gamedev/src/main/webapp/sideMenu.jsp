<%@ page import="java.io.*, java.util.*, java.text.*, com.uniinformation.utils.*, com.uniinformation.webcore.*, org.apache.commons.lang3.StringUtils, org.apache.commons.lang3.math.NumberUtils, org.apache.commons.lang.StringEscapeUtils" %>
<%
	SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper(request, response);
	if (sessionHelper.isLogin()){
		if ("N".equals(request.getParameter("sidemenu"))){  //andrew200924: fix no sidemenu affect open/close state
			return;
		}
		String requestURL = request.getServletPath().substring(1);
		String requestSidrAction = sessionHelper.getRequestSidrAction();
%>
		<script type="text/javascript">
		if (window.jQuery){
			//console.log("has jquery");
			$(document).ready(function () {
				//console.log('sidr: requestSidrAction:<%=requestSidrAction%> autoHide:<%=sessionHelper.getSideMenuAutoHide()%>');
				var disableOpenOnLoad = <%=requestURL.startsWith("custom_menu.html")%>;
				<%
					if (sessionHelper.getSideMenuAutoHide()){
						if (sessionHelper.getSideMenuAutoHideDefaultPin()){
				%>
							$.sidrDefault('sidemenu-button', 'sidr', 'container', '<%=requestSidrAction%>',true,false,'#sidr, #sidemenu-button-container', '#sidemenu-filter-area', disableOpenOnLoad);
				<%
						} else {
				%>
							$.sidrDefault('sidemenu-button', 'sidr', 'container', '<%=requestSidrAction%>',false,true,'#sidr, #sidemenu-button-container', '#sidemenu-filter-area', disableOpenOnLoad);
				<%
						}
					}
					else  {
						if (sessionHelper.isMobileDevice()) {
				%>
							$.sidrDefault('sidemenu-button', 'sidr', 'container', '<%=requestSidrAction%>',false,false,'#sidr, #sidemenu-button-container', '#sidemenu-filter-area', disableOpenOnLoad);
				<%
						} else {
				%>
							$.sidrDefault('sidemenu-button', 'sidr', 'container', '<%=requestSidrAction%>',true,false,'#sidr, #sidemenu-button-container', '#sidemenu-filter-area', disableOpenOnLoad);
				<%
						}
					}
				%>
				
			});
		}
		else{
			console.log("no jquery");
		}
		var lastCompId = null;
		function zkGetSidrStatus(p_compId) {
			$(document).ready(function () {
				if (p_compId != null){
					lastCompId = p_compId;
				}
				if (lastCompId === null){
					console.log("zkGetSidrStatus ignore, id is null");
					return;
				}
				var autoHide = undefined;
				var sideMenu = $.sidr('status').curSideMenu;
				if (typeof sideMenu !== 'undefined') 
					autoHide = $('#' + sideMenu).data('autoHide');
				if (typeof autoHide === 'undefined')
					autoHide = <%=sessionHelper.getSideMenuAutoHide()%>;
				var opened = $.sidr('status').opened;
	        	var zkComp = zk.Widget.$('$' + lastCompId);  //get rows component by id
	        	var browserWinId = "";
	        	try{
	        		browserWinId = getBrowserWinId();
	        	}
	        	catch(err){
	        		console.log('err:' + err);
	        	}
	        	if (zkComp !== null && zAu !== null){
	        		if (opened){
	        			zAu.send(new zk.Event(zkComp, "onSidrOpen", {autoHide:autoHide, 'browserWinId':browserWinId}, {toServer:true}));
	        		}
	        		else{
	        			zAu.send(new zk.Event(zkComp, "onSidrClose", {autoHide:autoHide,'browserWinId':browserWinId}, {toServer:true}));
	        		}
	        		
	        	}
			});
		}
		function zkLogout() {
			$(document).ready(function () {
				if (lastCompId === null){
					console.log("id is null, action ignore");
					return;
				}
	        	var zkComp = zk.Widget.$('$' + lastCompId);
	        	if (zkComp !== null && zAu !== null){
	        		zAu.send(new zk.Event(zkComp, "onLogout", null, {toServer:true}));
	        	}
			});
		}
		function zkReportProblem() {
			$(document).ready(function () {
				if (lastCompId === null){
					console.log("id is null, action ignore");
					return;
				}
	        	var zkComp = zk.Widget.$('$' + lastCompId);
	        	if (zkComp !== null && zAu !== null){
	        		var ele = document.body;
        		    /* var r = 1080.0 / ele.scrollHeight;
        		    if (r > 1)
						r = 1;
        		    console.log('ratio', r);*/
	        		html2canvas(ele, 
	        				{
	        				//scale: r, 
	        				//width: ele.clientWidth,
	        				//height: ele.clientHeight,
	        				windowWidth: ele.scrollWidth,
	        				windowHeight: ele.scrollHeight
	        				}
	        			).then(function(canvas) {
	        		    //document.body.appendChild(canvas);
	        		    //console.log("html2canvas:" + canvas.toDataURL("image/jpg"));
	        		    jq('$' + lastCompId).data('canvasDataUrl', canvas.toDataURL('image/jpeg'));
	        			zAu.send(new zk.Event(zkComp, "onReportProblem", {shotTime: Date.now(), currentUrl: window.location.href }, {toServer:true}));
	        		});
	        	}
			});
		}
		function zkToggleSidr() {
			$(document).ready(function () {
				<%
				if (sessionHelper.getSideMenuAutoHide()){
				%>
					$(".pin").click();
					var autoHide = false;
					if (typeof $.sidr('status').autoHide !== 'undefined'){
						autoHide = $.sidr('status').autoHide;
					}
					if (autoHide){
						$.sidr('close');
					}
					else{
						$.sidr('open');
					}
				<%
				}
				else{
				%>
					$.sidr('toggle');
				<%
				}
				%>
				
			});
		}
		function zkHideSidr(){
			$(document).ready(function () {
				if ($("#sidemenu-button-container").is(":visible")){
					$("#sidemenu-button-container").hide(); 	
					$.sidr('disable');	
				}
				else{
					console.log("zkHideSidr ignore");
				}
			});
		}
		function zkShowSidr(){
			$(document).ready(function () {
				if (!$("#sidemenu-button-container").is(":visible")){
					$("#sidemenu-button-container").show(); 	
					$.sidr('enable');	
				}
				else{
					console.log("zkShowSide ignore");
				}
			});
		}
		function zkActiveSidrItem(itemid){
			$(document).ready(function(){
				var $actitem = $('#sidr #menuitem' + itemid);
				var actcolor = '#fff';
				//$actitem.children().css('color', actcolor);
				$actitem.find('span').css('color', actcolor);
				$actitem.closest('li').css('background', '#0093f9');
				$($actitem.parents('ul').toArray().reverse()).each(function(){
					var $dropdownlink = $(this).prev('.dropdownlink');
					//$dropdownlink.children().css('color', actcolor);
					$dropdownlink.click();
				});
				var title = $actitem.find('span').text();
				if (typeof title !== 'undefined' && title !== null && title !== '') {
					document.title = title;	
					$(document).data('titleSetted', true);
				}
			});
		}
		
		/*
		//function for adjust color brightness experimental
		function getColorHexBrightness(rgb, percent){
			return calColorBrightness(getColorHex(rgb), percent);
		}
		function calColorBrightness(hex, percent){
			if (typeof percent === 'undefined' || percent < 0){
				console.log('precent out of range. use default');
				percent = 0;
			}
			else if (percent >= 100){
				return '#ffffff';
			}
		    hex = hex.replace(/^\s*#|\s*$/g, '');
		    if(hex.length == 3){
		        hex = hex.replace(/(.)/g, '$1$1');
		    }
		    var r = parseInt(hex.substr(0, 2), 16), g = parseInt(hex.substr(2, 2), 16), b = parseInt(hex.substr(4, 2), 16);
		    
		    return '#' +
		       ((0|(1<<8) + r + (256 - r) * percent / 100).toString(16)).substr(1) +
		       ((0|(1<<8) + g + (256 - g) * percent / 100).toString(16)).substr(1) +
		       ((0|(1<<8) + b + (256 - b) * percent / 100).toString(16)).substr(1);
		}
		function getColorHex(rgb){
			if (!rgb){
		    	console.log('invalid rgb value, use default');
				return '#ffffff'; //return default
			}
			if (rgb.startsWith('#')){
				return rgb;
			}
		    
		    rgb = rgb.match(/^.*rgb\((\d+),\s*(\d+),\s*(\d+)\).*$/);
		    if (rgb == null){
		    	console.log('invalid rgb value, use default');
		    	return '#ffffff';
		    }
		    return( "#" +
		        ("0" + parseInt(rgb[1]).toString(16)).slice(-2) +
		        ("0" + parseInt(rgb[2]).toString(16)).slice(-2) +
		        ("0" + parseInt(rgb[3]).toString(16)).slice(-2));
		}
		*/
		</script>
<%
		if (!(request.getParameter("sidemenu") != null && (request.getParameter("sidemenu").equals("N") || request.getParameter("sidemenu").equals("n")))){
			sessionHelper.generateSideMenu(request, out, "sidr");
			String itemid = sessionHelper.getCurrentSideMenuItem(request);
			if (itemid != null && sessionHelper.getAllowSideMenuIndicator())
				out.print("<script>zkActiveSidrItem('"+itemid+"')</script>");

			out.println("<div class=\"sidemenu-container\">");
			sessionHelper.generateTopMenu(out);
			out.println("</div>");
		}
	}
%>