package ng.inscripciones_sb.service.alumno;

import ng.inscripciones_sb.model.Alumno;
import ng.inscripciones_sb.model.Grupos;
import ng.inscripciones_sb.model.Invitaciones;
import ng.inscripciones_sb.repository.AlumnoRepo;
import ng.inscripciones_sb.repository.GruposRepo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public Page<Alumno> listPreAlumnos(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return alumnoRepo.findAll(pageable);
    }

    @Override
    public List<Alumno> uploadAlumnosExcel(MultipartFile file) throws IOException {
        List<Alumno> alumnos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell nombreCell = row.getCell(1);
                String nombre = obtenerValorCelda(nombreCell);

                Cell dniCell = row.getCell(0);
                String dni = obtenerValorCelda(dniCell);

                if (nombre != null && !nombre.isBlank() && dni != null && !dni.isBlank()) {
                    if (!alumnoRepo.existsByDni(dni)) {
                        Alumno alumno = new Alumno();
                        alumno.setName(nombre.trim());
                        alumno.setDni(dni.trim());
                        alumnos.add(alumno);
                    }
                }
            }
        }

        return alumnoRepo.saveAll(alumnos);
    }

    private String obtenerValorCelda(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC ->
                    String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    @Override
    public Alumno saveAlumno(Alumno alumno) {
        return this.alumnoRepo.save(alumno);
    }

    @Override
    public Alumno searchByDni(String dni) {
        return alumnoRepo.findByDni(dni).orElse(null);
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
