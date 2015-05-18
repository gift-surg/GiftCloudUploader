package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import java.util.concurrent.Callable;

public interface CallableWithParameter<S, T> extends Callable<S> {
    T getParameter();
}
