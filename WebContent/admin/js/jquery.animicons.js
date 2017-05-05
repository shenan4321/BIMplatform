
$(document).ready(function(){
  
    var interval = null;
    $.each( $('[data-start]'), function(idx, obj){
      
        var $obj      = $(obj);
        var start     = $obj.attr('data-start');
        var frames    = $obj.attr('data-frames')
        interval      = $obj.attr('data-interval') || 500;
        
        $obj.attr('data-icon', $('<div/>').html("&#x"+ start + ";").text() )
        $obj.attr('data-index', 0);
            
        setInterval( function(){
          var index = parseInt($obj.attr('data-index')) + 1;   
          $obj.attr('data-index', index);
        
          if (index === parseInt(frames) ){
            $obj.attr('data-index', 0);
            index = 0
          }
          else{
            $obj.attr('data-index',   index )         
          }
          
          var val = (parseInt(start, 16) + index).toString(16);
          var enc = $('<div/>').html("&#x"+ val + ";").text();             
          $obj.attr('data-icon', enc);
         }
      , parseInt(interval) );
    });
    
});