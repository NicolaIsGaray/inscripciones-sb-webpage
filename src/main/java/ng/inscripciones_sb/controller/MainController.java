package ng.inscripciones_sb.controller;

import jakarta.servlet.http.HttpServletResponse;
import ng.inscripciones_sb.model.Alumno;
import ng.inscripciones_sb.model.Grupos;
import ng.inscripciones_sb.model.Invitaciones;
import ng.inscripciones_sb.service.alumno.AlumnoService;
import ng.inscripciones_sb.service.grupos.GrupoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/")
public class MainController {
    private Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    AlumnoService alumnoService;

    @Autowired
    GrupoService grupoService;

    @GetMapping("/alumnos")
    public List<Alumno> getAlumnos() {
        List<Alumno> alumnoList = this.alumnoService.listAlumno();

        if (!alumnoList.isEmpty()) {
            logger.info("\n Alumnos Registrados:\n");
            alumnoList.forEach(alumno -> logger.info(alumno.toString() + "\n"));
        } else {
            logger.info("\n No hay alumnos registrados.\n");
        }

        return alumnoList;
    }

    @GetMapping("/alumnos/{dni}")
    public Alumno getAlumnoByDni(@PathVariable String dni) {
        Alumno alumno = this.alumnoService.searchByDni(dni);

        if (alumno == null) {
            logger.info("\nAlumno no encontrado.\n");
        }

        return alumno;
    }

    @PostMapping("/registrar-alumno")
    public Alumno saveAlumno(@RequestBody Alumno alumno) {
        Alumno registered = this.alumnoService.saveAlumno(alumno);

        if (registered != null) {
            logger.info("\nAlumno registrado exitosamente.\n");
            logger.info("\n" + registered.toString() + "\n");
        } else {
            logger.info("\nNo se ha podido registrar al alumno.\n");
        }

        return registered;
    }

    @PutMapping("/editar-alumno/{dni}")
    public ResponseEntity<Alumno> updateAlumno(@RequestBody Alumno alumno, @PathVariable String dni) {
        Alumno toUpdate = this.alumnoService.searchByDni(dni);

        toUpdate.setDni(alumno.getDni());
        toUpdate.setName(alumno.getName());
        toUpdate.setEmail(alumno.getEmail());
        toUpdate.setIsAlone(alumno.getIsAlone());
        toUpdate.setHasGroup(alumno.getHasGroup());
        toUpdate.setIsRegistered(true);

        this.alumnoService.saveAlumno(toUpdate);

        return ResponseEntity.ok(toUpdate);
    }

    @GetMapping("/grupos")
    public List<Grupos> getGrupos() {
        List<Grupos> grupos = this.grupoService.listGrupos();

        if (!grupos.isEmpty()) {
            logger.info("\n Alumnos Registrados:\n");
            grupos.forEach(grupo -> logger.info(grupo.toString() + "\n"));
        } else {
            logger.info("\nNo hay grupos registrados.\n");
        }

        return grupos;
    }

    @PostMapping("/crear-grupo")
    public Grupos createGroup(@RequestBody Grupos grupo) {
        Grupos createdGroup = this.grupoService.createGroup(grupo);

        if (createdGroup != null) {
            logger.info("\nGrupo creado exitosamente.\n");
            logger.info("\n" + createdGroup.toString() + "\n");
        } else {
            logger.info("\nNo se ha podido crear el grupo.");
        }

        return createdGroup;
    }

    @PostMapping("/{id}/invitaciones")
    public ResponseEntity<Alumno> addInvitacion(
            @PathVariable String id,
            @RequestBody Invitaciones invitation
    ) {
        Alumno updated = alumnoService.addInvitacion(id, invitation);
        return ResponseEntity.ok(updated);
    }


    @PostMapping("/{id}/invitaciones/{groupId}/accept")
    public ResponseEntity<Alumno> acceptInvitacion(@PathVariable String id, @PathVariable String groupId) {
        Alumno updated = alumnoService.acceptInvitacion(id, groupId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/invitaciones/{groupId}/reject")
    public ResponseEntity<Alumno> rejectInvitation(@PathVariable String id, @PathVariable String groupId) {
        Alumno updated = alumnoService.rejectInvitacion(id, groupId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/descargar-pdfs")
    public void descargarPDFs(HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=documentación-inscripciones-sb-2026.zip");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            String[] archivos = {"FICHA DE DIFUSIÓN DE IMAGEN 2026.pdf", "FICHA DE INSCRIPCIÓN 2026.pdf", "FICHA DE SALUD 2026.pdf"};

            for (String nombre : archivos) {
                InputStream inputStream = getClass().getResourceAsStream("/pdfs/" + nombre);
                if (inputStream != null) {
                    zipOut.putNextEntry(new ZipEntry(nombre));
                    inputStream.transferTo(zipOut);
                    zipOut.closeEntry();
                }
            }
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getPreAlumnoList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Alumno> alumnosPage = alumnoService.listPreAlumnos(page, size);

        Map<String, Object> response = new HashMap<>();

        response.put("alumnos", alumnosPage.getContent());
        response.put("totalAlumnos", alumnosPage.getTotalElements());
        response.put("totalPages", alumnosPage.getTotalPages());
        response.put("currentPage", alumnosPage.getNumber());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcel(@RequestParam("file")MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body("Debe ser un archivo Excel");
        }

        try {
            List<Alumno> alumnos = alumnoService.uploadAlumnosExcel(file);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Archivo procesado correctamente.",
                    "alumnosCreados", alumnos.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    "Error procesando el archivo: " + e.getMessage()
            );
        }
    }

}
