package lehmann.master.thesis.mcc.tu.berlin.de.inspector;

import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.RecordMetadata;

public class DoNothingInspector implements Inspector {

	@Override
	public void close() {}

	@Override
	public void addProducedRecord(long sendTime, Future<RecordMetadata> send) {}


	@Override
	public void informChange(double value) {}

}
