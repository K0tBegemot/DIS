CREATE TABLE hash_crack_task_table
(
    id         bigserial PRIMARY KEY,
    version    bigint NOT NULL DEFAULT 0,
    hash       character varying NOT NULL,
    max_length bigint NOT NULL
);

CREATE TABLE executor_service_part_table
(
    id             bigserial PRIMARY KEY,
    version        bigint NOT NULL DEFAULT 0,
    parent_task_id bigint NOT NULL,
    executor_id     bigint,
    CONSTRAINT executor_service_part_table_fk1
        FOREIGN KEY (parent_task_id)
            REFERENCES hash_crack_task_table (id)
);

CREATE TABLE diapason_part_table
(
    id      bigserial PRIMARY KEY,
    version bigint NOT NULL DEFAULT 0,
    word_number bigint NOT NULL,
    first_word bigint NOT NULL,
    parent_executor_id bigint NOT NULL,
    CONSTRAINT diapason_part_table_fk1
        FOREIGN KEY (parent_executor_id)
            REFERENCES executor_service_part_table(id)
);