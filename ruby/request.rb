$LOAD_PATH.unshift 'protobuf/ruby'
require 'tinkoff/cloud/stt/v1/stt_services_pb'
require 'active_support/core_ext/numeric/time'
require 'jwt'

class BaseRequest
  def initialize api_key:, api_secret:, options: {}
    @api_key = api_key
    @api_secret = api_secret
    @options = options
  end
end

class RecognitionRequest < BaseRequest
  def initialize api_key:, api_secret:, audio_frames:, sample_rate:, num_channels:, format: :json, options: {}
    super api_key: api_key, api_secret: api_secret, options: options

    @audio_frames = audio_frames
    @sample_rate = sample_rate
    @num_channels = num_channels
    @format = format

    @scope = 'tinkoff.cloud.stt'
  end

  def body
    request = Tinkoff::Cloud::Stt::V1::RecognizeRequest.new(
      audio: Tinkoff::Cloud::Stt::V1::RecognitionAudio.new(content: @audio_frames),
      config: Tinkoff::Cloud::Stt::V1::RecognitionConfig.new
    )
    
    request.config.encoding = Tinkoff::Cloud::Stt::V1::AudioEncoding::LINEAR16
    request.config.sample_rate_hertz = @sample_rate
    request.config.num_channels = @num_channels
    request.config.max_alternatives = @options[:max_alternatives] || 2
    request.config.do_not_perform_vad = @options[:do_not_perform_vad] || true
    request.config.language_code = 'ru-RU'
    request.config.enable_automatic_punctuation = @options[:automatic_punctuation] || false
    
    @format == :json ? request.to_json : request
  end

  def headers(expiration_time: 10.minutes)
    now = Time.now.getgm
    payload = {
      iss: 'test_issuer',
      sub: 'test_user',
      aud: @scope,
      exp: (now + expiration_time).to_i,
    }

    { "authorization" => "Bearer #{JWT.encode(payload, Base64.urlsafe_decode64(@api_secret), 'HS256', { kid: @api_key, typ: 'JWT' })}" }
  end

end
