package ng.inscripciones_sb.service.grupos;

import ng.inscripciones_sb.model.Alumno;
import ng.inscripciones_sb.model.Grupos;
import ng.inscripciones_sb.model.Invitaciones;
import ng.inscripciones_sb.repository.AlumnoRepo;
import ng.inscripciones_sb.repository.GruposRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GrupoService implements IGrupos{
    @Autowired
    private GruposRepo gruposRepo;

    @Autowired
    private AlumnoRepo alumnoRepo;

    @Override
    public List<Grupos> listGrupos() {
        List<Grupos> grupos = this.gruposRepo.findAll();
        return grupos;
    }

    @Override
    public Grupos createGroup(Grupos grupo) {
        Alumno lider = grupo.getLeader();

        Optional<Grupos> existingGroup = gruposRepo.findByLeaderDni(lider.getDni());
        if (existingGroup.isPresent()) {
            throw new RuntimeException("El líder ya tiene un grupo creado.");
        }

        Grupos grupoGuardado = gruposRepo.save(grupo);

        lider.setHasGroup(true);
        alumnoRepo.save(lider);

        if (grupo.getPending() != null) {
            for (Alumno pendiente : grupo.getPending()) {
                pendiente.setHasGroup(false);
                alumnoRepo.save(pendiente);
            }
        }

        if (grupo.getMembers() != null) {
            for (Alumno miembro : grupo.getMembers()) {
                miembro.setHasGroup(true);
                alumnoRepo.save(miembro);
            }
        }

        return grupoGuardado;
    }

    private void enviarInvitaciones(Grupos grupo) {
        Alumno leader = grupo.getLeader();
        List<String> memberNames = grupo.getPending().stream()
                .map(Alumno::getName)
                .toList();

        for (Alumno invitado : grupo.getPending()) {
            Invitaciones inv = new Invitaciones(
                    grupo.getId(),
                    leader.getName(),
                    memberNames
            );
            invitado.getInvitations().add(inv);
            alumnoRepo.save(invitado);
        }
    }

    public void responderInvitacion(String dniAlumno, String groupId, boolean aceptar) {
        Alumno alumno = alumnoRepo.findByDni(dniAlumno)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado."));

        alumno.getInvitations().removeIf(inv -> inv.getGroupId().equals(groupId));

        if (aceptar) {
            Grupos grupo = gruposRepo.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Grupo no encontrado."));

            grupo.getMembers().add(alumno);
            alumno.setHasGroup(true);
            gruposRepo.save(grupo);
        }

        alumnoRepo.save(alumno);
    }

    @Override
    public void deleteGroup(String id) {
        this.gruposRepo.deleteById(id);
    }
}
