insert into pessoa(pessoa_id, nome) values (1, 'Filho');
insert into pessoa(pessoa_id, nome) values (2, 'Ademar');

insert into pessoa_fisica(id_pessoa, cpf) values (1, '231312312');
insert into pessoa_fisica(id_pessoa, cpf) values (2, '412312441');

insert into paciente(id_pessoa, telefone) values (1, '19281378');
insert into medico(id_pessoa, crm) values (2, '126218TO');

insert into perfil(id, nome) values (1, "ADMIN");
insert into perfil(id, nome) values (2, "PACIENTE");
insert into perfil(id, nome) values (3, "SECRETARIA");
insert into perfil(id, nome) values (4, "MEDICO");

insert into usuario(usuario_id, ativo, login, password, pessoa_id) values (1, true, "mhg", "$2a$04$A/C1FZVlB2U.CYkIAodiT.uc/sgOiKhuzQ0MBwMWfVqujrq4BLJ2i", null);
insert into usuario(usuario_id, ativo, login, password, pessoa_id) values (2, true, "paciente", "$2a$04$6b9tJClQQ7.cjFtehV5QI.vb7GMuSJ/3xV9lclGZDhPkDlNO.Wb06", 1);

insert into usuario_perfil(perfil_id, usuario_id) values (1, 1);
insert into usuario_perfil(perfil_id, usuario_id) values (2, 2);