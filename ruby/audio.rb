require 'miniaudio'

class Audio
  def self.read_frames options
    filename = options.delete :input
    lib_format = if filename.end_with? '.wav'
      :wav
    elsif filename.end_with? '.mp3'
      :mp3
    elsif filename.end_with? '.oga'
      :vorbis
    elsif filename.end_with? '.flac'
      :flac
    end
    Miniaudio.send "#{lib_format}_read_file_s16", filename
  end
end
