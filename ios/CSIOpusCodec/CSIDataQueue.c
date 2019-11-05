//
//  CSIDataQueue.c
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

#include "CSIDataQueue.h"
#include <string.h>

#define DEFAULT_CAPACITY 4096

struct CSIDataQueueOpaque
{
    void* dataStartPointer;
    void* dataEndPointer;
    void* activeStartPointer;
    void* activeEndPointer;
    
    size_t capacity;
    size_t activeLength;
};

CSIDataQueueRef CSIDataQueueCreate()
{
    
    CSIDataQueueRef queue = (CSIDataQueueRef)malloc(sizeof(struct CSIDataQueueOpaque));
    size_t capacity = DEFAULT_CAPACITY;
    void* data = malloc(capacity);
    queue->dataStartPointer = data;
    queue->capacity = capacity;
    queue->dataEndPointer = data + capacity;
    queue->activeStartPointer = data;
    queue->activeEndPointer = data;
    queue->activeLength = 0;
    
    return queue;
}

void CSIDataQueueDestroy(CSIDataQueueRef queue)
{
    free(queue->dataStartPointer);
    free(queue);
}

static void upgradeToLargerCapacity(CSIDataQueueRef queue)
{
    size_t newCapacity = queue->capacity * 2;
    void* newData = malloc(newCapacity);
    CSIDataQueuePeek(queue, newData, queue->activeLength);
    void* oldData = queue->dataStartPointer;
    queue->dataStartPointer = newData;
    queue->capacity = newCapacity;
    queue->dataEndPointer = newData + newCapacity;
    queue->activeStartPointer = newData;
    queue->activeEndPointer = newData + queue->activeLength;

    free(oldData);
}

size_t CSIDataQueueEnqueue(CSIDataQueueRef queue, const void* data, size_t dataLength)
{
    if(data == NULL) return SIZE_MAX;
    if(dataLength == 0) return 0;
    
    size_t newLength = queue->activeLength + dataLength;
    while(newLength > queue->capacity)
    {
        upgradeToLargerCapacity(queue);
    }
    
    void* newEndPointer = queue->activeEndPointer + dataLength;
    
    if(newEndPointer <= queue->dataEndPointer)
    {
        memcpy(queue->activeEndPointer, data, dataLength);
        queue->activeEndPointer += dataLength;
    }
    else
    {
        size_t sizeToEnd = (size_t)queue->dataEndPointer - (size_t)queue->activeEndPointer;
        memcpy(queue->activeEndPointer, data, sizeToEnd);
        const void* dataLeft = data + sizeToEnd;
        size_t dataLeftLength = dataLength - sizeToEnd;
        memcpy(queue->dataStartPointer, dataLeft, dataLeftLength);
        queue->activeEndPointer = queue->dataStartPointer + dataLeftLength;
    }
    
    queue->activeLength += dataLength;
    return dataLength;
}

size_t CSIDataQueuePeek(CSIDataQueueRef queue, void* data, size_t dataLength)
{
    size_t readLength = queue->activeLength > dataLength ? dataLength : queue->activeLength;
    void* readEndPointer = queue->activeStartPointer + readLength;
    
    if(readEndPointer <= queue->dataEndPointer)
    {
        memcpy(data, queue->activeStartPointer, readLength);
    }
    else
    {
        size_t sizeToEnd = (size_t)queue->dataEndPointer - (size_t)queue->activeStartPointer;
        memcpy(data, queue->activeStartPointer, sizeToEnd);
        void* secondReadPointer = data + sizeToEnd;
        size_t dataLeft = readLength - sizeToEnd;
        memcpy(secondReadPointer, queue->dataStartPointer, dataLeft);
    }
    
    return readLength;
}

size_t CSIDataQueueDequeue(CSIDataQueueRef queue, void* data, size_t dataLength)
{
    size_t readLength = CSIDataQueuePeek(queue, data, dataLength);
    
    void* newStartPointer = queue->activeStartPointer + readLength;
    
    if(newStartPointer <= queue->dataEndPointer)
    {
        queue->activeStartPointer = newStartPointer;
    }
    else
    {
        size_t sizeToEnd = (size_t)queue->dataEndPointer - (size_t)queue->activeStartPointer;
        size_t dataLeft = readLength - sizeToEnd;
        queue->activeStartPointer = queue->dataStartPointer + dataLeft;
    }
    
    queue->activeLength -= readLength;
    return readLength;
}

size_t CSIDataQueueGetLength(CSIDataQueueRef queue)
{
    return queue->activeLength;
}

void CSIDataQueueClear(CSIDataQueueRef queue)
{
    queue->activeStartPointer = queue->dataStartPointer;
    queue->activeEndPointer = queue->dataStartPointer;
    queue->activeLength = 0;
}

int CSIDataQueueRunTests()
{
    char* testInput = "1234567890";
    char* clearedInput = "0987654321";
    size_t testInputLength = 10;
    char* testOutput = malloc(testInputLength);
    int testIterations = 4096;
    
    CSIDataQueueRef queue = CSIDataQueueCreate();
    
    // Test Adding more than capacity
    size_t totalInput = 0;
    for(int i=0; i < testIterations; ++i)
    {
        size_t amountInput = CSIDataQueueEnqueue(queue, testInput, testInputLength);
        if(amountInput != testInputLength)
        {
            free(testOutput);
            return -1;
            //NSLog(@"Input the wrong amount of data: %zd instead of %zd", amountInput, testInputLength);
        }
        
        totalInput += amountInput;
        size_t currentSize = CSIDataQueueGetLength(queue);
        if(currentSize != totalInput)
        {
            free(testOutput);
            return -2;
            //NSLog(@"The active size is not being reported correctly: %zd instead of %zd", currentSize, totalInput);
        }
    }
    
    // Test removing all of it
    for(int i=0; i < testIterations; ++i)
    {
        size_t amountOutput = CSIDataQueueDequeue(queue, testOutput, testInputLength);
        if(amountOutput != testInputLength)
        {
            return -3;
            //NSLog(@"Output the wrong amount of data: %zd instead of %zd", amountOutput, testInputLength);
        }
        
        totalInput -= amountOutput;
        size_t currentSize = CSIDataQueueGetLength(queue);
        if(currentSize != totalInput)
        {
            return -4;
            //NSLog(@"The active size is not being reported correctly: %zd instead of %zd", currentSize, totalInput);
        }
    }
    
    // Test adding and removing
    CSIDataQueueDestroy(queue);
    queue = CSIDataQueueCreate();
    for(int i=0; i < testIterations; ++i)
    {
        size_t amountInput = CSIDataQueueEnqueue(queue, testInput, testInputLength);
        if(amountInput != testInputLength)
        {
            return -5;
            //NSLog(@"Input the wrong amount of data: %zd instead of %zd", amountInput, testInputLength);
        }
        
        size_t amountPeeked = CSIDataQueuePeek(queue, testOutput, testInputLength);
        if(amountPeeked != testInputLength)
        {
            return -6;
            //NSLog(@"Peeked the wrong amount of data: %zd instead of 10", amountPeeked);
        }
        
        memcpy(testOutput, clearedInput, testInputLength);
        size_t amountOutput = CSIDataQueueDequeue(queue, testOutput, testInputLength);
        if(amountOutput != testInputLength)
        {
            return -7;
            //NSLog(@"Output the wrong amount of data: %zd instead of 10", amountOutput);
        }
        
        int result = memcmp(testInput, testOutput, testInputLength);
        if(result != 0)
        {
            return -8;
            //NSLog(@"The input and output don't match");
        }
    }
    
    return 0;
    //NSLog(@"Queue tests complete");
}
