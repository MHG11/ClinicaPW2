package com.example.hospital.services;

import com.example.hospital.model.Medico;
import com.example.hospital.model.Paciente;
import com.example.hospital.model.Perfil;
import com.example.hospital.model.StatusCadastroMedico;
import com.example.hospital.model.Usuario;
import com.example.hospital.repository.PerfilRepository;
import com.example.hospital.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroUsuarioServiceTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CadastroUsuarioService service;

    @Test
    void cadastraPacienteComPerfilAtivoEAssociado() {
        when(usuarioRepository.existsByLogin("ana")).thenReturn(false);
        when(perfilRepository.findByNome("PACIENTE")).thenReturn(Optional.of(new Perfil("PACIENTE")));
        when(passwordEncoder.encode("senha123")).thenReturn("hash");

        service.cadastrarPaciente(" ana ", "senha123", " Ana ", " 123 ", " 555 ");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuario = captor.getValue();
        assertEquals("ana", usuario.getLogin());
        assertTrue(usuario.isAtivo());
        assertEquals("PACIENTE", usuario.getPerfis().get(0).getNome());
        Paciente paciente = assertInstanceOf(Paciente.class, usuario.getPessoa());
        assertEquals("Ana", paciente.getNome());
        assertEquals("123", paciente.getCpf());
        assertEquals("555", paciente.getTelefone());
    }

    @Test
    void solicitaCadastroMedicoInativoAguardandoAprovacao() {
        when(usuarioRepository.existsByLogin("drana")).thenReturn(false);
        when(perfilRepository.findByNome("MEDICO")).thenReturn(Optional.of(new Perfil("MEDICO")));
        when(passwordEncoder.encode("senha123")).thenReturn("hash");

        service.solicitarCadastroMedico("drana", "senha123", "Ana", "123", "CRM-1", "Clínica");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuario = captor.getValue();
        assertFalse(usuario.isAtivo());
        assertEquals(StatusCadastroMedico.PENDENTE, usuario.getStatusCadastroMedico());
        assertEquals("MEDICO", usuario.getPerfis().get(0).getNome());
        Medico medico = assertInstanceOf(Medico.class, usuario.getPessoa());
        assertEquals("CRM-1", medico.getCrm());
        assertEquals("Clínica", medico.getEspecialidade());
    }

    @Test
    void aprovarMedicoPendenteLiberaAcesso() {
        Usuario usuario = new Usuario("drana", "hash");
        usuario.setPessoa(new Medico());
        usuario.setAtivo(false);
        usuario.setStatusCadastroMedico(StatusCadastroMedico.PENDENTE);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        when(perfilRepository.findByNome("MEDICO")).thenReturn(Optional.of(new Perfil("MEDICO")));

        service.aprovarMedico(7L);

        assertTrue(usuario.isAtivo());
        assertEquals(StatusCadastroMedico.APROVADO, usuario.getStatusCadastroMedico());
        assertEquals("MEDICO", usuario.getPerfis().get(0).getNome());
    }
}
