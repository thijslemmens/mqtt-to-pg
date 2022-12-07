CREATE EXTENSION IF NOT EXISTS timescaledb;
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE metrics (
                         time TIMESTAMPTZ NOT NULL,
                         name TEXT NOT NULL,
                         value DOUBLE PRECISION NOT NULL,
                         labels hstore NULL,
                         unit text NULL,
                         UNIQUE (time, name, labels)
);

create index metrics_name on metrics using btree (name);
create index metrics_value on metrics using btree (value);
create index metrics_labels on metrics using gin (labels);
create index metrics_unit on metrics using btree (unit);

SELECT create_hypertable('metrics', 'time');

DROP MATERIALIZED VIEW if exists electricity_1min;
CREATE MATERIALIZED VIEW electricity_1min
            WITH (timescaledb.continuous)
AS
SELECT
    time_bucket('1 minute', time) AS time_1min,
    round((max(case when (labels->'tariff'='high' and labels->'direction'='in') then value else 0 end) + max(case when (labels->'tariff'='low' and labels->'direction'='in') then value else 0 end))::numeric,4) AS meter_in,
    round((max(case when (labels->'tariff'='high' and labels->'direction'='out') then value else 0 end) + max(case when (labels->'tariff'='low' and labels->'direction'='out') then value else 0 end))::numeric,4) AS meter_out
FROM
    metrics
WHERE
        name = 'electricity.meter'
GROUP BY
    time_1min
    WITH NO DATA;

SELECT add_continuous_aggregate_policy('electricity_1min',
                                       start_offset => INTERVAL '10 minutes',
                                       end_offset => INTERVAL '2 minute',
                                       schedule_interval => INTERVAL '10 minutes');

DROP MATERIALIZED VIEW if exists gas_5min;
CREATE MATERIALIZED VIEW gas_5min
            WITH (timescaledb.continuous)
AS
SELECT
    time_bucket('5 minute', time) AS time_5min,
    max(value) as gasMeter
FROM
    metrics
WHERE
        name = 'gas.meter'
GROUP BY
    time_5min
    WITH NO DATA;

SELECT add_continuous_aggregate_policy('gas_5min',
                                       start_offset => INTERVAL '1 hour',
                                       end_offset => INTERVAL '30 minutes',
                                       schedule_interval => INTERVAL '10 minutes');