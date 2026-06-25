/* global FullCalendar, moment, Swal, $, createPickr, zkbiSend */

    var calendar = null;
    
	function createCalendar(p_id, p_data, p_loginuser){
		console.log('called: id:' + p_id +' data:' + JSON.stringify(p_data) + " loginuser:" + p_loginuser);
		if (!p_id){
			console.log('invalid id, skip rendar calendar');
			return;
		}
		
        var calDiv= $('#' +p_id);
        calDiv.css({
        'user-select': 'none',
        '-moz-user-select': 'none',
        '-khtml-user-select': 'none',
        '-webkit-user-select': 'none',
        '-o-user-select': 'none',
        });
        
        var defaultTextColor = 'white';
        var defaultBackgroundColor = '#3788d8';
        
        let requestMonthsEventList = new Set();
        
        //construct a sample opts
   		var opts = {
          plugins: ["interaction", "dayGrid","timeGrid", "list"],
          header: {
            //left: "prevYear,prev,next,nextYear,today,btAdd,btExpand",
            left: "prevYear,prev,next,nextYear,today",
            center: "title",
            right: "dayGridMonth,timeGridWeek,listWeek,timeGridDay,listDay",
          },
          locale: "en-gb",  //zh-tw, zh-cn, en-gb
          defaultDate: moment().format("YYYY-MM-DD"),
          navLinks: true, // can click day/week names to navigate views
          editable: true,
          //eventLimit: true, // allow "more" link when too many events
          eventLimit: 5, // false: no limit
          views: {
        	listDay: { buttonText: 'daylist' },
        	listWeek: { buttonText: 'weeklist' },
     	 },
       	 buttonText: {
            today: 'today',
         },
         windowResize:  function(view) {
        	 console.log("windowResize??");
         },
         contentHeight: "auto",  //auto: exapand all item, no scrollbar. remark: unset will give wrong height in mobile view
         aspectRatio: 1.5,
         customButtons: {
    		btAdd: {
      			text: 'new event',
      			click: function() {
      				fcAddEvent(calendar);
      			}
    		},
    		btExpand: {
      			text: 'expand',
      			click: function() {
      				if (calendar.getOption('eventLimit') != false){
      					calendar.setOption('eventLimit',false);
      				}
      				else{
      					calendar.setOption('eventLimit',4);
      				}
      			}
    		},
  		 },
  		 /*
  		 //append class to event
  		 eventRender: function (info) {
              var originalClass = info.el.className;
              info.el.className = originalClass + ' hasmenu';
  		 },
  		 */
  		 eventRender: function(info) {
 			 if (info.event.extendedProps) {
 				 if (info.event.extendedProps.faIcon) {
 					 //console.log("eventRender info", info);
 					 const $t = $("<i class='fa fa-" + info.event.extendedProps.faIcon + "'></i>");
 					 if (info.event.extendedProps.faIconColor)
 						 $t.css("color", info.event.extendedProps.faIconColor);
 					 $(info.el).find(".fc-content >.fc-time").prepend("&nbsp;").prepend($t);
 					 $(info.el).find(".fc-list-item-title >a").prepend($t);
 				 }
 				 if (info.event.extendedProps.cursor)
 					 $(info.el).css("cursor", info.event.extendedProps.cursor);
 			 }
 		 },
  		 eventClick: function(info) {
  			 console.log(info);
 			 if (p_data.opt.requestEventClickEvent) {
 				 let data = {};
 				 data.rg = info.event.extendedProps.rg ? info.event.extendedProps.rg : -1;
 				 data.id = info.event.id;
 				 data.title = info.event.title;
 				 data.start = moment(info.event.start).format('YYYY-MM-DDTHH:mm:SS');
 				 data.end = moment(info.event.end).format('YYYY-MM-DDTHH:mm:SS');
 				 zkbiSend(p_data.opt.requestEventClickEvent, data);
 			 }
 			 else
 				 fcAddEvent(this, info.event, info.event.extendedProps.rg, moment(info.event.start), moment(info.event.end), info.event.title,info.event.allDay, 
  					 			info.event.extendedProps.isPublic,
  					 			info.event.extendedProps.owner);
  			 /*
  		    alert('Event: ' + info.event.title);
  		    alert('Coordinates: ' + info.jsEvent.pageX + ',' + info.jsEvent.pageY);
  		    alert('View: ' + info.view.type);
  		    */
  			 /*
  		    
            var title = prompt('Event Title:', info.event.title, { buttons: { Ok: true, Cancel: false} });
            if (title){
            	info.event.setProp('title', title);
            }
            */
  		 },
  		 selectable: true,
  		 eventDrop: function(info) {
  			 console.log(info.event.title + " was dropped on " + moment(info.event.start).format('YYYY-MM-DDTHH:mm:SS'));
  			 var cbData = {};
  			 cbData.rg = info.event.extendedProps.rg ? info.event.extendedProps.rg : -1;
  			 cbData.action = 'drop';
  			 cbData.title = info.event.title;
  			 cbData.start = moment(info.event.start).format('YYYY-MM-DDTHH:mm:SS');
  			 cbData.end = moment(info.event.end).format('YYYY-MM-DDTHH:mm:SS');
  			 zkbiSend('onCalEvent', cbData);
  			 //info.revert();
  		 },
  		 eventResize: function(info) {
  			 console.log(info.event.title + " end is now " + moment(info.event.end).format('YYYY-MM-DDTHH:mm:SS'));
  			 var cbData = {};
  			 cbData.rg = info.event.extendedProps.rg ? info.event.extendedProps.rg : -1;
  			 cbData.action = 'resize';
  			 cbData.title = info.event.title;
  			 cbData.start = moment(info.event.start).format('YYYY-MM-DDTHH:mm:SS');
  			 cbData.end = moment(info.event.end).format('YYYY-MM-DDTHH:mm:SS');
  			 zkbiSend('onCalEvent', cbData);
  			 //info.revert();
  		 },
  		 select: function(event) {
  			 console.log('select: start:' + moment(event.start,'YYYY-MM-DD HH:mm') + " end:" + event.end);
  			 console.log(event);
  			 //remark: exclusive end day (the moment immediate after end). seems a bit misleading from user point of view
  			 fcAddEvent(this,null,-1,moment(event.start),moment(event.end),null,false,false,p_loginuser);
  			 calendar.unselect()
  		 },
  		 eventMouseEnter: function(info) {
  			 //console.log("eventMouseEnter");
  			 var event = info.event;
  			 info.el.title = event.title + "\n"
  			 					+ moment(event.start).format('YYYY-MM-DD HH:mm:SS') + " to " 
  			 					+ moment(event.end).format('YYYY-MM-DD HH:mm:SS') + "\n" 
  			 					+ event.extendedProps.owner + " "
  			 					+ (event.extendedProps.isPublic ? "Public" : "Private");
 			 if (info.event.extendedProps && info.event.extendedProps.extraDesc)
 				 info.el.title += "\n" + info.event.extendedProps.extraDesc;
  		 },
  		 eventMouseLeave: function(info) {
  			 //console.log("eventMouseLeave");
  		 },
         eventTimeFormat: {
    		hour: '2-digit',
    		minute: '2-digit',
    		hour12: false,
  		  },
  		  /*minTime: "06:00:00",*/
  		  eventTextColor: defaultTextColor,
  		  eventBackgroundColor: defaultBackgroundColor,
  		  
  		  datesRender: function(info) {
  			  if (p_data.opt.requestMonthsData === true) {
  				  console.log('datesRender', info);
  				  let mStart, mEnd, activeStart, activeEnd;
  				  if (info.view.currentStart) {
  					  activeStart = moment(info.view.currentStart);
  					  mStart = moment(info.view.currentStart).startOf('month').subtract(1, 'months');
  				  }
  				  if (info.view.currentEnd) {
  					  activeEnd = moment(info.view.currentEnd);
	  				  mEnd = moment(info.view.currentEnd);
	  				  let mEnd1 = mEnd.clone().startOf('month');
	  				  if (mEnd.isAfter(mEnd1))
	  					  mEnd.add(1, 'months').startOf('month');
	  				  mEnd.add(1, 'months');
	  			  }
  				  if (info.view.activeStart)
  					  activeStart = moment(info.view.activeStart);
  				  if (info.view.activeEnd)
  					  activeEnd = moment(info.view.activeEnd);

	  			  let addMonthStarts = [];
	  			  for (let m = mStart.clone(); m.isBefore(mEnd); m.add(1, 'months')) {
	  				  let mstr = m.format('YYYY-MM-DD');
	  				  //console.log('m', mstr);
	  				  if (!requestMonthsEventList.has(mstr)) {
	  					  requestMonthsEventList.add(mstr);
	  					  addMonthStarts.push(mstr);
	  				  }
	  			  }

	  			  let removeMonthStarts = [];
	  			  let arr = Array.from(requestMonthsEventList);
	  			  arr.sort(function(a, b) { 
	  				  if (moment(a).isBefore(moment(b)))
	  					  return -1;
	  				  else if (moment(a).isAfter(moment(b)))
	  					  return 1;
	  				  else
	  					  return 0;
	  			  });
	  			  let size = arr.length;
	  			  const maxSize = p_data.opt.requestMaxMonthsSize;
	  			  let i = 0, j = size - 1;
	  			  console.log("requestMaxMonthsSize", maxSize);
	  			  while (size > maxSize && i < j) {
	  				  let mstr = arr[i];
	  				  let m = moment(mstr);
	  				  if (m.isBefore(mStart)) {
	  					  if (size > maxSize) {
	  						  removeMonthStarts.push(mstr);
	  						  size--;
	  					  }
	  				  }
	  				  mstr = arr[j];
	  				  m = moment(mstr);
	  				  if (m.isSameOrAfter(mEnd)) {
	  					  if (size > maxSize) {
	  						  removeMonthStarts.push(mstr);
	  						  size--;
	  					  }
	  				  }
	  				  i++;
	  				  j--;
	  			  }

	  			  console.log("requestMonthsEventList", requestMonthsEventList);
	  			  console.log("addMonthStarts", addMonthStarts);
	  			  console.log("removeMonthStarts", removeMonthStarts);
  				  removeMonthStarts.forEach(mstr => { requestMonthsEventList.delete(mstr); });
	  			  console.log("requestMonthsEventList", requestMonthsEventList);

  				  let data = {};
  				  data.action = 'addMonthsEvent';
  				  data.activeStart = activeStart.format('YYYY-MM-DD');
  				  data.activeEnd = activeEnd.format('YYYY-MM-DD');
	  			  if (addMonthStarts.length > 0)
	  				  data.requestAddMonthStarts = addMonthStarts;
	  			  if (removeMonthStarts.length > 0)
	  				  data.requestRemoveMonthStarts = removeMonthStarts;
  				  console.log('onCalEvent', data);
  				  zkbiSend('onCalEvent', data);
  			  }
  		  },
  		  /*//dummy event for testing old, will replaced by p_data.events
          events: [
            {
              title: "All Day Event",
              //start: "2020-05-01",
              start: moment().startOf('month').format("YYYY-MM-DD"),
              color: "pink",
              textColor: "red",
            },
            {
              title: "Long Event",
              start: moment().startOf('month').add(3,'d').format("YYYY-MM-DD"),
              end: moment().startOf('month').add(8,'d').format("YYYY-MM-DD"),
              color: "orange",
              textColor: "white",
            },
            {
              title: "Conference",
              start: moment().startOf('month').add(8,'d').format("YYYY-MM-DD"),
              end: moment().startOf('month').add(9,'d').format("YYYY-MM-DD"),
              color: "#3788d8",
              textColor: "white",
            },
            {
              title: "Meeting",
              start: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT10:30:00"),
              end: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT12:30:00"),
            },
            {
              title: "very early",
              start: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT01:00:00"),
              end: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT03:00:00"),
              color: "grey",
              textColor: "white",
            },
            {
              title: "Lunch",
              start: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT12:00:00"),
              color: "green",
              textColor: "white",
            },
            {
              title: "Meeting",
              start: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT14:00:00"),
            },
            {
              title: "Happy Hour",
              start: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT17:30:00"),
            },
            {
              title: "Dinner",
              start: moment().startOf('month').add(12,'d').format("YYYY-MM-DDT20:00:00"),
            },
            {
              title: "Birthday Party",
              start: moment().startOf('month').add(13,'d').format("YYYY-MM-DDT07:00:00"),
            },
            {
              title: "Click haha1",
              url: 'javascript:alert("haha1");',
              start: moment().startOf('month').add(20,'d').format("YYYY-MM-DDT07:00:00"),
            },
          ],*/
        }
   		/*if (p_data && p_data.events){
   			console.log("found");
   			//opts['events'].push(p_data.events);
   			opts.events = opts.events.concat(p_data.events)
   		}
   		else{
   			console.log("not found");
   		}*/
		opts.events = p_data.events;
   		if (p_data.opt)
   			$.extend(opts, p_data.opt);
		console.log('new opt:' + JSON.stringify(opts));
        

        calendar = new FullCalendar.Calendar(calDiv[0], opts);
        calendar.setOption('defaultTextColor', defaultTextColor);
        calendar.setOption('defaultBackgroundColor', defaultBackgroundColor);
        calendar.setOption('loginUser', p_loginuser);

        calendar.render();
    }
	
	async function fcAddEvent(p_calendar, p_event, p_rg, p_start, p_end, p_title, p_wholeDay, p_ispublic, p_owner){
		var loginUser = p_calendar.getOption("loginUser");
		if (loginUser !== p_owner)
			return;

       	var cbData = {};
       	cbData.rg = p_rg ? p_rg : -1;  //TODO obtain rg from event
       	
		console.log(p_calendar);
	    var textColorPickr;
	    var backgroundColorPickr;
		const result = await Swal.fire({
			  title: p_event == null ? 'New Event' : 'Update Event',
			  html: 
				 '<div style="text-align:left;margin-top:0.5em"><span class="swal2-label">Event Name</span></div>' +
				 '<input id="ip-eventname" style="margin:0.1em auto" class="swal2-input" placeholder="Event Name">' +
				 
				 '<div style="text-align:left"><label for="cb-wholeday" class="swal2-checkbox" style="border:1px solid #d9d9d9;">Whole Day<input type="checkbox" name="cb-wholeday" id="cb-wholeday"></label></div>' +
				 
				 '<div style="text-align:left;margin-top:0.5em"><span class="swal2-label">Start</span></div>' +
			     '<input id="datetime1" type="text" class="swal2-input" placeholder="Start Date" style="margin:0.1em auto">' +
			     
				 '<div style="text-align:left;margin-top:0.5em"><span class="swal2-label">End</span></div>' +
			     '<input id="datetime2" type="text" class="swal2-input" placeholder="End Date" style="margin:0.1em auto">' +
			     
				 '<div style="position:relative;margin-top:0.5em;height:60px;">' +
				 '<div style="display:inline-block;position:absolute;left:20px;top:20px;border:1px solid;border-radius:4px;"><div id="background-color"></div></div>'+
				 '<div style="display:inline-block;position:absolute;left:0px;border:1px solid;border-radius:4px;"><div id="text-color"></div></div>'+
				 '<div id="predefined-colors-container" style="display:inline-block;position:absolute;left:80px;top:10px;"></div>' +
				 '</div>' +
			     
				 '<div style="text-align:left"><label for="cb-public" class="swal2-checkbox" style="border:1px solid #d9d9d9;">Public<input type="checkbox" name="cb-public" id="cb-public"></label></div>' +
				 '<div style="text-align:left"><span class="swal2-label">Owner: '+p_owner+'</span></div>' +
			     ' ',
			  focusConfirm: true,
			  allowOutsideClick: false,
			  showCloseButton: true,
			  showCancelButton: p_event ? true : false,  //use cancel button as delete
			  cancelButtonText: 'Delete',
			  cancelButtonColor: '#ff5555',
			  onBeforeOpen: () => {
			  	//console.log('before open');
			  	$('#datetime1').datetimepicker({ mask:false,scrollMonth:false});
			  	$('#datetime2').datetimepicker({ mask:false,scrollMonth:false});
			  	//console.log(p_start);
			  	if (p_start && p_start.isValid()){
			  		$('#datetime1').val(p_start.format('YYYY/MM/DD HH:mm'));
			  	}
			  	if (p_end && p_end.isValid()){
			  		$('#datetime2').val(p_end.format('YYYY/MM/DD HH:mm'));
			  	}
			  	if (p_title){
			      $('#ip-eventname').val(p_title);
			  	}
			    console.log('wholeday:' + p_wholeDay);
			    if (p_wholeDay){
			    	$("#cb-wholeday").prop("checked",true);
			    }
			    $('#cb-wholeday').checkboxradio();
			    if (p_ispublic){
			    	$("#cb-public").prop("checked",true);
			    }
			    $('#cb-public').checkboxradio();
			    /*
			    $('#cb-wholeday').change(function() {
			    	$( "#datetime2" ).prop( "disabled", this.checked );
			    });
			    */
			    var predefinedColors = [ 
			    	{textColor:'white',color:'#3788d8'},
			    	{textColor:'white',color:'red'},
			    	{textColor:'white',color:'orange'},
			    	{textColor:'grey',color:'yellow'},
			    	{textColor:'white',color:'forestgreen'},
			    	{textColor:'white',color:'mediumorchid'},
			    	{textColor:'red',color:'pink'},
			    ];
			    $.each(predefinedColors, (i, c) => {
		    		var child = $('<div style="display:inline-block;position:relative;margin-right:4px;cursor:pointer;width:36px;height:36px;border-radius:4px;background:'+c.color+';">' +
		    				'<span style="position:absolute;left:9px;top:1px;color:'+c.textColor+';font-size:30px;">A</span></div>');
		    		$('#predefined-colors-container').append(child);
		    		child.click(() => {
		    			textColorPickr.setColor(c.textColor);
		    			backgroundColorPickr.setColor(c.color);
		    		});
		    	});
				 
			    var eventTextColor;
			    var eventBackgroundColor;
			    if (p_event != null) {
			    	eventTextColor = p_event.textColor;
			    	eventBackgroundColor = p_event.backgroundColor;
			    	if (!eventTextColor || $.trim(eventTextColor) === "")
			    		eventTextColor = p_calendar.getOption("defaultTextColor");
			    	if (!eventBackgroundColor || $.trim(eventBackgroundColor) === "")
			    		eventBackgroundColor = p_calendar.getOption("defaultBackgroundColor");
			    } else {
			    	eventTextColor = p_calendar.getOption("defaultTextColor");
			    	eventBackgroundColor = p_calendar.getOption("defaultBackgroundColor");
			    }
			    textColorPickr = createPickr({el: "#text-color", default : eventTextColor});
			    backgroundColorPickr = createPickr({el : "#background-color", default : eventBackgroundColor});
			  },
			  preConfirm: () => { //export the output
				  console.log('textColorPickr:' + textColorPickr.getColor().toHEXA());
				  console.log('backgroundColorPickr:' + backgroundColorPickr.getColor().toHEXA());
			      if (!document.getElementById('ip-eventname').value){
			    	  Swal.showValidationMessage('Event Name Missing');  
			    	  return;
			      }
			      if (!document.getElementById('datetime1').value){
			    	  Swal.showValidationMessage('Start Date Missing');  
			    	  return;
			      }
			      if (!$("#cb-wholeday").prop("checked") && !document.getElementById('datetime2').value){
			    	  Swal.showValidationMessage('End Date Missing');  
			    	  return;
			      }
			    return [
			      document.getElementById('ip-eventname').value,
			      $("#cb-wholeday").prop("checked"),
			      document.getElementById('datetime1').value,
			      document.getElementById('datetime2').value,
			      textColorPickr.getColor().toHEXA().toString(),
			      backgroundColorPickr.getColor().toHEXA().toString(),
			      $("#cb-public").prop("checked"),
			    ]
			  }
		});
		console.log('result:' + JSON.stringify(result));
		if (result.isDismissed && result.dismiss == 'cancel'){ //delete
			p_event.remove();
			Swal.fire(
				      'Deleted',
				      'Event - ' + p_event.title,
				      'success'
				    )
		    cbData.action = 'delete';
  			cbData.title = p_event.title;
		    zkbiSend('onCalEvent', cbData);
			return;
		}
		if (result.isDismissed){
			console.log('dismiss:' + result.dismiss);
			return;
		}
		if (!result.value){
			console.log('no data, ignore');
			return;
		}
		var eventName = result.value[0];
		var wholeDay = result.value[1];
		var startDay = moment(result.value[2], "YYYY/MM/DD HH:mm");
		var endDay = moment(result.value[3],"YYYY/MM/DD HH:mm");
		var textColor = result.value[4];
		var backgroundColor = result.value[5];
		var isPublic = result.value[6];
		
		console.log('eventName:' + eventName);
		console.log('startDay:' + startDay);
		console.log('endDay:' + endDay);
		console.log('wholeDay:' + wholeDay);
		console.log('textColor:' + textColor);
		console.log('backgroundColor:' + backgroundColor);
		console.log('isPublic:' + isPublic);
		console.log(p_calendar);
		var valObj = {};
		valObj.title = eventName;
        if (startDay.isValid()) {
   		    valObj.start = startDay.format("YYYY-MM-DDTHH:mm");
        }
        if (endDay.isValid()) {
   		    valObj.end = endDay.format("YYYY-MM-DDTHH:mm");
        }
   		valObj.allDay = wholeDay;
     	valObj.textColor = textColor;
       	valObj.color = backgroundColor;
       	valObj.isPublic = isPublic;
    	valObj.owner = p_owner;
       	if (p_event){
       		//valObj.textColor = p_event.textColor;
       		//valObj.color = p_event.backgroundColor;
       		p_event.remove();
       		cbData.action = 'update';
       		p_calendar.addEvent(valObj);
       	}
       	else{
       		cbData.action = 'add';
       	}
       	
       	//p_calendar.addEvent(valObj);
       	for (key in valObj) {
       	    cbData[key] = valObj[key];
       	}
       	zkbiSend('onCalEvent', cbData);
        
	}

	function addCalendarEvent(p_valObj){
		//console.log("addCalendarEvent " + p_valObj.id);
		calendar.addEvent(p_valObj);
	}

	function removeCalendarEvent(p_id) {
		//console.log("removeCalendarEvent " + p_id);
		const event = calendar.getEventById(p_id);
		if (event)
			event.remove();
	}

	function addCalendarEventList(p_eventList){
		console.log("addCalendarEventList " + p_eventList.length);
		//calendar.addEventSource(p_eventList);
		calendar.batchRendering(() => {
			for (let i in p_eventList)
				addCalendarEvent(p_eventList[i]);
		});
	}

	function removeCalendarEventList(p_idList) {
		console.log("removeCalendarEventList " + p_idList.length);
		calendar.batchRendering(() => {
			for (let i in p_idList)
				removeCalendarEvent(p_idList[i]);
		});
	}
	
	function showCalenderDayNumberFaIcon(p_dateStr, p_faIcon, p_faIconColor) {
		//console.log("showCalenderDayNumberFaIcon", p_dateStr + "," + p_faIcon + "," + p_faIconColor);
		const $el = $(calendar.el).find(".fc-dayGridMonth-view .fc-body .fc-day-grid .fc-day-top[data-date='"+p_dateStr+"'] .fc-day-number")
		if ($el.length === 0)
			return;
		if (p_faIcon) {
			if ($el.find(".icon").length === 0) {
				const $t = $("<div class='icon' style='display:inline-block'><i class='fa fa-" + p_faIcon + "'></i>&nbsp;</div>");
				if (p_faIconColor)
					$t.css("color", p_faIconColor);
				$el.prepend($t);
			}
		}
		else
			$el.find(".icon").remove();
	}

	function showCalenderDayNumberFaIconByList(p_dateStrList, p_faIcon, p_faIconColor) {
		console.log("showCalenderDayNumberFaIconByList " + p_dateStrList.length);
		for (let i in p_dateStrList)
			showCalenderDayNumberFaIcon(p_dateStrList[i], p_faIcon, p_faIconColor);
	}