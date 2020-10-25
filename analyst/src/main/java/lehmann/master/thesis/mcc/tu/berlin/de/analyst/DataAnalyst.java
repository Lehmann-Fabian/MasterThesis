package lehmann.master.thesis.mcc.tu.berlin.de.analyst;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.FilteredDataEntry;
import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.FilteredDataEntryDeserializer;
import lehmann.master.thesis.mcc.tu.berlin.de.analyst.data.Warning;

public class DataAnalyst {
	
	private static Logger log = LoggerFactory.getLogger(DataAnalyst.class);
	private KafkaConsumer<Long, FilteredDataEntry> consumer;
	private final Properties propsConsumer;
	private final String TOPIC;
	private final String TOPIC_OUTPUT;
	private final Function<float[], List<String>> analyst;
	private final int analyseSize;
	private final KafkaProducer<Long, String> producer;
	private final AdminConnection zk;
	private final String BOOTSTRAP_SERVERS;
	private final AtomicInteger counter;
	
	public DataAnalyst(String BOOTSTRAP_SERVERS, String TOPIC, Function<float[], List<String>> filter, int analyseSize) {
		
		this.TOPIC = TOPIC + "_filtered";
		this.TOPIC_OUTPUT = TOPIC + "_warnings";
		this.analyst = filter;
		this.analyseSize = analyseSize;
		this.BOOTSTRAP_SERVERS = BOOTSTRAP_SERVERS;

		this.propsConsumer = new Properties();
		
		propsConsumer.put(ConsumerConfig.CLIENT_ID_CONFIG, "Data-Analyst-Consumer" + new Random().nextInt());

        propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, this.TOPIC);
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, FilteredDataEntryDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        propsConsumer.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2000);
        
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
        
        
        Properties propsProducer = new Properties();
        
        propsProducer.put(ProducerConfig.CLIENT_ID_CONFIG, "Data-Analyst-Producer" + new Random().nextInt());
        
        propsProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        propsProducer.put(ProducerConfig.CLIENT_ID_CONFIG, TOPIC_OUTPUT);
        propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  StringSerializer.class.getName());
