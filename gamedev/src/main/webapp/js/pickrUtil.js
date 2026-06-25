function createPickr(p_paramObj) {
	var pickr = Pickr.create($.extend({
	    //el: p_el,
	    theme: 'nano', 
	    //default: p_defaultColor,

	    swatches: [
	        '#F44336FF',
	        '#E91E63F2',
	        '#9C27B0E6',
	        '#673AB7D9',
	        '#3F51B5CC',
	        'rgba(33, 150, 243, 0.75)',
	        'rgba(3, 169, 244, 0.7)',
	        'rgba(0, 188, 212, 0.7)',
	        'rgba(0, 150, 136, 0.75)',
	        'rgba(76, 175, 80, 0.8)',
	        'rgba(139, 195, 74, 0.85)',
	        'rgba(205, 220, 57, 0.9)',
	        'rgba(255, 235, 59, 0.95)',
	        'rgba(255, 193, 7, 1)'
	    ],

	    components: {

	        // Main components
	        preview: true,
	        opacity: true,
	        hue: true,

	        // Input / output Options
	        interaction: {
	            hex: false,
	            rgba: false,
	            hsla: false,
	            hsva: false,
	            cmyk: false,
	            input: true,
	            clear: false,
	            save: true
	        }
	    },
	    // Translations
        i18n: {
        	'btn:save': 'OK'
        }
	}, p_paramObj));

	pickr.on('changestop', (color, instance) => {
		console.log('changestop');
	}).on('swatchselect', (color, instance) => {
		console.log('swatchselect');
		//instance.applyColor(false);
	}).on('save', (color, instance) => {
		console.log('save');
		instance.hide();
	});
	return pickr;
}