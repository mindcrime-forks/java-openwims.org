CREATE TABLE ontology (concept TEXT, parent TEXT, definition TEXT);

CREATE TABLE words (representation TEXT, CONSTRAINT words_pkey PRIMARY KEY (representation));

CREATE TABLE senses (id TEXT, word TEXT, definition TEXT,
 CONSTRAINT senses_pkey PRIMARY KEY (id), 
 CONSTRAINT senses_fkey FOREIGN KEY (word) REFERENCES words (representation) ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE structures (id INTEGER PRIMARY KEY AUTOINCREMENT, sense TEXT, series int, label TEXT, optional BOOLEAN, 
 CONSTRAINT structures_fkey FOREIGN KEY (sense) REFERENCES senses (id) ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE dependencies (id INTEGER PRIMARY KEY AUTOINCREMENT, struct int, dependency TEXT, governor TEXT, dependent TEXT,
 CONSTRAINT dependencies_fkey FOREIGN KEY (struct) REFERENCES structures (id) ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE specifications (id INTEGER PRIMARY KEY AUTOINCREMENT, dependency int, spec TEXT, expectation TEXT, 
 CONSTRAINT specifications_fkey FOREIGN KEY (dependency) REFERENCES dependencies (id) ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE meanings (id INTEGER PRIMARY KEY AUTOINCREMENT, sense TEXT, target TEXT, relation TEXT, wim TEXT,
 CONSTRAINT meanings_fkey FOREIGN KEY (sense) REFERENCES senses (id) ON DELETE CASCADE ON UPDATE CASCADE);

CREATE TABLE edited (sense TEXT,
 CONSTRAINT edited_pkey PRIMARY KEY (sense));

CREATE INDEX meanings_sense_index ON meanings (sense ASC);
CREATE INDEX senses_id_index ON senses (id ASC);
CREATE INDEX senses_word_index ON senses (word ASC);
CREATE INDEX specifications_dependency_index ON specifications (dependency ASC);
CREATE INDEX structures_id_index ON structures (id ASC);
CREATE INDEX structures_sense_index ON structures (sense ASC);
CREATE INDEX words_representation_index ON words (representation ASC);