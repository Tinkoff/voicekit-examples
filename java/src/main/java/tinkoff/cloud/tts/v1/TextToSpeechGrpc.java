package tinkoff.cloud.tts.v1;

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
 * Speech synthesis.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.32.1)",
    comments = "Source: tinkoff/cloud/tts/v1/tts.proto")
public final class TextToSpeechGrpc {

  private TextToSpeechGrpc() {}

  public static final String SERVICE_NAME = "tinkoff.cloud.tts.v1.TextToSpeech";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.ListVoicesRequest,
      tinkoff.cloud.tts.v1.Tts.ListVoicesResponses> getListVoicesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListVoices",
      requestType = tinkoff.cloud.tts.v1.Tts.ListVoicesRequest.class,
      responseType = tinkoff.cloud.tts.v1.Tts.ListVoicesResponses.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.ListVoicesRequest,
      tinkoff.cloud.tts.v1.Tts.ListVoicesResponses> getListVoicesMethod() {
    io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.ListVoicesRequest, tinkoff.cloud.tts.v1.Tts.ListVoicesResponses> getListVoicesMethod;
    if ((getListVoicesMethod = TextToSpeechGrpc.getListVoicesMethod) == null) {
      synchronized (TextToSpeechGrpc.class) {
        if ((getListVoicesMethod = TextToSpeechGrpc.getListVoicesMethod) == null) {
          TextToSpeechGrpc.getListVoicesMethod = getListVoicesMethod =
              io.grpc.MethodDescriptor.<tinkoff.cloud.tts.v1.Tts.ListVoicesRequest, tinkoff.cloud.tts.v1.Tts.ListVoicesResponses>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListVoices"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.tts.v1.Tts.ListVoicesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.tts.v1.Tts.ListVoicesResponses.getDefaultInstance()))
              .setSchemaDescriptor(new TextToSpeechMethodDescriptorSupplier("ListVoices"))
              .build();
        }
      }
    }
    return getListVoicesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest,
      tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse> getSynthesizeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Synthesize",
      requestType = tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest.class,
      responseType = tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest,
      tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse> getSynthesizeMethod() {
    io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest, tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse> getSynthesizeMethod;
    if ((getSynthesizeMethod = TextToSpeechGrpc.getSynthesizeMethod) == null) {
      synchronized (TextToSpeechGrpc.class) {
        if ((getSynthesizeMethod = TextToSpeechGrpc.getSynthesizeMethod) == null) {
          TextToSpeechGrpc.getSynthesizeMethod = getSynthesizeMethod =
              io.grpc.MethodDescriptor.<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest, tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Synthesize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TextToSpeechMethodDescriptorSupplier("Synthesize"))
              .build();
        }
      }
    }
    return getSynthesizeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest,
      tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse> getStreamingSynthesizeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StreamingSynthesize",
      requestType = tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest.class,
      responseType = tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest,
      tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse> getStreamingSynthesizeMethod() {
    io.grpc.MethodDescriptor<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest, tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse> getStreamingSynthesizeMethod;
    if ((getStreamingSynthesizeMethod = TextToSpeechGrpc.getStreamingSynthesizeMethod) == null) {
      synchronized (TextToSpeechGrpc.class) {
        if ((getStreamingSynthesizeMethod = TextToSpeechGrpc.getStreamingSynthesizeMethod) == null) {
          TextToSpeechGrpc.getStreamingSynthesizeMethod = getStreamingSynthesizeMethod =
              io.grpc.MethodDescriptor.<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest, tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StreamingSynthesize"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse.getDefaultInstance()))
              .setSchemaDescriptor(new TextToSpeechMethodDescriptorSupplier("StreamingSynthesize"))
              .build();
        }
      }
    }
    return getStreamingSynthesizeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TextToSpeechStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TextToSpeechStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TextToSpeechStub>() {
        @java.lang.Override
        public TextToSpeechStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TextToSpeechStub(channel, callOptions);
        }
      };
    return TextToSpeechStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TextToSpeechBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TextToSpeechBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TextToSpeechBlockingStub>() {
        @java.lang.Override
        public TextToSpeechBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TextToSpeechBlockingStub(channel, callOptions);
        }
      };
    return TextToSpeechBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TextToSpeechFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TextToSpeechFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TextToSpeechFutureStub>() {
        @java.lang.Override
        public TextToSpeechFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TextToSpeechFutureStub(channel, callOptions);
        }
      };
    return TextToSpeechFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Speech synthesis.
   * </pre>
   */
  public static abstract class TextToSpeechImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * (Not implemented) Method for retrieving available voice list.
     * </pre>
     */
    public void listVoices(tinkoff.cloud.tts.v1.Tts.ListVoicesRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.ListVoicesResponses> responseObserver) {
      asyncUnimplementedUnaryCall(getListVoicesMethod(), responseObserver);
    }

    /**
     * <pre>
     * (Not implemented) Method for fragment synthesis.
     * </pre>
     */
    public void synthesize(tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getSynthesizeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Method for streaming synthesis.
     * </pre>
     */
    public void streamingSynthesize(tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getStreamingSynthesizeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getListVoicesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                tinkoff.cloud.tts.v1.Tts.ListVoicesRequest,
                tinkoff.cloud.tts.v1.Tts.ListVoicesResponses>(
                  this, METHODID_LIST_VOICES)))
          .addMethod(
            getSynthesizeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest,
                tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse>(
                  this, METHODID_SYNTHESIZE)))
          .addMethod(
            getStreamingSynthesizeMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest,
                tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse>(
                  this, METHODID_STREAMING_SYNTHESIZE)))
          .build();
    }
  }

  /**
   * <pre>
   * Speech synthesis.
   * </pre>
   */
  public static final class TextToSpeechStub extends io.grpc.stub.AbstractAsyncStub<TextToSpeechStub> {
    private TextToSpeechStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TextToSpeechStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TextToSpeechStub(channel, callOptions);
    }

    /**
     * <pre>
     * (Not implemented) Method for retrieving available voice list.
     * </pre>
     */
    public void listVoices(tinkoff.cloud.tts.v1.Tts.ListVoicesRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.ListVoicesResponses> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getListVoicesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * (Not implemented) Method for fragment synthesis.
     * </pre>
     */
    public void synthesize(tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSynthesizeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Method for streaming synthesis.
     * </pre>
     */
    public void streamingSynthesize(tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest request,
        io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getStreamingSynthesizeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Speech synthesis.
   * </pre>
   */
  public static final class TextToSpeechBlockingStub extends io.grpc.stub.AbstractBlockingStub<TextToSpeechBlockingStub> {
    private TextToSpeechBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TextToSpeechBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TextToSpeechBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * (Not implemented) Method for retrieving available voice list.
     * </pre>
     */
    public tinkoff.cloud.tts.v1.Tts.ListVoicesResponses listVoices(tinkoff.cloud.tts.v1.Tts.ListVoicesRequest request) {
      return blockingUnaryCall(
          getChannel(), getListVoicesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * (Not implemented) Method for fragment synthesis.
     * </pre>
     */
    public tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse synthesize(tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest request) {
      return blockingUnaryCall(
          getChannel(), getSynthesizeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Method for streaming synthesis.
     * </pre>
     */
    public java.util.Iterator<tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse> streamingSynthesize(
        tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getStreamingSynthesizeMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Speech synthesis.
   * </pre>
   */
  public static final class TextToSpeechFutureStub extends io.grpc.stub.AbstractFutureStub<TextToSpeechFutureStub> {
    private TextToSpeechFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TextToSpeechFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TextToSpeechFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * (Not implemented) Method for retrieving available voice list.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<tinkoff.cloud.tts.v1.Tts.ListVoicesResponses> listVoices(
        tinkoff.cloud.tts.v1.Tts.ListVoicesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getListVoicesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * (Not implemented) Method for fragment synthesis.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse> synthesize(
        tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getSynthesizeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_VOICES = 0;
  private static final int METHODID_SYNTHESIZE = 1;
  private static final int METHODID_STREAMING_SYNTHESIZE = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final TextToSpeechImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(TextToSpeechImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_VOICES:
          serviceImpl.listVoices((tinkoff.cloud.tts.v1.Tts.ListVoicesRequest) request,
              (io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.ListVoicesResponses>) responseObserver);
          break;
        case METHODID_SYNTHESIZE:
          serviceImpl.synthesize((tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest) request,
              (io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechResponse>) responseObserver);
          break;
        case METHODID_STREAMING_SYNTHESIZE:
          serviceImpl.streamingSynthesize((tinkoff.cloud.tts.v1.Tts.SynthesizeSpeechRequest) request,
              (io.grpc.stub.StreamObserver<tinkoff.cloud.tts.v1.Tts.StreamingSynthesizeSpeechResponse>) responseObserver);
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
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class TextToSpeechBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TextToSpeechBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return tinkoff.cloud.tts.v1.Tts.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TextToSpeech");
    }
  }

  private static final class TextToSpeechFileDescriptorSupplier
      extends TextToSpeechBaseDescriptorSupplier {
    TextToSpeechFileDescriptorSupplier() {}
  }

  private static final class TextToSpeechMethodDescriptorSupplier
      extends TextToSpeechBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    TextToSpeechMethodDescriptorSupplier(String methodName) {
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
      synchronized (TextToSpeechGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TextToSpeechFileDescriptorSupplier())
              .addMethod(getListVoicesMethod())
              .addMethod(getSynthesizeMethod())
              .addMethod(getStreamingSynthesizeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
