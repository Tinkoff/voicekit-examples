import UIKit
import VoiceKit
import SwiftJWT
import Foundation
import AVFoundation
import CSIOpusCodec


enum State {
    case idle
    case recognizing
    case synthesizing
}


class ViewController: UIViewController {
    private let sttClient = TVKSRSpeechToText(host: "stt.tinkoff.ru:443")
    private let ttsClient = TVKSSTextToSpeech(host: "tts.tinkoff.ru:443")
    private let opusEncoder : CSIOpusEncoder = CSIOpusEncoder(sampleRate: Int32(Config.preferredSampleRate), channels: 1, frameDuration: 0.02)
    private let opusDecoder : CSIOpusDecoder = CSIOpusDecoder(sampleRate: Int32(Config.preferredSampleRate), channels: 1)
    private let recognizeDispatchQueue = DispatchQueue(label: "recognize-queue", qos: .userInteractive)
    
    private var audioUnit: AudioUnit? = nil
    private var sttWriter: GRXBufferedPipe? = nil
    private var sttCall: GRPCProtoCall? = nil
    private var state: State = .idle
    private var synthesizeCallFinished = false

    @IBOutlet weak var recognitionResultsTextView: UITextView!
    @IBOutlet weak var automaticPunctuationSwitch: UISwitch!
    @IBOutlet weak var interimResultsSwitch: UISwitch!
    @IBOutlet weak var singleUtteranceSwitch: UISwitch!
    @IBOutlet weak var vadSwitch: UISwitch!
    @IBOutlet weak var vadSilenceSlider: UISlider!
    @IBOutlet weak var vadSilenceLabel: UILabel!
    @IBOutlet weak var recognizeButton: UIButton!
    @IBOutlet weak var synthesizeButton: UIButton!
    
    @objc func dismissKeyboard (_ sender: UITapGestureRecognizer) {
        self.view.endEditing(true)
    }
    
    @IBAction func sliderValueChanged(_ sender: Any) {
        vadSilenceLabel.text = "VAD silence threshold: \(String(format: "%.1f", vadSilenceSlider.value)) s"
    }
    
    @IBAction func vadSwitchValueChanged(_ sender: Any) {
        vadSilenceSlider.isEnabled = vadSwitch.isOn
        vadSilenceLabel.isEnabled = vadSwitch.isOn
    }
    
    @IBAction func recognizeTouch(_ sender: Any) {
        if state == .idle {
            transitionToState(.recognizing)
            startReconition()
        } else if state == .recognizing {
            transitionToState(.idle)
            stopRecognition()
        }
    }
    
    @IBAction func synthesizeTouch(_ sender: Any) {
        if state == .idle {
            transitionToState(.synthesizing)
            startSynthesis()
        }
    }
    
    func transitionToState(_ newState: State) {
        if newState == .idle {
            recognizeButton.setTitle("Recognize", for: .normal)
            recognizeButton.setTitleColor(.none, for: .normal)
            synthesizeButton.isEnabled = true
            recognizeButton.isEnabled = true
        } else if newState == .recognizing {
            recognizeButton.setTitle("Stop", for: .normal)
            recognizeButton.setTitleColor(.red, for: .normal)
            recognizeButton.isEnabled = true
            synthesizeButton.isEnabled = false
        } else if newState == .synthesizing {
            recognizeButton.isEnabled = false
            synthesizeButton.isEnabled = false
        }
        
        state = newState
    }
    
    func newJWTToken(forScope scope: String) -> String {
        struct MyClaims: Claims {
            let iss: String
            let sub: String
            let exp: Date
            let aud: String
        }
        
        let header = Header(kid: Config.apiKey)
        let myClaims = MyClaims(iss: "test_issuer", sub: "test_user", exp: Date().addingTimeInterval(600), aud: scope)
        var myJWT = JWT(header: header, claims: myClaims)
        let jwtSigner = JWTSigner.hs256(key: Data(base64Encoded: Config.secretKey)!)
        return try! myJWT.sign(using: jwtSigner)
    }
    
    func initAudioSession() {
        let audioSession = AVAudioSession.sharedInstance()
        try? audioSession.setActive(false)
        try? audioSession.setCategory(AVAudioSession.Category.playAndRecord,
                                      mode: Config.audioSessionMode, options: [.defaultToSpeaker, .allowBluetoothA2DP])
        try? audioSession.setPreferredSampleRate(Double(Config.preferredSampleRate))
        try? audioSession.setActive(true, options: .notifyOthersOnDeactivation)
    }
    
