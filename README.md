GIFT-Cloud Uploader
-------------------

GIFT-Cloud is a secure data storage and collaboration platform for medical imaging research.

GiftCloudUploader is a Java Web Start application used to anonymise and upload data to a GIFT-Cloud Server.

GiftCloudUploader can be run as a standalone application on any computer. It can be used to upload data directly, and it can act as a Gateway Server. A Gateway server can receive data pushed from a Dicom device and automatically anonymise and upload the data to a GIFT-Cloud server, making it suitable for integrating a PACS with a GIFT-Cloud research server.
 
Authors: Tom Doel and Dzhoshkun Shakir, [Translational Imaging Group][tig], [Centre for Medical Image Computing][cmic], [University College London (UCL)][ucl].
GIFT-Cloud was developed as part of the [GIFT-Surg][giftsurg] project. 
If you use this software, please cite [this paper][citation]. 

GIFT-Cloud Server extends the [XNAT][xnat] system developed by Harvard University / Howard Hughes Medical Institute (HHMI) / Washington University.



Disclaimer
----------

 * GIFT-Cloud and XNAT are not certified for clinical use.


Software links
--------------

 - [GitHub mirror][githubhome].
 - [XNAT][xnat]
 - [XNAT 1.6 documentation][xnatdocumentation]
 - [XNAT 1.6 installation instructions][xnatinstall]
 - [XNAT 1.6 prerequisites][xnatprerequisites]
 - [XNAT working with modules][xnatmodule]
 - [XNAT discussion group][xnatdiscussion]



License
-------

Copyright (c) 2014-2016, [University College London][ucl].

GIFT-Cloud is available as free open-source software under a BSD 3-Clause License.

Parts of GIFT-Cloud derive from XNAT
 - [XNAT][xnat] (Harvard University / Howard Hughes Medical Institute (HHMI) / Washington University) uses the BSD 2-Clause License

Parts of GIFT-Cloud derive from DicomCleaner
 - [DicomCleaner][dicomcleaner] (Copyright (c) 2001-2014, David A. Clunie DBA PixelMed Publishing.) uses the BSD 3-Clause License




System requirements
-------------------

GIFT-Cloud Uploader connects to a GIFT-Cloud Server, or XNAT server running XNAT 1.6 or later. Please see [XNAT 1.6 prerequisites][xnatprerequisites].

GIFT-Cloud Uploader can be installed on Linux, macOS or Windows.

GIFT-Cloud Uploader requires:
 * Oracle Java SDK 1.7 or later
 * Maven


Compilation and deployment
-----------

 * GIFT-Cloud Uploader can only be compiled using Maven
 * If you wish to customise and compile GIFT-Cloud Uploader, you will need to create a code signing key to sign the application (Java Web Start cannot run unsigned applications)



Issues
------

If you experience issues installing or operating the GIFT-Cloud Server, please consult the XNAT documentation
 * Check the [XNAT documentation][xnatdocumentation];
 * Search the [XNAT discussion group][xnatdiscussion] as your issue will often have been reported by someone else;
 * If you can't find a solution, post a new message in the [XNAT discussion group][xnatdiscussion] and explain that you are installing a customised version of XNAT 1.6.

If you specifically have an issue with GIFT-Cloud Uploader
 * Please check you have the correct version of Java installed
 * Please check your firewall is configured to allow connection to your GIFT-Cloud Server


Funding
-------

GIFT-Cloud is part of GIFT-Surg. [GIFT-Surg][[giftsurg] was supported through an Innovative Engineering for Health award by the [Wellcome Trust][wellcometrust] [WT101957], the [Engineering and Physical Sciences Research Council (EPSRC)][epsrc] [NS/A000027/1] and a [National Institute for Health Research][nihr] Biomedical Research Centre [UCLH][uclh]/UCL High Impact Initiative.


[tig]: http://cmictig.cs.ucl.ac.uk
[giftsurg]: http://www.gift-surg.ac.uk
[cmic]: http://cmic.cs.ucl.ac.uk
[ucl]: http://www.ucl.ac.uk

[wellcometrust]: http://www.wellcome.ac.uk
[epsrc]: http://www.epsrc.ac.uk
[nihr]: http://www.nihr.ac.uk/research
[uclh]: http://www.uclh.nhs.uk

[citation]: http://www.gift-surg.ac.uk/media-engagement/academic-journals/
[githubhome]: https://github.com/gift-surg/GiftCloudServerBuilder

[xnat]: https://www.xnat.org
[xnatinstall]: https://wiki.xnat.org/display/XNAT16/XNAT+1.6+Installation+Guide
[xnatprerequisites]: https://wiki.xnat.org/display/XNAT16/Prerequisites
[xnatbuilder]: https://bitbucket.org/nrg/xnat_builder_1_6dev
[xnatmodule]: https://wiki.xnat.org/display/XNAT16/Developing+Modules
[xnatdocumentation]: https://wiki.xnat.org/display/XNAT16/Home
[xnatdiscussion]: http://groups.google.com/group/xnat_discussion



Copyright and licensing information for GIFT-Cloud and GIFT-Cloud Uploader
-------


GIFT-Cloud incorporates code from XNAT. Please see the file XNAT_license.txt for copyright and licensing information relating to original XNAT code.

The code modifications and new code developed at UCL are covered by the following copyright and licensing information:


Copyright (c) 2014-6, University College London
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of GIFT-Cloud nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.




Copyright and licensing information for XNAT
-------

XNAT 
http://www.xnat.org
Copyright (c) 2014, Washington University School of Medicine
All Rights Reserved
Released under the Simplified BSD.

Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) / Washington University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the names of Washington University, Harvard University and HHMI nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Acknowledgements

XNAT is developed by Randy Buckner at Harvard University and the Neuroinformatics Research Group at Washington University School of Medicine with contributions from developers around the world. We request that you acknowledge the support of XNAT in publications that utilize the software such as by "This product includes XNAT, developed by Randy Buckner at Harvard University and the Neuroinformatics Research Group at Washington University School of Medicine" and/or by citing:

Marcus, D.S., Olsen T., Ramaratnam M., and Buckner, R.L. (2007) The Extensible Neuroimaging Archive Toolkit (XNAT): An informatics platform for managing, exploring, and sharing neuroimaging data. Neuroinformatics 5(1): 11-34.



Copyright and licensing information for DicomCleaner
-------

Copyright (c) 2001-2014, David A. Clunie DBA PixelMed Publishing. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are
permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimers.

2. Redistributions in binary form must reproduce the above copyright notice, this list of
   conditions and the following disclaimers in the documentation and/or other materials
   provided with the distribution.

3. Neither the name of PixelMed Publishing nor the names of its contributors may
   be used to endorse or promote products derived from this software.

This software is provided by the copyright holders and contributors "as is" and any
express or implied warranties, including, but not limited to, the implied warranties
of merchantability and fitness for a particular purpose are disclaimed. In no event
shall the copyright owner or contributors be liable for any direct, indirect, incidental,
special, exemplary, or consequential damages (including, but not limited to, procurement
of substitute goods or services; loss of use, data or profits; or business interruption)
however caused and on any theory of liability, whether in contract, strict liability, or
tort (including negligence or otherwise) arising in any way out of the use of this software,
even if advised of the possibility of such damage.

This software has neither been tested nor approved for clinical use or for incorporation in
a medical device. It is the redistributor's or user's responsibility to comply with any
applicable local, state, national or international regulations.




