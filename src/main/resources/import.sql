insert into pessoa(pessoa_id, nome) values (1, 'Filho');
insert into pessoa(pessoa_id, nome) values (2, 'Ademar');
insert into pessoa(pessoa_id, nome) values (3, 'Julio');
insert into pessoa(pessoa_id, nome) values (4, 'Vanessa');

insert into pessoa_fisica(id_pessoa, cpf) values (1, '231312312');
insert into pessoa_fisica(id_pessoa, cpf) values (2, '412312441');
insert into pessoa_fisica(id_pessoa, cpf) values (3, '123123213');
insert into pessoa_fisica(id_pessoa, cpf) values (4, '435345433');

insert into paciente(id_pessoa, telefone) values (1, '19281378');
insert into paciente(id_pessoa, telefone) values (3, '19281378');
insert into paciente(id_pessoa, telefone) values (4, '56788765');
insert into medico(id_pessoa, crm) values (2, '126218TO');

insert into perfil(id, nome) values (1, "ADMIN");
insert into perfil(id, nome) values (2, "PACIENTE");
insert into perfil(id, nome) values (3, "SECRETARIA");
insert into perfil(id, nome) values (4, "MEDICO");

insert into usuario(usuario_id, ativo, login, password, pessoa_id) values (1, true, "mhg", "$2a$04$A/C1FZVlB2U.CYkIAodiT.uc/sgOiKhuzQ0MBwMWfVqujrq4BLJ2i", null);
insert into usuario(usuario_id, ativo, login, password, pessoa_id) values (2, true, "paciente", "$2a$04$A/C1FZVlB2U.CYkIAodiT.uc/sgOiKhuzQ0MBwMWfVqujrq4BLJ2i", 1);
insert into usuario(usuario_id, ativo, login, password, pessoa_id) values (3, true, "vanessa", "$2a$04$A/C1FZVlB2U.CYkIAodiT.uc/sgOiKhuzQ0MBwMWfVqujrq4BLJ2i", 4);
insert into usuario(usuario_id, ativo, login, password, pessoa_id) values (4, true, "julio", "$2a$04$A/C1FZVlB2U.CYkIAodiT.uc/sgOiKhuzQ0MBwMWfVqujrq4BLJ2i", 3);

insert into consulta (valor,data,id_medico,id_paciente,observacao) values ("1233",null,2,3,"seila");
insert into consulta (valor,data,id_medico,id_paciente,observacao) values ("1233",null,2,4,"seila");

insert into usuario_perfil(perfil_id, usuario_id) values (1, 1);
insert into usuario_perfil(perfil_id, usuario_id) values (2, 2);
insert into usuario_perfil(perfil_id, usuario_id) values (2, 3);
insert into usuario_perfil(perfil_id, usuario_id) values (2, 4);
