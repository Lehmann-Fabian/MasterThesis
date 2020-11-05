package lehmann.master.thesis.mcc.tu.berlin.de.producer.data;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Serializer;

public class RawDataEntrySerializer implements Serializer<RawDataEntry> {

	@Override
	public byte[] serialize(String topic, RawDataEntry data) {
	    ByteBuffer buffer = ByteBuffer.allocate(12);
	    buffer.putLong(data.getTimestamp());
	    buffer.putFloat(data.getMeasurement());
		return buffer.array();
	}
	
}