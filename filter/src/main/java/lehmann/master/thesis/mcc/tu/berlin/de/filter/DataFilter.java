package lehmann.master.thesis.mcc.tu.berlin.de.filter;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lehmann.master.thesis.mcc.tu.berlin.de.filter.data.FilteredDataEntry;
import lehmann.master.thesis.mcc.tu.berlin.de.filter.data.FilteredDataEntrySerializer;
import lehmann.master.thesis.mcc.tu.berlin.de.filter.data.RawDataEntry;
import lehmann.master.thesis.mcc.tu.berlin.de.filter.data.RawDataEntryDeserializer;

public class DataFilter {
	
	private static Logger log = LoggerFactory.getLogger(DataFilter.class);
	private KafkaConsumer<Long, RawDataEntry> consumer;
	private final Properties propsConsumer;
	private final String BOOTSTRAP_SERVERS;
	private final String TOPIC;
	private final String TOPIC_OUTPUT;
	private final Function<Float[], Float> filter;
	private final int filterSize;
	private final KafkaProducer<Long, FilteredDataEntry> producer;
	private final AdminConnection zk;
	private final AtomicInteger counter;
	
	public DataFilter(String BOOTSTRAP_SERVERS, String TOPIC, Function<Float[], Float> filter, int filterSize) {
		
		assert(filterSize > 0 && filterSize % 2 == 1);
		
		this.BOOTSTRAP_SERVERS = BOOTSTRAP_SERVERS;
		this.TOPIC = TOPIC;
		this.TOPIC_OUTPUT = TOPIC + "_filtered";
		this.filter = filter;
		this.filterSize = filterSize;

		this.propsConsumer = new Properties();

        propsConsumer.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, TOPIC);
        propsConsumer.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, RawDataEntryDeserializer.class.getName());
        propsConsumer.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        propsConsumer.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        propsConsumer.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2000);
        
        propsConsumer.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 6000);
        propsConsumer.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        propsConsumer.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 6000);
        propsConsumer.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        propsConsumer.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 1500);
        propsConsumer.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 3000);

        // Create the consumer using props.
        this.consumer = new KafkaConsumer<>(propsConsumer);
        
        
        Properties propsProducer = new Properties();
        propsProducer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        propsProducer.put(ProducerConfig.CLIENT_ID_CONFIG, TOPIC_OUTPUT);
        propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  FilteredDataEntrySerializer.class.getName());
//        propsProducer.put(ProducerConfig.LINGER_MS_CONFIG, 20);
        propsProducer.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
//        propsProducer.put(ProducerConfig.BATCH_SIZE_CONFIG, 100);
        //actually time is in seconds
        propsProducer.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        //linger + request timeout
        propsProducer.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, Integer.MAX_VALUE);
