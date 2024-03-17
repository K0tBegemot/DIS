ALTER TABLE hash_crack_task_table
    ADD COLUMN status character varying;
CREATE TABLE hash_crack_task_data_table(
    id bigserial PRIMARY KEY,
    version bigint NOT NULL DEFAULT 0,
    parent_task_id bigint NOT NULL,
    data character varying NOT NULL,
    CONSTRAINT hash_crack_task_data_table_fk1
        FOREIGN KEY (parent_task_id)
            REFERENCES hash_crack_task_table(id)
);
CREATE TABLE outbox_rabbit_table(
    id bigserial PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    exchange_name character varying NOT NULL,
    routing_key character varying NOT NULL,
    message character varying NOT NULL
);