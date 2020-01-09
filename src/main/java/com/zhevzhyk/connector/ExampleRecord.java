package com.zhevzhyk.connector;

import com.zhevzhyk.model.ExampleMessage;
import lombok.Data;
import org.apache.pulsar.functions.api.Record;

@Data
public class ExampleRecord implements Record<ExampleMessage> {
    private final ExampleMessage value;
}
