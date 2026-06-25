//andrew200616: obsolected, will remove it later
function generateUUID(){
    	var d = new Date().getTime();
    	return ""+d;
}
function setBrowserWindowId(uuid) {
		if(self.window.name.trim() == "") self.window.name=generateUUID();
		var msgDOM = jq('#' + uuid),
		msgWidget = zk.Widget.$(uuid);
		msgWidget.setValue(self.window.name);
		msgWidget.smartUpdate('value', self.window.name);
}
function DetectBrowserExit()
{
//   alert('Execute task which do you want before exit');
}
window.onbeforeunload = function(){ DetectBrowserExit(); }

function goBack() {
    window.history.back();
}