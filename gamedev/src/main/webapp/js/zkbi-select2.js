var zkbis2 = (function() {
	function getFocusable() {
		return $('a[href],button:not([disabled]),input:not([disabled]):not([type=hidden]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])').filter(':visible');
	}

	function focusNext(p_selection) {
		const focusable = getFocusable();
		const index = focusable.index(p_selection);
		if (index >= 0 && index + 1 < focusable.length)
			focusable.eq(index + 1).focus();
	}

	function setupReverseTab() {
		if (document.zkbiSelect2ReverseTabSetup)
			return;
		document.zkbiSelect2ReverseTabSetup = true;
		document.addEventListener('keydown', function(event) {
			if (event.key !== 'Tab' || !event.shiftKey)
				return;
			const focusable = getFocusable();
			const index = focusable.index(event.target);
			if (index <= 0)
				return;
			const previous = focusable.eq(index - 1);
			if (!previous.hasClass('select2-selection'))
				return;
			event.preventDefault();
			event.stopPropagation();
			previous.focus();
		}, true);
	}
	function restoreSelectionAfterUpdate(p_uuid) {
		[50, 150, 300].forEach(function(delay) {
			setTimeout(function() {
				const active = document.activeElement;
				const focusWasLost = !active || active === document.body
						|| !active.isConnected || $(active).hasClass('z-focus-a');
				if (!focusWasLost)
					return;
				const selection = $('#' + p_uuid).next('.select2')
						.find('.select2-selection');
				if (selection.length)
					selection.focus();
			}, delay);
		});
	}

	function setupKeyboard(p_source) {
		setupReverseTab();
		const selection = p_source.next('.select2').find('.select2-selection');
		const focusSelect2 = function() {
			const search = $('.select2-container--open .select2-search__field').last();
			if (search.length)
				search.focus();
			else
				selection.focus();
		};
		selection.off('mouseup.zkbiKeyboard')
			.on('mouseup.zkbiKeyboard', function() {
				setTimeout(focusSelect2, 20);
			});
		p_source.off('select2:open.zkbiKeyboard select2:close.zkbiKeyboard')
			.on('select2:open.zkbiKeyboard', function() {
				const source = $(this);
				setTimeout(function() {
					const search = $('.select2-container--open .select2-search__field').last();
					if (!search.length)
						return;
					search.focus();
					search.off('keydown.zkbiKeyboard')
						.on('keydown.zkbiKeyboard', function(event) {
							if (event.key !== 'Tab' || event.shiftKey)
								return;
							event.preventDefault();
							const selection = source.next('.select2').find('.select2-selection');
							source.select2('close');
							setTimeout(function() {
								focusNext(selection);
							}, 30);
						});
				}, 20);
			})
			.on('select2:close.zkbiKeyboard', function() {
				setTimeout(function() {
					selection.focus();
				}, 20);
			});
	}
	function destroy(p_uuid) {
		const source = $('#' + p_uuid);
		if (!source.length)
			return false;
		const resizeObserver = source.data('resizeObserver');
		if (resizeObserver)
			resizeObserver.disconnect();
		source.removeData('resizeObserver')
			.removeData('observeResizeElement')
			.off('.zkbiSelect2')
			.off('.zkbiKeyboard');
		if (source.data('select2'))
			source.select2('destroy');
		source.data('setupSelect2Status', '');
		return true;
	}
	function setupZkLifecycle(p_uuid) {
		if (typeof zk === 'undefined' || !zk.Widget)
			return;
		const widget = zk.Widget.$('#' + p_uuid);
		if (!widget || typeof widget.unbind_ !== 'function'
				|| widget._zkbiSelect2LifecycleSetup)
			return;
		const originalUnbind = widget.unbind_;
		widget._zkbiSelect2LifecycleSetup = true;
		widget.unbind_ = function() {
			destroy(this.uuid);
			return originalUnbind.apply(this, arguments);
		};
	}

	let setup = function(p_uuid, p_multiple, p_tags, p_placeholder, p_allowClear, p_allowListenResizeEvent) {
		if (!p_uuid || typeof $ === 'undefined' || !$.fn.select2)
			return false;
		const $uuid = $('#' + p_uuid);
		if (!$uuid.length)
			return false;
		//console.log('setup:'+ p_uuid +" comp:" + zk.Widget.$('#' + p_uuid) + " id:" + (zk.Widget.$('#' + p_uuid) == null ? "na" : zk.Widget.$('#' + p_uuid).id) );
   			//const comp = zk.Widget.$('#' + p_uuid);  //210511 pass const comp obj to inner function will trigger Cannot read property 'desktop' of null bug. probably a zk bug
   			const selectEvent = function(e) {
			//console.log('selectevent:'+ p_uuid +" comp:" + zk.Widget.$('#' + p_uuid) + " id:" + (zk.Widget.$('#' + p_uuid) == null ? "na" : zk.Widget.$('#' + p_uuid).id) );
				const comp = typeof zk !== 'undefined' && zk.Widget
						? zk.Widget.$('#' + p_uuid) : null;
			if (!comp || typeof zAu === 'undefined')
				return;
			const selectedItemIds = [];
			const removeItemIds = [];
			const tagItemValues = [];
			$(e.target).find('option').each(function(i, opt){
				if (opt.selected) {
					if (opt.id)
						selectedItemIds.push(opt.id);
					else {
						tagItemValues.push(opt.value);
						$(opt).remove();
					}
				}
				else if ($(opt).attr('data-select2-tag') && opt.id)
					removeItemIds.push(opt.id);
			});
			zAu.send(new zk.Event(comp, 'onSelect2Select', {listboxId: comp.id, selectedItemIds: selectedItemIds, removeItemIds: removeItemIds, tagItemValues: tagItemValues}, {toServer:true}));
   			};
			const opts = {};
   			if (typeof p_multiple !== "undefined"){
   				opts.multiple = p_multiple;
		}
   			if (typeof p_tags !== "undefined"){
   				opts.tags = p_tags;
		}
    		if (typeof p_placeholder !== "undefined" && p_placeholder){
   				opts.placeholder = p_placeholder;
    		}
    		if (typeof p_allowClear !== "undefined"){
    			opts.allowClear = p_allowClear;
    		}
    		
    		//change the matcher
    		opts.matcher = customMatcher;
    		
    		//console.log(opts);
		if ($uuid.data('setupSelect2Status') == 'Y')
			destroy(p_uuid);
		$uuid.select2(opts)
					.on('select2:select.zkbiSelect2', function(e){
						selectEvent(e);
						restoreSelectionAfterUpdate(p_uuid);
					})
					.on('select2:unselect.zkbiSelect2', function(e){
						selectEvent(e);
						restoreSelectionAfterUpdate(p_uuid);
					})
						.data('setupSelect2Status', 'Y')
						;
		setupKeyboard($uuid);
		setupZkLifecycle(p_uuid);

		//handle select2 resize event
		if (p_allowListenResizeEvent && typeof ResizeObserver === 'function') {
			const resizeObserver = new ResizeObserver(function(entries) {
				for (var i in entries) {
					var entry = entries[i];
					if ($(entry.target).hasClass('select2')) {
							const zkComp = typeof zk !== 'undefined' && zk.Widget
									? zk.Widget.$('#' + p_uuid) : null;
						if (zkComp && typeof zAu !== 'undefined')
							zAu.send(new zk.Event(zkComp, "onResize", {width: entry.contentRect.width, height: entry.contentRect.height}, {toServer:true}));
					}
				}
			});
			const observeEle = $uuid.closest('div').find('.select2')[0];
			if (observeEle) {
				resizeObserver.observe(observeEle);
				$uuid.data('resizeObserver', resizeObserver);
				$uuid.data('observeResizeElement', observeEle);
			}
		}
		return true;
	}
	let destroyAll = function() {
		$('select').each(function() {
			if ($(this).data('select2'))
				destroy(this.id);
		});
	};
	function customMatcher(params, data) {
	    params.term = params.term || '';
		//console.log('params.term:' + params.term);
		//console.log('data.text:' + data.text);
		
		//match from beginning
		if (params.term.startsWith("=")){
	    	if (data.text.toUpperCase().indexOf(params.term.substring(1).toUpperCase()) == 0) {
	    	    return data;
	    	}
		}
		else{
	    	if (data.text.toUpperCase().indexOf(params.term.toUpperCase()) >= 0) {
	    	    return data;
	    	}
		}
		return false;
	}	
	return { setup:setup, destroy:destroy, destroyAll:destroyAll };
	
})();
