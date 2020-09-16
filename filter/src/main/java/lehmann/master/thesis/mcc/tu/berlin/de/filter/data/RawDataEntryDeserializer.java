package lehmann.master.thesis.mcc.tu.berlin.de.filter.data;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Deserializer;

public class RawDataEntryDeserializer implements Deserializer<RawDataEntry> {

	@Override
	public RawDataEntry deserialize(String topic, byte[] data) {
		ByteBuffer wrap = ByteBuffer.wrap(data);
		long timestamp = wrap.getLong();
		float measurement = wrap.getFloat();
		return new RawDataEntry(timestamp, measurement);
	}
	
}