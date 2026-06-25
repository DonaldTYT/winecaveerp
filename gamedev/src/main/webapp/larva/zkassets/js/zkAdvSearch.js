var zkAdvSearchHelper = {};

// Function provided by Tim Down at https://stackoverflow.com/a/28275304/1009922
zkAdvSearchHelper.createCollapsedRangeFromPoint = function(x, y) {
	var position, range = null;
    if (document.caretPositionFromPoint) {
        position = document.caretPositionFromPoint(x, y);
        range = document.createRange();
        range.setStart(position.offsetNode, position.offset);
        range.collapse(true);
    } else if (document.caretRangeFromPoint) {
        range = document.caretRangeFromPoint(x, y);
    } else if (document.body.createTextRange) {
        range = document.body.createTextRange();
        range.moveToPoint(x, y);
    }
    return range;
};
zkAdvSearchHelper.findPredicateFieldRange = function(p_ce, p_fieldNum) {
	for (var i = 0; i < p_ce[0].childNodes.length; i++) {
		if ($(p_ce[0].childNodes[i]).attr('fieldNum') === '' + p_fieldNum) {
			var range = document.createRange();
			range.setStart(p_ce[0], i);
			range.setEnd(p_ce[0], i + 1);
			return range;
		}
	}
	return null;
}
zkAdvSearchHelper.bindPredicateFieldElement = function(p_ce) {
	//setup field num
	var $field = p_ce.find('.field');
	var count = p_ce.attr('maxFieldNum');
	if (!count)
		count = 0;
	$field.each(function(i, f) {
		if (!$(f).attr('fieldNum'))
			$(f).attr('fieldNum', ++count);
	});
	p_ce.attr('maxFieldNum', count);

	//click field span to select text range
   	$field.off('click').on('click', function(e) {
   		var range = zkAdvSearchHelper.findPredicateFieldRange(p_ce, $(e.target).attr('fieldNum'));
   		if (range) {
       		var sel = window.getSelection();
       		sel.removeAllRanges(); 
       		sel.addRange(range);
   		}
   	});
}

