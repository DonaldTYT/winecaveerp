<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
response.setHeader("Expires", "0"); // Proxies.
%>
<html>
<head>
<style type="text/css">
* {
	padding: 0;
	margin: 0;
}
body {
    background: transparent none;
}
#printContainer > div {
    position: relative;
    top: 0;
    left: 0;
    width: 1px;
    height: 1px;
    page-break-after: always;
    page-break-inside: avoid;
}
#printContainer > div > img {
  object-fit: contain;
  object-center: contain;
}
</style>
<script type="text/javascript" src="js/pdfjs/build/pdf.js"></script>
<script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>
<script type="text/javascript">
var CSS_UNITS = 96.0 / 72.0;
//var PRINT_RESOLUTION = 150;
var PRINT_RESOLUTION = 300;
var PRINT_UNITS = PRINT_RESOLUTION / 72.0;
/*var PRINT_PAGE_SIZE = {
	"A4": {"width": 595.275588, "height": 841.8897648, "safari_owidth": -114, "safari_oheight": -161},
	"A3": {"width": 841.8897648, "height": 1190.5511832, "safari_owidth": -174, "safari_oheight": -262}
};*/
function pdfPageToImage(scratchCanvas, printItem) {
    var img = document.createElement('img');
    img.style.width = printItem.width + 'px';
    img.style.height = printItem.height + 'px';
    if ('toBlob' in scratchCanvas && !PDFJS.disableCreateObjectURL) {
      scratchCanvas.toBlob(function (blob) {
        img.src = URL.createObjectURL(blob);
      });
    } else {
      img.src = scratchCanvas.toDataURL();
    }
    var wrapper = document.createElement('div');
    wrapper.appendChild(img);
    $('#printContainer')[0].appendChild(wrapper);
    return new Promise(function (resolve, reject) {
      img.onload = resolve;
      img.onerror = reject;
    });
}
$(document).ready(function(){
    var scratchCanvas = document.createElement("canvas");
    var url = '<%=request.getParameter("pdfurl")%>';
	console.log('url:' + url);
	PDFJS.cMapUrl = './js/pdfjs/web/cmaps/';
	PDFJS.cMapPacked = true;
	var loadingTask = PDFJS.getDocument(url);
	loadingTask.promise.then(function(pdfDocument) {
      	var pageCount = pdfDocument.numPages;
		console.log('PDF loaded pagecount:' + pageCount);
		
		var renderNextPage = function(pageNumber, resolve, reject) {
			pdfDocument.getPage(pageNumber).then(function(pdfPage){
				var viewport = pdfPage.getViewport(1);
				console.log('Page loaded pagenumber:' + pageNumber + ",vw:" + viewport.width + ",vh:" + viewport.height);	

				var pageWidth = viewport.width;
				var pageHeight = viewport.height;
				/*if (navigator.userAgent.toLowerCase().indexOf('safari') >= 0) {
					for (var k in PRINT_PAGE_SIZE) {
						var v = PRINT_PAGE_SIZE[k];
						if (Math.abs(v["width"] - viewport.width) < 1 && Math.abs(v["height"] - viewport.height) < 1) {
							pageWidth = viewport.width + v["safari_owidth"];
							pageHeight = viewport.height + v["safari_oheight"];
							break;	
						}
					}
				}*/


  				scratchCanvas.width = viewport.width * PRINT_UNITS;
  				scratchCanvas.height = viewport.height * PRINT_UNITS;
  				var width = pageWidth * CSS_UNITS;
  				var height = Math.floor(pageHeight * CSS_UNITS);
				console.log('Page loaded imgWidth:' + width + ',imgHeight:' + height);

  				var ctx = scratchCanvas.getContext('2d');
  				ctx.save();
  				ctx.fillStyle = 'rgb(255, 255, 255)';
  				ctx.fillRect(0, 0, scratchCanvas.width, scratchCanvas.height);
  				ctx.restore();
				var renderContext = {
					canvasContext: ctx,
					viewport: viewport,
      				transform: [PRINT_UNITS, 0, 0, PRINT_UNITS, 0, 0],
      				intent: 'print'
			    };
				pdfPage.render(renderContext).then(function () {
					console.log('Page rendered');
					//document.body.appendChild(canvas);
					pdfPageToImage(scratchCanvas, {width: width, height: height}).then(function(){
						if (++pageNumber <= pageCount)
							renderNextPage(pageNumber, resolve, reject);
						else
							resolve.call();
					}, function(){ reject.call() });
				}, function(){ reject.call(); });
				if (!$(document.body).hasClass('pdfjsprinting')) {
					$(document.body).addClass('pdfjsprinting');
					var style = document.createElement('style');
					//andrew210714 tmpfix, for print A5 portrait. remove @page to show print orientation in chrome
					//style.textContent = '@supports ((size:A4) and (size:1pt 1pt)) { @page { size: ' + pageWidth + 'pt ' + pageHeight + 'pt; margin: 0; } }';
					document.head.appendChild(style);
				}
			}, function(){ reject.call(); });
		};
		renderNextPage(1, function(){
			setTimeout(function(){
				window.print();
			}, 10);
		}, function(){
			this.parent.postMessage(url + ' print fail', '*');
		});
		$(window).bind('afterprint', function(){
			this.parent.postMessage(url + ' print finish', '*');
		});
	}, function(exception){
		console.error(exception);
		this.parent.postMessage(url + ' print fail', '*');
	});
});
</script>
</head>
<body>
<div id="printContainer"></div>
</body>
</html>