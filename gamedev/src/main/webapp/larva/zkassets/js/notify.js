/* Notify.js - http://notifyjs.com/ Copyright (c) 2015 MIT */
(function (factory) {
	// UMD start
	// https://github.com/umdjs/umd/blob/master/jqueryPluginCommonjs.js
	if (typeof define === 'function' && define.amd) {
		// AMD. Register as an anonymous module.
		define(['jquery'], factory);
	} else if (typeof module === 'object' && module.exports) {
		// Node/CommonJS
		module.exports = function( root, jQuery ) {
			if ( jQuery === undefined ) {
				// require('jQuery') returns a factory that requires window to
				// build a jQuery instance, we normalize how we use modules
				// that require this pattern but the window provided is a noop
				// if it's defined (how jquery works)
				if ( typeof window !== 'undefined' ) {
					jQuery = require('jquery');
				}
				else {
					jQuery = require('jquery')(root);
				}
			}
			factory(jQuery);
			return jQuery;
		};
	} else {
		// Browser globals
		factory(jQuery);
	}
}(function ($) {
	//IE8 indexOf polyfill
	var indexOf = [].indexOf || function(item) {
		for (var i = 0, l = this.length; i < l; i++) {
			if (i in this && this[i] === item) {
				return i;
			}
		}
		return -1;
	};

	var pluginName = "notify";
	var pluginClassName = pluginName + "js";
	var blankFieldName = pluginName + "!blank";

	var positions = {
		t: "top",
		m: "middle",
		b: "bottom",
		l: "left",
		c: "center",
		r: "right"
	};
	var hAligns = ["l", "c", "r"];
	var vAligns = ["t", "m", "b"];
	var mainPositions = ["t", "b", "l", "r"];
	var opposites = {
		t: "b",
		m: null,
		b: "t",
		l: "r",
		c: null,
		r: "l"
	};

	var parsePosition = function(str) {
		var pos;
		pos = [];
		$.each(str.split(/\W+/), function(i, word) {
			var w;
			w = word.toLowerCase().charAt(0);
			if (positions[w]) {
				return pos.push(w);
			}
		});
		return pos;
	};

	var styles = {};

	var coreStyle = {
		name: "core",
		html: "<div class=\"" + pluginClassName + "-wrapper\">\n	<div class=\"" + pluginClassName + "-arrow\"></div>\n	<div class=\"" + pluginClassName + "-container\"></div>\n</div>",
		css: "." + pluginClassName + "-corner {\n	position: fixed;\n	margin: 5px;\n	z-index: 1050;\n}\n\n." + pluginClassName + "-corner ." + pluginClassName + "-wrapper,\n." + pluginClassName + "-corner ." + pluginClassName + "-container {\n	position: relative;\n	display: block;\n	height: inherit;\n	width: inherit;\n	margin: 3px;\n}\n\n." + pluginClassName + "-wrapper {\n	z-index: 1;\n	position: absolute;\n	display: inline-block;\n	height: 0;\n	width: 0;\n}\n\n." + pluginClassName + "-container {\n	display: none;\n	z-index: 1;\n	position: absolute;\n}\n\n." + pluginClassName + "-hidable {\n	cursor: pointer;\n}\n\n[data-notify-text],[data-notify-html] {\n	position: relative;\n}\n\n." + pluginClassName + "-arrow {\n	position: absolute;\n	z-index: 2;\n	width: 0;\n	height: 0;\n}"
	};

	var stylePrefixes = {
		"border-radius": ["-webkit-", "-moz-"]
	};

	var getStyle = function(name) {
		return styles[name];
	};

	var removeStyle = function(name) {
		if (!name) {
			throw "Missing Style name";
		}
		if (styles[name]) {
			delete styles[name];
		}
	};

	var addStyle = function(name, def) {
		if (!name) {
			throw "Missing Style name";
		}
		if (!def) {
			throw "Missing Style definition";
		}
		if (!def.html) {
			throw "Missing Style HTML";
		}
		//remove existing style
		var existing = styles[name];
		if (existing && existing.cssElem) {
			if (window.console) {
				console.warn(pluginName + ": overwriting style '" + name + "'");
			}
			styles[name].cssElem.remove();
		}
		def.name = name;
		styles[name] = def;
		var cssText = "";
		if (def.classes) {
			$.each(def.classes, function(className, props) {
				cssText += "." + pluginClassName + "-" + def.name + "-" + className + " {\n";
				$.each(props, function(name, val) {
					if (stylePrefixes[name]) {
						$.each(stylePrefixes[name], function(i, prefix) {
							return cssText += "	" + prefix + name + ": " + val + ";\n";
						});
					}
					return cssText += "	" + name + ": " + val + ";\n";
				});
				return cssText += "}\n";
			});
		}
		if (def.css) {
			cssText += "/* styles for " + def.name + " */\n" + def.css;
		}
		if (cssText) {
			def.cssElem = insertCSS(cssText);
			def.cssElem.attr("id", "notify-" + def.name);
		}
		var fields = {};
		var elem = $(def.html);
		findFields("html", elem, fields);
		findFields("text", elem, fields);
		def.fields = fields;
	};

	var insertCSS = function(cssText) {
		var e, elem, error;
		elem = createElem("style");
		elem.attr("type", 'text/css');
		$("head").append(elem);
		try {
			elem.html(cssText);
		} catch (_) {
			elem[0].styleSheet.cssText = cssText;
		}
		return elem;
	};

	var findFields = function(type, elem, fields) {
		var attr;
		if (type !== "html") {
			type = "text";
		}
		attr = "data-notify-" + type;
		return find(elem, "[" + attr + "]").each(function() {
			var name;
			name = $(this).attr(attr);
			if (!name) {
				name = blankFieldName;
			}
			fields[name] = type;
		});
	};

	var find = function(elem, selector) {
		if (elem.is(selector)) {
			return elem;
		} else {
			return elem.find(selector);
		}
	};

	var pluginOptions = {
		clickToHide: true,
		autoHide: true,
		autoHideDelay: 5000,
		arrowShow: true,
		arrowSize: 5,
		breakNewLines: true,
		elementPosition: "bottom",
		globalPosition: "top right",
		style: mobileUtil.muIsMobile() ? "bootstrap" : "zkdefault",
		className: "error",
		showAnimation: "slideDown",
		showDuration: 400,
		hideAnimation: "slideUp",
		hideDuration: 200,
		gap: 5,
		offsetX: mobileUtil.muIsMobile() ? -10 : -80,
		offsetY: mobileUtil.muIsMobile() ? -10 : -40 
	};

	var inherit = function(a, b) {
		var F;
		F = function() {};
		F.prototype = a;
		return $.extend(true, new F(), b);
	};

	var defaults = function(opts) {
		return $.extend(pluginOptions, opts);
	};

	var createElem = function(tag) {
		return $("<" + tag + "></" + tag + ">");
	};

	var globalAnchors = {};

	var getAnchorElement = function(element) {
		var radios;
		if (element.is('[type=radio]')) {
			radios = element.parents('form:first').find('[type=radio]').filter(function(i, e) {
				return $(e).attr("name") === element.attr("name");
			});
			element = radios.first();
		}
		return element;
	};

	var incr = function(obj, pos, val) {
		var opp, temp;
		if (typeof val === "string") {
			val = parseInt(val, 10);
		} else if (typeof val !== "number") {
			return;
		}
		if (isNaN(val)) {
			return;
		}
		opp = positions[opposites[pos.charAt(0)]];
		temp = pos;
		if (obj[opp] !== undefined) {
			pos = positions[opp.charAt(0)];
			val = -val;
		}
		if (obj[pos] === undefined) {
			obj[pos] = val;
		} else {
			obj[pos] += val;
		}
		return null;
	};

	var realign = function(alignment, inner, outer) {
		if (alignment === "l" || alignment === "t") {
			return 0;
		} else if (alignment === "c" || alignment === "m") {
			return outer / 2 - inner / 2;
		} else if (alignment === "r" || alignment === "b") {
			return outer - inner;
		}
		throw "Invalid alignment";
	};

	var encode = function(text) {
		encode.e = encode.e || createElem("div");
		return encode.e.text(text).html();
	};

	function Notification(elem, data, options) {
		if (typeof options === "string") {
			options = {
				className: options
			};
		}
		this.options = inherit(pluginOptions, $.isPlainObject(options) ? options : {});
		this.loadHTML();
		this.wrapper = $(coreStyle.html);
		if (this.options.clickToHide) {
			this.wrapper.addClass(pluginClassName + "-hidable");
		}
		this.wrapper.data(pluginClassName, this);
		this.arrow = this.wrapper.find("." + pluginClassName + "-arrow");
		this.container = this.wrapper.find("." + pluginClassName + "-container");
		this.container.append(this.userContainer);
		if (elem && elem.length) {
			this.elementType = elem.attr("type");
			this.originalElement = elem;
			this.elem = getAnchorElement(elem);
			this.elem.data(pluginClassName, this);
			this.elem.before(this.wrapper);
		}
		this.container.hide();
		this.run(data);
	}

	Notification.prototype.loadHTML = function() {
		var style;
		style = this.getStyle();
		this.userContainer = $(style.html);
		this.userFields = style.fields;
	};

	Notification.prototype.show = function(show, userCallback) {
		var args, callback, elems, fn, hidden;
		callback = (function(_this) {
			return function() {
				if (!show && !_this.elem) {
					_this.destroy();
				}
				if (userCallback) {
					return userCallback();
				}
			};
		})(this);
		hidden = this.container.parent().parents(':hidden').length > 0;
		elems = this.container.add(this.arrow);
		args = [];
		if (hidden && show) {
			fn = "show";
		} else if (hidden && !show) {
			fn = "hide";
		} else if (!hidden && show) {
			fn = this.options.showAnimation;
			args.push(this.options.showDuration);
		} else if (!hidden && !show) {
			fn = this.options.hideAnimation;
			args.push(this.options.hideDuration);
		} else {
			return callback();
		}
		args.push(callback);
		return elems[fn].apply(elems, args);
	};

	Notification.prototype.setGlobalPosition = function() {
		var p = this.getPosition();
		var pMain = p[0];
		var pAlign = p[1];
		var main = positions[pMain];
		var align = positions[pAlign];
		var key = pMain + "|" + pAlign;
		var anchor = globalAnchors[key];
		if (!anchor || !document.body.contains(anchor[0])) {
			anchor = globalAnchors[key] = createElem("div");
			var css = {};
			css[main] = 0;
			if (align === "middle") {
				css.top = '45%';
			} else if (align === "center") {
				css.left = '45%';
			} else {
				css[align] = 0;
			}
			//offset X, offset Y
			for (p in css) {
				if (typeof css[p] === "number") {
					if (this.options.offsetX) {
						if (p == "left")
							css[p] += this.options.offsetX;
						else if (p == "right")
							css[p] -= this.options.offsetX;
					}
					if (this.options.offsetY) {
						if (p == "top")
							css[p] += this.options.offsetY;
						else if (p == "bottom")
							css[p] -= this.options.offsetY;
					}
				}
			}
			anchor.css(css).addClass(pluginClassName + "-corner");
			$("body").append(anchor);
		}
		return anchor.prepend(this.wrapper);
	};

	Notification.prototype.setElementPosition = function() {
		var arrowColor, arrowCss, arrowSize, color, contH, contW, css, elemH, elemIH, elemIW, elemPos, elemW, gap, j, k, len, len1, mainFull, margin, opp, oppFull, pAlign, pArrow, pMain, pos, posFull, position, ref, wrapPos;
		position = this.getPosition();
		pMain = position[0];
		pAlign = position[1];
		pArrow = position[2];
		elemPos = this.elem.position();
		elemH = this.elem.outerHeight();
		elemW = this.elem.outerWidth();
		elemIH = this.elem.innerHeight();
		elemIW = this.elem.innerWidth();
		wrapPos = this.wrapper.position();
		contH = this.container.height();
		contW = this.container.width();
		mainFull = positions[pMain];
		opp = opposites[pMain];
		oppFull = positions[opp];
		css = {};
		css[oppFull] = pMain === "b" ? elemH : pMain === "r" ? elemW : 0;
		incr(css, "top", elemPos.top - wrapPos.top);
		incr(css, "left", elemPos.left - wrapPos.left);
		//offset X, offset Y
		for (p in css) {
			if (typeof css[p] === 'number') {
				if (this.options.offsetX && (p == 'left' || p == 'right'))
					css[p] += this.options.offsetX;
				else if (this.options.offsetY && (p == 'top' || p == 'bottom'))
					css[p] += this.options.offsetY;
			}
		}
		ref = ["top", "left"];
		for (j = 0, len = ref.length; j < len; j++) {
			pos = ref[j];
			margin = parseInt(this.elem.css("margin-" + pos), 10);
			if (margin) {
				incr(css, pos, margin);
			}
		}
		gap = Math.max(0, this.options.gap - (this.options.arrowShow ? arrowSize : 0));
		incr(css, oppFull, gap);
		if (!this.options.arrowShow) {
			this.arrow.hide();
		} else {
			arrowSize = this.options.arrowSize;
			arrowCss = $.extend({}, css);
			arrowColor = this.userContainer.css("border-color") || this.userContainer.css("border-top-color") || this.userContainer.css("background-color") || "white";
			for (k = 0, len1 = mainPositions.length; k < len1; k++) {
				pos = mainPositions[k];
				posFull = positions[pos];
				if (pos === opp) {
					continue;
				}
				color = posFull === mainFull ? arrowColor : "transparent";
				arrowCss["border-" + posFull] = arrowSize + "px solid " + color;
			}
			incr(css, positions[opp], arrowSize);
			if (indexOf.call(mainPositions, pAlign) >= 0) {
				incr(arrowCss, positions[pAlign], arrowSize * 2);
			}
		}
		if (indexOf.call(vAligns, pMain) >= 0) {
			incr(css, "left", realign(pAlign, contW, elemW));
			if (arrowCss) {
				incr(arrowCss, "left", realign(pAlign, arrowSize, elemIW));
			}
		} else if (indexOf.call(hAligns, pMain) >= 0) {
			incr(css, "top", realign(pAlign, contH, elemH));
			if (arrowCss) {
				incr(arrowCss, "top", realign(pAlign, arrowSize, elemIH));
			}
		}
		if (this.container.is(":visible")) {
			css.display = "block";
		}
		this.container.removeAttr("style").css(css);
		if (arrowCss) {
			return this.arrow.removeAttr("style").css(arrowCss);
		}
	};

	Notification.prototype.getPosition = function() {
		var pos, ref, ref1, ref2, ref3, ref4, ref5, text;
		text = this.options.position || (this.elem ? this.options.elementPosition : this.options.globalPosition);
		pos = parsePosition(text);
		if (pos.length === 0) {
			pos[0] = "b";
		}
		if (ref = pos[0], indexOf.call(mainPositions, ref) < 0) {
			throw "Must be one of [" + mainPositions + "]";
		}
		if (pos.length === 1 || ((ref1 = pos[0], indexOf.call(vAligns, ref1) >= 0) && (ref2 = pos[1], indexOf.call(hAligns, ref2) < 0)) || ((ref3 = pos[0], indexOf.call(hAligns, ref3) >= 0) && (ref4 = pos[1], indexOf.call(vAligns, ref4) < 0))) {
			pos[1] = (ref5 = pos[0], indexOf.call(hAligns, ref5) >= 0) ? "m" : "l";
		}
		if (pos.length === 2) {
			pos[2] = pos[1];
		}
		return pos;
	};

	Notification.prototype.getStyle = function(name) {
		var style;
		if (!name) {
			name = this.options.style;
		}
		if (!name) {
			name = "default";
		}
		style = styles[name];
		if (!style) {
			throw "Missing style: " + name;
		}
		return style;
	};

	Notification.prototype.updateClasses = function() {
		var classes, style;
		classes = ["base"];
		if ($.isArray(this.options.className)) {
			classes = classes.concat(this.options.className);
		} else if (this.options.className) {
			classes.push(this.options.className);
		}
		style = this.getStyle();
		classes = $.map(classes, function(n) {
			return pluginClassName + "-" + style.name + "-" + n;
		}).join(" ");
		return this.userContainer.attr("class", classes);
	};

	Notification.prototype.run = function(data, options) {
		var d, datas, name, type, value;
		if ($.isPlainObject(options)) {
			$.extend(this.options, options);
		} else if ($.type(options) === "string") {
			this.options.className = options;
		}
		if (this.container && !data) {
			this.show(false);
			return;
		} else if (!this.container && !data) {
			return;
		}
		datas = {};
		if ($.isPlainObject(data)) {
			datas = data;
		} else {
			datas[blankFieldName] = data;
		}
		for (name in datas) {
			d = datas[name];
			type = this.userFields[name];
			if (!type) {
				continue;
			}
			if (type === "text") {
				d = encode(d);
				if (this.options.breakNewLines) {
					d = d.replace(/\n/g, '<br/>');
				}
			}
			value = name === blankFieldName ? '' : '=' + name;
			find(this.userContainer, "[data-notify-" + type + value + "]").html(d);
		}
		this.updateClasses();
		if (this.elem) {
			this.setElementPosition();
		} else {
			this.setGlobalPosition();
		}
		this.show(true);
		if (this.options.autoHide) {
			clearTimeout(this.autohideTimer);
			this.autohideTimer = setTimeout(this.show.bind(this, false), this.options.autoHideDelay);
		}
	};

	Notification.prototype.destroy = function() {
		this.wrapper.data(pluginClassName, null);
		this.wrapper.remove();
	};

	$[pluginName] = function(elem, data, options) {
		if ((elem && elem.nodeName) || elem.jquery) {
			$(elem)[pluginName](data, options);
		} else {
			options = data;
			data = elem;
			new Notification(null, data, options);
		}
		return elem;
	};

	$.fn[pluginName] = function(data, options) {
		$(this).each(function() {
			var prev = getAnchorElement($(this)).data(pluginClassName);
			if (prev) {
				prev.destroy();
			}
			var curr = new Notification($(this), data, options);
		});
		return this;
	};

	$.extend($[pluginName], {
		defaults: defaults,
		addStyle: addStyle,
		removeStyle: removeStyle,
		pluginOptions: pluginOptions,
		getStyle: getStyle,
		insertCSS: insertCSS
	});

	//always include the default bootstrap style
	addStyle("bootstrap", {
		html: "<div>\n<span data-notify-text></span>\n</div>",
		classes: {
			base: {
				"font-weight": "bold",
				"padding": "8px 15px 8px 14px",
				"text-shadow": "0 1px 0 rgba(255, 255, 255, 0.5)",
				"background-color": "#fcf8e3",
				"border": "1px solid #fbeed5",
				"border-radius": "4px",
				"white-space": "nowrap",
				"padding-left": "25px",
				"background-repeat": "no-repeat",
				"background-position": "3px center"
			},
			error: {
				"color": "#B94A48",
				"background-color": "#F2DEDE",
				"border-color": "#DA596C",
				"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAtRJREFUeNqkVc1u00AQHq+dOD+0poIQfkIjalW0SEGqRMuRnHos3DjwAH0ArlyQeANOOSMeAA5VjyBxKBQhgSpVUKKQNGloFdw4cWw2jtfMOna6JOUArDTazXi/b3dm55socPqQhFka++aHBsI8GsopRJERNFlY88FCEk9Yiwf8RhgRyaHFQpPHCDmZG5oX2ui2yilkcTT1AcDsbYC1NMAyOi7zTX2Agx7A9luAl88BauiiQ/cJaZQfIpAlngDcvZZMrl8vFPK5+XktrWlx3/ehZ5r9+t6e+WVnp1pxnNIjgBe4/6dAysQc8dsmHwPcW9C0h3fW1hans1ltwJhy0GxK7XZbUlMp5Ww2eyan6+ft/f2FAqXGK4CvQk5HueFz7D6GOZtIrK+srupdx1GRBBqNBtzc2AiMr7nPplRdKhb1q6q6zjFhrklEFOUutoQ50xcX86ZlqaZpQrfbBdu2R6/G19zX6XSgh6RX5ubyHCM8nqSID6ICrGiZjGYYxojEsiw4PDwMSL5VKsC8Yf4VRYFzMzMaxwjlJSlCyAQ9l0CW44PBADzXhe7xMdi9HtTrdYjFYkDQL0cn4Xdq2/EAE+InCnvADTf2eah4Sx9vExQjkqXT6aAERICMewd/UAp/IeYANM2joxt+q5VI+ieq2i0Wg3l6DNzHwTERPgo1ko7XBXj3vdlsT2F+UuhIhYkp7u7CarkcrFOCtR3H5JiwbAIeImjT/YQKKBtGjRFCU5IUgFRe7fF4cCNVIPMYo3VKqxwjyNAXNepuopyqnld602qVsfRpEkkz+GFL1wPj6ySXBpJtWVa5xlhpcyhBNwpZHmtX8AGgfIExo0ZpzkWVTBGiXCSEaHh62/PoR0p/vHaczxXGnj4bSo+G78lELU80h1uogBwWLf5YlsPmgDEd4M236xjm+8nm4IuE/9u+/PH2JXZfbwz4zw1WbO+SQPpXfwG/BBgAhCNZiSb/pOQAAAAASUVORK5CYII=)"
			},
			success: {
				"color": "#468847",
				"background-color": "#DFF0D8",
				"border-color": "#D6E9C6",
				"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAutJREFUeNq0lctPE0Ecx38zu/RFS1EryqtgJFA08YCiMZIAQQ4eRG8eDGdPJiYeTIwHTfwPiAcvXIwXLwoXPaDxkWgQ6islKlJLSQWLUraPLTv7Gme32zoF9KSTfLO7v53vZ3d/M7/fIth+IO6INt2jjoA7bjHCJoAlzCRw59YwHYjBnfMPqAKWQYKjGkfCJqAF0xwZjipQtA3MxeSG87VhOOYegVrUCy7UZM9S6TLIdAamySTclZdYhFhRHloGYg7mgZv1Zzztvgud7V1tbQ2twYA34LJmF4p5dXF1KTufnE+SxeJtuCZNsLDCQU0+RyKTF27Unw101l8e6hns3u0PBalORVVVkcaEKBJDgV3+cGM4tKKmI+ohlIGnygKX00rSBfszz/n2uXv81wd6+rt1orsZCHRdr1Imk2F2Kob3hutSxW8thsd8AXNaln9D7CTfA6O+0UgkMuwVvEFFUbbAcrkcTA8+AtOk8E6KiQiDmMFSDqZItAzEVQviRkdDdaFgPp8HSZKAEAL5Qh7Sq2lIJBJwv2scUqkUnKoZgNhcDKhKg5aH+1IkcouCAdFGAQsuWZYhOjwFHQ96oagWgRoUov1T9kRBEODAwxM2QtEUl+Wp+Ln9VRo6BcMw4ErHRYjH4/B26AlQoQQTRdHWwcd9AH57+UAXddvDD37DmrBBV34WfqiXPl61g+vr6xA9zsGeM9gOdsNXkgpEtTwVvwOklXLKm6+/p5ezwk4B+j6droBs2CsGa/gNs6RIxazl4Tc25mpTgw/apPR1LYlNRFAzgsOxkyXYLIM1V8NMwyAkJSctD1eGVKiq5wWjSPdjmeTkiKvVW4f2YPHWl3GAVq6ymcyCTgovM3FzyRiDe2TaKcEKsLpJvNHjZgPNqEtyi6mZIm4SRFyLMUsONSSdkPeFtY1n0mczoY3BHTLhwPRy9/lzcziCw9ACI+yql0VLzcGAZbYSM5CCSZg1/9oc/nn7+i8N9p/8An4JMADxhH+xHfuiKwAAAABJRU5ErkJggg==)"
			},
			info: {
				"color": "#3A87AD",
				"background-color": "#D9EDF7",
				"border-color": "#45C1DA",
				"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3QYFAhkSsdes/QAAA8dJREFUOMvVlGtMW2UYx//POaWHXg6lLaW0ypAtw1UCgbniNOLcVOLmAjHZolOYlxmTGXVZdAnRfXQm+7SoU4mXaOaiZsEpC9FkiQs6Z6bdCnNYruM6KNBw6YWewzl9z+sHImEWv+vz7XmT95f/+3/+7wP814v+efDOV3/SoX3lHAA+6ODeUFfMfjOWMADgdk+eEKz0pF7aQdMAcOKLLjrcVMVX3xdWN29/GhYP7SvnP0cWfS8caSkfHZsPE9Fgnt02JNutQ0QYHB2dDz9/pKX8QjjuO9xUxd/66HdxTeCHZ3rojQObGQBcuNjfplkD3b19Y/6MrimSaKgSMmpGU5WevmE/swa6Oy73tQHA0Rdr2Mmv/6A1n9w9suQ7097Z9lM4FlTgTDrzZTu4StXVfpiI48rVcUDM5cmEksrFnHxfpTtU/3BFQzCQF/2bYVoNbH7zmItbSoMj40JSzmMyX5qDvriA7QdrIIpA+3cdsMpu0nXI8cV0MtKXCPZev+gCEM1S2NHPvWfP/hL+7FSr3+0p5RBEyhEN5JCKYr8XnASMT0xBNyzQGQeI8fjsGD39RMPk7se2bd5ZtTyoFYXftF6y37gx7NeUtJJOTFlAHDZLDuILU3j3+H5oOrD3yWbIztugaAzgnBKJuBLpGfQrS8wO4FZgV+c1IxaLgWVU0tMLEETCos4xMzEIv9cJXQcyagIwigDGwJgOAtHAwAhisQUjy0ORGERiELgG4iakkzo4MYAxcM5hAMi1WWG1yYCJIcMUaBkVRLdGeSU2995TLWzcUAzONJ7J6FBVBYIggMzmFbvdBV44Corg8vjhzC+EJEl8U1kJtgYrhCzgc/vvTwXKSib1paRFVRVORDAJAsw5FuTaJEhWM2SHB3mOAlhkNxwuLzeJsGwqWzf5TFNdKgtY5qHp6ZFf67Y/sAVadCaVY5YACDDb3Oi4NIjLnWMw2QthCBIsVhsUTU9tvXsjeq9+X1d75/KEs4LNOfcdf/+HthMnvwxOD0wmHaXr7ZItn2wuH2SnBzbZAbPJwpPx+VQuzcm7dgRCB57a1uBzUDRL4bfnI0RE0eaXd9W89mpjqHZnUI5Hh2l2dkZZUhOqpi2qSmpOmZ64Tuu9qlz/SEXo6MEHa3wOip46F1n7633eekV8ds8Wxjn37Wl63VVa+ej5oeEZ/82ZBETJjpJ1Rbij2D3Z/1trXUvLsblCK0XfOx0SX2kMsn9dX+d+7Kf6h8o4AIykuffjT8L20LU+w4AZd5VvEPY+XpWqLV327HR7DzXuDnD8r+ovkBehJ8i+y8YAAAAASUVORK5CYII=)"
			},
			warn: {
				"color": "#C09853",
				"background-color": "#FCF8E3",
				"border-color": "#DABF31",
				"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAMAAAC6V+0/AAABJlBMVEXr6eb/2oD/wi7/xjr/0mP/ykf/tQD/vBj/3o7/uQ//vyL/twebhgD/4pzX1K3z8e349vK6tHCilCWbiQymn0jGworr6dXQza3HxcKkn1vWvV/5uRfk4dXZ1bD18+/52YebiAmyr5S9mhCzrWq5t6ufjRH54aLs0oS+qD751XqPhAybhwXsujG3sm+Zk0PTwG6Shg+PhhObhwOPgQL4zV2nlyrf27uLfgCPhRHu7OmLgAafkyiWkD3l49ibiAfTs0C+lgCniwD4sgDJxqOilzDWowWFfAH08uebig6qpFHBvH/aw26FfQTQzsvy8OyEfz20r3jAvaKbhgG9q0nc2LbZxXanoUu/u5WSggCtp1anpJKdmFz/zlX/1nGJiYmuq5Dx7+sAAADoPUZSAAAAAXRSTlMAQObYZgAAAAFiS0dEAIgFHUgAAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfdBgUBGhh4aah5AAAAlklEQVQY02NgoBIIE8EUcwn1FkIXM1Tj5dDUQhPU502Mi7XXQxGz5uVIjGOJUUUW81HnYEyMi2HVcUOICQZzMMYmxrEyMylJwgUt5BljWRLjmJm4pI1hYp5SQLGYxDgmLnZOVxuooClIDKgXKMbN5ggV1ACLJcaBxNgcoiGCBiZwdWxOETBDrTyEFey0jYJ4eHjMGWgEAIpRFRCUt08qAAAAAElFTkSuQmCC)"
			}
		}
	});

	addStyle('zkdefault', {
		html: "<div>\n<span data-notify-text></span>\n</div>",
	    classes: {
	      base: {
	        "font-size": "24px",
	        "font-weight": "bold",
	        "padding": "18px 15px 18px 4px",
	        "text-shadow": "0 1px 0 rgba(255, 255, 255, 0.5)",
	        "background-color": "#fcf8e3",
	        "border": "1px solid #fbeed5",
	        "border-radius": "4px",
	        "white-space": "nowrap",
	        "padding-left": "32px",
	        "background-repeat": "no-repeat",
	        "background-position": "5px center"
	      },
	      error: {
	        "color": "#B94A48",
	        "background-color": "#F2DEDE",
	        "border-color": "#DA596C",
	        //"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAtRJREFUeNqkVc1u00AQHq+dOD+0poIQfkIjalW0SEGqRMuRnHos3DjwAH0ArlyQeANOOSMeAA5VjyBxKBQhgSpVUKKQNGloFdw4cWw2jtfMOna6JOUArDTazXi/b3dm55socPqQhFka++aHBsI8GsopRJERNFlY88FCEk9Yiwf8RhgRyaHFQpPHCDmZG5oX2ui2yilkcTT1AcDsbYC1NMAyOi7zTX2Agx7A9luAl88BauiiQ/cJaZQfIpAlngDcvZZMrl8vFPK5+XktrWlx3/ehZ5r9+t6e+WVnp1pxnNIjgBe4/6dAysQc8dsmHwPcW9C0h3fW1hans1ltwJhy0GxK7XZbUlMp5Ww2eyan6+ft/f2FAqXGK4CvQk5HueFz7D6GOZtIrK+srupdx1GRBBqNBtzc2AiMr7nPplRdKhb1q6q6zjFhrklEFOUutoQ50xcX86ZlqaZpQrfbBdu2R6/G19zX6XSgh6RX5ubyHCM8nqSID6ICrGiZjGYYxojEsiw4PDwMSL5VKsC8Yf4VRYFzMzMaxwjlJSlCyAQ9l0CW44PBADzXhe7xMdi9HtTrdYjFYkDQL0cn4Xdq2/EAE+InCnvADTf2eah4Sx9vExQjkqXT6aAERICMewd/UAp/IeYANM2joxt+q5VI+ieq2i0Wg3l6DNzHwTERPgo1ko7XBXj3vdlsT2F+UuhIhYkp7u7CarkcrFOCtR3H5JiwbAIeImjT/YQKKBtGjRFCU5IUgFRe7fF4cCNVIPMYo3VKqxwjyNAXNepuopyqnld602qVsfRpEkkz+GFL1wPj6ySXBpJtWVa5xlhpcyhBNwpZHmtX8AGgfIExo0ZpzkWVTBGiXCSEaHh62/PoR0p/vHaczxXGnj4bSo+G78lELU80h1uogBwWLf5YlsPmgDEd4M236xjm+8nm4IuE/9u+/PH2JXZfbwz4zw1WbO+SQPpXfwG/BBgAhCNZiSb/pOQAAAAASUVORK5CYII=)"
	        "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RjY3QTI3Njg3REM3MTFFQkFCOTVFNUI2NDhEQzI1NEQiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RjY3QTI3Njk3REM3MTFFQkFCOTVFNUI2NDhEQzI1NEQiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpGNjdBMjc2NjdEQzcxMUVCQUI5NUU1QjY0OERDMjU0RCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpGNjdBMjc2NzdEQzcxMUVCQUI5NUU1QjY0OERDMjU0RCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pi/Bm2IAAAQ3SURBVHjavFZLbxtVGD3zsmfssS07b6dpWsgighQ1taISpHaDRNizQsqKfdVf0H/TDQv2KBJCSEhZmYiQIBYtshvydGxnHHvG8+Zcxw4Tk7ArV7q6d+7jfN93vscdCXc36Y5x1OI7xltB7gIWXU6MyRYNQUfjrYKksXk8BignwG5ryf2kwBHWvwSMgBVxYAJI3QfmMsAiNybYjaGKDnvTBurvgOMm4A3vhuMWqWOWXGs/D0w+Bz76TFXXF3K5SkbXF9OaVojjGF4QWLbj1A+63ep2EGz/BPx+CJwPweWhoIEVSb4HmnOilIHpTeCLJ6b51fzCwkr54cN8vlQyFElShYAwjoNOq+Uc1Wqdw4ODvZ1u97vXwNYRcBZfgV9boiSoGczvEfxrYGMtn9/8pFJ5Or+8PKlls2k3jmXLddELQ0iqKqdNM12cnc3nstlZtdWaKnlej3QddYBe0gdKUvspck5aKs9N85tHjx8/zZXLZiBJuDg9RVAuY+7lS5jPnqG1v49evQ4lk4FqGKmsrk9I7bbp+P7bMwqx//FBfCMEZ4HyWiq1Plsur6RLJbPrOGg1m7hoNOCnUsivrg66n06jw/WOZcH1fRiTk+bUzMzKmqatC4ykP5NOjhki9+cMo1Kcns5b3S58Xhat73nQej30SZFYa56doUWrdM4lWYamacKS/GQqVcn4/s+8UhsFjprwgXBKSdW0BzxsXFJ7j4DCqZfUNGq3cUJgl2sNgp8fH0OlEoIHXdeRz2YNHn5AtUvJfBoPU4Y8CtxVwyCAK4TYNi7PzxFSQKffh6IokOkXjYI1jkIBmWcD11XjKCoMMZC04EZxCUUY8oJPShzyHJIeWXSuxdRYIf8pntGFEHawDyKE++LueMqryfrBTdsJQ8tut+eosapQe5UADEnEb96g/uIFZHIe1WrIce06iQRwEAR+HFtMADuJKSfSWuJm69J1a4wQRyPXWYIbQlM6M1MqobyxMejZYhEardJ5SeynKcD1PMcOw5rASORBnLRApuh3R65bLVjW6lImkxNU+EysHrnPLi3h41evBhrt7Owg2N1FxjAG30xCXHhe5ySKqgIjETQDikYVUGYxOfo1irYLtv1pUVEKZUUx89SQkQWZuXCytXUV4IyiAteE9k4UoeX73TPf39uN4+0GMZLlXBovFdPAzOfAl8uquvnIMJ7cU1VTUCEy2lKvDC7QoSqBHc7/Ivhev//LH2H4+gfge2by6SheRrXoRn1nIekfAydaFPV6QWB2okiUaEmPIiXrOLJO37iUd8j93/r9833Pq+5H0bc/stiRgcb4IzT+HI5qk1pkuf6Q5foDYH1KliumLC/SksIgswGrG0X1RhxX/yQtb1mu21flOkhoj6SAWx8cZkyKiHP8WOStCQwfHDaHB5tEqVt8cOz/eHD+1yfzvT/67+W35W8BBgCa3SBoEUZ/KQAAAABJRU5ErkJggg==)",
	      },
	      success: {
	        "color": "#468847",
	        "background-color": "#DFF0D8",
	        "border-color": "#D6E9C6",
	        //"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAutJREFUeNq0lctPE0Ecx38zu/RFS1EryqtgJFA08YCiMZIAQQ4eRG8eDGdPJiYeTIwHTfwPiAcvXIwXLwoXPaDxkWgQ6islKlJLSQWLUraPLTv7Gme32zoF9KSTfLO7v53vZ3d/M7/fIth+IO6INt2jjoA7bjHCJoAlzCRw59YwHYjBnfMPqAKWQYKjGkfCJqAF0xwZjipQtA3MxeSG87VhOOYegVrUCy7UZM9S6TLIdAamySTclZdYhFhRHloGYg7mgZv1Zzztvgud7V1tbQ2twYA34LJmF4p5dXF1KTufnE+SxeJtuCZNsLDCQU0+RyKTF27Unw101l8e6hns3u0PBalORVVVkcaEKBJDgV3+cGM4tKKmI+ohlIGnygKX00rSBfszz/n2uXv81wd6+rt1orsZCHRdr1Imk2F2Kob3hutSxW8thsd8AXNaln9D7CTfA6O+0UgkMuwVvEFFUbbAcrkcTA8+AtOk8E6KiQiDmMFSDqZItAzEVQviRkdDdaFgPp8HSZKAEAL5Qh7Sq2lIJBJwv2scUqkUnKoZgNhcDKhKg5aH+1IkcouCAdFGAQsuWZYhOjwFHQ96oagWgRoUov1T9kRBEODAwxM2QtEUl+Wp+Ln9VRo6BcMw4ErHRYjH4/B26AlQoQQTRdHWwcd9AH57+UAXddvDD37DmrBBV34WfqiXPl61g+vr6xA9zsGeM9gOdsNXkgpEtTwVvwOklXLKm6+/p5ezwk4B+j6droBs2CsGa/gNs6RIxazl4Tc25mpTgw/apPR1LYlNRFAzgsOxkyXYLIM1V8NMwyAkJSctD1eGVKiq5wWjSPdjmeTkiKvVW4f2YPHWl3GAVq6ymcyCTgovM3FzyRiDe2TaKcEKsLpJvNHjZgPNqEtyi6mZIm4SRFyLMUsONSSdkPeFtY1n0mczoY3BHTLhwPRy9/lzcziCw9ACI+yql0VLzcGAZbYSM5CCSZg1/9oc/nn7+i8N9p/8An4JMADxhH+xHfuiKwAAAABJRU5ErkJggg==)"
	        "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QjUwNzBDRDI3REM4MTFFQkJEOEVBQjQxRTNCODQyRjkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QjUwNzBDRDM3REM4MTFFQkJEOEVBQjQxRTNCODQyRjkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpCNTA3MENEMDdEQzgxMUVCQkQ4RUFCNDFFM0I4NDJGOSIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpCNTA3MENEMTdEQzgxMUVCQkQ4RUFCNDFFM0I4NDJGOSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PktJkFsAAASqSURBVHjavFZLjBRFGP6qu7pnuqd3hp3HAguyyxIeGfBB1oibKCQmoCEeVE4Y4sGb3rl48WQ0XhRjfGE0ihdiPJvggZiIq5gxJjw0xl1mFmZnX7PLPLp7+lFV1ryW3hHCRa2kUj3TVd//f/X9jya49yD3WPtD3GPdMOh9gNtTiazRwXugfABc3A2s/ywGAJUI2N1G9H3UYB/rHwz6oGpnQ0bRMKZshSnGQHhGHjN6ProQShUOKaHEK6hyv4fFBlnQASZ3vN+mZHEklqdT1pQ5OjQZM2Jjqqam2ptYwGqe65XscqPAfmpO43v/Ospspee90jPUYRG9767nRM5RdQSnEsfMR1InRjePHtiWHU0OW8NGXI/RtmstzwvXmqvurep8fWGxctX5rfYNvrIvYJ4tSd9Zz0DnytSImN3n7RL8pPm0NZk+dWDP/kMTufGsSc2YwojCQw4ecHlbUHRVj6WHMklzyNhS05u5IMNszLF51IUd1UDd4H1O0XFYnzSfTL+c350/NJIYsYj0JQgC+IHfWduTsRANx4bvtZDQLN20zExda1pBy5vBEp+HI0TPQIeBsm5ol/KAdjx5fHR8+zPbh7dlRchgO04PlIFzjoAFWGwu4+jwYRxNH4HDXXjE023fNhy9tcxnvSIWRa0vNt0QoiZ2GJsTk7lULuk6Lqr2KnRJytISCHkI23PQ9BqYSj6Gk8PPY392H2I3Y7hcKoASmtQy8cnQxA8Sq9gPHBrRQIoi0pRq45pKjabdRD62Byt+FcW1m+BtNq6DRxMP4ZWRl7Cf7kZ5sYzZ8mxnJYZiEIhxKCQdzSdlY5gKUxCREpLZZjWH0xOv4kTuWTQadczfriAf343X957GwU0PYslfwSc3vsSbf77bviIQAsoFb4exGcWkg8nv+x4Siomn0k8gYRs4mXkOs+NFXFr8GW/sew351N4O4Ds3PsaHc58jMEJomgZGuxqtp9pAonUzjxNHnq3N25Wt3za/o/tSO7FT34EXcy/gWOoIHt50AEyG1Vsz7+HswjnYcQdDmiW9J22NQhnCNWnAiWLSfjh1KDGyypyguNxYGivS0tCZ4FPoagyHM49DSogqW8WZ0lmcrZ5DLV7Hpliq65fgCG/7rnDCojSwGskDEWWgSNtzXtkt0JR20Jgwhy4Gl0BXKVJmEuNkBz6qfIEP1j6DnXA64EKGO1GltJ6Af7tV5wtBQebA3J2g6cb/nUIn5GuVE54Wu7SktsXMmvoV8QfcoCVFXcbba+/DTwawzASEJlHiBO3sDpZaTa/kFsRl/2v8xa7LUsh75WK9VHRVb79owOUWgpD5ORl6mXja0Gck8x9bv4CPcGgGBaHdGxCeBC+7zdbVxq/89+A8psNprIh6pGxvYNBlYYsWKmyBa8wObd8SDWYEakACK1C1OFWIPMrtMAxveXbrSnPFv+YU+DX/PC6GFyT48kATEoPtsM+IYhhZ7CJ5TKhTyog6qVjKGDTSVdUXNW7zklhiBTHLpjGD61gT7XId9oDX+8JgRyMbGo4JHSlslb/G2u1H/mf09rpyZ1XClFBDRQaH3zvLot7/Ly2T/ItNX9yv6f8nny1/CzAAxF5HmB8SNswAAAAASUVORK5CYII=)"
	      },
	      info: {
	        "color": "#3A87AD",
	        "background-color": "#D9EDF7",
	        "border-color": "#45C1DA",
	        //"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH3QYFAhkSsdes/QAAA8dJREFUOMvVlGtMW2UYx//POaWHXg6lLaW0ypAtw1UCgbniNOLcVOLmAjHZolOYlxmTGXVZdAnRfXQm+7SoU4mXaOaiZsEpC9FkiQs6Z6bdCnNYruM6KNBw6YWewzl9z+sHImEWv+vz7XmT95f/+3/+7wP814v+efDOV3/SoX3lHAA+6ODeUFfMfjOWMADgdk+eEKz0pF7aQdMAcOKLLjrcVMVX3xdWN29/GhYP7SvnP0cWfS8caSkfHZsPE9Fgnt02JNutQ0QYHB2dDz9/pKX8QjjuO9xUxd/66HdxTeCHZ3rojQObGQBcuNjfplkD3b19Y/6MrimSaKgSMmpGU5WevmE/swa6Oy73tQHA0Rdr2Mmv/6A1n9w9suQ7097Z9lM4FlTgTDrzZTu4StXVfpiI48rVcUDM5cmEksrFnHxfpTtU/3BFQzCQF/2bYVoNbH7zmItbSoMj40JSzmMyX5qDvriA7QdrIIpA+3cdsMpu0nXI8cV0MtKXCPZev+gCEM1S2NHPvWfP/hL+7FSr3+0p5RBEyhEN5JCKYr8XnASMT0xBNyzQGQeI8fjsGD39RMPk7se2bd5ZtTyoFYXftF6y37gx7NeUtJJOTFlAHDZLDuILU3j3+H5oOrD3yWbIztugaAzgnBKJuBLpGfQrS8wO4FZgV+c1IxaLgWVU0tMLEETCos4xMzEIv9cJXQcyagIwigDGwJgOAtHAwAhisQUjy0ORGERiELgG4iakkzo4MYAxcM5hAMi1WWG1yYCJIcMUaBkVRLdGeSU2995TLWzcUAzONJ7J6FBVBYIggMzmFbvdBV44Corg8vjhzC+EJEl8U1kJtgYrhCzgc/vvTwXKSib1paRFVRVORDAJAsw5FuTaJEhWM2SHB3mOAlhkNxwuLzeJsGwqWzf5TFNdKgtY5qHp6ZFf67Y/sAVadCaVY5YACDDb3Oi4NIjLnWMw2QthCBIsVhsUTU9tvXsjeq9+X1d75/KEs4LNOfcdf/+HthMnvwxOD0wmHaXr7ZItn2wuH2SnBzbZAbPJwpPx+VQuzcm7dgRCB57a1uBzUDRL4bfnI0RE0eaXd9W89mpjqHZnUI5Hh2l2dkZZUhOqpi2qSmpOmZ64Tuu9qlz/SEXo6MEHa3wOip46F1n7633eekV8ds8Wxjn37Wl63VVa+ej5oeEZ/82ZBETJjpJ1Rbij2D3Z/1trXUvLsblCK0XfOx0SX2kMsn9dX+d+7Kf6h8o4AIykuffjT8L20LU+w4AZd5VvEPY+XpWqLV327HR7DzXuDnD8r+ovkBehJ8i+y8YAAAAASUVORK5CYII=)"
	        "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MjdCRUNEODU3REM5MTFFQjhCMTFFODE5ODE5OTM4RUUiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MjdCRUNEODY3REM5MTFFQjhCMTFFODE5ODE5OTM4RUUiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDoyN0JFQ0Q4MzdEQzkxMUVCOEIxMUU4MTk4MTk5MzhFRSIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDoyN0JFQ0Q4NDdEQzkxMUVCOEIxMUU4MTk4MTk5MzhFRSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PsremcoAAAVzSURBVHja1FZLbxNXFD53Zu7MJH6N7ThxQiCBNCU04VXog0JLIIRuKJVaiVathFh20R0/oOoPYN0trdRNVXXRVl0QBMRUNFIDRCU0LXkTE8fP8Xg8nvfcHhtBScOWRa985Tvy3POd+53vfNeEMQYvcnDwgofwZPHVd3NgOR4YlgPpTgV29iUhc+0asOjOpNx5MH1vLkfLtUbQfDcZbef2DnW7ZuHuBmhL5WNHj8PqegnyRQ3aZQqSJMDnH+/bDNBkKggY+H4AhBDgeQ44AmA63gjv+u9wHCfgaPGJa+K6vuc6XkYkMMkLXGuP77NWDPa8E/A8gUhYJrIsMMtyQVUbb3ogD5RU+7zEaacFgcfsJGjWTMB3C4Uq2KpzpatN7lU1Y9G23amE0g5U4AiDfwv7FAADkN60AuGQJN2Ymh9cy9W+LOqx0XKDUsErQ3t7uycLQDBFCDyX5fMNwbf4cfCjo9P3sjfCMnfx6OGBecO0nVyhSpqkbCoyHrsJjMM7ZDScSzNzpbG5FU/U6gLTVJ1ZRoXfkSZc/zaOc3Fd0zSmmwKbzwbi/YXKWMNyL0HgH2rGaMX67wmSXUlYzS7DzO07/Xk9fOLhesD7yLTiebzd0IkiKnDk1T2YCMCD+w+glMOCRmLM0C23ARaNStaJH39Z+npo5MCtjq7erTVIpJRkRVWGF9caF+ZX16lHEozKtqD7KvEdA4qPqpCZEFvFLGTXoKGZ4Fp1znUDTKPB7lZUOrBduTByKJZVEon7GLK8CWBxodCl6eQ4JyXG8/l5Fo74KIs6BBaA38xRDoFbTwOKDLTiOlSLdRDEMD4z4jgNMAyV9fZ0j5eqzk1vIVc6Ndy9GeDq1WnRsW26/igLzLN9p1HlwTeIiJpWCznY1d0Pn370FngewO9TvyFFWYinesB1PZw2c23bf7iWFTI3p6ksSeJn73+wmaLlpaXAcZygUiqB71kk4ALwgCIYgFHZAKseAyUaQq1jjxAPmO8+VggLWk1EkLxyqYqPywGlNNhSA44EuLE5fdzoAooZOXbBQlmCaWIX+hgHJYq/UkmCtvYQzjBGsIE4yCZmgupp1ehZe3sKsOflXZxlmZxj1yGfy7ImFa4TAGvKhueB4Gx1KW4OhSMQTaQgEk+BaNtgGlXE91hnZxwtZgdHqchtATj73htOuVRzK+U83Jmu8zyVMRUBu5ZHAPwWMeuQ2OwzCEcVwA+EYgkE8JouQLDI/PZtXXBy9LCrKGFni5sOD6XynUl+kvO1iURMJn7dQEYChv6DaUgARAK1ZoFqOEBoCKS2GEhyBKgcZlQKsWQ8RGw9N6FEyOTQ7m35LQCVklbmAyPz+v7uy0de2+dKSGZQ0z0WsECOxqFqcPDDTzPw88TfoFkiSJEEggqBgzLiiE/2j/S7I4PKZeLpGa2ilrdQVMgVobcrBZ+cO7tis8z1xdXi2MqfK9St64EYS7CNkgnffn8LaUmCjAXmqQS2pYPA6jSmEP+V3Tuunxnfv2KaPmwUSxgxvhnAshyyklWhYdq347HoxXMfnro03XdvdOrWDK1VS4QJksfLOiGiDBxPWOCqgiy4MLSzwxno673RJtOLs39tzEsiJU3Lf2J2z94HrFa3iG5Y9u7BvtmhlxJfBHZhwGcHzgty6nRRswTT50CUZUgmI9CR6AGrujzRmzC/OfPu3sWFpfJsdr2CbtxGZIluNbvHjkoYNiHE4xHYnlamZBpMnRw/mO0bPPzrlSt/CEtrecaICJ2pBHn72LD3aN7NBPraZLorhreZCSKttWIQ8pw+gGeO0jyigxaA0oZ0R2i2pztekmWZoqJQpTzeHZSLx8OuHmvbqNRIyy6aPfK8Qf73/yr+EWAAfkDQidJN0AwAAAAASUVORK5CYII=)"
	      },
	      warn: {
	        "color": "#C09853",
	        "background-color": "#FCF8E3",
	        "border-color": "#DABF31",
	        //"background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAMAAAC6V+0/AAABJlBMVEXr6eb/2oD/wi7/xjr/0mP/ykf/tQD/vBj/3o7/uQ//vyL/twebhgD/4pzX1K3z8e349vK6tHCilCWbiQymn0jGworr6dXQza3HxcKkn1vWvV/5uRfk4dXZ1bD18+/52YebiAmyr5S9mhCzrWq5t6ufjRH54aLs0oS+qD751XqPhAybhwXsujG3sm+Zk0PTwG6Shg+PhhObhwOPgQL4zV2nlyrf27uLfgCPhRHu7OmLgAafkyiWkD3l49ibiAfTs0C+lgCniwD4sgDJxqOilzDWowWFfAH08uebig6qpFHBvH/aw26FfQTQzsvy8OyEfz20r3jAvaKbhgG9q0nc2LbZxXanoUu/u5WSggCtp1anpJKdmFz/zlX/1nGJiYmuq5Dx7+sAAADoPUZSAAAAAXRSTlMAQObYZgAAAAFiS0dEAIgFHUgAAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfdBgUBGhh4aah5AAAAlklEQVQY02NgoBIIE8EUcwn1FkIXM1Tj5dDUQhPU502Mi7XXQxGz5uVIjGOJUUUW81HnYEyMi2HVcUOICQZzMMYmxrEyMylJwgUt5BljWRLjmJm4pI1hYp5SQLGYxDgmLnZOVxuooClIDKgXKMbN5ggV1ACLJcaBxNgcoiGCBiZwdWxOETBDrTyEFey0jYJ4eHjMGWgEAIpRFRCUt08qAAAAAElFTkSuQmCC)"
	        "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OTA3MzgwNDI4MTZBMTFFQjlCQzFGQzFDNjdBNzAxQjMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OTA3MzgwNDM4MTZBMTFFQjlCQzFGQzFDNjdBNzAxQjMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo5MDczODA0MDgxNkExMUVCOUJDMUZDMUM2N0E3MDFCMyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo5MDczODA0MTgxNkExMUVCOUJDMUZDMUM2N0E3MDFCMyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PonucFIAAAGvSURBVHjaYnz98hkDLQETA43BCLNgz47U/7u3Jf2niQXb13n9N1LRZjBRUWPYvtr+P1UtWDJX93+4VxGDmqwag6q0AkO4YyjDktlq/6liwYvrHf9drKIZ2Bh/MTDKpTB0rf4NZH9ncNE3YXhxKuo/RRZsWeX0X0dJjwGEGf58QkgA2TrSomC8ZYn2/8GZimZNkfvvax8DcTkMI/kAhn11FRhmTZb+T5IF5093/bfQc2UQ5uFCMuwjmgWfwViY4y+DhRw/w85tpf+JtuDqxYUMNnp2CIPBGNkHIMO/QPDfLww28uwMrx8tJ84H+3dn/3e38AOmlJ8IgzGCCGIwBH9jYGP4weCuxMywenk4hi9Y0AX+fr7JoGkWhmo4NFj+H7MC0kvhBkM0fGdg+PeDQVPoN8PFF4cHQSoSFxTBDBpopDJaHWPoOh4NcT3I5VDXM/z7CdHLjWkBRhBduXeRwUpVnoGd8QcitUDD/f9+JaCh/QiDQQBq+M+/QL1vGBh00cxjxFajLZiu8F+Ai4OB4f8/IP4LFf0H4cNp1Pj8ALQvIfsZIwMxFozWaMPLAoAAAwDgnuDpqHGAXQAAAABJRU5ErkJggg==)"
	      }
	    }
	});

	$(function() {
		insertCSS(coreStyle.css).attr("id", "core-notify");
		$(document).on("click", "." + pluginClassName + "-hidable", function(e) {
			$(this).trigger("notify-hide");
		});
		$(document).on("notify-hide", "." + pluginClassName + "-wrapper", function(e) {
			var elem = $(this).data(pluginClassName);
			if(elem) {
				elem.show(false);
			}
		});
	});

}));
