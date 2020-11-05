package lehmann.master.thesis.mcc.tu.berlin.de.producer.data;

import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.RecordMetadata;

public class Triple {
	
	private final long timestamp;
	private final Future<RecordMetadata> data;
	private final long producedElements;
	
	public Triple(long timestamp, Future<RecordMetadata> data, long producedElements) {
		this.timestamp = timestamp;
		this.data = data;
		this.producedElements = producedElements;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Future<RecordMetadata> getData() {
		return data;
	}
	
	public long getProducedElements() {
		return producedElements;
	}
	
}
