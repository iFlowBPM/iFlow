    function initProcFrame() {
        var h1, div;
		if (document.getElementById('dados'))
			h1 = document.getElementById('dados').offsetHeight;
		else
			h1 = 0;
		if (h1 > 0) {
			div = window.parent.document.getElementById('section3_content_div');
			if(div) div.style.height = (h1 + 40)+ 'px';
		}
	}
