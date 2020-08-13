package lehmann.master.thesis.mcc.tu.berlin.de.filter;

import org.apache.kafka.common.KafkaFuture.Function;

public abstract class Filter extends Function<Float[], Float>{
	
	private final int size;

	public Filter(int size) {
		assert(size > 0 && size % 2 == 1);
		this.size = size;
	}
	
	@Override
	public Float apply(Float[] a) {
		assert(a.length == size);
		return null;
	}
	
	public int getSize() {
		return size;
	}

}
