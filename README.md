dbdeploy with checksum validation
=================================

This fork of [Graham Tackley's][tackley] database migration project extends dbdeploy with checksum comparison of allready applied scripts.

Checksum validation
-------------------

For each script in the script directory that is applied, a sha256 checksum is additionally stored to the changelog table.
For all scripts in the script directory, that are allready applied to the databases, the changelog checksum is compared to the current checksum of the script file. If the checksums differ, an exception is thrown. 

We added this feature because our DB developers are currently so much used to changing database scripts instead of adding another file with incremental changes, that we wanted to have a mechanism im place to issue a warning if that happend. Mainly to ensure that every change we will push to productions is consistently applied to dev and staging, too.


Contributors
------------

The initial checksum implementation was done by Michael Gruber from Immobilien Scout GmbH
 

References 
----------

[tackley]: https://github.com/tackley/dbdeploy 


