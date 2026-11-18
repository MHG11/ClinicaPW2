# ClinicaPW2

## Agenda e agendamentos

Médicos, secretaria e administradores podem cadastrar horários em **Disponibilizar horário**. A tela **Horários disponíveis** mostra somente horários livres; pacientes autenticados podem reservar para si, enquanto secretaria e administradores selecionam o paciente.

Em **Agendamentos**, a clínica pode confirmar o comparecimento. A confirmação cria a `Consulta` vinculada ao `Agendamento` e altera sua situação para `REALIZADO`. Agendar, por si só, não cria consulta. O cancelamento de um agendamento pendente altera sua situação para `CANCELADO` e libera o horário novamente.

O médico responsável pode informar ou atualizar o valor de um agendamento pendente. Esse valor é copiado para a consulta quando a presença é confirmada.

O cadastro de horários impede duplicidade por médico/data/hora. A reserva bloqueia a linha da agenda durante a transação e somente aceita horários `DISPONIVEL`, evitando que duas requisições reservem o mesmo horário.
