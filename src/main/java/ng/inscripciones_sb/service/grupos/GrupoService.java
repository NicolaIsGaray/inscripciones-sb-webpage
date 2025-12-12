package ng.inscripciones_sb.service.grupos;

import ng.inscripciones_sb.model.Alumno;
import ng.inscripciones_sb.model.Grupos;
import ng.inscripciones_sb.model.Invitaciones;
import ng.inscripciones_sb.repository.AlumnoRepo;
import ng.inscripciones_sb.repository.GruposRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        // Asignar nuevo número de grupo de forma segura
        Optional<Grupos> lastGroup = gruposRepo.findTopByGroupNumberIsNotNullOrderByGroupNumberDesc();
        int nextGroupNumber = lastGroup.map(g -> g.getGroupNumber() + 1).orElse(1);
        grupo.setGroupNumber(nextGroupNumber);

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
    public ByteArrayInputStream exportGruposToExcel() throws IOException {
        String[] columns = {"Número de Grupo", "Rol", "Nombre del Integrante", "DNI"};
        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            Sheet sheet = workbook.createSheet("Grupos");

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
            }

            List<Grupos> grupos = this.listGrupos();
            int rowIdx = 1;
            for (Grupos grupo : grupos) {
                // Fila para el líder
                Row leaderRow = sheet.createRow(rowIdx++);
                Integer groupNumber = grupo.getGroupNumber();
                leaderRow.createCell(0).setCellValue(groupNumber != null ? groupNumber : 0);
                leaderRow.createCell(1).setCellValue("Líder");
                leaderRow.createCell(2).setCellValue(grupo.getLeader().getName());
                leaderRow.createCell(3).setCellValue(grupo.getLeader().getDni());

                // Filas para los miembros
                if (grupo.getMembers() != null) {
                    for (Alumno miembro : grupo.getMembers()) {
                        Row memberRow = sheet.createRow(rowIdx++);
                        memberRow.createCell(0).setCellValue(groupNumber != null ? groupNumber : 0);
                        memberRow.createCell(1).setCellValue("Miembro");
                        memberRow.createCell(2).setCellValue(miembro.getName());
                        memberRow.createCell(3).setCellValue(miembro.getDni());
                    }
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    public void deleteGroup(String id) {
        this.gruposRepo.deleteById(id);
    }
}
