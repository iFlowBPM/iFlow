  (function( $jQuery ) {
  
        $(document).ready(function(){
          $('.combobox').combobox()
        });

  
  return;
  
  
    $jQuery.widget( "custom.combobox", {
      _create: function() {
        this.wrapper = $jQuery( "<span>" )
          .addClass( "custom-combobox" )
          .addClass( "combobox" )
          .insertAfter( this.element );
        this.element.hide();
        this._createAutocomplete();
        this._createShowAllButton();
      },
 
      _createAutocomplete: function() {
        var selected = this.element.children( ":selected" ),
          value = selected.val() ? selected.text() : "";
        var selectedName = this.element.attr('name');
        this.input = $jQuery( "<input>" )
          .appendTo( this.wrapper )
          .val( value )
          .attr( "title", "" )
          .attr( "name", selectedName )
          .attr('pattern',selected.context.value)
          
          .keypress(function( event ) {
            if ( event.which == 13 ) {
               event.preventDefault();
               $jQuery(this).val($jQuery(this).val());
               $jQuery(this).attr('value',$jQuery(this).attr('pattern'));
               ajaxFormRefresh(this);
               //alert('1111');
            }
          })
          /*
          .on('focus',    $jQuery.proxy(this.focus, this))
          .on('blur',     $jQuery.proxy(this.blur, this))
          .on('keypress', $jQuery.proxy(this.keypress, this))
          .on('keyup',    $jQuery.proxy(this.keyup, this))
          */
          .change(function() {
            //alert('aa1');
            $jQuery(this).val($jQuery(this).val());
            $jQuery(this).attr('value',$jQuery(this).attr('pattern'));
            //alert('aaaaaccc');
            ajaxFormRefresh(this);
          })
          
          .addClass( "custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left" )
          .addClass( "combobox" )
          .autocomplete({
            delay: 0,
            minLength: 0,
            source: $jQuery.proxy( this, "_source" )
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
 
        $jQuery( "<a>" )
          .attr( "tabIndex", -1 )
          .attr( "title", "Mostrar todos os items" )
          .tooltip()
          .appendTo( this.wrapper )
          .button({
            icons: {
              primary: "ui-icon-triangle-1-s"
            },
            text: false
          })
          .removeClass( "ui-corner-all" )
          .addClass( "custom-combobox-toggle ui-corner-right form-input" )
		  .css( 'width', '200px')
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
        var matcher = new RegExp( $jQuery.ui.autocomplete.escapeRegex(request.term), "i" );
        response( this.element.children( "option" ).map(function() {
          var text = $jQuery( this ).text();
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
          if ( $jQuery( this ).text().toLowerCase() === valueLowerCase ) {
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
          .attr( "title", value + " não coincide com nenhum item" )
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
 
  var $jQuery = jQuery.noConflict();
  $jQuery(function() {
    var $jQuery = jQuery.noConflict();
    $jQuery('.combobox').combobox();
  });
  
  
  /*
  //var $jQuery = jQuery.noConflict();
  $(document).ready(function(){
    //var $jQuery = jQuery.noConflict();
    $('.combobox').combobox();
  });
*/