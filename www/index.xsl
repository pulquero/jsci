<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" doctype-public="-//W3C//DTD XHTML 1.1//EN"
                             doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"/>
   <xsl:param name="myEmail">mark.hale@physics.org</xsl:param>
   <xsl:param name="myHomePage">http://www.maths.qmul.ac.uk/~mh/</xsl:param>

   <xsl:template match="package">
      <xsl:text>version </xsl:text><xsl:value-of select="version"/>
      <xsl:text>, </xsl:text><xsl:value-of select="date"/>
      <xsl:text>, </xsl:text><xsl:value-of select="file-size"/><xsl:value-of select="file-size/@units"/>
      <xsl:text>, </xsl:text><xsl:value-of select="notes"/>
   </xsl:template>

   <xsl:template match="packages">
<html xml:lang="en">
<head>
<meta name="author" content="Mark Hale"/>
<meta name="description" content="JSci home page"/>
<meta name="keywords" content="Java, science"/>
<style type="text/css">
h1 {text-align: center}
div.right {text-align: right}
img#sflogo {border: 0}
p#trademarks {font-style: bold; font-variant: small-caps}
</style>
<title>JSci - A science API for Java</title>
</head>

<body>
<div class="right">
<a href="http://sourceforge.net/">
<img id="sflogo" src="http://sourceforge.net/sflogo.php?group_id=47741&amp;type=2"
width="125" height="37" alt="SourceForge Logo"/>
</a>
</div>
<h1>JSci - A science API for Java<sup><small>TM</small></sup></h1>
<hr/>
<p>
This is a <b>free</b> set of Java<sup><small>TM</small></sup> packages.
The aim is to encapsulate scientific methods/principles in the most natural way possible.
As such they should greatly aid the development of scientific based software.
This software is supplied "as is" and that there are no guarantees.
</p>
<p>This is a SourceForge <a href="http://sourceforge.net/projects/jsci">project</a>.</p>
<p>
This work is open to suggestions and <a href="contributors.html">contributions</a>.
If you like, you can be notified of future updates either via
<a href="mailto:{$myEmail}">email</a> or the JSci
<a href="http://www.egroups.com/list/jsci/">e-group</a>.
</p>
<h3>Features</h3>
<ul>
<li>Abstract math interfaces</li>
<li>Linear algebra (support for various matrix and vector types)</li>
<li>Statistics (including probability distributions)</li>
<li>Wavelets</li>
<li>Newtonian mechanics</li>
<li>Chart/graph components (AWT and Swing)</li>
<li>MathML DOM implementation</li>
<li>Lots more...</li>
</ul>
<h3>License</h3>
<ul>
<li><a href="LICENSE">Lesser General Public License (LGPL)</a></li>
</ul>
<h3>Downloads</h3>
<ul>
<li><a href="http://prdownloads.sourceforge.net/jsci/JSci.zip?download">Current version</a>
(<xsl:apply-templates select="package[file-name='JSci.zip']"/>)</li>
<li><a href="http://prdownloads.sourceforge.net/jsci/JSci118.zip?download">Last release for JDK 1.1.8</a>
(version 0.90, 04/10/03 19:23, 2.5MB, JDK 1.1.8)</li>
<li><a href="http://prdownloads.sourceforge.net/jsci/LPSolve.zip?download">LPSolve package</a></li>
<li><a href="http://prdownloads.sourceforge.net/jsci/mathmldom.jar?download">MathML DOM classes</a></li>
</ul>
<h3>Installation</h3>
<ol>
<li>Download.</li>
<li>Unzip JSci.zip.</li>
<li>Browse index.html for further instructions.</li>
</ol>
<p>Note: <a href="http://xml.apache.org">xerces</a> is required for some classes.</p>
<h3>Online Documentation</h3>
<ul>
<li><a href="api/index.html">API (latest)</a></li>
<li><a href="api-0.94/index.html">API (v0.94)</a></li>
</ul>
<h3>Online Examples</h3>
<p>
My <a href="examples/index.html">chaos applets</a> are now online.
</p>
<h3>In the Works</h3>
<p>
Improve documentation.
Eigenvalue methods for more general matrices.
</p>
<p>Back to my <a href="{$myHomePage}">home page</a>.</p>
<hr/>
<p id="trademarks">
<b><small>
Java and all Java-based trademarks and logos are trademarks or registered trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
</small></b>
</p>
</body>
</html>
   </xsl:template>
</xsl:stylesheet>
