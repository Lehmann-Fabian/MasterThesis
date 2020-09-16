package lehmann.master.thesis.mcc.tu.berlin.de.analyst.data;

import java.nio.ByteBuffer;

import org.apache.kafka.common.serialization.Deserializer;

public class FilteredDataEntryDeserializer implements Deserializer<FilteredDataEntry> {

	@Override
	public FilteredDataEntry deserialize(String topic, byte[] data) {
		ByteBuffer wrap = ByteBuffer.wrap(data);
		long offset = wrap.getLong();
		long timestamp = wrap.getLong();
		float measurement = wrap.getFloat();
		return new FilteredDataEntry(offset, timestamp, measurement);
	}
	
}