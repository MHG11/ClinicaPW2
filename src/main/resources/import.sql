INSERT INTO pessoa(nome) VALUES ('Adilso');

INSERT INTO pessoa_fisica(id_pessoa, cpf) VALUES (1, '231312312');
INSERT INTO paciente(id_pessoa, telefone) VALUES (1, '19281378');


INSERT INTO pessoa(nome) VALUES ('Filho');

INSERT INTO pessoa_fisica(id_pessoa, cpf) VALUES (2, '412312441');
INSERT INTO medico(id_pessoa, crm) VALUES (2, '126218TO');


INSERT INTO consulta(data, observacao, valor, id_medico, id_paciente) VALUES ('2026-09-20', 'olhar', 345.23, 2, 1);

insert into perfil (id,nome) values (1,"ADMIN");

INSERT INTO usuario (ativo,id,login,password) values (true,1,"mhg","$2a$04$A/C1FZVlB2U.CYkIAodiT.uc/sgOiKhuzQ0MBwMWfVqujrq4BLJ2i");

insert into usuario_perfil (perfil_id,usuario_id) values (1,1);