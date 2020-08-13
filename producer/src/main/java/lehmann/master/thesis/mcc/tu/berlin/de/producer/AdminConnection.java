package lehmann.master.thesis.mcc.tu.berlin.de.producer;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AdminConnection {
	
	private final AdminClient admin;
	private static Logger log = LoggerFactory.getLogger(AdminConnection.class);
	
	public AdminConnection(String BOOTSTRAP_SERVERS) {
		Properties config = new Properties();
	    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

	    this.admin = AdminClient.create(config);
	}
	
	public boolean hasTopic(String topic) {
		Set<String> names = getAllTopics();
		boolean result = names.contains(topic);
		log.info("Topic: " + topic + " exist == " + result);
		return result;
	}
	
	public void createTopic(String topic, int numPartitions, short replicationFactor) {
		Collection<NewTopic> newTopics = Collections.singleton(new NewTopic(topic, numPartitions, replicationFactor));
		CreateTopicsResult createTopics = admin.createTopics(newTopics);
		try {
			createTopics.all().get();
		} catch (InterruptedException e) {
			log.error("Create topic was interrupted", e);
			e.printStackTrace();
		} catch (ExecutionException e) {
			log.error("Create topic throws exception", e);
			e.printStackTrace();
		}
	}
	
	public Set<String> getAllTopics() {
		try {
			return admin.listTopics().names().get();
		} catch (InterruptedException e) {
			log.error("Get topic was interrupted", e);
			e.printStackTrace();
		} catch (ExecutionException e) {
			log.error("Get topic throws exception", e);
			e.printStackTrace();
		}
		return null;
	}

}
