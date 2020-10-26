package VoiceKit.ResponseHandlers;

import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public abstract class BaseHandler<T> implements StreamObserver<T> {
    CountDownLatch _latch = new CountDownLatch(1);

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        _latch.countDown();
    }

    @Override
    public void onCompleted() {
        _latch.countDown();
    }

    public void waitOnComplete() throws InterruptedException {
        _latch.await();
    }
}
