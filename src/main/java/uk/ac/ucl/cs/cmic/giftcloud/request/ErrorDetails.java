/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.request;

class ErrorDetails {
    private final String title;
    private final String htmlText;
    private Status status;

    enum Status {
        OK,
        ERROR,
        WARNING
    }


    private ErrorDetails(final String title, final String htmlText, final Status status) {
        this.title = title;
        this.htmlText = htmlText;
        this.status = status;
    }

    String getTitle() {
        return title;
    }

    String getHtmlText() {
        return htmlText;
    }

    static ErrorDetails error(final String title, final String htmlText) {
        return new ErrorDetails(title, htmlText, Status.ERROR);
    }

    static ErrorDetails ok(final String title, final String htmlText) {
        return new ErrorDetails(title, htmlText, Status.OK);
    }

    static ErrorDetails warning(final String title, final String htmlText) {
        return new ErrorDetails(title, htmlText, Status.WARNING);
    }
}
