//
//  CSIOpusAdapter.m
//  OpusIPhoneTest
//
//  Copyright (c) 2012 Sam Leitch. All rights reserved.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to
//  deal in the Software without restriction, including without limitation the
//  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
//  sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
//  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//  IN THE SOFTWARE.
//

#import "CSIOpusEncoder.h"
#include "CSIDataQueue.h"
#include <libopus/opus.h>
#define BUFFER_LENGTH 4096

@interface CSIOpusEncoder ()
@property (assign) OpusEncoder *encoder;
@property (assign) double frameDuration;
@property (assign) int bytesPerFrame;
@property (assign) int samplesPerFrame;
@property (assign) CSIDataQueueRef inputBuffer;
@property (assign) opus_int16 *frameBuffer;
@property (assign) void *encodeBuffer;
@end

@implementation CSIOpusEncoder

- (id)initWithSampleRate:(opus_int32)sampleRate channels:(int)channels frameDuration:(double)frameDuration
{
    self = [super init];

    if(self)
    {

        NSLog(@"Creating an encoder using Opus version %s", opus_get_version_string());

        int error;
        self.encoder = opus_encoder_create(sampleRate, channels, OPUS_APPLICATION_VOIP, &error);

        if(error != OPUS_OK)
        {
            NSLog(@"Opus encoder encountered an error %s", opus_strerror(error));
        }

        self.frameDuration = frameDuration;
        self.samplesPerFrame = (int)(sampleRate * frameDuration);
        int bytesPerSample = sizeof(opus_int16);
        self.bytesPerFrame = self.samplesPerFrame * bytesPerSample;

        self.inputBuffer = CSIDataQueueCreate();
        self.encodeBuffer = malloc(BUFFER_LENGTH);
        self.frameBuffer = malloc((unsigned long)self.bytesPerFrame);
    }

    return self;
}

- (void)setBitrate:(opus_int32)bitrate {
    _bitrate = bitrate;
    
    opus_encoder_ctl(self.encoder, OPUS_SET_BITRATE(bitrate));
}

- (void)setVariableBitrate:(BOOL)variableBitrate {
    _variableBitrate = variableBitrate;
    
    opus_encoder_ctl(self.encoder, OPUS_SET_VBR(variableBitrate));
}

+ (CSIOpusEncoder *)encoderWithSampleRate:(opus_int32)sampleRate channels:(int)channels frameDuration:(double)frameDuration
{
    CSIOpusEncoder *encoder = [[CSIOpusEncoder alloc] initWithSampleRate:sampleRate channels:channels frameDuration:frameDuration];
    return encoder;
}

- (NSArray *)encodeSample:(CMSampleBufferRef)sampleBuffer
{
//#if DEBUG
//    CMItemCount numSamplesInBuffer = CMSampleBufferGetNumSamples(sampleBuffer);
//    CMTime duration = CMSampleBufferGetDuration(sampleBuffer);
//    Float64 durationInSeconds = CMTimeGetSeconds(duration);
//    NSLog(@"The sample rate is %f", numSamplesInBuffer / durationInSeconds);
//#endif

    CMBlockBufferRef blockBuffer = CMSampleBufferGetDataBuffer(sampleBuffer);
    AudioBufferList audioBufferList;

    CMSampleBufferGetAudioBufferListWithRetainedBlockBuffer(sampleBuffer,
                                                            NULL,
                                                            &audioBufferList,
                                                            sizeof(audioBufferList),
                                                            NULL,
                                                            NULL,
                                                            kCMSampleBufferFlag_AudioBufferList_Assure16ByteAlignment,
                                                            &blockBuffer);


    return [self encodeBufferList:&audioBufferList];
}

- (NSArray *)encodeBufferList:(AudioBufferList *)audioBufferList
{
    NSMutableArray *output = [NSMutableArray array];

    for (UInt32 i=0; i < audioBufferList->mNumberBuffers; ++i) {
        AudioBuffer audioBuffer = audioBufferList->mBuffers[i];
        CSIDataQueueEnqueue(self.inputBuffer, audioBuffer.mData, audioBuffer.mDataByteSize);
    }

    while ((int)CSIDataQueueGetLength(self.inputBuffer) > self.bytesPerFrame) {
        CSIDataQueueDequeue(self.inputBuffer, self.frameBuffer, (size_t)self.bytesPerFrame);
        opus_int32 result = opus_encode(self.encoder, self.frameBuffer, self.samplesPerFrame, self.encodeBuffer, BUFFER_LENGTH);

        if(result < 0) {
            NSLog(@"Opus encoder encountered an error %s", opus_strerror(result));
            return nil;
        }

        NSData *encodedData = [NSData dataWithBytes:self.encodeBuffer length:(NSUInteger)result];
        [output addObject:encodedData];
    }
    
//#if DEBUG
//    int bandwidth = opus_packet_get_bandwidth(self.encodeBuffer);
//    NSLog(@"Opus encoder packet bandwidth at %d Hz", bandwidth);
//
//    opus_int32 bitrate = 0;
//    opus_encoder_ctl(self.encoder, OPUS_GET_BITRATE(&bitrate));
//    NSLog(@"Opus encoder bitrate at %d bps", bitrate);
//#endif
    
    return output;
}

- (void)dealloc
{
    free(self.encodeBuffer);
    free(self.frameBuffer);
    CSIDataQueueDestroy(self.inputBuffer);
    opus_encoder_destroy(self.encoder);
}

@end
