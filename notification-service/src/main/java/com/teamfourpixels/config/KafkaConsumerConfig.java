package com.teamfourpixels.config;

import com.teamfourpixels.dto.BudgetLimitEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BudgetLimitEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, BudgetLimitEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, BudgetLimitEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}