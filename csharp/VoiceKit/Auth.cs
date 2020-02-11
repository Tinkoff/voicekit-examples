using System;
using System.Security.Cryptography;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

namespace Tinkoff.VoiceKit
{
    public class Auth
    {
        string _apiKey;
        string _secretKey;
        string _endpoint;
        DateTimeOffset _expTime;
        string _jwt;
        Dictionary<string, string> _header;
        Dictionary<string, object> _payload;

        public string GetToken
        {
            get
            {
                if (_expTime == null || _expTime < DateTimeOffset.UtcNow)
                {
                    _expTime = DateTimeOffset.UtcNow.AddMinutes(5);
                    _payload["exp"] = _expTime.ToUnixTimeSeconds();
                    CreateJWT();
                }
                return _jwt;
            }
        }

        public Auth(string apiKey, string secretKey, string endpoint)
        {
            _apiKey = apiKey;
            _secretKey = secretKey;
            _expTime = DateTimeOffset.UtcNow.AddMinutes(5);
            _endpoint = endpoint;

            _payload = new Dictionary<string, object>
            {
                { "aud", _endpoint },
                { "exp", _expTime.ToUnixTimeSeconds() }
            };
            _header = new Dictionary<string, string>
            {
                { "alg", "HS256" },
                { "typ", "JWT" },
                { "kid", _apiKey }
            };

            CreateJWT();
        }

        private static string UrlSafeEncode(string data)
        {
            byte[] dataBytes = Encoding.UTF8.GetBytes(data);
            string dataBase64 = Convert.ToBase64String(dataBytes);
            string dataUrlSaveBase64 = dataBase64.TrimEnd('=')
            .Replace('+', '-')
            .Replace('/', '_');

            return dataUrlSaveBase64;
        }

        private static byte[] UrlSafeDecode(string data)
        {
            string incoming = data.Replace('_', '/').Replace('-', '+');
            switch (data.Length % 4)
            {
                case 1: incoming += "==="; break;
                case 2: incoming += "=="; break;
                case 3: incoming += "="; break;
            }
            return Convert.FromBase64String(incoming);
        }

        private void CreateJWT()
        {
            string payloadJson = JsonConvert.SerializeObject(_payload, Formatting.Indented);
            string headerJson = JsonConvert.SerializeObject(_header, Formatting.Indented);

            string payloadUrlSaveBase64 = UrlSafeEncode(payloadJson);
            string headerUrlSaveBase64 = UrlSafeEncode(headerJson);
            string data = string.Concat(headerUrlSaveBase64, ".", payloadUrlSaveBase64);
            byte[] secretBytes = UrlSafeDecode(_secretKey);

            using (var hmac = new HMACSHA256(secretBytes))
            {
                var signatureBytes = hmac.ComputeHash(Encoding.UTF8.GetBytes(data));
                var signature = Convert.ToBase64String(signatureBytes).
                Replace('+', '-').
                Replace('/', '_');
                _jwt = string.Concat(data, ".", signature);
            }
        }
    }
}
