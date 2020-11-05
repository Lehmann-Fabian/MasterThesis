package lehmann.master.thesis.mcc.tu.berlin.de.analyst.data;

public class Warning {
	
	private final String warning;
	private final long beginOffset;
	private final long endOffset;
	private final float[] values;
	private final long beginTimestamp;
	private final long endTimestamp;

	private Warning() {
		this(null, -1, -1, null, -1, -1);
	}
	
	public Warning(String warning, long beginOffset, long endOffset, float[] values, long beginTimestamp, long endTimestamp) {
		this.warning = warning;
		this.beginOffset = beginOffset;
		this.endOffset = endOffset;
		this.values = values;
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
	}

	public String getWarning() {
		return warning;
	}

	public long getBeginOffset() {
		return beginOffset;
	}

	public long getEndOffset() {
		return endOffset;
	}

	public float[] getValues() {
		return values;
	}

	public long getBeginTimestamp() {
		return beginTimestamp;
	}

	public long getEndTimestamp() {
		return endTimestamp;
	}
	
}
