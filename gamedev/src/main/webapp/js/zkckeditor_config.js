CKEDITOR.editorConfig = function(config) {
    //config.resize_enabled = false;
    config.toolbar = 'Simple';
    config.toolbar_Simple = [ [ 'Font', 'FontSize', 'TextColor', 'BGColor', 'Bold', 'Italic', 'Underline', 'Strike' ,
                               'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock' ]
                              ];
    //config.toolbar_Simple = [ [ 'Bold', 'Italic', '-', 'NumberedList', 'BulletedList', '-', 'Link', 'Unlink', '-', 'About' ] ];
    config.toolbar_Complex = [
            [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript',
                    'Superscript', 'TextColor', 'BGColor', '-', 'Cut', 'Copy',
                    'Paste', 'Link', 'Unlink', 'Image'],
            [ 'Undo', 'Redo', '-', 'JustifyLeft', 'JustifyCenter',
                    'JustifyRight', 'JustifyBlock' ],
            [ 'Table', 'Smiley', 'SpecialChar', 'PageBreak',
                    'Styles', 'Format', 'Font', 'FontSize', 'Maximize'] ];

    config.toolbar_HelpCk = [
    		['Source', 'Print'],
    		['Bold', 'Italic', 'Underline', 'Strike'],
    		['NumberedList', 'BulletedList', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'],
    		['Link', 'Unlink'],
    		['Font', 'FontSize', '-', 'TextColor', 'BGColor'],
    		['Maximize', 'ShowBlocks'] ];

    config.toolbar_Simple1 = [ 
    				[ 'Bold', 'Italic', 'Underline', 'Strike' ],
					[ 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock' ], 
    				[ 'Styles', 'Format', 'Font', 'FontSize' ], 
    				[ 'TextColor', 'BGColor' ], 
					[ 'Link', 'Unlink' ]
                 ];
};