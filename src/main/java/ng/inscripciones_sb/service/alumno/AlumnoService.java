package ng.inscripciones_sb.service.alumno;

import ng.inscripciones_sb.model.Alumno;
import ng.inscripciones_sb.model.Grupos;
import ng.inscripciones_sb.model.Invitaciones;
import ng.inscripciones_sb.repository.AlumnoRepo;
import ng.inscripciones_sb.repository.GruposRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlumnoService implements IAlumno {

    @Autowired
    private AlumnoRepo alumnoRepo;

    @Autowired
    private GruposRepo gruposRepo;

    @Override
    public List<Alumno> listAlumno() {
        List<Alumno> alumnos = alumnoRepo.findAll();
        return alumnos;
    }

    @Override
    public Alumno saveAlumno(Alumno alumno) {
        Alumno registered = this.alumnoRepo.save(alumno);
        return registered;
    }

    @Override
    public Alumno searchByDni(String dni) {
        Alumno alumno = alumnoRepo.findByDni(dni).orElse(null);
        return alumno;
    }

    public Alumno addInvitacion(String id, Invitaciones invitation) {
        Alumno alumno = alumnoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        if (alumno.getInvitations() == null) {
            alumno.setInvitations(new ArrayList<>());
        }

        alumno.getInvitations().add(invitation);
        return alumnoRepo.save(alumno);
    }


    public Alumno acceptInvitacion(String alumnoId, String groupId) {
        Alumno alumno = alumnoRepo.findById(alumnoId)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        Grupos grupo = this.gruposRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        alumno.getInvitations().removeIf(inv -> inv.getGroupId().equals(groupId));

        alumno.setHasGroup(true);

        grupo.getPending().removeIf(a -> a.getId().equals(alumnoId));
        grupo.getMembers().add(alumno);

        alumnoRepo.save(alumno);
        gruposRepo.save(grupo);

        return alumno;
    }


    public Alumno rejectInvitacion(String id, String groupId) {
        Alumno alumno = alumnoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        alumno.getInvitations().removeIf(inv -> inv.getGroupId().equals(groupId));

        Grupos grupo = gruposRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (grupo.getPending() != null) {
            grupo.getPending().removeIf(a -> a.getId().equals(alumno.getId()));
        }

        gruposRepo.save(grupo);
        return alumnoRepo.save(alumno);
    }



    @Override
    public void deleteAlumno(String dni) {
        alumnoRepo.deleteByDni(dni);
    }
}