    func initAudioUnit() {
        var audioComponentDescription = AudioComponentDescription(
            componentType: kAudioUnitType_Output,
            componentSubType: Config.audioUnitSubType,
            componentManufacturer: kAudioUnitManufacturer_Apple,
            componentFlags: 0,
            componentFlagsMask: 0)

        guard let comp = AudioComponentFindNext(nil, &audioComponentDescription) else { return }
        guard AudioComponentInstanceNew(comp, &audioUnit) == 0 else { return }
        guard let localAudioUnit = audioUnit else { return }

        var one: UInt32 = 1
        guard AudioUnitSetProperty(localAudioUnit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Input, 1, &one, SizeOf32(one)) == 0 else {
            print("error enabling io")
            return
        }
        guard AudioUnitSetProperty(localAudioUnit, kAudioOutputUnitProperty_EnableIO, kAudioUnitScope_Output, 0, &one, SizeOf32(one)) == 0 else {
            print("error enabling io")
            return
        }

        if Config.audioUnitSubType == kAudioUnitSubType_VoiceProcessingIO {
            guard AudioUnitSetProperty(localAudioUnit, kAUVoiceIOProperty_VoiceProcessingEnableAGC, kAudioUnitScope_Global, 1, &one, SizeOf32(one)) == 0 else {
                print("error enabling AGC")
                return
            }
            guard AudioUnitSetProperty(localAudioUnit, kAUVoiceIOProperty_VoiceProcessingEnableAGC, kAudioUnitScope_Global, 0, &one, SizeOf32(one)) == 0 else {
                print("Error enabling AGC")
                return
            }
        }
        var asbd = AudioStreamBasicDescription(
            mSampleRate: Double(Config.preferredSampleRate),
            mFormatID: kAudioFormatLinearPCM,
            mFormatFlags: kAudioFormatFlagIsPacked | kAudioFormatFlagIsSignedInteger,
            mBytesPerPacket: 2,
            mFramesPerPacket: 1,
            mBytesPerFrame: 2,
            mChannelsPerFrame: 1,
            mBitsPerChannel: 16,
            mReserved: 0)
        guard AudioUnitSetProperty(localAudioUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Input, 0, &asbd, SizeOf32(asbd)) == 0 else {
            print("error setting stream format")
            return
        }
        guard AudioUnitSetProperty(localAudioUnit, kAudioUnitProperty_StreamFormat, kAudioUnitScope_Output, 1, &asbd, SizeOf32(asbd)) == 0 else {
            print("error setting stream format")
            return
        }
        var renderInputCallback = AURenderCallbackStruct(
            inputProc: TVKAudioUnitRenderInputCallback,
            inputProcRefCon: Unmanaged.passUnretained(self).toOpaque()
        )
        var renderOutputCallback = AURenderCallbackStruct(
            inputProc: TVKAudioUnitRenderOutputCallback,
            inputProcRefCon: Unmanaged.passUnretained(self).toOpaque()
        )
        guard AudioUnitSetProperty(localAudioUnit, kAudioOutputUnitProperty_SetInputCallback, kAudioUnitScope_Global, 1, &renderInputCallback, SizeOf32(renderInputCallback)) == 0 else {
            print("error setting callback")
            return
        }
        guard AudioUnitSetProperty(localAudioUnit, kAudioUnitProperty_SetRenderCallback, kAudioUnitScope_Global, 0, &renderOutputCallback, SizeOf32(renderOutputCallback)) == 0 else {
            print("error setting callback")
            return
        }
        guard AudioUnitInitialize(localAudioUnit) == 0 else {
            print("error initializing unit")
            return
        }
        guard AudioOutputUnitStart(localAudioUnit) == 0 else {
            print("error starting unit")
            return
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        initAudioSession()
        initAudioUnit()
        
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(self.dismissKeyboard (_:)))
        self.view.addGestureRecognizer(tapGesture)
    }
    
    func startReconition() {
        let request = TVKSRStreamingRecognizeRequest()
        request.streamingConfig.interimResultsConfig.enableInterimResults = interimResultsSwitch.isOn
        request.streamingConfig.singleUtterance = singleUtteranceSwitch.isOn
        request.streamingConfig.config.enableAutomaticPunctuation = automaticPunctuationSwitch.isOn
        if vadSwitch.isOn {
            request.streamingConfig.config.vadConfig.silenceDurationThreshold = vadSilenceSlider.value
        } else {
            request.streamingConfig.config.doNotPerformVad = true
        }
        request.streamingConfig.config.numChannels = 1
        request.streamingConfig.config.encoding = .rawOpus
        request.streamingConfig.config.sampleRateHertz = UInt32(Config.preferredSampleRate)
        self.recognitionResultsTextView.text = ""
        
        recognizeDispatchQueue.async {
            self.sttWriter = GRXBufferedPipe()
            self.sttCall = self.sttClient.rpcToStreamingRecognize(withRequestsWriter: self.sttWriter!) {
                (done, response, error) in
                
                let streamingRecognitionResult = response?.resultsArray[0] as? TVKSRStreamingRecognitionResult
                let speechRecognitionResult = streamingRecognitionResult?.recognitionResult
                let speechRecognitionAlternative = speechRecognitionResult?.alternativesArray[0] as? TVKSRSpeechRecognitionAlternative
                let transcript = speechRecognitionAlternative?.transcript
                if transcript != nil {
                    let parts = self.recognitionResultsTextView.text.split(omittingEmptySubsequences: false){$0.isNewline}.map(String.init)
                    let newParts = parts.prefix(max(parts.count - 1, 0)) + [transcript!]
                    self.recognitionResultsTextView.text = newParts.joined(separator: "\n")
                }
                if streamingRecognitionResult?.isFinal == true {
                    self.recognitionResultsTextView.text.append("\n")
                }
                
                if done {
                    if self.state == .recognizing {
                        self.stopRecognition()
                        self.transitionToState(.idle)
                    }
                }
            }

            self.sttCall!.oauth2AccessToken = self.newJWTToken(forScope: "tinkoff.cloud.stt")
            self.sttCall!.start()
            self.sttWriter!.writeValue(request)
        }
    }
    
