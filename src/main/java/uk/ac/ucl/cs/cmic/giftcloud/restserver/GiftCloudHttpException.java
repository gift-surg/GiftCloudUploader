/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.io.IOException;

public class GiftCloudHttpException extends IOException {
	private static final long serialVersionUID = 1L;
	private final int responseCode;
    private final String htmlText;

	public GiftCloudHttpException(final int responseCode, final String responseMessage, final String text, final String htmlText) {
		super(buildMessage(responseCode, responseMessage, text));
		this.responseCode = responseCode;
        this.htmlText = htmlText;
	}
	
	public int getResponseCode() { return responseCode; }

    public String getHtmlText() {
        return htmlText;
    }
	
	private static String buildMessage(final int code, final String message, final String text) {
		final StringBuilder sb = new StringBuilder("HTTP status ");
		sb.append(code);
		if (null != message && ! "".equals(message)) {
			sb.append("(").append(message).append(")");
		}
		if (null != text && ! "".equals(text)) {
			sb.append(": ").append(text);
		}
		return sb.toString();
	}
}
