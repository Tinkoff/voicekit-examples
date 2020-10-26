package VoiceKit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import org.apache.commons.codec.binary.Base64;

import java.util.Date;
import java.util.concurrent.Executor;

public class Auth extends CallCredentials {
    private static final long EXPIRATION = 5*60*1000;
    String _apiKey;
    String _secretKey;
    String _endpoint;

    public Auth(String apiKey, String secretKey, String endpoint) {
        _apiKey = apiKey;
        _secretKey = secretKey;
        _endpoint = endpoint;
    }

    String createToken() {
        return JWT.create()
                .withKeyId(_apiKey)
                .withClaim("aud", _endpoint)
                .withExpiresAt(new Date(System.currentTimeMillis()+EXPIRATION))
                .sign(Algorithm.HMAC256(Base64.decodeBase64(_secretKey)));
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier applier) {
        executor.execute(() -> {
        try {
            Metadata headers = new Metadata();
            Metadata.Key<String> jwtKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
            String jwt = createToken();
            headers.put(jwtKey, String.join(" ", "Bearer", jwt));
            applier.apply(headers);
        } catch (Throwable e) {
            applier.fail(Status.UNAUTHENTICATED.withCause(e));
        }
    });
    }

    @Override
    public void thisUsesUnstableApi() {
    }
}
