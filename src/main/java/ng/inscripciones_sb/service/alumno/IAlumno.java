package ng.inscripciones_sb.service.alumno;

import ng.inscripciones_sb.model.Alumno;

import java.util.List;

public interface IAlumno {
    List<Alumno> listAlumno();
    Alumno saveAlumno(Alumno alumno);
    Alumno searchByDni(String dni);
    void deleteAlumno(String dni);
}
