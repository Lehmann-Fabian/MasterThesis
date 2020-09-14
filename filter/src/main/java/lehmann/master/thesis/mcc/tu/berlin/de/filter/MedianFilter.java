package lehmann.master.thesis.mcc.tu.berlin.de.filter;

import java.util.Arrays;
import java.util.function.Function;


public class MedianFilter implements Function<Float[], Float>{

	@Override
	public Float apply(Float[] a) {
		Arrays.sort(a);
		return a[a.length / 2];
	}

}
