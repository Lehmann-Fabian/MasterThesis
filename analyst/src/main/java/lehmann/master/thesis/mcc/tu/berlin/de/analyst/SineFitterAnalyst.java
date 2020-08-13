package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.fitting.HarmonicCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SineFitterAnalyst implements Function<float[], List<String>> {
	
	private static Logger log = LoggerFactory.getLogger(SineFitterAnalyst.class);
	private final int Hz;
	private final double periodLength;
	private final double amplitude;
	private final double acceptedDiff;
	
	public SineFitterAnalyst(int hz, double amplitude, double periodLength, double acceptedDiff) {
		Hz = hz;
		this.periodLength = periodLength;
		this.amplitude = amplitude;
		this.acceptedDiff = acceptedDiff;
	}

	@Override
	public List<String> apply(float[] t) {
		
		HarmonicCurveFitter curveFit = HarmonicCurveFitter.create();
		
		double[] guess = {this.amplitude, (2 * Math.PI) / this.periodLength, 1};
		curveFit = curveFit.withStartPoint(guess);
		
		List<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>(t.length);
		
		for (int i = 0; i < t.length; i++) {
			points.add(new WeightedObservedPoint(1, (i - (t.length / 2)) / (double) Hz, t[i]));
		}
		
		double[] fit = curveFit.fit(points);
		
		double amplitude = Math.abs(fit[0]);
		double periodLength = Math.abs((2 * Math.PI) / fit[1]) * this.Hz;
		
		log.info(String.format("Amplitude: %.2f; PeriodLength: %.2f", amplitude, periodLength));
		
		LinkedList<String> result = new LinkedList<String>();
		
		if(!(this.amplitude * (1 - acceptedDiff) < amplitude && this.amplitude * (1 + acceptedDiff) > amplitude)) {
			result.add(String.format("Amplitude was %.5f, expected was %.5f, allowed difference is %.2f!", amplitude, this.amplitude, acceptedDiff));
		}
		if(!(this.periodLength * (1 - acceptedDiff) < periodLength && this.periodLength * (1 + acceptedDiff) > periodLength)) {
			result.add(String.format("Period length was %.5f, expected was %.5f, allowed difference is %.2f!", periodLength, this.periodLength, acceptedDiff));
		}
		
		return result;
	}

}
