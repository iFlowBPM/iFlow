
Spry.Utils.addLoadListener(function() {    


// Accordion Menu slide 1
$(function() {                       
	$( "#Accordion1" ).accordion({ // Accordion template1
		heightStyle:"content",
	   active:0
	}); 
});
$(function() {
	$( "#Accordion2" ).accordion({ // Accordion template1
		heightStyle:"content",
	   active:1
	}); 
});

$(function() {                       
	$( "#Accordion3" ).accordion({ // Accordion template1
		heightStyle:"content",
	   active:2
	}); 
});

/*
// Canvas arrow cores Categorias Menu slide 1
var c=document.getElementById("gray");
var ctx=c.getContext("2d");
ctx.beginPath();
ctx.lineWidth=4;
ctx.strokeStyle="#999999";
ctx.lineJoin="round";
ctx.lineJoin="round";
ctx.moveTo(5,0);
ctx.lineTo(12,15);
ctx.lineTo(5,30);
ctx.stroke();


var c=document.getElementById("orange");
var ctx=c.getContext("2d");
ctx.beginPath();
ctx.lineWidth=4;
ctx.strokeStyle="#ff9966";
ctx.lineJoin="round";
ctx.moveTo(5,0);
ctx.lineTo(12,15);
ctx.lineTo(5,30);
ctx.stroke();

var c=document.getElementById("green");
var ctx=c.getContext("2d");
ctx.beginPath();
ctx.lineWidth=4;
ctx.strokeStyle="#67bf74";
ctx.lineJoin="round";
ctx.moveTo(5,0);
ctx.lineTo(12,15);
ctx.lineTo(5,30);
ctx.stroke();

var c=document.getElementById("purple");
var ctx=c.getContext("2d");
ctx.beginPath();
ctx.lineWidth=4;
ctx.strokeStyle="#ad62a7";
ctx.lineJoin="round";
ctx.moveTo(5,0);
ctx.lineTo(12,15);
ctx.lineTo(5,30);
ctx.stroke();

var c=document.getElementById("blue");
var ctx=c.getContext("2d");
ctx.beginPath();
ctx.lineWidth=4;
ctx.strokeStyle="#3399cc";
ctx.lineJoin="round";
ctx.moveTo(5,0);
ctx.lineTo(12,15);
ctx.lineTo(5,30);
ctx.stroke();
*/


});

 // Submenu  vertical  processos
	$(function() {
	
	    var menu_ul = $('.menu > li > ul'),
	           menu_a  = $('.menu > li > a');
	    
	    menu_ul.hide();
	
	    menu_a.click(function(e) {
	        e.preventDefault();
	        if(!$(this).hasClass('active')) {
	            menu_a.removeClass('active');
	            menu_ul.filter(':visible').slideUp('normal');
	            $(this).addClass('active').next().stop(true,true).slideDown('normal');
	        } else {
	            $(this).removeClass('active');
	            $(this).next().stop(true,true).slideUp('normal');
	        }
	    });
	
	});

 // Panels Collapse
	
$(function() {
	$( ".PanelCollapse" ).accordion({
		collapsible:true,
		animate:{easing: "swing"}
	}); 
});	
	

 // combobox joao
 
  $(function( $j ) {
    $j.widget( "custom.combobox", {
      _create: function() {
        this.wrapper = $j( "<span>" )
          .addClass( "custom-combobox" )
          .insertAfter( this.element );
 
        this.element.hide();
        this._createAutocomplete();
        this._createShowAllButton();
      },
 
      _createAutocomplete: function() {
        var selected = this.element.children( ":selected" ),
          value = selected.val() ? selected.text() : "";
 
        this.input = $j( "<input>" )
          .appendTo( this.wrapper )
          .val( value )
          .attr( "title", "" )
          .addClass( "custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left" )
          .autocomplete({
            delay: 0,
            minLength: 0,
            source: $j.proxy( this, "_source" )
          })
          .tooltip({
            tooltipClass: "ui-state-highlight"
          });
 
        this._on( this.input, {
          autocompleteselect: function( event, ui ) {
            ui.item.option.selected = true;
            this._trigger( "select", event, {
              item: ui.item.option
            });
          },
 
          autocompletechange: "_removeIfInvalid"
        });
      },
 
      _createShowAllButton: function() {
        var input = this.input,
          wasOpen = false;
 
        $j( "<a>" )
          .attr( "tabIndex", -1 )
          .attr( "title", "Show All Items" )
          .tooltip()
          .appendTo( this.wrapper )
          .button({
            icons: {
              primary: "ui-icon-triangle-1-s"
            },
            text: false
          })
          .removeClass( "ui-corner-all" )
          .addClass( "custom-combobox-toggle ui-corner-right" )
          .mousedown(function() {
            wasOpen = input.autocomplete( "widget" ).is( ":visible" );
          })
          .click(function() {
            input.focus();
 
            // Close if already visible
            if ( wasOpen ) {
              return;
            }
 
            // Pass empty string as value to search for, displaying all results
            input.autocomplete( "search", "" );
          });
      },
 
      _source: function( request, response ) {
        var matcher = new RegExp( $j.ui.autocomplete.escapeRegex(request.term), "i" );
        response( this.element.children( "option" ).map(function() {
          var text = $j( this ).text();
          if ( this.value && ( !request.term || matcher.test(text) ) )
            return {
              label: text,
              value: text,
              option: this
            };
        }) );
      },
 
      _removeIfInvalid: function( event, ui ) {
 
        // Selected an item, nothing to do
        if ( ui.item ) {
          return;
        }
 
        // Search for a match (case-insensitive)
        var value = this.input.val(),
          valueLowerCase = value.toLowerCase(),
          valid = false;
        this.element.children( "option" ).each(function() {
          if ( $j( this ).text().toLowerCase() === valueLowerCase ) {
            this.selected = valid = true;
            return false;
          }
        });
 
        // Found a match, nothing to do
        if ( valid ) {
          return;
        }
 
        // Remove invalid value
        this.input
          .val( "" )
          .attr( "title", value + " didn't match any item" )
          .tooltip( "open" );
        this.element.val( "" );
        this._delay(function() {
          this.input.tooltip( "close" ).attr( "title", "" );
        }, 2500 );
        this.input.data( "ui-autocomplete" ).term = "";
      },
 
      _destroy: function() {
        this.wrapper.remove();
        this.element.show();
      }
    });
  })( jQuery );
 
  $(function() {
	var $j = jQuery.noConflict();
    $j( '.combobox' ).combobox();
    $j( "#toggle" ).click(function() {
      $j( "#combobox" ).toggle();
    });
  });
 