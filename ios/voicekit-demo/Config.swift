//
//  Config.swift
//  voicekit-demo
//
//  Created by Andrew Stepanov on 05.11.2019.
//  Copyright © 2019 com.example. All rights reserved.
//

import Foundation
import AVFoundation

class Config {
    static let preferredSampleRate = 48000
    static let audioSessionMode = AVAudioSession.Mode.default
    static let audioUnitSubType = kAudioUnitSubType_RemoteIO  // can change to kAudioUnitSubType_VoiceProcessingIO
    static let apiKey = "<YOUR_API_KEY>"
    static let secretKey = "<YOUR_SECRET_KEY>"
}
