package lehmann.master.thesis.mcc.tu.berlin.de.filter.data;

public class RawDataEntry {
	
	final private long timestamp;
	final private float measurement;
	
	public RawDataEntry(long timestamp, float measurement) {
		this.timestamp = timestamp;
		this.measurement = measurement;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public float getMeasurement() {
		return measurement;
	}
	
}
