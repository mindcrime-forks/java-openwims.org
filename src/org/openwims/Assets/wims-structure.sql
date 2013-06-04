--Primary schema

CREATE TABLE tokens (representation TEXT, CONSTRAINT tokens_pkey PRIMARY KEY (representation));
CREATE TABLE senses (id TEXT, token TEXT, 
 CONSTRAINT senses_pkey PRIMARY KEY (id), 
 CONSTRAINT senses_fkey FOREIGN KEY (token) REFERENCES tokens (representation) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE structures (id int, sense TEXT, series int, dependency TEXT, governor TEXT, dependent TEXT, 
 CONSTRAINT structures_pkey PRIMARY KEY (id),
 CONSTRAINT structures_fkey FOREIGN KEY (sense) REFERENCES senses (id) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE specifications (id int, struct int, spec TEXT, expectation TEXT, 
 CONSTRAINT specifications_pkey PRIMARY KEY (id),
 CONSTRAINT specifications_fkey FOREIGN KEY (struct) REFERENCES structures (id) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE meanings (id int, sense TEXT, target TEXT, relation TEXT, wim TEXT,
 CONSTRAINT meanings_pkey PRIMARY KEY (id),
 CONSTRAINT meanings_fkey FOREIGN KEY (sense) REFERENCES senses (id) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE ontology (concept TEXT, parent TEXT, definition TEXT,
 CONSTRAINT ontology_pkey PRIMARY KEY (concept));

CREATE INDEX tokens_representation_index ON tokens (representation ASC);
CREATE INDEX senses_id_index ON senses (id ASC);
CREATE INDEX senses_token_index ON senses (token ASC);
CREATE INDEX structures_sense_index ON structures (sense ASC);
CREATE INDEX structures_id_index ON structures (id ASC);
CREATE INDEX specifications_struct_index ON specifications (struct ASC);
CREATE INDEX meanings_sense_index ON meanings (sense ASC);

--Appended to schema; not used in processing

CREATE TABLE wordnet (id int, wordnet_sense TEXT, sense TEXT, definition TEXT,
 CONSTRAINT wordnet_pkey PRIMARY KEY (id),
 CONSTRAINT wordnet_fkey FOREIGN KEY (sense) REFERENCES senses (id) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE edited (sense TEXT,
 CONSTRAINT edited_pkey PRIMARY KEY (sense));

CREATE INDEX wordnet_sense_index ON wordnet (sense ASC);

--Sample data

INSERT INTO tokens (representation) VALUES ('the');
INSERT INTO tokens (representation) VALUES ('man');
INSERT INTO tokens (representation) VALUES ('hit');
INSERT INTO tokens (representation) VALUES ('building');

INSERT INTO senses (id, token) VALUES ('@human:man-n-1', 'man');
INSERT INTO senses (id, token) VALUES ('@hit:hit-v-1', 'hit');
INSERT INTO senses (id, token) VALUES ('@hit:hit-v-2', 'hit');
INSERT INTO senses (id, token) VALUES ('@structure:building-n-1', 'building');

INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (1, '@hit:hit-v-1', 1, 'nsubj', 'SELF', 'agent');
INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (2, '@hit:hit-v-1', 1, 'dobj', 'SELF', 'theme');
INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (3, '@hit:hit-v-2', 1, 'nsubj', 'SELF', 'agent');
INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (4, '@hit:hit-v-2', 1, 'dobj', 'SELF', 'theme');
INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (5, '@hit:hit-v-2', 1, 'prep', 'SELF', 'with');
INSERT INTO structures (id, sense, series, dependency, governor, dependent) VALUES (6, '@hit:hit-v-2', 1, 'pobj', 'with', 'instrument');

INSERT INTO specifications (id, struct, spec, expectation) VALUES (1, 1, 'pos', 'NN');
INSERT INTO specifications (id, struct, spec, expectation) VALUES (2, 2, 'pos', 'NN');
INSERT INTO specifications (id, struct, spec, expectation) VALUES (3, 3, 'pos', 'NN');
INSERT INTO specifications (id, struct, spec, expectation) VALUES (4, 4, 'pos', 'NN');
INSERT INTO specifications (id, struct, spec, expectation) VALUES (5, 5, 'pos', 'IN');
INSERT INTO specifications (id, struct, spec, expectation) VALUES (6, 5, 'token', 'with');
INSERT INTO specifications (id, struct, spec, expectation) VALUES (7, 6, 'pos', 'NN');

INSERT INTO meanings (sense, target, relation, wim) VALUES ('@hit:hit-v-1', 'SELF', 'agent', 'agent');
INSERT INTO meanings (sense, target, relation, wim) VALUES ('@hit:hit-v-1', 'SELF', 'theme', 'theme');
INSERT INTO meanings (sense, target, relation, wim) VALUES ('@hit:hit-v-2', 'SELF', 'agent', 'agent');
INSERT INTO meanings (sense, target, relation, wim) VALUES ('@hit:hit-v-2', 'SELF', 'theme', 'theme');
INSERT INTO meanings (sense, target, relation, wim) VALUES ('@hit:hit-v-2', 'SELF', 'instrument', 'instrument');