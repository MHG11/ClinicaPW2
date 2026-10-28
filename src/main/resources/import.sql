INSERT INTO pessoa(nome) VALUES ('Adilso');

INSERT INTO pessoa_fisica(id_pessoa, cpf) VALUES (1, '231312312');
INSERT INTO paciente(id_pessoa, telefone) VALUES (1, '19281378');


INSERT INTO pessoa(nome) VALUES ('Filho');

INSERT INTO pessoa_fisica(id_pessoa, cpf) VALUES (2, '412312441');
INSERT INTO medico(id_pessoa, crm) VALUES (2, '126218TO');


INSERT INTO consulta(data, observacao, valor, id_medico, id_paciente) VALUES ('2026-09-20', 'olhar', 345.23, 2, 1);