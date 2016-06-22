JODConverter
============

This is JODConverter 3.0 beta.

JODConverter automates conversions between office document formats
using LibreOffice or Apache OpenOffice. 

See http://jodconverter.googlecode.com for the latest documentation.

JAHIA FORK
==========
This fork contains the following fixes :
- Allow to connect to an external office process on another host
- Solve the problem with connection to LibreOffice 3.5 on Windows. The "basis-link" file does not exist any more in
version 3.5. It is replaced with "ure-link". Correctly detect the URE path location on Windows in case of LibreOffice 3.5.
- Address JODConverter issue (http://code.google.com/p/jodconverter/issues/detail?id=102 )
with HTML to PDF/ODT/Text conversion: use proper export filters in case of the HTML input document.
- Added support for killing existing office processes on Windows ( QA-3698 , DOCSPACE-300 ).
- Jahia 5 patch force restart of Open/Libre Office if thread can't take anymore tasks

Licensing
---------

JODConverter is open source software, you can redistribute it and/or
modify it under either (at your option) of the following licenses

1. The GNU Lesser General Public License v3 (or later)
   -> see LICENSE-LGPL.txt
2. The Apache License, Version 2.0
   -> see LICENSE-Apache.txt