//        propsProducer.put(ProducerConfig.ACKS_CONFIG, "all");
        propsProducer.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        propsProducer.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 1500);
        propsProducer.put(ProducerConfig.METADATA_MAX_IDLE_CONFIG, 5000);
        this.producer = new KafkaProducer<>(propsProducer);

        // Subscribe to the topic.
        //this.consumer.subscribe(Collections.singletonList(TOPIC));
        this.consumer.assign(Collections.singleton(new TopicPartition(this.TOPIC, 0)));
        this.zk = new AdminConnection(BOOTSTRAP_SERVERS);
        this.counter = new AtomicInteger(0);
	}
	
	/**
	 * 
	 * @return The offset to handle it correctly
	 */
	private long seekToStart(int size) {
		
		log.info("Seek to start for topic: " + TOPIC);
		
		int move = (size - 1) / 2;
		
		OffsetFinder offsetFinder = new OffsetFinder(BOOTSTRAP_SERVERS, TOPIC_OUTPUT, this.zk);
		long offset = offsetFinder.getOffset();
		
		//Create produced data topic if not exist
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
		
		//Not enough to move
		if(move > offset) {
			this.consumer.seekToBeginning(partitions);
			return 0;
		}else {
			this.consumer.seekToEnd(partitions);
			for (TopicPartition topicPartition : partitions) {
				long last = this.consumer.position(topicPartition);
				this.consumer.seek(topicPartition, Math.min(offset - move, last));
			}
			return offset - move;
		}
		
	}
	
	private void extractValues(ConsumerRecord<Long, RawDataEntry>[] input, Float[] output) {
		for (int i = 0; i < input.length; i++) {
			output[i] = input[i].value().getMeasurement();
		}
	}
	
	public void runFilter() {
		
		log.info("Run filter for topic: " + TOPIC);
		
		final int maxValues = filterSize;
		int posActual = filterSize / 2;
		
		long lastProcessed = seekToStart(maxValues);
		
		long skipUntil = lastProcessed > 0 ? lastProcessed + (maxValues - 1) / 2 : 0;
		
		Float[] data = new Float[maxValues];
		@SuppressWarnings("unchecked")
		ConsumerRecord<Long, RawDataEntry>[] bufferData = new ConsumerRecord[maxValues];
				
		log.info("Last processed record for topic " + TOPIC +  " on startup: " + lastProcessed);
		
		long highestTimestamp = 0;
		long highestOffset = lastProcessed;
		
		ArrayDeque<ConsumerRecord<Long, RawDataEntry>> buffer = new ArrayDeque<ConsumerRecord<Long, RawDataEntry>>(maxValues);
		try {
			
//			long i = 0;
			
			while(true) {
				
				
				ConsumerRecords<Long, RawDataEntry> records = this.consumer.poll(Duration.ofSeconds(1));
				
				log.info("Got " + records.count() + " for topic: " + TOPIC);
				
				if(records.isEmpty()) {
					
					try {
						consumer.unsubscribe();
						TopicPartition topicPartition = new TopicPartition(this.TOPIC, 0);
						this.consumer.assign(Collections.singleton(topicPartition));
						this.consumer.seekToEnd(Collections.singleton(topicPartition));
						long last = this.consumer.position(topicPartition);
						if(highestOffset < 0) {
							this.consumer.seekToBeginning(Collections.singleton(topicPartition));
						}else {
							this.consumer.seek(topicPartition, Math.min(highestOffset, last));
						}
					}catch (Exception e) {
						log.error("While fetching partitions...", e);
					}
					
				}
				
				for (ConsumerRecord<Long, RawDataEntry> consumerRecord : records) {
					
					if(consumerRecord.value().getTimestamp() >= highestTimestamp && consumerRecord.offset() > highestOffset) {
						
//						i++;
						
						if(consumerRecord.offset() != highestOffset + 1) {
							log.error(String.format("Potentially skipped one or more records, current record with offset: %d and timestamp %d but highest timestamp was %d and highest offset was %d", 
									consumerRecord.offset(), consumerRecord.value().getTimestamp(), highestTimestamp, highestOffset));
						}
						
						highestTimestamp = consumerRecord.value().getTimestamp();
						highestOffset = consumerRecord.offset();
						
						if(buffer.size() == maxValues) buffer.poll();
						
						buffer.add(consumerRecord);
						
						FilteredDataEntry output = null;
						long currentOffset = -1;
						
						if(buffer.size() <= maxValues / 2 && consumerRecord.offset() > skipUntil) {
							
							output = new FilteredDataEntry(consumerRecord.offset(), consumerRecord.value().getTimestamp(), consumerRecord.value().getMeasurement());
							currentOffset = consumerRecord.offset();
							
						}else if(buffer.size() == maxValues) {
							
							ConsumerRecord<Long, RawDataEntry>[] array = buffer.toArray(bufferData);
							
							ConsumerRecord<Long, RawDataEntry> current = array[posActual];
							currentOffset = current.offset();
							
							extractValues(array, data);
							
							float filteredValue = filter.apply(data);
							
							output = new FilteredDataEntry(current.offset(), current.value().getTimestamp(), filteredValue);
							
							
						}
						
						//Push
						if(output != null && currentOffset > lastProcessed) {
							lastProcessed = currentOffset;
							
							final ProducerRecord<Long, FilteredDataEntry> record = new ProducerRecord<>(TOPIC_OUTPUT, output);
				    		producer.send(record, (metadata, exception) -> {
				    			if(exception != null) {
				    				log.error(exception.getMessage());
//				    				if(exception instanceof TimeoutException) {
				    					//There seems to be no connection, Kubernetes will reschedule a new service
//				    					System.exit(100);
//				    				}
				    			}else if(this.counter.incrementAndGet() % 200 == 0) {
				    					log.info("Send message successfully " + metadata.offset());
				    			}
				    			
				    		});
				    		
				    		//Flush at least all 10 values
				    		//if(i % (10) == 0) producer.flush();
							
//							log.info(String.format("Push record to topic: " + TOPIC_OUTPUT + " : o=%d, ts=%d, m=%f", 
//									output.getOffset(), output.getTimestamp(), output.getMeasurement()));
						}
						
					} else {
						log.error(String.format("Received message with offset: %d and timestamp %d but highest timestamp was %d and highest offset was %d", 
								consumerRecord.offset(), consumerRecord.value().getTimestamp(), highestTimestamp, highestOffset));
					}
					
				}
				
			}
		} finally {
	        producer.flush();
	        producer.close();
	    }
		
	}

}
