package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalystRunner {
	
	private static Logger log = LoggerFactory.getLogger(AnalystRunner.class);
	private static String TOPIC = "t1";
    private static String BOOTSTRAP_SERVERS = "localhost:9093";
	
	public static void main(String[] args) throws Exception {
		
		if(args.length >= 3) {
			BOOTSTRAP_SERVERS = args[0];
			TOPIC = args[1];
			String podstart = args[2];
			TOPIC = TOPIC.substring(podstart.length());
			TOPIC = "t" + (Integer.parseInt(TOPIC) + 1);
			System.out.println("Use server: " + BOOTSTRAP_SERVERS);
			System.out.println("Use topic: " + TOPIC);
		}
		
		try {
			PropertyConfigurator.configure("log4j.properties");
			DataAnalyst dataFilter = new DataAnalyst(BOOTSTRAP_SERVERS, TOPIC, new ValueFitterAnalyst(), 1);
			
			dataFilter.runAnalysis();
		}catch (Exception e) {
			log.error("Error while running", e);
			e.printStackTrace();
		}
    	
	}

}
