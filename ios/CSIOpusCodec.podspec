Pod::Spec.new do |s|
  s.name         = "CSIOpusCodec"
  s.version      = "0.1.0"
  s.summary      = "Another Objective-C wrapper for Opus Codec."

  s.homepage     = "https://github.com/yalnazov/CSIOpusCodec"
  s.description  = <<-DESC
                   This Pod is built to package the existing opus codec wrapper
                   by Sam Leitch. Patch for Swift and libopus-rs by Andrew Stepanov.
                   DESC

  s.source = { :git => 'https://github.com/TinkoffCreditSystems/voicekit-examples.git' }
  s.author       = "Andrew Stepanov"
  s.platform     = :ios
  s.requires_arc = true

  s.source_files  = "#{s.name}/*.{h,m,c}"
  s.pod_target_xcconfig = {
   'DEFINES_MODULE' => 'YES',
  }
  s.dependencies = { 'libopus' => '~> 1.1' }
end

