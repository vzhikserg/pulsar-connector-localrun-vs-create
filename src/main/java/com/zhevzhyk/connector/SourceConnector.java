package com.zhevzhyk.connector;

import com.zhevzhyk.model.Format;
import lombok.SneakyThrows;
import org.apache.pulsar.io.core.PushSource;
import org.apache.pulsar.io.core.SourceContext;
import org.apache.pulsar.io.core.annotations.Connector;
import org.apache.pulsar.io.core.annotations.IOType;
import com.zhevzhyk.model.ExampleMessage;

import java.util.Map;

@Connector(
        name = "source-connector",
        type = IOType.SOURCE,
        help = "A simple source connector",
        configClass = SourceConnectorConfig.class)
public class SourceConnector extends PushSource<ExampleMessage> {

    @Override
    public void open(Map<String, Object> config, SourceContext sourceContext) throws Exception {
        Thread thread = new Thread(new PulsarConnectorRunnable(this));
        thread.start();
    }

    @Override
    public void close() throws Exception {}

    private class PulsarConnectorRunnable implements Runnable {

        private final SourceConnector sourceConnector;

        public PulsarConnectorRunnable(SourceConnector sourceConnector) {
            this.sourceConnector = sourceConnector;
        }

        @SneakyThrows
        public void run() {

            while (true) {
                sourceConnector.consume(new ExampleRecord(
                        ExampleMessage.newBuilder()
                                .setFormat(Format.UNKNOWN)
                                .setSourceApplication("Example application")
                                .setUuid("123e4567-e89b-12d3-a456-426655440000")
                                .build()));

                Thread.sleep(5000);
            }
        }
    }
}