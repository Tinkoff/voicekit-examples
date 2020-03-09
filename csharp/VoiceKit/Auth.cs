using System;
using JWT.Builder;
using JWT.Algorithms;

namespace Tinkoff.VoiceKit
{
    public class Auth
    {
        string _apiKey;
        string _secretKey;
        string _endpoint;
        DateTimeOffset _expTime;
        string _jwt;

        public string Token
        {
            get
            {
                if (_expTime == null || _expTime < DateTimeOffset.UtcNow)
                {
                    CreateJWT();
                }
                return _jwt;
            }
        }

        public Auth(string apiKey, string secretKey, string endpoint)
        {
            _apiKey = apiKey;
            _secretKey = secretKey;
            _endpoint = endpoint;

            CreateJWT();
        }

        private void CreateJWT()
        {
            _expTime = DateTimeOffset.UtcNow.AddMinutes(5);

            _jwt = new JwtBuilder()
            .WithAlgorithm(new HMACSHA256Algorithm())
            .WithSecret(Convert.FromBase64String(_secretKey))
            .AddClaim("aud", _endpoint)
            .AddClaim("exp", _expTime.ToUnixTimeSeconds())
            .AddHeader(HeaderName.KeyId, _apiKey)
            .Build();
        }
    }
}
