package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueFitterAnalyst implements Function<float[], List<String>> {
	
	private static Logger log = LoggerFactory.getLogger(ValueFitterAnalyst.class);
	
	public ValueFitterAnalyst() {
	}

	@Override
	public List<String> apply(float[] t) {
		
		LinkedList<String> result = new LinkedList<String>();
		
		for (float f : t) {
			System.out.println(f);
			if(f > 1.0) result.add("Was " + f + " expected a value between 0 and 1.");
			return result;
		}
		
		return result;
	}

}
