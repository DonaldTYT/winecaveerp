var ZkPrint = {};
ZkPrint.print = function(uuid, uri, data) {
    if (uuid && uri && uuid !== '' && uri !== '') {
        var wgt = zk.Widget.$(uuid),
            body = document.body,
            ifr = jq('#zk_printframe');
        if (!ifr[0]) {
            jq(body).append('<iframe id="zk_printframe" name="zk_printframe"' +
                ' style="width:0;height:0;border:0;position:fixed;"'+
                '></iframe>');
            ifr = jq('#zk_printframe');
        }
        // wait form submit response then call print function
        // reference: http://isometriks.com/jquery-ajax-form-submission-with-iframes
        ifr.unbind('load.ajaxsubmit').bind('load.ajaxsubmit', function() {
        	setTimeout(function(){
        		var iw = ifr[0].contentWindow || ifr[0];
        		iw.document.body.focus();
        		iw.print();
        	}, 100);
        });
         
        /*jq(body).append('<form id="zk_printform" action="' + uri + '" method="post" target="zk_printframe"></form>');
        var form = jq('#zk_printform');
        var content = '<div style="width: ' + wgt.$n().offsetWidth + 'px">' + jq(wgt.$n())[0].outerHTML + '</div>';
        form.append(jq('<input/>').attr({name: 'printContent', value: content}));
        if (cssuri) {
            form.append(jq('<input/>').attr({name: 'printStyle', value: cssuri}));
        }
        form.submit().remove();*/
        var $content = jq(wgt.$n()).clone();
        $content.find('input').each(function(){
        	var $this = jq(this);
        	$this.attr('value', $this.val());
        	if ($this.attr('type') == 'checkbox') {
        		if ($this.prop('checked'))
        			$this.attr('checked', 'checked');
        	}
        });
        var postData = {
        	printContent : '<div style="width: ' + wgt.$n().offsetWidth + 'px">' + $content.html() + '</div>'
        };
        if (data) 
        	jq.extend(postData, data);
        jq.post(uri, postData, function(data){
        	var iw = ifr[0].contentWindow || ifr[0];
			var doc = iw.document;
			doc.open('text/html', 'replace');
			doc.write(data);
			doc.close();
        });
    } else {
        window.print();
    }
}
ZkPrint.printFromStream = function(streamUri, mimeType) {
    if (streamUri && streamUri !== '' && mimeType && mimeType !== '') {
        var body = document.body,
            ifr = jq('#zk_printframe');
        if (!ifr[0]) {
            jq(body).append('<iframe id="zk_printframe" name="zk_printframe"' +
                ' style="display:none;width:0;height:0;border:1;position:fixed;"'+
                '></iframe>');
            ifr = jq('#zk_printframe');
        }
        if (mimeType === 'application/pdf') {
        	ifr.attr('src', 'zkprint_pdf.jsp?pdfurl=' + encodeURIComponent(streamUri));
	        $(window).unbind('message').one('message', function(e){
	        	if (e.originalEvent.data == streamUri + ' print finish') {
	        	}
	        });
        } else {
        	ifr.attr('src', streamUri);
        	ifr.unbind('load').bind('load', function() {
	        	setTimeout(function(){
	        		var iw = ifr[0].contentWindow || ifr[0];
	        		iw.document.body.focus();
	        		iw.print();
	        	}, 100);
	        });
        }
    } else
        window.print();
}