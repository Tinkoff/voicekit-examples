require_relative 'options'
require_relative 'request'
require 'json'
require_relative 'audio'

options = Options.parse

pcm, sample_rate, num_channels, total_frames = Audio.read_frames options

request = RecognitionRequest.new(
  api_key: ENV['API_KEY'],
  api_secret: ENV['SECRET_KEY'],
  audio_frames: pcm,
  sample_rate: sample_rate,
  num_channels: num_channels,
  format: :grpc,
  options: options
)

stub = Tinkoff::Cloud::Stt::V1::SpeechToText::Stub.new options[:endpoint] || 'stt.tinkoff.ru:433', GRPC::Core::ChannelCredentials.new
begin
  response = stub.recognize request.body, metadata: request.headers
rescue GRPC::BadStatus => e
  puts "Error: #{e}"
  exit 1
end

puts "Received response:"
puts JSON.pretty_generate(response)
