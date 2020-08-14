package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalystRunner {
	
	private static Logger log = LoggerFactory.getLogger(AnalystRunner.class);
	private final static String TOPIC = "t21";
    private final static String BOOTSTRAP_SERVERS = "localhost:9093";
    private final static String ZOOKEEPER_SERVERS = "localhost:2182";
	
	public static void main(String[] args) throws Exception {
		
		try {
			PropertyConfigurator.configure("log4j.properties");
			DataAnalyst dataFilter = new DataAnalyst(BOOTSTRAP_SERVERS, TOPIC, new SineFitterAnalyst(200, 3, 1000, 0.1), 500);
			
			dataFilter.runAnalysis();
		}catch (Exception e) {
			log.error("Error while running", e);
		}
    	
	}

}
