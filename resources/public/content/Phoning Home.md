Smeagol currently requests the WEFT logo in the page footer from my home site. This is mainly so I can get a feel for how many people are using the product. If you object to this, edit the file

    resources/templates/base.html

and replace the line

    <img height="16" width="16" alt="The Web Engineering Factory &amp; Toolworks" src="http://www.weft.scot/images/weft.logo.64.png"> Developed by <a href="http://www.weft.scot/">WEFT</a>

with the line

    <img height="16" width="16" alt="The Web Engineering Factory &amp; Toolworks" src="img/weft.logo.64.png"> Developed by <a href="http://www.weft.scot/">WEFT</a>
