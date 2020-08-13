package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import java.util.Random;
import java.util.function.Function;

public class SineCurveGenerator implements Function<Long, Float>{
	
	private int periodLength;
	private int nextPeriodLength;
	private double amplitude;
	private double nextAmplitude;
	private long lastPeriodEnd = 0;
	private Random random = new Random(1000);
	
	public SineCurveGenerator(int periodLength, double amplitude) {
		this.periodLength = periodLength;
		this.nextPeriodLength = periodLength;
		this.amplitude = amplitude;
		this.nextAmplitude = amplitude;
	}
	
	public void setPeriodLength(int periodLength) {
		this.nextPeriodLength = periodLength;
	}
	
	public void setNextAmplitude(double amplitude) {
		this.nextAmplitude = amplitude;
	}

	@Override
	public Float apply(Long x) {
		
		long pos = (x - lastPeriodEnd) % periodLength;
		
		
		if(pos == 0) {
			this.periodLength = nextPeriodLength;
			this.amplitude = nextAmplitude;
			this.lastPeriodEnd = x;
		}
		
		double x_value = (pos / (double) periodLength) * (2 * Math.PI);
		
		double value = Math.sin(x_value) * this.amplitude;
		double noise = random.nextGaussian() * 0.1;
		
		return (float) (value + noise);
	}
	
}
