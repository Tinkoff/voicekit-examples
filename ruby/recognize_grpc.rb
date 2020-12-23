require_relative 'options'
require_relative 'request'
require 'json'

options = Options.parse

request = RecognitionRequest.grpc options

stub = Tinkoff::Cloud::Stt::V1::SpeechToText::Stub.new options[:endpoint] || 'stt.tinkoff.ru:443', GRPC::Core::ChannelCredentials.new
begin
  response = stub.recognize request.body, metadata: request.headers
rescue GRPC::BadStatus => e
  puts "Error: #{e}"
  exit 1
end

puts "Received response:"
puts JSON.pretty_generate JSON.parse response.to_json
