package ng.inscripciones_sb.repository;

import ng.inscripciones_sb.model.Alumno;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AlumnoRepo extends MongoRepository<Alumno, String> {
    Optional<Alumno> findByDni(String dni);
    Optional<Alumno> deleteByDni(String dni);
}
