// sample: <li><a href="#" onclick='jxzksubmit("jxzkloader.html?zul=JxZkSample.zul", {var1: "val1", var2: "val2"}, "post")'><img src="images/testing.jpg" /></a></li>
function jxzksubmit(path, params, method) {
    var form = document.createElement("form");
    form.setAttribute("method", method);
    form.setAttribute("action", path);

    for(var key in params) {
        if(params.hasOwnProperty(key)) {
            var hiddenField = document.createElement("input");
            hiddenField.setAttribute("type", "hidden");
            hiddenField.setAttribute("name", key);
            hiddenField.setAttribute("value", params[key]);
            form.appendChild(hiddenField);
         }
    }
    document.body.appendChild(form);
    form.submit();
}