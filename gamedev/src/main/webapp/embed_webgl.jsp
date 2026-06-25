	<style>
	#unity-container.unity-desktop { 
  margin: auto;
  width: 250px;
  height: 250px;
	}
	</style>
    <div id="unity-container" class="unity-desktop" style="display:none;">
      <canvas id="unity-canvas" width=250 height=250 tabindex="-1"></canvas>
      <div id="unity-loading-bar">
        <div id="unity-logo"></div>
        <div id="unity-progress-bar-empty">
          <div id="unity-progress-bar-full"></div>
        </div>
      </div>
      <div id="unity-warning"> </div>
    </div>
     <script>

      var container = document.querySelector("#unity-container");
      var canvas = document.querySelector("#unity-canvas");
      var loadingBar = document.querySelector("#unity-loading-bar");
      var progressBarFull = document.querySelector("#unity-progress-bar-full");
      var fullscreenButton = document.querySelector("#unity-fullscreen-button");
      var warningBanner = document.querySelector("#unity-warning");

      // Shows a temporary message banner/ribbon for a few seconds, or
      // a permanent error message on top of the canvas if type=='error'.
      // If type=='warning', a yellow highlight color is used.
      // Modify or remove this function to customize the visually presented
      // way that non-critical warnings and error messages are presented to the
      // user.
      function unityShowBanner(msg, type) {
        function updateBannerVisibility() {
          warningBanner.style.display = warningBanner.children.length ? 'block' : 'none';
        }
        var div = document.createElement('div');
        div.innerHTML = msg;
        warningBanner.appendChild(div);
        if (type == 'error') div.style = 'background: red; padding: 10px;';
        else {
          if (type == 'warning') div.style = 'background: yellow; padding: 10px;';
          setTimeout(function() {
            warningBanner.removeChild(div);
            updateBannerVisibility();
          }, 5000);
        }
        updateBannerVisibility();
      }

//      var buildUrl = "https://sot.pedderhealth.com:19083/pmsdemo/webgl/barcodeScanner/Build";
//      var buildUrl = "http://localhost:8080/pmsdemo/webgl/barcodeScanner/Build";
      var buildUrl = "webgl/barcodeScanner/Build";
      var loaderUrl = buildUrl + "/webgl.loader.js";
      var config = {
        dataUrl: buildUrl + "/webgl.data",
        frameworkUrl: buildUrl + "/webgl.framework.js",
        codeUrl: buildUrl + "/webgl.wasm",
        streamingAssetsUrl: "StreamingAssets",
        companyName: "DefaultCompany",
        productName: "WIne3D",
        productVersion: "0.1.0",
        showBanner: unityShowBanner,
      };
        canvas.style.width = "250px";
        canvas.style.height = "250px";
      loadingBar.style.display = "block";
      
      function toggle_webgl() {
			//prompt("HAHA in webgl init 123");
			  var x = document.getElementById("unity-container");
			  if (x.style.display === "none") {
			    x.style.display = "block";
			  } else {
			    x.style.display = "none";
			  }
      };
      function start_webgl() {
        createUnityInstance(canvas, config, (progress) => {
          progressBarFull.style.width = 100 * progress + "%";
              }).then((unityInstance) => {
                loadingBar.style.display = "none";
              }).catch((message) => {
                alert(message);
              });
      };
      console.log("unity_webgl_initialized!");

      var script = document.createElement("script");
      script.src = loaderUrl;
      script.onload = () => {
            };
      document.body.appendChild(script);

    </script>  