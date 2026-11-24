# ClinicaPW2

## Agenda e agendamentos

Médicos, secretaria e administradores podem cadastrar horários em **Disponibilizar horário**. O formulário gera blocos automaticamente no período e nos dias selecionados, conforme a faixa de atendimento e duração da consulta (30 minutos por padrão). Os blocos partem exatamente do horário inicial informado; horários duplicados são ignorados. Na tela **Horários disponíveis**, pacientes escolhem e reservam seus próprios horários; secretaria e administração também podem registrar uma reserva para um paciente.

Os formulários de cadastro de médico e paciente mantêm a opção de vincular um usuário existente ainda não associado a uma pessoa.

No cadastro público, pacientes informam seus dados pessoais e credenciais; o sistema cria e vincula `Usuario` e `Paciente` com o perfil `PACIENTE`, liberando o acesso imediatamente. Quem seleciona **Sou médico** informa CRM e especialidade; a conta e o cadastro médico são criados juntos, mas ficam inativos e pendentes até um administrador aprovar ou rejeitar a solicitação em **Solicitações de médicos**.

Em **Agendamentos**, a clínica pode confirmar o comparecimento. A confirmação cria a `Consulta` vinculada ao `Agendamento` e altera sua situação para `REALIZADO`. Agendar, por si só, não cria consulta. O cancelamento de um agendamento pendente altera sua situação para `CANCELADO` e libera o horário novamente.

O médico responsável pode informar ou atualizar o valor de um agendamento pendente. Esse valor é copiado para a consulta quando a presença é confirmada.

O cadastro de horários impede duplicidade por médico/data/hora. A reserva bloqueia a linha da agenda durante a transação e somente aceita horários `DISPONIVEL`, evitando que duas requisições reservem o mesmo horário.
