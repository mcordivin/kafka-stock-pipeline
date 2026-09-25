package com.stockpipeline.streams.config;

import com.stockpipeline.streams.model.StockTick;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public class TradeTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        if(record.value() instanceof StockTick t && t.tradeTimestampMillis() > 0) {
            return t.tradeTimestampMillis();
        }
        return record.timestamp();
    }
}
