jQuery(document).ready(function(){
  jQuery('.yui-nav li').click(function () {
    jQuery('.yui-nav li').each(function() {
    	jQuery(this).removeClass('selected');
    })
    jQuery(this).addClass('selected');
        var index = jQuery(".yui-nav li").index(this);
        jQuery('.yui-content').children().each(function() {
            jQuery(this).hide();
    })
    jQuery('.yui-content>#wiki-tab-0-' + index).show();
 });
});