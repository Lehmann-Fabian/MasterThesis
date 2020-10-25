package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.Warning;
import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.WarningDeserializer;

public class OffsetFinder {
	
	private static Logger log = LoggerFactory.getLogger(OffsetFinder.class);
	private final Consumer<Long, Warning> consumer;
	private final String topic;
	private AdminConnection zk;
	
	public OffsetFinder(String BOOTSTRAP_SERVERS, String topic, AdminConnection zk) {

		this.topic = topic;
		this.zk = zk;
		
		final Properties propsConsumer = new Properties();
		propsConsumer.put(ConsumerConfig.CLIENT_ID_CONFIG, "Data-Analyst-Offset-Finder-Consumer" + new Random().nextInt());
        propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        //Always read from beginning
//        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, topic + Math.random());
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WarningDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        propsConsumer.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        propsConsumer.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 1500);
        
        propsConsumer.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 6000);
        propsConsumer.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        propsConsumer.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 6000);
        propsConsumer.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        propsConsumer.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 1500);
        propsConsumer.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 5000);
        
        
        propsConsumer.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 10);
        propsConsumer.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 10);
        propsConsumer.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 200);
        
        propsConsumer.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 2000);
        
        propsConsumer.put(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 5000);

        // Create the consumer using props.
        this.consumer = new KafkaConsumer<>(propsConsumer);

        // Subscribe to the topic.
//        this.consumer.subscribe(Collections.singletonList(topic));
        this.consumer.assign(Collections.singleton(new TopicPartition(topic, 0)));
	}
	
	/**
	 * 
	 * @return -1 if nothing was written already
	 */
	public long getOffset() {
		if(!zk.hasTopic(topic)) {
			//TODO replica
			zk.createTopic(topic, 1, (short) 3);
			return -1;
		}
		
		int maxTrials = 10;
		Set<TopicPartition> partitions;
		do{
			this.consumer.poll(Duration.ofMillis(1000));
			partitions = this.consumer.assignment();
		}while(partitions.isEmpty() && --maxTrials > 0);
		
		if(partitions.isEmpty()) throw new IllegalStateException("Cannot connect to topic: " + topic);
		else if(partitions.size() != 1) throw new IllegalArgumentException("Topic has more than one partition");
		
		this.consumer.seekToEnd(partitions);
		for (TopicPartition partition : partitions) {
			long pos = this.consumer.position(partition);
			if(pos == 0) {
				return -1;
			}else {
				this.consumer.seek(partition, this.consumer.position(partition) - 1);
			}
			
			maxTrials = 10;
			ConsumerRecords<Long, Warning> records;
			
			do{
				records = this.consumer.poll(Duration.ofMillis(1000));
			}while(records.isEmpty() && --maxTrials > 0);
			
			if(records.isEmpty()) {
				throw new IllegalStateException("Didn't receive a record for topic: " + topic);
			}
			
			log.info("Got " + records.count() + " for topic: " + topic);
			
			for (ConsumerRecord<Long, Warning> record : records) {
				log.info("Got following record: " + record.value().toString() + " for topic: "+ topic);
				return record.value().getEndOffset();
			}
		}
		throw new IllegalStateException("Something happened");
	}
	
	public void close() {
		new Thread(() -> this.consumer.close()).start();
	}

}
