/*
 * Optional camera/barcode helper for zkbiloader pages.
 *
 * Include this file only on pages that need camera support, then call:
 *   ZkBiCamera.open({ mode: "photo" });
 *   ZkBiCamera.open({ mode: "scanner", onScan: function(text) { ... } });
 *
 * The script is intentionally dormant until open(), startPhoto(), startScanner(),
 * takeNativePhoto(), or one of the legacy global wrappers is called.
 */
(function(window, document) {
  "use strict";

  var state = {
    created: false,
    visible: false,
    mode: "photo",
    stream: null,
    scannerStream: null,
    scanner: null,
    scannerRunning: false,
    scannerPaused: false,
    shouldRestartAfterNative: false,
    facingMode: "environment",
    lastBlob: null,
    lastDataUrl: null,
    lastObjectUrl: null,
    options: {},
    caps: {},
    settings: {}
  };

  var ids = {
    root: "zkbi-camera-root",
    panel: "zkbi-camera-panel",
    title: "zkbi-camera-title",
    viewport: "zkbi-camera-viewport",
    video: "zkbi-camera-video",
    reader: "zkbi-camera-reader",
    canvas: "zkbi-camera-canvas",
    preview: "zkbi-camera-preview",
    nativeInput: "zkbi-camera-native-input",
    status: "zkbi-camera-status",
    btnClose: "zkbi-camera-close",
    btnStartPhoto: "zkbi-camera-start-photo",
    btnStartScanner: "zkbi-camera-start-scanner",
    btnCapture: "zkbi-camera-capture",
    btnNative: "zkbi-camera-native",
    btnStop: "zkbi-camera-stop",
    btnTorch: "zkbi-camera-torch",
    btnSwitch: "zkbi-camera-switch",
    zoom: "zkbi-camera-zoom",
    focus: "zkbi-camera-focus"
  };

  var defaultOptions = {
    mode: "photo",
    facingMode: "environment",
    autoStart: true,
    autoCloseAfterCapture: false,
    autoStopAfterScan: false,
    imageType: "image/jpeg",
    imageQuality: 0.92,
    scannerFps: 10,
    scannerQrboxRatio: 0.7,
    scannerScriptUrl: "https://unpkg.com/html5-qrcode@2.3.7/html5-qrcode.min.js",
    showPreview: false,
    onCapture: null,
    onScan: null,
    onError: null,
    onClose: null,
    log: false
  };

  function extend(base, extra) {
    var out = {}, k;
    for (k in base) if (Object.prototype.hasOwnProperty.call(base, k)) out[k] = base[k];
    extra = extra || {};
    for (k in extra) if (Object.prototype.hasOwnProperty.call(extra, k)) out[k] = extra[k];
    return out;
  }

  function el(id) {
    return document.getElementById(id);
  }

  function log(message) {
    if (state.options.log && window.console) console.log("[ZkBiCamera] " + message);
    var status = el(ids.status);
    if (status) status.textContent = message || "";
  }

  function fail(error) {
    var message = error && error.message ? error.message : String(error || "Camera error");
    log(message);
    if (typeof state.options.onError === "function") state.options.onError(error);
  }

  function isSecureCameraContext() {
    return window.isSecureContext || location.hostname === "localhost" || location.hostname === "127.0.0.1";
  }

  function ensureStyle() {
    if (document.getElementById("zkbi-camera-style")) return;
    var style = document.createElement("style");
    style.id = "zkbi-camera-style";
    style.type = "text/css";
    style.appendChild(document.createTextNode(
      "#zkbi-camera-root{display:none;position:fixed;inset:0;z-index:2147483000;background:rgba(0,0,0,.72);font-family:Arial,sans-serif;color:#f5fff5;text-align:left}" +
      "#zkbi-camera-root.zkbi-camera-open{display:block}" +
      "#zkbi-camera-panel{position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);width:min(96vw,440px);max-height:96vh;background:#070b0a;border:1px solid #19b568;border-radius:8px;box-shadow:0 18px 60px rgba(0,0,0,.45);overflow:auto}" +
      "#zkbi-camera-panel *{box-sizing:border-box}" +
      "#zkbi-camera-head{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:8px 10px;border-bottom:1px solid rgba(25,181,104,.45)}" +
      "#zkbi-camera-title{font-size:14px;font-weight:bold;color:#dfffe9;line-height:24px}" +
      "#zkbi-camera-close{width:28px;height:28px;border-radius:4px;border:1px solid #19b568;background:#101614;color:#dfffe9;cursor:pointer}" +
      "#zkbi-camera-body{padding:10px}" +
      "#zkbi-camera-viewport{position:relative;width:100%;aspect-ratio:1/1;background:#000;border:1px solid #19b568;border-radius:6px;overflow:hidden}" +
      "#zkbi-camera-video,#zkbi-camera-reader,#zkbi-camera-reader video{width:100%!important;height:100%!important;object-fit:contain!important;background:#000}" +
      "#zkbi-camera-reader{display:none}" +
      "#zkbi-camera-preview{display:none;width:100%;margin-top:8px;border:1px solid #19b568;border-radius:6px;background:#000}" +
      "#zkbi-camera-controls{display:flex;gap:6px;flex-wrap:wrap;align-items:center;margin-top:8px}" +
      "#zkbi-camera-controls button,#zkbi-camera-controls select{height:30px;border:1px solid #19b568;background:#101614;color:#dfffe9;border-radius:4px;padding:3px 8px;font-size:12px}" +
      "#zkbi-camera-controls button{cursor:pointer}" +
      "#zkbi-camera-controls button:disabled,#zkbi-camera-controls select:disabled{opacity:.48;cursor:not-allowed}" +
      "#zkbi-camera-controls label{display:flex;align-items:center;gap:5px;color:#dfffe9;font-size:12px}" +
      "#zkbi-camera-zoom{width:92px}" +
      "#zkbi-camera-status{min-height:18px;margin-top:7px;color:#9ee8ba;font-size:12px;white-space:pre-wrap}" +
      "#zkbi-camera-canvas,#zkbi-camera-native-input{display:none}"
    ));
    document.head.appendChild(style);
  }

  function ensureDom() {
    if (state.created) return;
    ensureStyle();

    var root = document.createElement("div");
    root.id = ids.root;
    root.innerHTML =
      '<div id="' + ids.panel + '" role="dialog" aria-modal="true">' +
        '<div id="zkbi-camera-head">' +
          '<div id="' + ids.title + '">Camera</div>' +
          '<button type="button" id="' + ids.btnClose + '" title="Close">x</button>' +
        '</div>' +
        '<div id="zkbi-camera-body">' +
          '<div id="' + ids.viewport + '">' +
            '<video id="' + ids.video + '" autoplay playsinline muted></video>' +
            '<div id="' + ids.reader + '"></div>' +
          '</div>' +
          '<img id="' + ids.preview + '" alt="Captured photo preview" />' +
          '<canvas id="' + ids.canvas + '"></canvas>' +
          '<input id="' + ids.nativeInput + '" type="file" accept="image/*" capture="environment" />' +
          '<div id="zkbi-camera-controls">' +
            '<button type="button" id="' + ids.btnStartPhoto + '">Photo</button>' +
            '<button type="button" id="' + ids.btnStartScanner + '">Scan</button>' +
            '<button type="button" id="' + ids.btnCapture + '" disabled>Capture</button>' +
            '<button type="button" id="' + ids.btnNative + '">Native</button>' +
            '<button type="button" id="' + ids.btnTorch + '" disabled>Flash</button>' +
            '<button type="button" id="' + ids.btnSwitch + '">Switch</button>' +
            '<label>Zoom <input id="' + ids.zoom + '" type="range" min="1" max="1" step="0.1" value="1" disabled></label>' +
            '<select id="' + ids.focus + '" disabled><option value="">Focus</option></select>' +
            '<button type="button" id="' + ids.btnStop + '" disabled>Stop</button>' +
          '</div>' +
          '<div id="' + ids.status + '"></div>' +
        '</div>' +
      '</div>';
    document.body.appendChild(root);

    el(ids.btnClose).onclick = close;
    el(ids.btnStartPhoto).onclick = function() { startPhoto(); };
    el(ids.btnStartScanner).onclick = function() { startScanner(); };
    el(ids.btnCapture).onclick = function() { capturePhoto(); };
    el(ids.btnNative).onclick = function() { takeNativePhoto(); };
    el(ids.btnTorch).onclick = function() { toggleTorch(); };
    el(ids.btnSwitch).onclick = function() { switchCamera(); };
    el(ids.btnStop).onclick = function() { stop(); };
    el(ids.zoom).oninput = function() { setZoom(Number(this.value)); };
    el(ids.focus).onchange = function() { if (this.value) setFocusMode(this.value); };
    el(ids.nativeInput).onchange = onNativeFileChange;

    state.created = true;
  }

  function show() {
    ensureDom();
    el(ids.root).className = "zkbi-camera-open";
    state.visible = true;
  }

  function hide() {
    if (state.created) el(ids.root).className = "";
    state.visible = false;
  }

  function setModeUI(mode) {
    state.mode = mode || state.mode;
    el(ids.title).textContent = state.mode === "scanner" ? "Barcode Scanner" : "Camera";
    el(ids.video).style.display = state.mode === "scanner" ? "none" : "block";
    el(ids.reader).style.display = state.mode === "scanner" ? "block" : "none";
  }

  function getTrack() {
    if (state.scannerStream && typeof state.scannerStream.getVideoTracks === "function") {
      var scannerTracks = state.scannerStream.getVideoTracks();
      if (scannerTracks && scannerTracks.length) return scannerTracks[0];
    }
    if (state.stream && typeof state.stream.getVideoTracks === "function") {
      var tracks = state.stream.getVideoTracks();
      if (tracks && tracks.length) return tracks[0];
    }
    var video = getActiveVideo();
    var stream = video && video.srcObject;
    if (stream && typeof stream.getVideoTracks === "function") {
      var videoTracks = stream.getVideoTracks();
      if (videoTracks && videoTracks.length) return videoTracks[0];
    }
    return null;
  }

  function getActiveVideo() {
    var scannerVideo = document.querySelector("#" + ids.reader + " video");
    if (state.mode === "scanner" && scannerVideo) return scannerVideo;
    return el(ids.video) || scannerVideo;
  }

  function stopStream() {
    if (state.stream && typeof state.stream.getTracks === "function") {
      var tracks = state.stream.getTracks();
      for (var i = 0; i < tracks.length; i++) tracks[i].stop();
    }
    state.stream = null;
    var video = el(ids.video);
    if (video) video.srcObject = null;
  }

  function setButtons(running) {
    el(ids.btnCapture).disabled = !running;
    el(ids.btnStop).disabled = !running;
    refreshCapabilityUI();
  }

  function refreshCapabilities() {
    var track = getTrack();
    state.caps = {};
    state.settings = {};
    if (!track) return;
    try { if (typeof track.getCapabilities === "function") state.caps = track.getCapabilities() || {}; } catch (ignore1) {}
    try { if (typeof track.getSettings === "function") state.settings = track.getSettings() || {}; } catch (ignore2) {}
  }

  function refreshCapabilityUI() {
    refreshCapabilities();
    var caps = state.caps || {};
    var settings = state.settings || {};
    var zoom = el(ids.zoom);
    var torch = el(ids.btnTorch);
    var focus = el(ids.focus);

    if (caps.zoom) {
      zoom.disabled = false;
      zoom.min = caps.zoom.min != null ? caps.zoom.min : 1;
      zoom.max = caps.zoom.max != null ? caps.zoom.max : 1;
      zoom.step = caps.zoom.step != null ? caps.zoom.step : 0.1;
      zoom.value = settings.zoom != null ? settings.zoom : zoom.min;
      log("Zoom ready: " + zoom.min + "..." + zoom.max);
    } else {
      zoom.disabled = true;
      zoom.min = 1;
      zoom.max = 1;
      zoom.value = 1;
      if (state.mode === "scanner" && state.scannerRunning) log("Scanner ready. Zoom is not exposed by this browser/device.");
    }

    torch.disabled = !caps.torch;

    while (focus.options.length > 1) focus.remove(1);
    if (caps.focusMode && caps.focusMode.length) {
      focus.disabled = false;
      for (var i = 0; i < caps.focusMode.length; i++) {
        var opt = document.createElement("option");
        opt.value = caps.focusMode[i];
        opt.text = caps.focusMode[i];
        focus.appendChild(opt);
      }
      focus.value = settings.focusMode || "";
    } else {
      focus.disabled = true;
      focus.value = "";
    }
  }

  function applyAdvancedConstraints(values) {
    var track = getTrack();
    if (!track || typeof track.applyConstraints !== "function") return Promise.reject(new Error("Camera constraints are not supported"));
    return track.applyConstraints({ advanced: [values] }).then(function() {
      refreshCapabilityUI();
    });
  }

  function setZoom(value) {
    return applyAdvancedConstraints({ zoom: value }).catch(fail);
  }

  function setTorch(enabled) {
    return applyAdvancedConstraints({ torch: !!enabled }).catch(fail);
  }

  function toggleTorch() {
    refreshCapabilities();
    var next = !state.settings.torch;
    return setTorch(next).then(function() {
      el(ids.btnTorch).textContent = next ? "Flash Off" : "Flash";
    });
  }

  function setFocusMode(mode) {
    return applyAdvancedConstraints({ focusMode: mode }).catch(fail);
  }

  function waitForVideoReady(video, timeoutMs) {
    timeoutMs = timeoutMs || 4000;
    return new Promise(function(resolve, reject) {
      var startTime = new Date().getTime();
      function check() {
        if (video && video.videoWidth > 0 && video.readyState >= 2) {
          resolve(video);
          return;
        }
        if (new Date().getTime() - startTime > timeoutMs) {
          reject(new Error("Video element is not ready"));
          return;
        }
        window.requestAnimationFrame(check);
      }
      check();
    });
  }

  function getMediaConstraints() {
    return {
      video: {
        facingMode: state.facingMode || "environment",
        width: { ideal: 1920 },
        height: { ideal: 1080 }
      },
      audio: false
    };
  }

  function startPhoto(options) {
    state.options = extend(state.options || defaultOptions, options || {});
    show();
    setModeUI("photo");
    stopScannerOnly();
    stopStream();

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      fail(new Error("getUserMedia is not available in this browser"));
      return Promise.reject(new Error("getUserMedia is not available"));
    }
    if (!isSecureCameraContext()) {
      fail(new Error("Use HTTPS or localhost for camera access"));
      return Promise.reject(new Error("Camera requires HTTPS or localhost"));
    }

    log("Starting camera...");
    return navigator.mediaDevices.getUserMedia(getMediaConstraints()).then(function(stream) {
      var video = el(ids.video);
      state.stream = stream;
      video.srcObject = stream;
      return waitForVideoReady(video).catch(function() { return video; });
    }).then(function() {
      setButtons(true);
      log("Camera ready");
    }).catch(function(error) {
      setButtons(false);
      fail(error);
      throw error;
    });
  }

  function stopScannerOnly() {
    var scanner = state.scanner;
    state.scannerRunning = false;
    state.scannerPaused = false;
    state.scannerStream = null;
    if (scanner && typeof scanner.stop === "function") {
      try {
        return scanner.stop().catch(function() {});
      } catch (ignore) {}
    }
    return Promise.resolve();
  }

  function startScanner(options) {
    state.options = extend(state.options || defaultOptions, options || {});
    show();
    setModeUI("scanner");
    stopStream();

    if (!isSecureCameraContext()) {
      fail(new Error("Use HTTPS or localhost for camera access"));
      return Promise.reject(new Error("Camera requires HTTPS or localhost"));
    }

    return ensureHtml5Qrcode().then(function() {
      if (!state.scanner) state.scanner = new window.Html5Qrcode(ids.reader);
      log("Starting scanner...");
      return state.scanner.start(
      { facingMode: state.facingMode || "environment" },
      {
        fps: state.options.scannerFps,
        qrbox: function(viewWidth, viewHeight) {
          var size = Math.floor(Math.min(viewWidth, viewHeight) * state.options.scannerQrboxRatio);
          return { width: size, height: size };
        }
      },
      function(text, result) {
        log("Scanned: " + text);
        if (typeof state.options.onScan === "function") state.options.onScan(text, result);
        if (state.options.autoStopAfterScan) stop();
      },
      function() {}
      );
    }).then(function() {
      state.scannerRunning = true;
      setButtons(true);
      cacheScannerStreamAndRefresh(0);
      setTimeout(function() { cacheScannerStreamAndRefresh(1); }, 120);
      setTimeout(function() { cacheScannerStreamAndRefresh(2); }, 500);
      setTimeout(function() { cacheScannerStreamAndRefresh(3); }, 1200);
      log("Scanner ready");
    }).catch(function(error) {
      state.scannerRunning = false;
      setButtons(false);
      fail(error);
      throw error;
    });
  }

  function cacheScannerStreamAndRefresh(attempt) {
    var video = document.querySelector("#" + ids.reader + " video");
    if (video && video.srcObject && typeof video.srcObject.getVideoTracks === "function") {
      state.scannerStream = video.srcObject;
      refreshCapabilityUI();
      return;
    }
    refreshCapabilityUI();
    if (attempt < 8) {
      setTimeout(function() { cacheScannerStreamAndRefresh(attempt + 1); }, 250);
    }
  }

  function ensureHtml5Qrcode() {
    if (window.Html5Qrcode) return Promise.resolve();
    if (!state.options.scannerScriptUrl) {
      return Promise.reject(new Error("Html5Qrcode is not loaded"));
    }
    if (window.__zkbiCameraHtml5QrcodeLoading) return window.__zkbiCameraHtml5QrcodeLoading;

    log("Loading barcode scanner library...");
    window.__zkbiCameraHtml5QrcodeLoading = new Promise(function(resolve, reject) {
      var existing = document.querySelector('script[data-zkbi-camera-html5-qrcode="Y"]');
      if (existing) {
        existing.addEventListener("load", function() { window.Html5Qrcode ? resolve() : reject(new Error("Html5Qrcode failed to initialize")); });
        existing.addEventListener("error", function() { reject(new Error("Unable to load html5-qrcode")); });
        return;
      }
      var script = document.createElement("script");
      script.src = state.options.scannerScriptUrl;
      script.async = true;
      script.setAttribute("data-zkbi-camera-html5-qrcode", "Y");
      script.onload = function() {
        if (window.Html5Qrcode) resolve();
        else reject(new Error("Html5Qrcode failed to initialize"));
      };
      script.onerror = function() {
        reject(new Error("Unable to load html5-qrcode from " + state.options.scannerScriptUrl));
      };
      document.head.appendChild(script);
    });
    return window.__zkbiCameraHtml5QrcodeLoading;
  }

  function capturePhoto() {
    var video = getActiveVideo();
    if (!video) {
      fail(new Error("No active video"));
      return Promise.reject(new Error("No active video"));
    }

    var resumeScanner = false;
    if (state.scannerRunning && state.scanner && typeof state.scanner.pause === "function") {
      try {
        state.scanner.pause(true);
        state.scannerPaused = true;
        resumeScanner = true;
      } catch (ignore) {}
    }

    return waitForVideoReady(video).then(function(readyVideo) {
      var canvas = el(ids.canvas);
      var context = canvas.getContext("2d");
      canvas.width = readyVideo.videoWidth;
      canvas.height = readyVideo.videoHeight;
      context.drawImage(readyVideo, 0, 0, canvas.width, canvas.height);
      return canvasToCapture(canvas);
    }).then(function(capture) {
      if (typeof state.options.onCapture === "function") state.options.onCapture(capture);
      if (state.options.autoCloseAfterCapture) close();
      return capture;
    }).catch(function(error) {
      fail(error);
      throw error;
    }).then(function(capture) {
      if (resumeScanner) resumeScannerIfNeeded();
      return capture;
    }, function(error) {
      if (resumeScanner) resumeScannerIfNeeded();
      throw error;
    });
  }

  function canvasToCapture(canvas) {
    return new Promise(function(resolve) {
      var dataUrl = null;
      try { dataUrl = canvas.toDataURL(state.options.imageType, state.options.imageQuality); } catch (ignore) {}
      state.lastDataUrl = dataUrl;

      function finish(blob) {
        state.lastBlob = blob || null;
        if (state.lastObjectUrl) {
          try { window.URL.revokeObjectURL(state.lastObjectUrl); } catch (ignore) {}
        }
        state.lastObjectUrl = blob ? window.URL.createObjectURL(blob) : dataUrl;

        var preview = el(ids.preview);
        if (state.options.showPreview) {
          preview.src = state.lastObjectUrl || dataUrl || "";
          preview.style.display = preview.src ? "block" : "none";
        } else if (preview) {
          preview.removeAttribute("src");
          preview.style.display = "none";
        }
        log("Photo captured");
        resolve({
          blob: blob || null,
          dataUrl: dataUrl,
          objectUrl: state.lastObjectUrl,
          width: canvas.width,
          height: canvas.height
        });
      }

      if (canvas.toBlob) {
        canvas.toBlob(finish, state.options.imageType, state.options.imageQuality);
      } else {
        finish(dataUrlToBlob(dataUrl));
      }
    });
  }

  function dataUrlToBlob(dataUrl) {
    if (!dataUrl) return null;
    var parts = dataUrl.split(",");
    if (parts.length < 2) return null;
    var header = parts[0];
    var mimeMatch = /data:([^;]+)/.exec(header);
    var mime = mimeMatch ? mimeMatch[1] : "image/jpeg";
    var binary = window.atob(parts[1]);
    var len = binary.length;
    var bytes = new Uint8Array(len);
    for (var i = 0; i < len; i++) bytes[i] = binary.charCodeAt(i);
    return new Blob([bytes], { type: mime });
  }

  function resumeScannerIfNeeded() {
    if (state.scannerPaused && state.scanner && typeof state.scanner.resume === "function") {
      try { state.scanner.resume(); } catch (ignore) {}
      state.scannerPaused = false;
    }
  }

  function takeNativePhoto(options) {
    state.options = extend(state.options || defaultOptions, options || {});
    ensureDom();
    var input = el(ids.nativeInput);
    state.shouldRestartAfterNative = state.scannerRunning || !!state.stream;
    var previousMode = state.mode;
    return stop().then(function() {
      state.mode = previousMode;
      input.value = "";
      input.click();
    });
  }

  function onNativeFileChange() {
    var input = el(ids.nativeInput);
    var file = input.files && input.files[0];
    if (!file) {
      restartAfterNativeIfNeeded();
      return;
    }
    if (state.lastObjectUrl) {
      try { window.URL.revokeObjectURL(state.lastObjectUrl); } catch (ignore) {}
    }
    state.lastBlob = file;
    state.lastDataUrl = null;
    state.lastObjectUrl = window.URL.createObjectURL(file);
    var preview = el(ids.preview);
    if (state.options.showPreview) {
      preview.src = state.lastObjectUrl;
      preview.style.display = "block";
    } else if (preview) {
      preview.removeAttribute("src");
      preview.style.display = "none";
    }
    var capture = {
      blob: file,
      dataUrl: null,
      objectUrl: state.lastObjectUrl,
      width: null,
      height: null
    };
    if (typeof state.options.onCapture === "function") state.options.onCapture(capture);
    restartAfterNativeIfNeeded();
  }

  function restartAfterNativeIfNeeded() {
    if (!state.shouldRestartAfterNative) return;
    state.shouldRestartAfterNative = false;
    if (state.mode === "scanner") startScanner().catch(function() {});
    else startPhoto().catch(function() {});
  }

  function switchCamera() {
    state.facingMode = state.facingMode === "environment" ? "user" : "environment";
    if (!state.visible) return Promise.resolve();
    if (state.mode === "scanner") return stopScannerOnly().then(function() { return startScanner(); });
    return startPhoto();
  }

  function stop() {
    var scannerPromise = stopScannerOnly();
    stopStream();
    setButtons(false);
    log("Camera stopped");
    return scannerPromise;
  }

  function close() {
    return stop().then(function() {
      hide();
      if (typeof state.options.onClose === "function") state.options.onClose();
    });
  }

  function open(options) {
    state.options = extend(defaultOptions, options || {});
    state.facingMode = state.options.facingMode || "environment";
    show();
    setModeUI(state.options.mode || "photo");
    if (state.options.autoStart === false) return Promise.resolve();
    if (state.mode === "scanner") return startScanner();
    return startPhoto();
  }

  window.ZkBiCamera = {
    open: open,
    close: close,
    stop: stop,
    startPhoto: startPhoto,
    startScanner: startScanner,
    capturePhoto: capturePhoto,
    takeNativePhoto: takeNativePhoto,
    setTorch: setTorch,
    toggleTorch: toggleTorch,
    setZoom: setZoom,
    setFocusMode: setFocusMode,
    switchCamera: switchCamera,
    getCapabilities: function() { refreshCapabilities(); return extend({}, state.caps); },
    getSettings: function() { refreshCapabilities(); return extend({}, state.settings); },
    getLastCapture: function() {
      return {
        blob: state.lastBlob,
        dataUrl: state.lastDataUrl,
        objectUrl: state.lastObjectUrl
      };
    },
    isRunning: function() { return !!(state.stream || state.scannerRunning); }
  };

  window.startWebcamCamera = function(options) {
    return window.ZkBiCamera.open(extend({ mode: "photo" }, options || {}));
  };
  window.startWebcamScanner = function(options) {
    return window.ZkBiCamera.open(extend({ mode: "scanner" }, options || {}));
  };
  window.stopWebcamScanner = function() {
    return window.ZkBiCamera.close();
  };
  window.captureWebcamPhoto = function() {
    return window.ZkBiCamera.capturePhoto();
  };
})(window, document);
