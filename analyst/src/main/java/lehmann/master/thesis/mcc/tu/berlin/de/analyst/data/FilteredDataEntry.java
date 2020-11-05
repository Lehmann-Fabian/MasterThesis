package lehmann.master.thesis.mcc.tu.berlin.de.analyst.data;

public class FilteredDataEntry {
	
	final private long offset;
	final private long timestamp;
	final private float measurement;
	
	public FilteredDataEntry(long offset, long timestamp, float measurement) {
		this.offset = offset;
		this.timestamp = timestamp;
		this.measurement = measurement;
	}
	
	public long getOffset() {
		return offset;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public float getMeasurement() {
		return measurement;
	}

	@Override
	public String toString() {
		return "DataEntryOutput [offset=" + offset + ", timestamp=" + timestamp + ", measurement=" + measurement + "]";
	}
	
}
