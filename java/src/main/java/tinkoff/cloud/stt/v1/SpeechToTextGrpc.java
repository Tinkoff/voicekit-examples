package tinkoff.cloud.stt.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Speech recognition.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.32.1)",
    comments = "Source: tinkoff/cloud/stt/v1/stt.proto")
public final class SpeechToTextGrpc {

  private SpeechToTextGrpc() {}

  public static final String SERVICE_NAME = "tinkoff.cloud.stt.v1.SpeechToText";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<tinkoff.cloud.stt.v1.Stt.RecognizeRequest,
      tinkoff.cloud.stt.v1.Stt.RecognizeResponse> getRecognizeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Recognize",
      requestType = tinkoff.cloud.stt.v1.Stt.RecognizeRequest.class,
      responseType = tinkoff.cloud.stt.v1.Stt.RecognizeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<tinkoff.cloud.stt.v1.Stt.RecognizeRequest,
      tinkoff.cloud.stt.v1.Stt.RecognizeResponse> getRecognizeMethod() {
    io.grpc.MethodDescriptor<tinkoff.cloud.stt.v1.Stt.RecognizeRequest, tinkoff.cloud.stt.v1.Stt.RecognizeResponse> getRecognizeMethod;
    if ((getRecognizeMethod = SpeechToTextGrpc.getRecognizeMethod) == null) {
      synchronized (SpeechToTextGrpc.class) {
        if ((getRecognizeMethod = SpeechToTextGrpc.getRecognizeMethod) == null) {
          SpeechToTextGrpc.getRecognizeMethod = getRecognizeMethod =
              io.grpc.MethodDescriptor.<tinkoff.cloud.stt.v1.Stt.RecognizeRequest, tinkoff.cloud.stt.v1.Stt.RecognizeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Recognize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.stt.v1.Stt.RecognizeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.stt.v1.Stt.RecognizeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SpeechToTextMethodDescriptorSupplier("Recognize"))
              .build();
        }
      }
    }
    return getRecognizeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest,
      tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse> getStreamingRecognizeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamingRecognize",
      requestType = tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest.class,
      responseType = tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest,
      tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse> getStreamingRecognizeMethod() {
    io.grpc.MethodDescriptor<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest, tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse> getStreamingRecognizeMethod;
    if ((getStreamingRecognizeMethod = SpeechToTextGrpc.getStreamingRecognizeMethod) == null) {
      synchronized (SpeechToTextGrpc.class) {
        if ((getStreamingRecognizeMethod = SpeechToTextGrpc.getStreamingRecognizeMethod) == null) {
          SpeechToTextGrpc.getStreamingRecognizeMethod = getStreamingRecognizeMethod =
              io.grpc.MethodDescriptor.<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest, tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamingRecognize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new SpeechToTextMethodDescriptorSupplier("StreamingRecognize"))
              .build();
        }
      }
    }
    return getStreamingRecognizeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static SpeechToTextStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SpeechToTextStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SpeechToTextStub>() {
        @java.lang.Override
        public SpeechToTextStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SpeechToTextStub(channel, callOptions);
        }
      };
    return SpeechToTextStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static SpeechToTextBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SpeechToTextBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SpeechToTextBlockingStub>() {
        @java.lang.Override
        public SpeechToTextBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SpeechToTextBlockingStub(channel, callOptions);
        }
      };
    return SpeechToTextBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static SpeechToTextFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<SpeechToTextFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<SpeechToTextFutureStub>() {
        @java.lang.Override
        public SpeechToTextFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new SpeechToTextFutureStub(channel, callOptions);
        }
      };
    return SpeechToTextFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Speech recognition.
   * </pre>
   */
  public static abstract class SpeechToTextImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Method to recognize whole audio at once: sending complete audio, getting complete recognition result.
     * </pre>
     */
    public void recognize(tinkoff.cloud.stt.v1.Stt.RecognizeRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.RecognizeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRecognizeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Method for streaming recognition.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest> streamingRecognize(
        io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getStreamingRecognizeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRecognizeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                tinkoff.cloud.stt.v1.Stt.RecognizeRequest,
                tinkoff.cloud.stt.v1.Stt.RecognizeResponse>(
                  this, METHODID_RECOGNIZE)))
          .addMethod(
            getStreamingRecognizeMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest,
                tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse>(
                  this, METHODID_STREAMING_RECOGNIZE)))
          .build();
    }
  }

  /**
   * <pre>
   * Speech recognition.
   * </pre>
   */
  public static final class SpeechToTextStub extends io.grpc.stub.AbstractAsyncStub<SpeechToTextStub> {
    private SpeechToTextStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SpeechToTextStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SpeechToTextStub(channel, callOptions);
    }

    /**
     * <pre>
     * Method to recognize whole audio at once: sending complete audio, getting complete recognition result.
     * </pre>
     */
    public void recognize(tinkoff.cloud.stt.v1.Stt.RecognizeRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.RecognizeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRecognizeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Method for streaming recognition.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeRequest> streamingRecognize(
        io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getStreamingRecognizeMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * Speech recognition.
   * </pre>
   */
  public static final class SpeechToTextBlockingStub extends io.grpc.stub.AbstractBlockingStub<SpeechToTextBlockingStub> {
    private SpeechToTextBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SpeechToTextBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SpeechToTextBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Method to recognize whole audio at once: sending complete audio, getting complete recognition result.
     * </pre>
     */
    public tinkoff.cloud.stt.v1.Stt.RecognizeResponse recognize(tinkoff.cloud.stt.v1.Stt.RecognizeRequest request) {
      return blockingUnaryCall(
          getChannel(), getRecognizeMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Speech recognition.
   * </pre>
   */
  public static final class SpeechToTextFutureStub extends io.grpc.stub.AbstractFutureStub<SpeechToTextFutureStub> {
    private SpeechToTextFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected SpeechToTextFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new SpeechToTextFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Method to recognize whole audio at once: sending complete audio, getting complete recognition result.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<tinkoff.cloud.stt.v1.Stt.RecognizeResponse> recognize(
        tinkoff.cloud.stt.v1.Stt.RecognizeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRecognizeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_RECOGNIZE = 0;
  private static final int METHODID_STREAMING_RECOGNIZE = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final SpeechToTextImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(SpeechToTextImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RECOGNIZE:
          serviceImpl.recognize((tinkoff.cloud.stt.v1.Stt.RecognizeRequest) request,
              (io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.RecognizeResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_STREAMING_RECOGNIZE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamingRecognize(
              (io.grpc.stub.StreamObserver<tinkoff.cloud.stt.v1.Stt.StreamingRecognizeResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class SpeechToTextBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    SpeechToTextBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return tinkoff.cloud.stt.v1.Stt.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("SpeechToText");
    }
  }

  private static final class SpeechToTextFileDescriptorSupplier
      extends SpeechToTextBaseDescriptorSupplier {
    SpeechToTextFileDescriptorSupplier() {}
  }

  private static final class SpeechToTextMethodDescriptorSupplier
      extends SpeechToTextBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    SpeechToTextMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (SpeechToTextGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new SpeechToTextFileDescriptorSupplier())
              .addMethod(getRecognizeMethod())
              .addMethod(getStreamingRecognizeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
