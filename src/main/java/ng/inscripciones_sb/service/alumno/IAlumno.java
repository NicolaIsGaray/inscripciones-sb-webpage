package ng.inscripciones_sb.service.alumno;

import ng.inscripciones_sb.model.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IAlumno {
    List<Alumno> listAlumno();
    Page<Alumno> listPreAlumnos(int page, int size);
    List<Alumno> uploadAlumnosExcel(MultipartFile file) throws IOException;
    Alumno saveAlumno(Alumno alumno);
    Alumno searchByDni(String dni);
    void deleteAlumno(String dni);
}
