CREATE TABLE IF NOT EXISTS smartbudget_analytics.analytics_events (
    eventId String,
    eventType String,
    userId Int64,
    timestamp DateTime64(3),
    platform String,
    payload String,
    event_date Date DEFAULT toDate(timestamp)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_date)
ORDER BY (event_date, eventType, userId);

CREATE TABLE IF NOT EXISTS smartbudget_analytics.events_queue (
    eventId String,
    eventType String,
    userId Int64,
    timestamp Array(UInt32),
    platform String,
    payload String
) ENGINE = Kafka
SETTINGS
    kafka_broker_list = 'kafka:29092',
    kafka_topic_list = 'analytics-events',
    kafka_group_name = 'clickhouse_analytics_group_init',
    kafka_format = 'JSONEachRow',
    kafka_skip_broken_messages = 1;

CREATE MATERIALIZED VIEW IF NOT EXISTS smartbudget_analytics.events_mv TO smartbudget_analytics.analytics_events AS
SELECT
    eventId, eventType, userId,
    makeDateTime(toInt32(timestamp[1]), toInt32(timestamp[2]), toInt32(timestamp[3]),
                 toInt32(timestamp[4]), toInt32(timestamp[5]), toInt32(timestamp[6])) AS timestamp,
    platform, payload
FROM smartbudget_analytics.events_queue;