require 'net/http'
require_relative 'options'
require_relative 'request'
require_relative 'audio'

options = Options.parse

pcm, sample_rate, num_channels, total_frames = Audio.read_frames options

request = RecognitionRequest.new(
  api_key: ENV['VOICEKIT_API_KEY'],
  api_secret: ENV['VOICEKIT_SECRET_KEY'],
  audio_frames: pcm,
  sample_rate: sample_rate,
  num_channels: num_channels,
  format: :json,
  options: options
)

uri = URI.parse options[:endpoint] || 'https://stt.tinkoff.ru/v1/stt:recognize'
http = Net::HTTP.new uri.host, uri.port
http.use_ssl = true
post = Net::HTTP::Post.new(uri, request.headers)
post.body = request.body

response = http.request post
puts "Received response [#{response.code}]:"
puts response.body
