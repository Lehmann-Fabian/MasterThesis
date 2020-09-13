package lehmann.master.thesis.mcc.tu.berlin.de.filter;

import java.util.Arrays;
import java.util.function.Function;


public class MeanFilter implements Function<Float[], Float>{

	@Override
	public Float apply(Float[] a) {
		float sum = 0;
		for (Float f : a) {
			sum += f;
		}
		
		return sum / a.length;
	}

}
