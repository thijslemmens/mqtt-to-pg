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