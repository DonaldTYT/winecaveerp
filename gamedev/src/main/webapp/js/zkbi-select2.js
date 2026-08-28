var zkbis2 = (function() {
	function getFocusScope(p_target) {
		if (typeof window.zkbiGetDetailFocusScope === 'function')
			return window.zkbiGetDetailFocusScope(p_target);
		return $();
	}

	function getFocusable(p_target) {
		const scope = getFocusScope(p_target);
		if (scope.length && typeof window.zkbiGetDetailFocusable === 'function')
			return window.zkbiGetDetailFocusable(scope);
		return $('a[href],button:not([disabled]),input:not([disabled]):not([type=hidden]),select:not([disabled]),textarea:not([disabled]),[tabindex]:not([tabindex="-1"])').filter(':visible');
	}

	function focusNext(p_selection) {
		const focusable = getFocusable(p_selection);
		const index = focusable.index(p_selection);
		if (index >= 0 && index + 1 < focusable.length)
			focusable.eq(index + 1).focus();
		else if (index >= 0 && getFocusScope(p_selection).length)
			focusable.first().focus();
	}

	function focusFirstRowEditor(p_row) {
		if (!p_row || !p_row.length)
			return false;
		// The native select is rendered before Select2 adds its marker class.
		// Detect it immediately so an early retry cannot fall through to Description.
		const select2Source = p_row.find('select').first();
		const select2Selection = select2Source.next('.select2').find('.select2-selection');
		if (select2Selection.length) {
			select2Selection.focus();
			return true;
		}
		// The ZK row can arrive before Select2 creates its visible selection.
		// Do not fall through to Description; let the scheduled retries wait for it.
		if (select2Source.length)
			return false;
		const editor = p_row.find('input:not([disabled]):not([type=hidden]),'
				+ 'select:not([disabled]),textarea:not([disabled]),'
				+ '[tabindex]:not([tabindex="-1"])').filter(':visible').first();
		if (!editor.length)
			return false;
		editor.focus();
		return true;
	}

	function restoreRowFocusAfterAltKey(p_source, p_key) {
		const sourceUuid = p_source.attr('id');
		const row = p_source.closest('.z-listitem');
		const rowUuid = row.attr('id');
		const nextRowUuid = row.nextAll('.z-listitem:visible').first().attr('id') || '';
		const previousRowUuid = row.prevAll('.z-listitem:visible').first().attr('id') || '';
		let restored = false;
		const restore = function() {
			if (restored)
				return;
			let target;
			if (p_key === 'A') {
				const currentRow = $('#' + sourceUuid).closest('.z-listitem');
				const insertedRow = currentRow.nextAll('.z-listitem:visible').first();
				if (!currentRow.length || !insertedRow.length
						|| insertedRow.attr('id') === nextRowUuid)
					return;
				target = insertedRow;
			} else {
				if ($('#' + rowUuid).length)
					return;
				target = nextRowUuid ? $('#' + nextRowUuid) : $('#' + previousRowUuid);
			}
			restored = focusFirstRowEditor(target);
		};
		[50, 150, 300, 600, 1000].forEach(function(delay) {
			setTimeout(restore, delay);
		});
	}

	function setupReverseTab() {
		if (document.zkbiSelect2ReverseTabSetup)
			return;
		document.zkbiSelect2ReverseTabSetup = true;
		document.addEventListener('keydown', function(event) {
			if (event.key !== 'Tab' || !event.shiftKey)
				return;
			const focusable = getFocusable(event.target);
			const index = focusable.index(event.target);
			if (index === 0 && getFocusScope(event.target).length) {
				event.preventDefault();
				event.stopPropagation();
				focusable.last().focus();
				return;
			}
			if (index < 0)
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
		const forwardRowAltKey = function(event) {
			if (!event.altKey || event.ctrlKey || event.metaKey)
				return false;
			const key = (event.key || '').toUpperCase();
			if (key !== 'A' && key !== 'R')
				return false;
			const widget = typeof zk !== 'undefined' && zk.Widget
					? zk.Widget.$('#' + p_source.attr('id')) : null;
			if (!widget || typeof zAu === 'undefined')
				return false;
			event.preventDefault();
			event.stopPropagation();
			restoreRowFocusAfterAltKey(p_source, key);
			zAu.send(new zk.Event(widget, 'onS2AltKey', key, {toServer:true}));
			return true;
		};
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
			})
			.off('keydown.zkbiRowAltKey')
			.on('keydown.zkbiRowAltKey', function(event) {
				if (forwardRowAltKey(event))
					return;
				if (event.key !== 'Tab' || event.shiftKey)
					return;
				event.preventDefault();
				focusNext(selection);
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
							if (forwardRowAltKey(event))
								return;
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
	function focus(p_uuid) {
		const focusSelection = function() {
			const source = $('#' + p_uuid);
			if (!source.length)
				return false;
			const selection = source.next('.select2').find('.select2-selection');
			if (!selection.length)
				return false;
			selection.focus();
			return true;
		};
		if (focusSelection())
			return true;
		[50, 150, 300].forEach(function(delay) {
			setTimeout(focusSelection, delay);
		});
		return false;
	}
	function focusComponent(p_uuid) {
		const focusWidget = function() {
			const widget = typeof zk !== 'undefined' && zk.Widget
					? zk.Widget.$('#' + p_uuid) : null;
			if (!widget)
				return false;
			const node = typeof widget.getInputNode === 'function'
					? widget.getInputNode() : widget.$n();
			if (!node || typeof node.focus !== 'function')
				return false;
			node.focus();
			return true;
		};
		if (focusWidget())
			return true;
		[50, 150, 300].forEach(function(delay) {
			setTimeout(focusWidget, delay);
		});
		return false;
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
	return { setup:setup, destroy:destroy, destroyAll:destroyAll,
		focus:focus, focusComponent:focusComponent,
		restoreRowFocusAfterAltKey:restoreRowFocusAfterAltKey };
	
})();
