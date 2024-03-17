ALTER TABLE outbox_rabbit_table
    ADD COLUMN mime_type character varying NOT NULL DEFAULT '',
    ADD COLUMN encoding character varying NOT NULL DEFAULT '',
    ADD COLUMN content_length bigint NOT NULL DEFAULT 0,
    ADD COLUMN class_id_field character varying NOT NULL DEFAULT '',
    ALTER COLUMN message TYPE bytea USING convert_to(message, 'UTF8');