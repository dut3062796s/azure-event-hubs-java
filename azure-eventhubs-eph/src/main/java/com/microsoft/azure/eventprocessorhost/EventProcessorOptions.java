/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

import com.microsoft.azure.eventhubs.PartitionReceiver;

public final class EventProcessorOptions
{
	private Consumer<ExceptionReceivedEventArgs> exceptionNotificationHandler = null;
    private Boolean invokeProcessorAfterReceiveTimeout = false;
    private boolean receiverRuntimeMetricEnabled = false;
    private int maxBatchSize = 10;
    private int prefetchCount = 300;
    private Duration receiveTimeOut = Duration.ofMinutes(1);
    private Function<String, Object> initialOffsetProvider = (partitionId) -> { return PartitionReceiver.START_OF_STREAM; };

    /***
     * Returns an EventProcessorOptions instance with all options set to the default values.
     * 
     * The default values are:
     * <pre>
     * MaxBatchSize: 10
     * ReceiveTimeOut: 1 minute
     * PrefetchCount: 300
     * InitialOffsetProvider: uses the last offset checkpointed, or START_OF_STREAM
     * InvokeProcessorAfterReceiveTimeout: false
     * ReceiverRuntimeMetricEnabled: false
     * </pre>
     * 
     * @return an EventProcessorOptions instance with all options set to the default values
     */
    public static EventProcessorOptions getDefaultOptions()
    {
        return new EventProcessorOptions();
    }

    public EventProcessorOptions()
    {
    }
    
	/**
	 * Sets a handler which receives notification of general exceptions.
	 * 
	 * Exceptions which occur while processing events from a particular Event Hub partition are delivered
	 * to the onError method of the event processor for that partition. This handler is called on occasions
	 * when there is no event processor associated with the throwing activity, or the event processor could
	 * not be created.
	 * 
	 * @param notificationHandler  Handler which is called when an exception occurs. Set to null to stop handling.  
	 */
    public void setExceptionNotification(Consumer<ExceptionReceivedEventArgs> notificationHandler)
    {
    	this.exceptionNotificationHandler = notificationHandler;
    }

    /**
     * Returns the maximum number of events that will be passed to one call to IEventProcessor.onEvents
     * 
     * @return the maximum maximum number of events that will be passed to one call to IEventProcessor.onEvents
     */
    public int getMaxBatchSize()
    {
        return this.maxBatchSize;
    }

    /**
     * Sets the maximum number of events that will be passed to one call to IEventProcessor.onEvents
     *  
     * @param maxBatchSize the maximum number of events that will be passed to one call to IEventProcessor.onEvents
     */
    public void setMaxBatchSize(int maxBatchSize)
    {
        this.maxBatchSize = maxBatchSize;
    }

    /**
     * Returns the timeout for receive operations.
     * 
     * @return the timeout for receive operations
     */
    public Duration getReceiveTimeOut()
    {
        return this.receiveTimeOut;
    }

    /**
     * Sets the timeout for receive operations.
     * 
     * @param receiveTimeOut new timeout for receive operations
     */
    public void setReceiveTimeOut(Duration receiveTimeOut)
    {
        this.receiveTimeOut = receiveTimeOut;
    }

    /***
     * Returns the current prefetch count for the underlying client.
     * 
     * @return the current prefetch count for the underlying client
     */
    public int getPrefetchCount()
    {
        return this.prefetchCount;
    }

    /***
     * Sets the prefetch count for the underlying client.
     * 
     * The default is 300.
     * 
     * @param prefetchCount  The new prefetch count.
     */
    public void setPrefetchCount(int prefetchCount)
    {
        this.prefetchCount = prefetchCount;
    }

    /***
     * If there is no checkpoint for a partition, the initialOffsetProvider function is used to determine
     * the offset at which to start receiving events for that partition.
     * 
     * @return the current offset provider function
     */
    public Function<String, Object> getInitialOffsetProvider()
    {
    	return this.initialOffsetProvider;
    }
    
    /***
     * Sets the function used to determine the offset at which to start receiving events for a
     * partition if there is no checkpoint for that partition.
     * 
     * The provider function takes one argument, the partition id (a String), and returns either the desired
     * starting offset (also a String) or the desired starting timestamp (an Instant).
     * 
     * @param initialOffsetProvider
     */
    public void setInitialOffsetProvider(Function<String, Object> initialOffsetProvider)
    {
    	this.initialOffsetProvider = initialOffsetProvider;
    }
    
    /***
     * Returns whether the EventProcessorHost will call IEventProcessor.onEvents() with an empty iterable
     * when a receive timeout occurs (true) or not (false).
     * 
     * Defaults to false.
     * 
     * @return true if EventProcessorHost will call IEventProcessor.OnEvents on receive timeout, false otherwise
     */
    public Boolean getInvokeProcessorAfterReceiveTimeout()
    {
        return this.invokeProcessorAfterReceiveTimeout;
    }

    /**
     * Changes whether the EventProcessorHost will call IEventProcessor.onEvents() with an empty iterable
     * when a receive timeout occurs (true) or not (false).
     * 
     * The default is false (no call).
     * 
     * @param invokeProcessorAfterReceiveTimeout  the new value for what to do
     */
    public void setInvokeProcessorAfterReceiveTimeout(Boolean invokeProcessorAfterReceiveTimeout)
    {
        this.invokeProcessorAfterReceiveTimeout = invokeProcessorAfterReceiveTimeout;
    }
    
    /**
     * Knob to enable/disable runtime metric of the receiver. If this is set to true, 
     * the first parameter {@link com.microsoft.azure.eventprocessorhost.PartitionContext#runtimeInformation} of
     * {@link IEventProcessor#onEvents(com.microsoft.azure.eventprocessorhost.PartitionContext, java.lang.Iterable)} will be populated.
     * <p>
     * Enabling this knob will add 3 additional properties to all raw AMQP messages received.
     * @return the {@link boolean} indicating, whether, the runtime metric of the receiver was enabled
     */
    public boolean getReceiverRuntimeMetricEnabled()
    {
        return this.receiverRuntimeMetricEnabled;
    }
    
    /**
     * Knob to enable/disable runtime metric of the receiver. If this is set to true, 
     * the first parameter {@link com.microsoft.azure.eventprocessorhost.PartitionContext#runtimeInformation} of
     * {@link IEventProcessor#onEvents(com.microsoft.azure.eventprocessorhost.PartitionContext, java.lang.Iterable)} will be populated.
     * <p>
     * Enabling this knob will add 3 additional properties to all raw AMQP messages received.
     * @param value the {@link boolean} to indicate, whether, the runtime metric of the receiver should be enabled
     */
    public void setReceiverRuntimeMetricEnabled(boolean value)
    {
        this.receiverRuntimeMetricEnabled = value;
    }

    void notifyOfException(String hostname, Exception exception, String action)
    {
    	notifyOfException(hostname, exception, action, ExceptionReceivedEventArgs.NO_ASSOCIATED_PARTITION);
    }
    
    void notifyOfException(String hostname, Exception exception, String action, String partitionId)
    {
    	// Capture handler so it doesn't get set to null between test and use
    	Consumer<ExceptionReceivedEventArgs> handler = this.exceptionNotificationHandler;
    	if (handler != null)
    	{
    		handler.accept(new ExceptionReceivedEventArgs(hostname, exception, action, partitionId));
    	}
    }
}
