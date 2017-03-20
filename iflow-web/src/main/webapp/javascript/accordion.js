/*var $jQuery = jQuery.noConflict();
$jQuery(function(){
  
  var j = 0;
  
  
  $jQuery('li .subheader').each(function(e){
    var panelId ="panelId"+j++;
    var $that = jQuery(this);
    //$jQuery('<div class="PanelCollapse ui-accordion ui-widget ui-helper-reset" role="tablist" id = "'+panelId+'></div>').insertBefore(this);
    if(this.classList[0]!="subheader"){
      $jQuery('<div id = "'+panelId+'">abc</div>').insertBefore($that);
    }
    
  });
   
  //$jQuery(".fieldlist").prepend('<div id= "aaa">aaa</div>');

  //$jQuery("<div>111</div>").insertBefore('.subheader');
  //$jQuery('<div class="PanelCollapse ui-accordion ui-widget ui-helper-reset" role="tablist" ></div>').insertBefore('.subheader');
  //$jQuery("<div>aaa</div>").insertAfter('.subheader');
  
  //$( ".container" ).append( $( "h2" ) );
  
  //alert("aaa");
  //$jQuery("<div id= 'aaa'>").prepend('aaaaaa');

  
  
  
});*/

var $jQuery = jQuery.noConflict()
$jQuery(function() {
  $jQuery( ".PanelCollapse" ).accordion({
    collapsible:true,
    animate:{easing: "swing"}
  }); 
}); 