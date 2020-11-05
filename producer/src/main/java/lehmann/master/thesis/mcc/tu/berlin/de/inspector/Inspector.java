package lehmann.master.thesis.mcc.tu.berlin.de.inspector;

import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.RecordMetadata;

public interface Inspector {

	void addProducedRecord(long sendTime, Future<RecordMetadata> send);

	void close();
	
	void informChange(double value, long producedElements);

}
