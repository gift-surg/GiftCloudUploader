/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.nrg.dcm.edit.ScriptEvaluationException;

import java.io.IOException;
import java.io.InputStream;


class HttpApplicatorResponseProcessor<ApplicatorT> extends HttpResponseProcessor<ApplicatorT> {

    private ScriptApplicatorFactory<ApplicatorT> factory;

    HttpApplicatorResponseProcessor(final ScriptApplicatorFactory<ApplicatorT> factory) {
        this.factory = factory;
    }

    protected ApplicatorT streamFromConnection(final InputStream inputStream) throws IOException {
        try {
            return factory.createScriptApplicator(inputStream);
        } catch (ScriptEvaluationException e) {
            throw new IOException("The following error occurred when attempting to process the DICOM anonymisation script: " + e.getCause().getLocalizedMessage(), e.getCause());
        } catch (org.nrg.ecat.edit.ScriptEvaluationException e) {
            throw new IOException("The following error occurred when attempting to process the ECAT anonymisation script: " + e.getCause().getLocalizedMessage(), e.getCause());
        }
    }
}