    func stopRecognition() {
        recognizeDispatchQueue.sync {
            self.sttWriter?.finishWithError(nil)
            self.sttCall = nil
            self.sttWriter = nil
        }
    }
    
    func startSynthesis() {
        self.synthesizeCallFinished = false
        let request = TVKSSSynthesizeSpeechRequest()
        request.audioConfig.audioEncoding = .rawOpus
        request.audioConfig.sampleRateHertz = Int32(Config.preferredSampleRate)
        request.input.text = recognitionResultsTextView.text
        
        let call = ttsClient.rpcToStreamingSynthesize(with: request) {
            (done, response, error) -> Void in
            if response != nil {
                self.opusDecoder.decode(response!.audioChunk)
            }
            if done {
                self.synthesizeCallFinished = true
            }
        }
        call.oauth2AccessToken = newJWTToken(forScope: "tinkoff.cloud.tts")
        call.start()
    }
    
    func performRenderInput(_ ioActionFlags: UnsafeMutablePointer<AudioUnitRenderActionFlags>, inTimeStamp: UnsafePointer<AudioTimeStamp>, inBusNumber: UInt32, inNumberFrames: UInt32, ioData: UnsafeMutablePointer<AudioBufferList>?) -> OSStatus {
        
        var list = AudioBufferList(mNumberBuffers: 1, mBuffers: AudioBuffer())
        guard AudioUnitRender(self.audioUnit!, ioActionFlags,
                              inTimeStamp, inBusNumber, inNumberFrames, &list) == noErr else {
            print("failed to render")
            return OSStatus("failed to render")!
        }
        for data in (opusEncoder.encode(&list) as! [NSData]) {
            let request = TVKSRStreamingRecognizeRequest()
            request.audioContent = Data(referencing: data)
            recognizeDispatchQueue.sync {
                sttWriter?.writeValue(request)
            }
        }
        return noErr
    }
    
    func performRenderOutput(_ ioActionFlags: UnsafeMutablePointer<AudioUnitRenderActionFlags>, inTimeStamp: UnsafePointer<AudioTimeStamp>, inBusNumber: UInt32, inNumberFrames: UInt32, ioData: UnsafeMutablePointer<AudioBufferList>?) -> OSStatus {
        
        let fillBufferResult = opusDecoder.tryFillBuffer(ioData)
        if fillBufferResult == 0 {
            let dataPtr = ioData!.pointee.mBuffers.mData!.bindMemory(to: Int16.self, capacity: Int(inNumberFrames))
            dataPtr.assign(repeating: 0, count: Int(inNumberFrames))
        }
        if state == .synthesizing && fillBufferResult == 0 && synthesizeCallFinished {
            state = .idle
            DispatchQueue.main.async {
                self.transitionToState(.idle)
            }
        }
        return noErr
    }
}

func SizeOf32<T>(_ X: T) ->UInt32 {return UInt32(MemoryLayout<T>.stride)}

func SizeOf32<T>(_ X: T.Type) ->UInt32 {return UInt32(MemoryLayout<T>.stride)}

let TVKAudioUnitRenderInputCallback: AURenderCallback = {
    (inRefCon, ioActionFlags, inTimeStamp, inBusNumber, inNumberFrames, ioData) -> OSStatus
    in
    let delegate = unsafeBitCast(inRefCon, to: ViewController.self)
    let result = delegate.performRenderInput(ioActionFlags, inTimeStamp: inTimeStamp, inBusNumber: inBusNumber,
                                             inNumberFrames: inNumberFrames, ioData: ioData)
    return result
}

let TVKAudioUnitRenderOutputCallback: AURenderCallback = {
    (inRefCon, ioActionFlags, inTimeStamp, inBusNumber, inNumberFrames, ioData) -> OSStatus
    in
    let delegate = unsafeBitCast(inRefCon, to: ViewController.self)
    let result = delegate.performRenderOutput(ioActionFlags, inTimeStamp: inTimeStamp, inBusNumber: inBusNumber,
                                              inNumberFrames: inNumberFrames, ioData: ioData)
    return result
}
