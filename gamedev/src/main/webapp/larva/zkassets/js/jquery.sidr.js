/*! sidr - v2.2.1 - 2016-02-17
 * http://www.berriart.com/sidr/
 * Copyright (c) 2013-2016 Alberto Varela; Licensed MIT */

(function () {
  'use strict';

  var babelHelpers = {};
  var isDisable = false;
  var lastOpenStatus = true;

  babelHelpers.classCallCheck = function (instance, Constructor) {
    if (!(instance instanceof Constructor)) {
      throw new TypeError("Cannot call a class as a function");
    }
  };

  babelHelpers.createClass = function () {
    function defineProperties(target, props) {
      for (var i = 0; i < props.length; i++) {
        var descriptor = props[i];
        descriptor.enumerable = descriptor.enumerable || false;
        descriptor.configurable = true;
        if ("value" in descriptor) descriptor.writable = true;
        Object.defineProperty(target, descriptor.key, descriptor);
      }
    }

    return function (Constructor, protoProps, staticProps) {
      if (protoProps) defineProperties(Constructor.prototype, protoProps);
      if (staticProps) defineProperties(Constructor, staticProps);
      return Constructor;
    };
  }();

  babelHelpers;

  var sidrStatus = {
    moving: false,
    opened: false,
    curSideMenu: undefined,
    autoHide: false  
  };
  //200106: add autoHide flag for expose autoHide in status object. for toggle menu function

  var helper = {
    // Check for valids urls
    // From : http://stackoverflow.com/questions/5717093/check-if-a-javascript-string-is-an-url

    isUrl: function isUrl(str) {
      var pattern = new RegExp('^(https?:\\/\\/)?' + // protocol
      '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|' + // domain name
      '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
      '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' + // port and path
      '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
      '(\\#[-a-z\\d_]*)?$', 'i'); // fragment locator

      if (pattern.test(str)) {
        return true;
      } else {
        return false;
      }
    },


    // Add sidr prefixes
    addPrefixes: function addPrefixes($element) {
      this.addPrefix($element, 'id');
      this.addPrefix($element, 'class');
      $element.removeAttr('style');
    },
    addPrefix: function addPrefix($element, attribute) {
      var toReplace = $element.attr(attribute);

      if (typeof toReplace === 'string' && toReplace !== '' && toReplace !== 'sidr-inner') {
        $element.attr(attribute, toReplace.replace(/([A-Za-z0-9_.\-]+)/g, 'sidr-' + attribute + '-$1'));
      }
    },

    // Check if transitions is supported
    transitions: function () {
      var body = document.body || document.documentElement,
          style = body.style,
          supported = false,
          property = 'transition';

      if (property in style) {
        supported = true;
      } else {
        (function () {
          var prefixes = ['moz', 'webkit', 'o', 'ms'],
              prefix = undefined,
              i = undefined;

          property = property.charAt(0).toUpperCase() + property.substr(1);
          supported = function () {
            for (i = 0; i < prefixes.length; i++) {
              prefix = prefixes[i];
              if (prefix + property in style) {
                return true;
              }
            }

            return false;
          }();
          property = supported ? '-' + prefix.toLowerCase() + '-' + property.toLowerCase() : null;
        })();
      }

      return {
        supported: supported,
        property: property
      };
    }(),
    
    // Check if transform is supported
    transform: function () {
      var body = document.body || document.documentElement,
          style = body.style,
          supported = false,
          property = 'transform';

      if (property in style) {
        supported = true;
      } else {
        (function () {
          var prefixes = ['moz', 'webkit', 'o', 'ms'],
              prefix = undefined,
              i = undefined;

          property = property.charAt(0).toUpperCase() + property.substr(1);
          supported = function () {
            for (i = 0; i < prefixes.length; i++) {
              prefix = prefixes[i];
              if (prefix + property in style) {
                return true;
              }
            }

            return false;
          }();
          property = supported ? '-' + prefix.toLowerCase() + '-' + property.toLowerCase() : null;
        })();
      }

      return {
        supported: supported,
        property: property
      };
    }()
  };

  var $$2 = jQuery;

  var bodyAnimationClass = 'sidr-animating';
  var openAction = 'open';
  var closeAction = 'close';
  var transitionEndEvent = 'webkitTransitionEnd otransitionend oTransitionEnd msTransitionEnd transitionend';
  var Menu = function () {
    function Menu(name) {
      babelHelpers.classCallCheck(this, Menu);

      this.name = name;
      this.item = $$2('#' + name);
      this.openClass = name === 'sidr' ? 'sidr-open' : 'sidr-open ' + name + '-open';
      this.menuWidth = this.item.outerWidth(true);
      this.speed = this.item.data('speed');
      this.side = this.item.data('side');
      this.displace = this.item.data('displace');
      this.autoHide = this.item.data('autoHide');
      this.timing = this.item.data('timing');
      this.method = this.item.data('method');
      this.onOpenCallback = this.item.data('onOpen');
      this.onCloseCallback = this.item.data('onClose');
      this.onOpenEndCallback = this.item.data('onOpenEnd');
      this.onCloseEndCallback = this.item.data('onCloseEnd');
      this.body = $$2(this.item.data('body'));
      
      sidrStatus.curSideMenu = this.name;
    }

    babelHelpers.createClass(Menu, [{
      key: 'getAnimation',
      value: function getAnimation(action, element) {
        var animation = {},
            prop = this.side;
        if (element === 'body')
        	prop = prop === 'right' ? 'padding-right' : 'padding-left';

        if (action === 'open' && element === 'body') {
          animation[prop] = this.menuWidth + 'px';
        } else if (action === 'close' && element === 'menu') {
          animation[prop] = '-' + this.menuWidth + 'px';
        } else {
          animation[prop] = 0;
        }

        return animation;
      }
    }, {
      key: 'prepareBody',
      value: function prepareBody(action) {
        var prop = action === 'open' ? 'hidden' : '';

        // Prepare page if container is body
        if (this.body.is('body')) {
          var $html = $$2('html'),
              scrollTop = $html.scrollTop();

          $html.css('overflow-x', prop).scrollTop(scrollTop);
        }
      }
    }, {
      key: 'openBody',
      value: function openBody() {
        if (this.displace) {
          var transitions = helper.transitions,
              $body = this.body;

          if (transitions.supported) {
            /*$body.css(transitions.property, this.side + ' ' + this.speed / 1000 + 's ' + this.timing).css(this.side, 0).css({
              width: $body.width(),
              position: 'absolute'
            });
            $body.css(this.side, this.menuWidth + 'px');*/
            $body.css(transitions.property, $body.data('transition'))
            	.css(this.side === 'right' ? 'padding-right' : 'padding-left', this.menuWidth + 'px');
          } else {
            var bodyAnimation = this.getAnimation(openAction, 'body');

            /*$body.css({
              width: $body.width(),
              position: 'absolute'
            }).animate(bodyAnimation, {
              queue: false,
              duration: this.speed
            });*/
            $body.animate(bodyAnimation, {
            		queue: false,
            		duration: this.speed
            	});
          }
        }
      }
    }, {
      key: 'onCloseBody',
      value: function onCloseBody() {
        var transitions = helper.transitions,
            resetStyles = {
          width: '',
          position: '',
          right: '',
          left: '',
          padding: ''
        };

        if (transitions.supported) {
          resetStyles[transitions.property] = '';
        }

        this.body.css(resetStyles).unbind(transitionEndEvent);
      }
    }, {
      key: 'closeBody',
      value: function closeBody() {
        var _this = this;

        if (this.displace) {
          if (helper.transitions.supported) {
            /*this.body.css(this.side, 0).one(transitionEndEvent, function () {
              _this.onCloseBody();
            });*/
            this.body.css('padding', 0).one(transitionEndEvent, function () {
              _this.onCloseBody();
            });
          } else {
            var bodyAnimation = this.getAnimation(closeAction, 'body');

            this.body.animate(bodyAnimation, {
              queue: false,
              duration: this.speed,
              complete: function complete() {
                _this.onCloseBody();
              }
            });
          }
        }
      }
    }, {
      key: 'moveBody',
      value: function moveBody(action) {
        if (action === openAction) {
          this.openBody();
        } else {
          this.closeBody();
        }
      }
    }, {
      key: 'onOpenMenu',
      value: function onOpenMenu(callback) {
        var name = this.name;

        sidrStatus.moving = false;
        sidrStatus.opened = name;

        this.item.unbind(transitionEndEvent);

        this.body.removeClass(bodyAnimationClass).addClass(this.openClass);

        this.onOpenEndCallback();

        if (typeof callback === 'function') {
          callback(name);
        }
      }
    }, {
      key: 'openMenu',
      value: function openMenu(callback) {
        var _this2 = this;

        var $item = this.item;

        if (helper.transitions.supported) {
          $item.css(this.side, 0).one(transitionEndEvent, function () {
            _this2.onOpenMenu(callback);
          });
        } else {
          var menuAnimation = this.getAnimation(openAction, 'menu');

          $item.css('display', 'block').animate(menuAnimation, {
            queue: false,
            duration: this.speed,
            complete: function complete() {
              _this2.onOpenMenu(callback);
            }
          });
        }
      }
    }, {
      key: 'onCloseMenu',
      value: function onCloseMenu(callback) {
        this.item.css({
          left: '',
          right: ''
        }).unbind(transitionEndEvent);
        $$2('html').css('overflow-x', '');

        sidrStatus.moving = false;
        sidrStatus.opened = false;

        this.body.removeClass(bodyAnimationClass).removeClass(this.openClass);

        this.onCloseEndCallback();

        // Callback
        if (typeof callback === 'function') {
          callback(name);
        }
      }
    }, {
      key: 'closeMenu',
      value: function closeMenu(callback) {
        var _this3 = this;

        var item = this.item;

        if (helper.transitions.supported) {
          item.css(this.side, '').one(transitionEndEvent, function () {
            _this3.onCloseMenu(callback);
          });
        } else {
          var menuAnimation = this.getAnimation(closeAction, 'menu');

          item.animate(menuAnimation, {
            queue: false,
            duration: this.speed,
            complete: function complete() {
              _this3.onCloseMenu();
            }
          });
        }
      }
    }, {
      key: 'moveMenu',
      value: function moveMenu(action, callback) {
        this.body.addClass(bodyAnimationClass);

        if (action === openAction) {
          this.openMenu(callback);
        } else {
          this.closeMenu(callback);
        }
      }
    }, {
      key: 'move',
      value: function move(action, callback) {
        // Lock sidr
        sidrStatus.moving = true;

        this.prepareBody(action);
        this.moveBody(action);
        this.moveMenu(action, callback);
      }
    }, {
      key: 'open',
      value: function open(callback) {
    	//if disable, ignore open/close action
    	if (isDisable) return;
    	
        var _this4 = this;

        // Check if is already opened or moving
        if (sidrStatus.opened === this.name || sidrStatus.moving) {
          return;
        }

        // If another menu opened close first
        if (sidrStatus.opened !== false) {
          var alreadyOpenedMenu = new Menu(sidrStatus.opened);

          alreadyOpenedMenu.close(function () {
            _this4.open(callback);
          });

          return;
        }

        this.move('open', callback);

        // onOpen callback
        this.onOpenCallback();
      }
    }, {
      key: 'openOnLoad',
      value: function openOnLoad(callback) {
        if (this.displace) {
        	sidrStatus.moving = true;
        	if (helper.transitions.supported)
        		this.item.css(helper.transitions.property, '');
        	this.prepareBody('open');
        	this.body.css(this.side === 'right' ? 'padding-right' : 'padding-left', this.menuWidth + 'px');
        	this.item.css(this.side, 0);
        	sidrStatus.moving = false;
        	sidrStatus.opened = this.name;
        	this.body.addClass(this.openClass);
        	if (typeof callback === 'function') {
        		callback(name);
        	}
        	this.onOpenEndCallback();
        	if (helper.transitions.supported) {
        		this.item.css(helper.transitions.property, this.item.data('transition'));
        		this.body.css(helper.transitions.property, this.body.data('transition'));
        	}
        }
      }
    }, {
      key: 'adjustBody',
      value: function adjustBody(callback) {
   	    if (sidrStatus.opened !== this.name)
   	    	return;
    	sidrStatus.moving = true;
    	this.prepareBody('open');
    	this.body.css(this.side === 'right' ? 'padding-right' : 'padding-left', (this.displace ? this.menuWidth : 0) + 'px');
        sidrStatus.moving = false;
        if (typeof callback === 'function')
        	callback(name);
        if (helper.transitions.supported)
        	this.body.css(helper.transitions.property, this.body.data('transition'));
      }
    }, {
      key: 'close',
      value: function close(callback) {
    	//if disable, ignore open/close action
    	if (isDisable) return;
        // Check if is already closed or moving
        if (sidrStatus.opened !== this.name || sidrStatus.moving) {
          return;
        }

        this.move('close', callback);

        // onClose callback
        this.onCloseCallback();
      }
    }, {
      key: 'toggle',
      value: function toggle(callback) {
        if (sidrStatus.opened === this.name) {
          this.close(callback);
        } else {
          this.open(callback);
        }
      }
    }, {
      key: 'disable',
      value: function disable(callback) {
    	lastOpenStatus = sidrStatus.opened;
        this.close(callback);
    	isDisable = true;
      }
    }, {
      key: 'enable',
      value: function enable(callback) {
    	isDisable = false;
    	if (lastOpenStatus){
    		this.open(callback);
    	}
      }
    }]);
    return Menu;
  }();

  var $$1 = jQuery;

  function execute(action, name, callback) {
    var sidr = new Menu(name);

    switch (action) {
      case 'open':
        sidr.open(callback);
        break;
      case 'close':
        sidr.close(callback);
        break;
      case 'toggle':
        sidr.toggle(callback);
        break;
      case 'disable':
        sidr.disable(callback);
        break;
      case 'enable':
        sidr.enable(callback);
        break;
      case 'openOnLoad':
        sidr.openOnLoad(callback);
    	break;
      case 'adjustBody':
        sidr.adjustBody(callback);
    	break;
      default:
        $$1.error('Method ' + action + ' does not exist on jQuery.sidr');
        break;
    }
  }

  var i;
  var $ = jQuery;
  var publicMethods = ['open', 'close', 'toggle', 'openOnLoad', 'adjustBody', 'disable', 'enable'];
  var methodName;
  var methods = {};
  var getMethod = function getMethod(methodName) {
    return function (name, callback) {
      // Check arguments
      if (typeof name === 'function') {
        callback = name;
        name = 'sidr';
      } else if (!name) {
        name = 'sidr';
      }

      execute(methodName, name, callback);
    };
  };
  for (i = 0; i < publicMethods.length; i++) {
    methodName = publicMethods[i];
    methods[methodName] = getMethod(methodName);
  }

  function sidr(method) {
    if (method === 'status') {
      return sidrStatus;
    } else if (methods[method]) {
      return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'function' || typeof method === 'string' || !method) {
      return methods.toggle.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.sidr');
    }
  }

  var $$3 = jQuery;

  function fillContent($sideMenu, settings) {
    // The menu content
    if (typeof settings.source === 'function') {
      var newContent = settings.source(name);

      $sideMenu.html(newContent);
    } else if (typeof settings.source === 'string' && helper.isUrl(settings.source)) {
      $$3.get(settings.source, function (data) {
        $sideMenu.html(data);
      });
    } else if (typeof settings.source === 'string') {
      var htmlContent = '',
          selectors = settings.source.split(',');

      $$3.each(selectors, function (index, element) {
        htmlContent += '<div class="sidr-inner">' + $$3(element).html() + '</div>';
      });

      // Renaming ids and classes
      if (settings.renaming) {
        var $htmlContent = $$3('<div />').html(htmlContent);

        $htmlContent.find('*').each(function (index, element) {
          var $element = $$3(element);

          helper.addPrefixes($element);
        });
        htmlContent = $htmlContent.html();
      }

      $sideMenu.html(htmlContent);
    } else if (settings.source !== null) {
      $$3.error('Invalid Sidr Source');
    }

    return $sideMenu;
  }

  function fnSidr(options) {
    var transitions = helper.transitions,
        settings = $$3.extend({
      name: 'sidr', // Name for the 'sidr'
      speed: 200, // Accepts standard jQuery effects speeds (i.e. fast, normal or milliseconds)
      side: 'left', // Accepts 'left' or 'right'
      source: null, // Override the source of the content.
      renaming: true, // The ids and classes will be prepended with a prefix when loading existent content
      body: 'body', // Page container selector,
      displace: true, // Displace the body content or not
      autoHide: false, // Auto hide sidr
      timing: 'ease', // Timing function for CSS transitions
      method: 'toggle', // The method to call when element is clicked
      bind: 'touchstart click', // The event(s) to trigger the menu
      onOpen: function onOpen() {},
      // Callback when sidr start opening
      onClose: function onClose() {},
      // Callback when sidr start closing
      onOpenEnd: function onOpenEnd() {},
      // Callback when sidr end opening
      onCloseEnd: function onCloseEnd() {} // Callback when sidr end closing
    }, options),
        name = settings.name,
        $sideMenu = $$3('#' + name);

    // If the side menu do not exist create it
    if ($sideMenu.length === 0) {
      $sideMenu = $$3('<div />').attr('id', name).appendTo($$3('body'));
    }

    // Add transition to menu if are supported
    var transitionValue = settings.side + ' ' + settings.speed / 1000 + 's ' + settings.timing;
    if (transitions.supported) {
      $sideMenu.css(transitions.property, transitionValue);
    }

    // Adding styles and options
    $sideMenu.addClass('sidr').addClass(settings.side).data({
      speed: settings.speed,
      side: settings.side,
      body: settings.body,
      displace: settings.displace,
      autoHide: settings.autoHide,
      timing: settings.timing,
      method: settings.method,
      onOpen: settings.onOpen,
      onClose: settings.onClose,
      onOpenEnd: settings.onOpenEnd,
      onCloseEnd: settings.onCloseEnd,
      transition: transitionValue
    });
    $(settings.body).data('transition', 'padding ' + settings.speed / 1000 + 's ' + settings.timing);

    $sideMenu = fillContent($sideMenu, settings);

    return this.each(function () {
      var $this = $$3(this),
          data = $this.data('sidr'),
          flag = false;

      // If the plugin hasn't been initialized yet
      if (!data) {
        sidrStatus.moving = false;
        sidrStatus.opened = false;

        $this.data('sidr', name);

        $this.bind(settings.bind, function (event) {
          event.preventDefault();

          if (!flag) {
            flag = true;
            sidr(settings.method, name);

            setTimeout(function () {
              flag = false;
            }, 100);
          }
        });
      }
    });
  }
  function sidrDropDownSetup(sidrMenu) {
	  var disableDropDown = false;
	  var Accordion = function(el, multiple) {
		  this.el = el || {};
		  // more then one submenu open?
		  this.multiple = multiple || false;
		  var dropdownlink = this.el.find('.dropdownlink');
		  dropdownlink.on('click',
		                    { el: this.el, multiple: this.multiple },
		                    this.dropdown);

		  //check pin status
		  var pinBtn = this.el.find('.pin');
		  pinBtn.hover(function(){
			  disableDropDown = true
		  }, function(){
			  disableDropDown = false
		  });
	  };
	  Accordion.prototype.dropdown = function(e) {
		  var $el = e.data.el, $this = $(this),
		  //this is the ul.submenuItems
		  $next = $this.next();

		  if (disableDropDown)
			  return;
		    
		  $next.slideToggle(200);
		  $this.parent().toggleClass('open');
		    
		  if(!e.data.multiple) {
			  //show only one menu at the same time
			  //$el.find('.submenuItems').not($next).slideUp().parent().removeClass('open');
			  $this.closest('ul').find('.submenuItems').not($next).slideUp(200).parent().removeClass('open');
		  }
	  }
	  var accordion = new Accordion($('#' + sidrMenu), false);
  }
  function sidrPinSetup(sidrMenu) {
	  var $sideMenu = $('#' + sidrMenu);
	  var $pinBtn = $sideMenu.find('.pin');
	  var changePinButton = function() {
		  sidrStatus.autoHide = $sideMenu.data('autoHide');
		  if ($sideMenu.data('autoHide')) {
			  //$pinBtn.addClass('fa-rotate-90');
			  if (helper.transform.supported) {
				  //$pinBtn.css(helper.transform.property, 'rotate(90deg) translate(-10px,5px)');
				  $pinBtn.css(helper.transform.property, 'rotate(90deg) translate(-5px,-2px)');
			  }
		  }
		  else {
			  //$pinBtn.removeClass('fa-rotate-90');
			  if (helper.transform.supported) {
				  //$pinBtn.css(helper.transform.property, 'rotate(0deg) translate(0px,-5px)');
				  $pinBtn.css(helper.transform.property, 'rotate(0deg) translate(5px,-2px)');
			  }
		  }
	  };
	  changePinButton();
	  $pinBtn.click(function(){
		  if ($sideMenu.data('autoHide')) {
			  $sideMenu.data('autoHide', false);
			  $sideMenu.data('displace', true);
		  } else {
			  $sideMenu.data('autoHide', true);
			  $sideMenu.data('displace', false);
		  }
   		  $.sidr('adjustBody', sidrMenu);
       	  zkGetSidrStatus();
		  changePinButton();
	  });
  }
  function sidrAutoHideSetup(sidrMenu, p_autohideArea) {
	  var $sideMenu = $('#' + sidrMenu);
	  if (typeof p_autohideArea !== 'undefined') {
		  $(p_autohideArea).hover(
			  function() {
				  //console.log("hover on. sidr.status:" +$.sidr('status').opened);
				  if (!$sideMenu.data('autoHide'))
					  return;
				  if ($.sidr('status').opened){
					  console.log("is opened. ignore open.");
					  return;
				  }
				  console.log("set sidr open");
		    	  $.sidr('open', 'sidr');
			  },
			  function(){
				  //console.log("hover off. sidr.status:" +$.sidr('status').opened);
				  if (!$sideMenu.data('autoHide'))
					  return;
				  if (!$.sidr('status').opened){
					  console.log("is closed. ignore close.");
					  return;
				  }
				  console.log("set sidr close");
		    	  $.sidr('close', 'sidr');
			  }
		  );
	  }
  }
  function showAppSetupButton(sidrMenu) {
	  var appSetupBtn = $('#' + sidrMenu + ' .appsetup');
	  if (typeof appSetupBtn !== 'undefined' 
		  && ((typeof android !== 'undefined' && typeof android.showSetup !== 'undefined') || typeof webkit !== 'undefined'))
		  appSetupBtn.show();
	  else
		  appSetupBtn.hide();
  }
  function showAppSetup() {
      if (typeof android !== "undefined"){
    	  android.showSetup();
      }
      else if (typeof webkit !== "undefined"){
    	  try{
    		  webkit.messageHandlers.callbackHandler.postMessage("showSetup");
    	  }
    	  catch(err){
    		  debugLog('error:' + err);
    	  }
      }
      else{
    	  debugLog('unknown device');
      }
  }
  function filterAreaSetup(p_filterArea, p_sidrMenu) {
	  if (typeof p_filterArea !== 'undefined' && $(p_filterArea).length > 0){
		  var $sidrMenu = $('#' + p_sidrMenu);
		  var $filterArea = $(p_filterArea);
		  var $itemsArea = $filterArea.prev();
		  var $topArea = $itemsArea.prev();
		  var $filterInput = $(p_filterArea).find('.input-text');
		  var $filterUpButton = $(p_filterArea).find('.up-button');
		  var $filterDownButton = $(p_filterArea).find('.down-button');
		  var $filterCurrentTotal = $(p_filterArea).find('.current-total');
		  //var filterAreaHeight = 0;
		  var foundFilterElements = [];
		  var foundFilterElementsIndex = -1;
		  /*//resize
		  var resizeEvent = function() {
			  var ch = $sidrMenu.prop("clientHeight");
			  var sh = $sidrMenu.prop("scrollHeight");
			  var itemsAreaTop = $itemsArea.prop("offsetTop");
			  var itemsAreaHeight = $itemsArea.prop("clientHeight");
			  var itemsAreaBottom = itemsAreaTop + itemsAreaHeight;
			  if (filterAreaHeight == 0)
				  filterAreaHeight = $filterArea.prop("clientHeight");
			  var filterAreaTop = ch - filterAreaHeight;
			  console.log('sidrmenu clientheight:' + ch + ",scrollheight:" + sh 
					+ ",itemsAreaTop:" + itemsAreaTop + ",itemsAreaHeight:" + itemsAreaHeight + ",itemsAreaBottom:" + itemsAreaBottom
					+ ",filterAreaTop:" + filterAreaTop);
			  if (!$.support.touch && itemsAreaBottom - filterAreaTop > 10) {
				  console.log('filterArea hide');
				  $filterArea.hide();
			  } else {
				  console.log('filterArea show');
				  $filterArea.show();
			  }
		  };
		  $sidrMenu.resize(function(){
			  resizeEvent();
		  });
		  $(document).ready(function(){
			  setTimeout(function () {
				  resizeEvent();
			  }, 1000);
		  });*/
		  //setup keyup event
		  var $lastFilterElement = null;
		  var openFilterMenuItem = function(){
			  console.log('openFilterMenuItem index:' + foundFilterElementsIndex);
			  var $foundFilterElement = foundFilterElements[foundFilterElementsIndex];
			  var dropdownlinkArr = [];
			  var $e = $foundFilterElement;
			  for (;;) {
				  var $submenuItems = $e.closest('.submenuItems');
				  var $dropdownlink = $submenuItems.prev('.dropdownlink');
				  if ($dropdownlink.length > 0) {
					  console.log('found dropdownlink:' + $dropdownlink.text());
					  dropdownlinkArr.push($dropdownlink);
					  $e = $dropdownlink;
				  } else
					  break;
			  }
			  if (dropdownlinkArr.length > 0 && !dropdownlinkArr[0].parent().hasClass('open')) {
				  dropdownlinkArr = dropdownlinkArr.reverse();
				  $.each(dropdownlinkArr, function(i, $d){
					  if (!$d.parent().hasClass('open')) {
						  console.log('dropdownlink clicked:' + $d.text());
						  $d.click();
					  }
				  });
			  }
			  if ($lastFilterElement){
				  $lastFilterElement.css('color', '');
				  $lastFilterElement.css('text-decoration','initial');
			  }
			  $foundFilterElement.css('color', 'yellow');
			  $foundFilterElement.css('text-decoration','underline');
			  $lastFilterElement = $foundFilterElement;
		  };
		  var filterEvent;
		  $filterInput.keyup(function(event){
			  //console.log('keyup:' + event.which);
			  switch (event.which) {
			  case 9:
			  case 13:
			  case 38:
			  case 40:
				  return;
			  }
			  clearTimeout(filterEvent);
			  filterEvent = setTimeout(function(){
			    foundFilterElements = [];
			    foundFilterElementsIndex = -1;
			    $.each([$topArea,$itemsArea], function(i, $ia){
			    	$ia.find('.item-title').each(function(){
			    		if ($(this).closest('.dropdownlink').length > 0) //Search result ignore folder
			    			return true;
			    		var title = $(this).text().toLowerCase();
			    		var desc = $(this).data("desc") ? $(this).data("desc").toLowerCase() : "";
			    		var inputText = $filterInput.val().toLowerCase();
			    		if (inputText.length > 0 && (title.indexOf(inputText) > -1 || desc.indexOf(inputText) > -1) && $(this).closest('li').css('display') !== 'none') {
			    			foundFilterElements.push($(this));
			    			console.log('title:' + title + ',inputText:' + inputText);
			    		}
			    	});
			    });
				if (foundFilterElements.length > 0) {
					foundFilterElementsIndex = 0;
					openFilterMenuItem();
					$filterUpButton.removeClass('disabled');
					$filterDownButton.removeClass('disabled');
					$filterCurrentTotal.text((foundFilterElementsIndex + 1) + '/' + foundFilterElements.length);
				} else {
					foundFilterElementsIndex = -1;
					if ($lastFilterElement){
						$lastFilterElement.css('color', '');
						$lastFilterElement.css('text-decoration','initial');
					}
					$filterUpButton.addClass('disabled');
					$filterDownButton.addClass('disabled');
					$filterCurrentTotal.text('');
				}
			}, 500);
		  });
		  $filterUpButton.click(function(e){
			  e.preventDefault();
			  if (foundFilterElements.length > 0) {
				  var oldIndex = foundFilterElementsIndex;
				  foundFilterElementsIndex--;
				  if (foundFilterElementsIndex < 0 || foundFilterElementsIndex > foundFilterElements.length - 1)
					  foundFilterElementsIndex = foundFilterElements.length - 1;
				  if (oldIndex !== foundFilterElementsIndex) {
					  openFilterMenuItem();
					  $filterCurrentTotal.text((foundFilterElementsIndex + 1) + '/' + foundFilterElements.length);
				  }
			  }
		  });
		  $filterDownButton.click(function(e){
			  e.preventDefault();
			  if (foundFilterElements.length > 0) {
				  var oldIndex = foundFilterElementsIndex;
				  foundFilterElementsIndex++;
				  if (foundFilterElementsIndex > foundFilterElements.length - 1 || foundFilterElementsIndex < 0)
					  foundFilterElementsIndex = 0;
				  if (oldIndex !== foundFilterElementsIndex) {
					  openFilterMenuItem();
					  $filterCurrentTotal.text((foundFilterElementsIndex + 1) + '/' + foundFilterElements.length);
				  }
			  }
		  });
		  //setup keydown event
		  $filterInput.keydown(function(event){
			  switch (event.which) {
			  case 13: //ENTER
				  if ($lastFilterElement && foundFilterElementsIndex >= 0)
					  $lastFilterElement.closest('a')[0].click();
				  break;
			  case 38: //UP
				  if (!$filterUpButton.hasClass('disabled')){
					  $filterUpButton.click();
				  }
				  event.preventDefault();
				  break;
			  case 40: //DOWN
				  if (!$filterDownButton.hasClass('disabled')){
					  $filterDownButton.click();
				  }
				  event.preventDefault();
				  break;
			  }
		  });
	  }
  }
  function sidrButtonStatusChange(sidrBtn) {
	  var opened = $.sidr('status').opened;
	  var $sidrBtn = $('#' + sidrBtn);
	  var $sidrBtnCoName = $sidrBtn.parent().find('.coname');
	  /*
	  $sidrBtn.find('.fa')
	  	.removeClass(opened ? 'fa-bars' : 'fa-caret-left')
	  	.addClass(opened ? 'fa-caret-left' : 'fa-bars');
	  */
	  /*
	  //hide coname
	  if (opened)
		  $sidrBtnCoName.fadeOut(100);
	  else
		  $sidrBtnCoName.fadeIn(100);
	  */
  }
  function sidrBodyWidthChange(newWidth, sideMenuWidth) {
	  $(document.body).width(newWidth);
	  var $zloading = $('.z-apply-loading');
	  if ($zloading.length > 0) {
		  var $parent = $zloading.parent();
		  var $zmask = $zloading.prev();
		  if ($parent.length == 0 || $zmask.length == 0)
			  return;
		  var parentId = $parent.attr('id');
		  var zloadingId = $zloading.attr('id');
		  if (parentId && parentId.endsWith('-shby') && zloadingId && zloadingId == parentId + '-z_loading' && $zmask.hasClass('z-apply-mask')) {
			  var zmaskWidth = $zmask.outerWidth(true);
			  var zloadingWidth = $zloading.outerWidth(true);
			  $zmask.css('left', sideMenuWidth + (newWidth - zmaskWidth) / 2);
			  $zloading.css('left', sideMenuWidth + (newWidth - zloadingWidth) / 2);
		  }
	  }
  }
  
  var openOnLoadFunc;
  function sidrDefault(sidrBtn, sidrMenu, container, requestAction, p_displace, p_autohide, p_autohideArea, p_filterArea, p_disableOpenOnLoad) {
	  
	if (typeof requestAction === 'undefined')
		requestAction = 'auto';
	
	var displace = true;
	if (typeof p_displace !== 'undefined'){
		displace = p_displace;
	}
	var autoHide = false;
	if (typeof p_autohide !== 'undefined'){
		autoHide = p_autohide;
	}
	var $sidrBtn, $sidrBtnCon, $sidrMenu, $container;
	//$(document).ready(function(){
		$sidrBtn = $('#' + sidrBtn);
		$sidrBtnCon = $sidrBtn.parent();
		$sidrMenu = $('#' + sidrMenu);
		$container = $('#' + container);
		if ($sidrMenu.length == 0) {
			if ($sidrBtnCon.length > 0)
				$sidrBtnCon.hide();
			return;
		}
		$sidrMenu.css('display', '');
		$sidrBtnCon.css('display', 'block');
		var sidrBtnInitLeft = $sidrBtnCon.position().left;
		var speed = 150;
		var timing = 'ease-in-out';
		$sidrBtn.sidr({
			displace: displace,
			autoHide: autoHide,
			name: sidrMenu,
	        timing: timing,
	        speed: speed,
	        onOpen: function () {
	        	if (helper.transitions.supported)
	        		$sidrBtnCon.css('left', sidrBtnInitLeft + $sidrMenu.outerWidth(true));
	        	else {
	        		$sidrBtnCon.animate({left : sidrBtnInitLeft + $sidrMenu.outerWidth(true)}, {
	        			queue: false,
	        			duration: this.speed
	        		});
	        	}
	        },
	        onClose: function () {
	        	if (helper.transitions.supported)
	        		$sidrBtnCon.css('left', sidrBtnInitLeft);
	        	else {
	        		$sidrBtnCon.animate({left : sidrBtnInitLeft}, {
	        			queue: false,
	        			duration: this.speed
	        		});
	        	}
	        },
	        onOpenEnd: function () {
    			/*var winWidth = $(window).width();
				var menuWidth = $sidrMenu.outerWidth(true);
				var containerWidth = $container.innerWidth();
				var minWidth = containerWidth + menuWidth;
		    	if (minWidth <= winWidth)
					sidrBodyWidthChange(winWidth - menuWidth, $sidrMenu.outerWidth(true));*/
	        	sidrButtonStatusChange(sidrBtn);
	        	zkGetSidrStatus();
	        },
	        onCloseEnd: function () {
	        	sidrButtonStatusChange(sidrBtn);
	        	zkGetSidrStatus();
	        }
      	});
		var bodyWidth = $(document.body).width();
      	var menuWidth = $sidrMenu.outerWidth(true);
		var containerWidth = $container.innerWidth();
		//190903: open close should not determin by containerWidth, as containerWidth is dynamic and it's undefine in many pages
		//dirty fix: assume containerWidth = 1200
		containerWidth = 1200;
		//console.log("requestAction: " + requestAction + " bodyWidth:" + bodyWidth +" containerWidth:" + containerWidth + " menuWidth:" + menuWidth + " displace:" + displace);
      	if (((requestAction === 'auto' || requestAction === 'default') && bodyWidth >= containerWidth + menuWidth) || requestAction === 'open') {
      		//220705 fix: scrollbar appear at bottom
      		var f = function() {
      			setTimeout(function () {
      				$.sidr('openOnLoad', sidrMenu, function() {
      					$sidrBtnCon.css('left', sidrBtnInitLeft + $sidrMenu.outerWidth(true));
      				});
      			}, 100);
      		};
      		if (!p_disableOpenOnLoad)
      			f();
      		else
      			openOnLoadFunc = f;
		} else
			sidrButtonStatusChange(sidrBtn);
       	if (helper.transitions.supported) {
       		setTimeout(function () {
       			$sidrBtnCon.css(helper.transitions.property, 'left ' + speed / 1000 + 's ' + timing);
       		}, 300);
  		}
      	sidrDropDownSetup(sidrMenu);
      	sidrPinSetup(sidrMenu);
      	sidrAutoHideSetup(sidrMenu, p_autohideArea);
      	showAppSetupButton(sidrMenu);
	//});
    filterAreaSetup(p_filterArea, sidrMenu);
   	/*
   	//190903: resize window shoud not affect the menu open/close
    var winWidth, winHeight;
	$( window ).resize(function () {
		if (isDisable)
			return;
		if ($sidrMenu.length == 0)
			return;
    	var width = $(window).width();
    	var height = $(window).height();
    	if (width === winWidth && height === winHeight)
    		return;
    	winWidth = width;
    	winHeight = height;
		var opened = $.sidr('status').opened;
		var menuWidth = $sidrMenu.outerWidth(true);
		var containerWidth = $container.innerWidth();
		var minWidth = containerWidth + menuWidth;
		if (requestAction === 'auto') {
			if (opened) {
			    if (minWidth <= winWidth) {
					//sidrBodyWidthChange(winWidth - menuWidth, $sidrMenu.outerWidth(true));
			    } else  {
					$.sidr('close', sidrMenu);
			    }
			} else {
			    if (minWidth <= winWidth) {
					$.sidr('open', sidrMenu);
			    }
			}
		}
    });
    */
  }
  
  function callOpenOnLoadFunc() {
	  if (openOnLoadFunc)
		  openOnLoadFunc();
  }
  
  jQuery.sidr = sidr;
  jQuery.fn.sidr = fnSidr;
  jQuery.sidrDefault = sidrDefault;
  jQuery.showAppSetup = showAppSetup;
  jQuery.sidrOpenOnLoad = callOpenOnLoadFunc;

}());
