package lehmann.master.thesis.mcc.tu.berlin.de.producer;

import java.util.function.Function;

public class SingleValueProducer implements Function<Long, Float>{

	private int add = 0;

	@Override
	public Float apply(Long t) {
		return (float) (add + Math.random());
	}
	
	public void setAdditionalFactor(int add) {
		this.add  = add;
	}
	

}
