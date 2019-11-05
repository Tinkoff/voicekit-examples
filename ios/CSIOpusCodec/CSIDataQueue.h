//
//  CSIDataQueue.h
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

#ifndef OpusIPhoneTest_CSIDataQueue_h
#define OpusIPhoneTest_CSIDataQueue_h
#include <stdlib.h>

typedef struct CSIDataQueueOpaque *CSIDataQueueRef;

CSIDataQueueRef CSIDataQueueCreate();
void CSIDataQueueClear(CSIDataQueueRef queue);
size_t CSIDataQueueEnqueue(CSIDataQueueRef queue, const void* data, size_t dataLength);
size_t CSIDataQueueDequeue(CSIDataQueueRef queue, void* data, size_t dataLength);
size_t CSIDataQueuePeek(CSIDataQueueRef queue, void* data, size_t dataLength);
size_t CSIDataQueueGetLength(CSIDataQueueRef queue);
void CSIDataQueueDestroy(CSIDataQueueRef queue);

int CSIDataQueueRunTests();

#endif
