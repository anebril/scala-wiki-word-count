-- Contains create statements for all tables used in DERBY database - has to be run after creating database

-- Table with text entries - each text entry represents result of single query to text source (wiki page)
CREATE TABLE text_entries (
    id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    text_source VARCHAR(256) NOT NULL,
    query VARCHAR(256) NOT NULL,
    text LONG VARCHAR NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT query_uq UNIQUE(text_source, query)
);

-- Table with word entries - for each text entry contains list of all words and their count
CREATE TABLE words (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    text_entry_id INTEGER NOT NULL CONSTRAINT text_entry_id_fk REFERENCES text_entries(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    word VARCHAR(128) NOT NULL,
    word_count INTEGER NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT text_entry_id_word_uq UNIQUE(text_entry_id, word)
)