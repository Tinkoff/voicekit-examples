require 'net/http'
require_relative 'options'
require_relative 'request'

options = Options.parse

request = RecognitionRequest.rest options

uri = URI.parse options[:endpoint] || 'https://stt.tinkoff.ru/v1/stt:recognize'
http = Net::HTTP.new uri.host, uri.port
http.use_ssl = true
post = Net::HTTP::Post.new(uri, request.headers)
post.body = request.body

response = http.request post
puts "Received response [#{response.code}]:"
puts response.body
