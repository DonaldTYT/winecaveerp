
	//for download cjs chart
	function cjsDownloadCanvas(p_canvasId, p_fileName){
		var canvas = document.getElementById(p_canvasId);
		if (canvas){
			var image = null;
			if (p_fileName.endsWith(".png")){
				image = canvas.toDataURL("image/png");
				//image = canvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
			}
			else if (p_fileName.endsWith(".jpg")){
				image = canvas.toDataURL("image/jpeg");
			}
			else{
				console.log("format not supported");
				return;
			}
		    var anchor = document.createElement('a');
		    anchor.setAttribute('download', p_fileName);
		    anchor.setAttribute('href', image);
		    anchor.click();
		}
		else{
			console.log("element not exist:" + p_canvasId);
		}
	}
	
	function cjsCreateChart(p_id, p_options, p_toServer){
//		alert("in cjsCreateChart id="+p_id);
		var ctx = $("#" + p_id);
		if (ctx.length <= 0){
			console.log(p_id + " not found");
			return;
		}
		//console.log("found " +p_id)
		ctx.empty();
		
		// draw background
		var backgroundColor = 'white';
		Chart.plugins.register({
		    beforeDraw: function(c) {
		        var ctx = c.chart.ctx;
		        ctx.fillStyle = backgroundColor;
		        ctx.fillRect(0, 0, c.chart.width, c.chart.height);
		    }
		});
		
		//format label
		p_options.options.tooltips = {
			callbacks : {
				label : function(tooltipItem, data) {
					var value = data['datasets'][tooltipItem.datasetIndex]['data'][tooltipItem['index']];
					value = value.toLocaleString(undefined, { maximumFractionDigits : 2 });
					
					//append dataset label
					if (data['datasets'].length > 1){
						value = value + " (" + data['datasets'][tooltipItem.datasetIndex]['label']  + ")";
					}
					return value;
				},
				title : function(tooltipItem, data) {
					return data['labels'][tooltipItem[0]['index']];
				},
			}
		};
		
		//format yAxes label
		
		if( p_options.options.scales == null) {
	    p_options.options.scales = {
	         yAxes: [{
	           ticks: {
				 beginAtZero:true, 
	             callback: function(value, index, values) {
	               return value.toLocaleString(undefined, { maximumFractionDigits:2 });
	             }
	           }
	         }]
	    };
		if (p_options.type == "pie"){
			//pie chart hide yAxes
			p_options.options.scales.yAxes[0].display = false;
		}
		}

		var chart = new Chart(ctx, p_options);
		if (typeof p_toServer !== "undefined" && p_toServer == true){
			//console.log("send img back to server");
			var chartImg = chart.toBase64Image();
   			var zkComp = zk.Widget.$('$' + p_id);
   			//console.log("zkComp:" + zkComp.getId());
			if (zkComp !== null && zAu !== null && zk !== null){ 
				zAu.send(new zk.Event(zkComp, "onCreateChartImgFinish", chartImg, {toServer:true}));
			}
			else{
				console.log("zkcomp not found");
			}
		}
	}	
