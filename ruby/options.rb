require 'optparse'

class Options
  def self.parse
    options = {
      max_alternatives: 2,
      do_not_perform_vad: false,
      silence_duration_threshold: 0.6,
      disable_automatic_punctuation: false,
    }

    OptionParser.new do |opts|
      opts.banner = "Usage: #{$1} [options]"

      opts.on('-f', '--input-file [FILE]', String, "Path to input file. Supported wav, mp3, flac, oga") do |file|
        options[:input] = file
      end

      opts.on('-a', '--max-alternatives [MAX_ALTERNATIVES]', Integer, 
        'Number of speech recognition alternatives to return') do |max_alternatives|
        
        options[:max_alternatives] = max_alternatives
      end

      opts.on('-v', '--do-not-perform-vad', 
        'Specify this to disable voice activity detection. All audio is processed as though it were a single utterance') do
        
        options[:do_not_perform_vad] = true
      end

      opts.on('-s', '--silence-duration-threshold [THR]', Float, 
        'Silence threshold in seconds for VAD to assume the current utterance is ended and the next utterance shall begin.') do |thr|
        
        options[:silence_duration_threshold] = thr
      end

      opts.on('-p', '--disable-automatic-punctuation',
        'Specify this to disable automatic punctuation in recognition results') do

        options[:disable_automatic_punctuation] = true
      end

      opts.on('-e', '--endpoint [ENDPOINT]', String, "Network endpoint") do |endpoint|
        options[:endpoint] = endpoint
      end
    end.parse!

    options
  end
end
