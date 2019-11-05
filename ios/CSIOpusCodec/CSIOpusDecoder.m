//
//  OpusDecoder.m
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

#import "CSIOpusDecoder.h"
#include "CSIDataQueue.h"
#include <libopus/opus.h>

#define OPUS_MAX_FRAME_SIZE (960*6)

@interface CSIOpusDecoder ()
@property (assign) OpusDecoder *decoder;
@property (assign) CSIDataQueueRef outputBuffer;
@property (assign) opus_int16 *decodeBuffer;
@end


@implementation CSIOpusDecoder

+ (CSIOpusDecoder*)decoderWithSampleRate:(opus_int32)sampleRate channels:(int)channels
{
    return [[CSIOpusDecoder alloc] initWithSampleRate:sampleRate channels:channels];
}

- (id)initWithSampleRate:(opus_int32)sampleRate channels:(int)channels
{
    self = [super init];

    if(self)
    {

        NSLog(@"Creating a decoder using Opus version %s", opus_get_version_string());

        int error;
        _decoder = opus_decoder_create(sampleRate, channels, &error);

        if(error != OPUS_OK)
        {
            NSLog(@"Opus decoder encountered an error %s", opus_strerror(error));
            return nil;
        }

        _outputBuffer = CSIDataQueueCreate();
        _decodeBuffer = malloc(sizeof(opus_int16) * OPUS_MAX_FRAME_SIZE * channels);
    }

    return self;
}

- (void)setBitrate:(opus_int32)bitrate {
    _bitrate = bitrate;
    
    opus_decoder_ctl(self.decoder, OPUS_SET_BITRATE(bitrate));
}

- (void)setVariableBitrate:(BOOL)variableBitrate {
    _variableBitrate = variableBitrate;
    
    opus_decoder_ctl(self.decoder, OPUS_SET_VBR(variableBitrate));
}

- (void)dealloc
{
    free(_decodeBuffer);
    CSIDataQueueDestroy(_outputBuffer);
}

- (void)decode:(NSData *)packet
{
    int result = opus_decode(self.decoder, packet.bytes, (opus_int32)packet.length, self.decodeBuffer, OPUS_MAX_FRAME_SIZE, 0);
    if(result < 0)
    {
        NSLog(@"Opus decoder encountered an error %s", opus_strerror(result));
        return;
    }

    @synchronized(self)
    {
        size_t bytesAvailable = (size_t)result * sizeof(opus_int16);
        CSIDataQueueEnqueue(self.outputBuffer, self.decodeBuffer, bytesAvailable);
        
//#if DEBUG
//        int bandwidth = opus_packet_get_bandwidth((const unsigned char *)self.decodeBuffer);
//        NSLog(@"Opus decoder packet bandwidth at %d Hz", bandwidth);
//
//        opus_int32 bitrate = 0;
//        opus_decoder_ctl(self.decoder, OPUS_GET_BITRATE(&bitrate));
//        NSLog(@"Opus decoder bitrate at %d bps", bitrate);
//#endif
    }
}

- (int)tryFillBuffer:(AudioBufferList *)audioBufferList
{
    uint totalBytesRequested = 0;
    for (UInt32 i=0; i < audioBufferList->mNumberBuffers; ++i)
    {
        AudioBuffer audioBuffer = audioBufferList->mBuffers[i];
        if(audioBuffer.mNumberChannels > 1) return NO;
        totalBytesRequested += audioBuffer.mDataByteSize;
    }

    if(totalBytesRequested == 0) return 0;

    @synchronized(self)
    {
        int bytesAvailable = (int)CSIDataQueueGetLength(self.outputBuffer);
        if(bytesAvailable < (int)totalBytesRequested)
        {
//#if DEBUG
//            NSLog(@"Couldnt fill buffer. Needed %d bytes but only have %d", totalBytesRequested, bytesAvailable);
//#endif
            return 0;
        }

        for (UInt32 i=0; i < audioBufferList->mNumberBuffers; ++i)
        {
            AudioBuffer audioBuffer = audioBufferList->mBuffers[i];
            CSIDataQueueDequeue(self.outputBuffer, audioBuffer.mData, audioBuffer.mDataByteSize);
        }

        return (int)totalBytesRequested;
    }
}


@end
