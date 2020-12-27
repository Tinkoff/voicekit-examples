package voicekit.response;

import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseHandler<T> implements StreamObserver<T> {
    private static final Logger logger = Logger.getLogger(BaseHandler.class.getName());

    CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void onError(Throwable t) {
        logger.log(Level.SEVERE, "Error in grpc response handler", t);
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        latch.countDown();
    }

    public void waitOnComplete() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Current thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
