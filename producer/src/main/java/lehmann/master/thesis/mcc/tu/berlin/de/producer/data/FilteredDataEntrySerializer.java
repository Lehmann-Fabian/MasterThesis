package lehmann.master.thesis.mcc.tu.berlin.de.producer.data;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Serializer;

public class FilteredDataEntrySerializer implements Serializer<FilteredDataEntry> {

	@Override
	public byte[] serialize(String topic, FilteredDataEntry data) {
	    ByteBuffer buffer = ByteBuffer.allocate(20);
	    buffer.putLong(data.getOffset());
	    buffer.putLong(data.getTimestamp());
	    buffer.putFloat(data.getMeasurement());
		return buffer.array();
	}
	
}