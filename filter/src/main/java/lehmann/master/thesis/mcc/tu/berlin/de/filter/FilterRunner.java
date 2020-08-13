package lehmann.master.thesis.mcc.tu.berlin.de.filter;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterRunner {
	
	private static Logger log = LoggerFactory.getLogger(FilterRunner.class);
	private final static String TOPIC = "t19";
    private final static String BOOTSTRAP_SERVERS = "localhost:9093";
    private final static String ZOOKEEPER_SERVERS = "localhost:2182";
	
	public static void main(String[] args) throws Exception {
		try {
			PropertyConfigurator.configure("log4j.properties");
			DataFilter dataFilter = new DataFilter(BOOTSTRAP_SERVERS, TOPIC, new MedianFilter(), 15);
			
			dataFilter.runFilter();
		}catch (Exception e) {
			log.error("Error while running", e);
		}
	}

}
