var VISU_DEBUG = false;
function visUtilInit(){
	if (typeof vis == 'undefined'){
		if (VISU_DEBUG) console.log('visUtilInit start');
	}
	else{
		if (VISU_DEBUG) console.log('already loaded');
		return;
	}
	//var cssFile = "css/vis.min.css";
	var cssFile = "css/vis-timeline-graph2d-7.5.1.min.css";
	if (VISU_DEBUG) console.log("visUtil Load css:" + cssFile);
	var cssTag = document.createElement("link");
	cssTag.setAttribute("rel", "stylesheet");
	cssTag.setAttribute("type", "text/css");
	cssTag.setAttribute("href", cssFile);
	document.getElementsByTagName("head")[0].appendChild(cssTag);
	
	/*
	//seems not required
	//var cssFile2 = "http://visjs.org/dist/vis-network.min.css";
	//console.log("visUtil Load css:" + cssFile2);
	//var cssTag2 = document.createElement("link");
	//cssTag2.setAttribute("rel", "stylesheet");
	//cssTag2.setAttribute("type", "text/css");
	//cssTag2.setAttribute("href", cssFile2);
	//document.getElementsByTagName("head")[0].appendChild(cssTag2);
	*/
	
	
	//vis timeline
	var jsElement = document.createElement('script');
	//jsElement.src = "js/vis.min.js";
	jsElement.src = "js/vis-timeline-graph2d-7.5.1.min.js";
	document.head.appendChild(jsElement);

	//vis network
	jsElement = document.createElement('script');
	jsElement.src = "js/vis-network-9.1.0.min.js";
	document.head.appendChild(jsElement);
}
function visUtilProcessTimeline(p_json, p_zkCompId){
	//var json = JSON.parse(decodeURIComponent(p_json));
	var json = p_json;
	if (typeof vis == 'undefined'){
		console.log("vis lib not loaded");
		return;
	}
	var containerId = json['containerId'];
	var container = document.getElementById(containerId);
	if (container == null){
		console.log("container is null:" + containerId)
		return;
	}
	$("#" + containerId).empty();
	var zkComp = null;
	if (p_zkCompId != null){
		//zkComp = jq('#' + p_zkCompId);
		zkComp = zk.Widget.$('$' + p_zkCompId);  //get rows component by id
	}
	
	var items = new vis.DataSet(json['items']); //seems iphone not support it
		  /*
	var orgItems = json['items'];
	var items = new vis.DataSet();
	for(var i = 0; i < orgItems.length; i++) {
		  var orgItem = orgItems[i];
		  var newItem = {};
		  if (orgItem.start != undefined && orgItem.start != null){
			  newItem.put("start", vis.moment(orgItem.start));
		  }
		  if (orgItem.end != undefined && orgItem.end != null){
			  newItem.put("start", vis.moment(orgItem.end));
		  }
		  if (orgItem.id != undefined && orgItem.id != null){
			  newItem.put("id", orgItem.id);
		  }
		  items.add(newItem);
		  for (var key in orgItem) {
			  if (orgItem.hasOwnProperty(key)) {
			    console.log(key + " -> " + orgItem[key]);
			  }
			}
		}
		  */
	/*
	  var items = new vis.DataSet([
	                               {id: 1, content: 'item 1', start: '2013-04-20'},
	                               {id: 2, content: 'item 2', start: '2013-04-14'},
	                               {id: 3, content: 'item 3', start: '2013-04-18'},
	                               {id: 4, content: 'item 4', start: '2013-04-16 00:00', end: '2013-04-19 23:59'},
	                               {id: 5, content: 'item 5', start: '2013-04-25'},
	                               {id: 6, content: 'item 6', start: '2013-04-27'}
	                             ]);
	                             */
	visUtilLogEvent("debug", items);

	var options = json['options'];
	if (typeof options['moment'] === 'string')
		options.moment = Function('return('+options['moment']+')')();
	else if (typeof options['moment'] === 'function')
		options.moment = options['moment'];
	else
		options.moment = function (date) {
	      	//return vis.moment(date).utcOffset('+08:00'); 
	      	return vis.moment(date).utc();
	      	//return vis.moment.parseZone(date).utcOffset();
	    	};
	var timeline = new vis.Timeline(container, items, options);
	if (zkComp !== null && zAu !== null && zk !== null){
		items.on('update', function (event, properties) {
			visUtilLogEvent(event, properties);
			zAu.send(new zk.Event(zkComp, "onVis"+event, properties, {toServer:true}));
		});
		items.on('remove', function (event, properties) {
			visUtilLogEvent(event, properties);
			zAu.send(new zk.Event(zkComp, "onVis"+event, properties, {toServer:true}));
		});
	}
	else{
		console.log("comp or zAu is null, skip register event");
	}
}
function visUtilProcessNetwork(p_json, p_zkCompId){
	var json = p_json;
	if (typeof vis == 'undefined'){
		console.log("vis lib not loaded");
		return;
	}
	var containerId = json['containerId'];
	var container = document.getElementById(containerId);
	if (container == null){
		console.log("container is null:" + containerId)
		return;
	}
	$("#" + containerId).empty();
	var zkComp = null;
	if (p_zkCompId != null){ //optional, for event callback only
		zkComp = zk.Widget.$('$' + p_zkCompId);  //get rows component by id
	}
	
	var items = json['items'];
	visUtilLogEvent("debug", items);

	var options = json['options'];
    var network = new vis.Network(container, items, options);
	if (zkComp !== null && zAu !== null && zk !== null){
		network.on('select', function (p) {
			visUtilLogEvent(p);
			zAu.send(new zk.Event(zkComp, "onVisselect", p, {toServer:true}));
		});
		network.on('selectNode', function ({nodes}) {
			zAu.send(new zk.Event(zkComp, "onVisselectNode", nodes, {toServer:true}));
		});
		network.on('selectEdge', function ({edges}) {
			zAu.send(new zk.Event(zkComp, "onVisselectEdge", edges, {toServer:true}));
		});
		network.on('deselectNode', function ({nodes, previousSelection}) {
			var prevNodes = previousSelection.nodes.map(function(x){ return x.id });
			var delNodes = prevNodes.filter(function(x){ return nodes.indexOf(x) < 0 });
			zAu.send(new zk.Event(zkComp, "onVisdeselectNode", delNodes, {toServer:true}));
		});
		network.on('deselectEdge', function ({edges, previousSelection}) {
			var prevEdges = previousSelection.edges.map(function(x){ return x.id});
			var delEdges = prevEdges.filter(function(x){ return edges.indexOf(x) < 0 });
			zAu.send(new zk.Event(zkComp, "onVisdeselectEdge", delEdges, {toServer:true}));
		});
	}
	else{
		console.log("comp or zAu is null, skip register event");
	}
	if (json.hasOwnProperty('selection')){
		if (VISU_DEBUG) console.log("with selection");
		network.setSelection(json['selection']);
	}
	else{
		if (VISU_DEBUG) console.log("without selection");
	}
	$("#" + containerId).data('visnetwork', network);
}
function visUtilLogEvent(event, properties) {
	/*
    console.log("visUtilLogEvent:"+event +"," + properties);
    var log = document.getElementById('visLog');
    if (log == null){
    	return;
    }
    var msg = document.createElement('div');
    msg.innerHTML = 'event=' + JSON.stringify(event) + ', ' + 'properties=' + JSON.stringify(properties);
    log.firstChild ? log.insertBefore(msg, log.firstChild) : log.appendChild(msg);
    */
  }
visUtilInit();