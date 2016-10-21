/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.util.concurrent.Callable;

public interface CallableWithParameter<S, T> extends Callable<S> {
    T getParameter();
}