function zkAdvSearchBindPredicateElement(p_ceId, p_placeholder) {
	if (!p_ceId){
   	   console.log('invalid param');
   	   return;
   	}
   	//var ce = $('#' + p_ceId);
   	var ce = $('#' + jq('$' + p_ceId).attr('id'));
   	if (ce.length == 0){
   	   console.log('ce not found');
   	   return;
   	}
   	var comp = zk.Widget.$("$" + p_ceId);
   	if (!comp){
   	   console.log('comp not found');
   	   return;
   	}
   	
   	ce.prop('contenteditable', 'true');
   	ce.addClass('zkbi-ce-placeholder');
   	//ce.attr('data-placeholder','Please drag data field here to build predicate');
   	ce.attr('data-placeholder',p_placeholder);
   	

   	//handle value update
   	//ie11 no support input event
   	ce.on('blur', function(e) {
   		console.log('blur');
   		//cancel old send event
   		/*var oldSendEvent = ce.data('sendEvent');
   		if (oldSendEvent)
   			clearTimeout(oldSendEvent);*/
   		
   		var arr = [];
   		ce.contents().each(function(idx, val) {
   			var curVal = $(val).data('cellfullname');
   			if (!curVal)
   				curVal = $(val).text();
   			arr.push(curVal);
   		});
   		//join and remove extra spaces
   		var text = arr.join(" ").replace(/\s\s+/g, ' ').trim();
	
   		//send to zk server
   		/*ce.data('sendEvent', setTimeout(function() {
   			zAu.send(new zk.Event(comp, 'onValueChange', {value: encodeURIComponent(text)}));
   		}, 500));*/
   		if (ce.data('predicateValue') !== text) {
   			console.log(text);
   			zAu.send(new zk.Event(comp, 'onValueChange', {value: encodeURIComponent(text)}));
   			ce.data('predicateValue', text);
   		}
   	});
   	
   	//ignore enter
   	ce.on('keydown', function(e) { 
       	if (e.keyCode === 13) {
       		console.log('ignore enter');
       		e.preventDefault();
       		return false;
       	}
   	}); 
   	
   	//copy or cut text range
   	ce.on('copy cut', function(e) {
   		console.log(e.type);
     	e.preventDefault();
     	var range = window.getSelection().getRangeAt(0);
   		var frag = e.type === 'cut' ? range.extractContents() : range.cloneContents();
		var s = '';
		for (var i = 0; i < frag.childNodes.length; i++) {
			var node = frag.childNodes[i];
			switch (node.nodeType) {
			case 1:
				s += $(node.outerHTML).removeAttr('fieldNum').prop('outerHTML');
				break;
			case 3:
				s += node.data;
				break;
			}
		};
		if (e.originalEvent.clipboardData)
			e.originalEvent.clipboardData.setData("advSearchData", s);
		else if (window.clipboardData)
			window.clipboardData.setData("text", "advSearchData:" + s);
     	//if (e.type === 'cut')
     	//	ce.trigger('input');
   	});
   	//paste text range
   	ce.on('paste', function(e) {
   		console.log('paste');
		var df = null;
		if (e.originalEvent.clipboardData)
			df = e.originalEvent.clipboardData.getData("advSearchData");
		else if (window.clipboardData) {
			df = window.clipboardData.getData("text");
			if (df !== null && df.startsWith("advSearchData:"))
				df = df.substring(14);
			else
				df = null;
		}
   	    if (df) {
   	    	e.preventDefault();
   	    	//document.execCommand('insertHTML', false, df);
   	    	var range = window.getSelection().getRangeAt(0);
   	    	var frag = range.createContextualFragment(df);
   	    	range.deleteContents();
   	    	range.insertNode(frag);
   	    	range.collapse(false);
   	    	
   	    	zkAdvSearchHelper.bindPredicateFieldElement(ce);
   	    }
   	});
}
function zkAdvSearchSetPredicateHtml(p_ceId, p_html) {
	if (!p_ceId){
   	   console.log('invalid param');
   	   return;
   	}
   	//var ce = $('#' + p_ceId);
   	var ce = $('#' + jq('$' + p_ceId).attr('id'));
   	if (ce.length == 0){
   	   console.log('ce not found');
   	   return;
   	}
	ce.html(p_html);

	zkAdvSearchHelper.bindPredicateFieldElement(ce);
	//ce.trigger('input');
}
function zkAdvSearchAppendPredicateHtml(p_ceId, p_html, p_pageX, p_pageY) {
	if (!p_ceId){
   	   console.log('invalid param');
   	   return;
   	}
   	//var ce = $('#' + p_ceId);
   	var ce = $('#' + jq('$' + p_ceId).attr('id'));
   	if (ce.length == 0){
   	   console.log('ce not found');
   	   return;
   	}

   	ce.focus();
   	var r = zkAdvSearchHelper.createCollapsedRangeFromPoint(p_pageX, p_pageY);
 	var range;
   	var dropElement;
   	if (r.select) {
   		//is TextRange
   		r.select();
    	range = window.getSelection().getRangeAt(0);
   		dropElement = $(r.parentElement());
   	}
   	else {
   		range = r;
   		dropElement = $(r.startContainer.parentNode);
   	}
   	if (dropElement.is('span')) {
   		range = zkAdvSearchHelper.findPredicateFieldRange(ce, dropElement.attr('fieldNum'));
   		if (range)
   			range.collapse(true);
   		else
   			dropElement.before(p_html);
   	}

   	if (range) {
   		if (range.previousSibling || range.startOffset > 0)
   			p_html = "&nbsp;" + p_html;
        var frag = range.createContextualFragment(p_html);
        range.insertNode(frag);
        range.collapse(false);
        var sel = window.getSelection();
        sel.removeAllRanges(); 
        sel.addRange(range);
   	}

	zkAdvSearchHelper.bindPredicateFieldElement(ce);
	//ce.trigger('input');
}