//        propsProducer.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        propsProducer.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
//        propsProducer.put(ProducerConfig.BATCH_SIZE_CONFIG, 100);
        //actually time is in seconds
        propsProducer.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        //linger + request timeout
        propsProducer.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, Integer.MAX_VALUE);
        propsProducer.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 1500);
        propsProducer.put(ProducerConfig.METADATA_MAX_IDLE_CONFIG, 5000);
        propsProducer.put(ProducerConfig.ACKS_CONFIG, "all");
        propsProducer.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        propsProducer.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 2000);
        
        propsProducer.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 10);
        propsProducer.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 10);
        propsProducer.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 200);
        
        propsProducer.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 2000);
        propsProducer.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        
        propsProducer.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 5000);
        
        
        this.producer = new KafkaProducer<>(propsProducer);

        // Subscribe to the topic.
        this.consumer.assign(Collections.singleton(new TopicPartition(this.TOPIC, 0)));//.subscribe(Collections.singletonList(this.TOPIC));
        
        this.zk = new AdminConnection(BOOTSTRAP_SERVERS);
        this.counter = new AtomicInteger(0);

	}
	
	private void extractValues(ConsumerRecord<Long, FilteredDataEntry>[] input, float[] output) {
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i].value().getMeasurement();
		}
	}
	
	
	/**
	 * 
	 * @return The offset to handle it correctly
	 */
	private long seekToStart() {
		
		log.info("Seek to start for topic: " + TOPIC);
		
		OffsetFinder offsetFinder = new OffsetFinder(BOOTSTRAP_SERVERS, TOPIC_OUTPUT, this.zk);
		long offset = offsetFinder.getOffset();
		
		//Create filtered topic if not exist
		if(!zk.hasTopic(TOPIC)) {
			zk.createTopic(TOPIC, 1, (short) 3);
			return -1;
		}
		
		log.info("New offset = " + offset + " for topic: " + TOPIC);
		
		int maxTrials = 10;
		Set<TopicPartition> partitions;
		do{
			this.consumer.poll(Duration.ofMillis(1000));
			partitions = this.consumer.assignment();
		}while(partitions.isEmpty() && --maxTrials > 0);
		
		if(partitions.isEmpty()) throw new IllegalStateException("Cannot connect to topic: " + TOPIC);
		else if(partitions.size() != 1) throw new IllegalStateException("Topic has more than one partition");
		
		if(offset < 0) {
			this.consumer.seekToBeginning(partitions);
		}else {
			this.consumer.seekToEnd(partitions);
			for (TopicPartition topicPartition : partitions) {
				long last = this.consumer.position(topicPartition);
				this.consumer.seek(topicPartition, Math.min(offset, last));
			}
			return offset;
		}
		
		return offset;
	}
	
	public void runAnalysis() {
		
		log.info("Run analysis for topic: " + TOPIC);
		
		//TODO replica to 3
		if(!zk.hasTopic(TOPIC_OUTPUT)) {
			zk.createTopic(TOPIC_OUTPUT, 1, (short) 3);
		}

		
		final int maxValues = analyseSize;
		
		float[] data = new float[maxValues];
		
		@SuppressWarnings("unchecked")
		ConsumerRecord<Long, FilteredDataEntry>[] bufferData = new ConsumerRecord[maxValues];
				
		long highestTimestamp = 0;
		long highestOffset = seekToStart();
		zk.close();
		//Change to last offset written
		int currentIndex = 0;
		
		try {
			while(true) {
				
				ConsumerRecords<Long, FilteredDataEntry> records = this.consumer.poll(Duration.ofSeconds(1));
				
				log.info("Got " + records.count() + " for topic: " + TOPIC);
				
//				if(records.isEmpty()) {
//					long last = -1;
//					TopicPartition topicPartition = new TopicPartition(this.TOPIC, 0);
//					try {
//						consumer.unsubscribe();
//						this.consumer.assign(Collections.singleton(topicPartition));
//						this.consumer.seekToEnd(Collections.singleton(topicPartition));
//						last = this.consumer.position(topicPartition);
//						if(highestOffset < 0) {
//							this.consumer.seekToBeginning(Collections.singleton(topicPartition));
//						}else {
//							this.consumer.seek(topicPartition, Math.min(highestOffset, last));
//						}
//					}catch (Exception e) {
//						log.error("While fetching partitions for topic " + TOPIC + "...", e);
//						
//					}
//					
//				}
				
				for (ConsumerRecord<Long, FilteredDataEntry> consumerRecord : records) {
					
					if(consumerRecord.value().getTimestamp() >= highestTimestamp && consumerRecord.offset() > highestOffset) {
						
						highestTimestamp = consumerRecord.value().getTimestamp();
						highestOffset = consumerRecord.offset();
						
						bufferData[currentIndex++] = consumerRecord;
						
						
						//Analyse and Push
						if(currentIndex == maxValues) {
							
							extractValues(bufferData, data);
//							log.info("Offset: " + bufferData[0].offset() + " value: " + data[0]);
							List<String> warnings = analyst.apply(data);
//							log.info(warnings.size() + " warnings");
							for (String warningText : warnings) {
								
								Warning warning = new Warning(warningText, bufferData[0].offset(), bufferData[maxValues - 1].offset(), data, bufferData[0].value().getTimestamp(), bufferData[maxValues - 1].value().getTimestamp());
								
								ObjectMapper objectMapper = new ObjectMapper();
								String warningJSON;
								try {
									warningJSON = objectMapper.writeValueAsString(warning);
								} catch (JsonProcessingException e) {
									warningJSON = warningText;
									e.printStackTrace();
								}
								
								final ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC_OUTPUT, warningJSON);
					    		producer.send(record, (metadata, exception) -> {
					    			if(exception != null) {
					    				log.error(exception.getMessage());
					    			}else if(this.counter.incrementAndGet() % 200 == 0) {
					    					log.info("Send message successfully " + metadata.offset());
					    			}
					    			
					    		});
//					    		log.info("Send warning: " + record + " to topic: " + TOPIC_OUTPUT);
							}
//							if(!warnings.isEmpty()) producer.flush();
							
							currentIndex = 0;
							
							//Commit processed data
//							OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(bufferData[maxValues - 1].offset());
//							Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
//							
//							for(TopicPartition partition : consumer.assignment()) {
//								offsets.put(partition, offsetAndMetadata);
//							}
//							
//							consumer.commitAsync(offsets, (a,b) -> {if (b != null) log.error("Problem while sending offsets", b.getCause());});
						}
						
					}
					
				}
				
			}
		} finally {
	        producer.flush();
	        producer.close();
	    }
		
	}

}
