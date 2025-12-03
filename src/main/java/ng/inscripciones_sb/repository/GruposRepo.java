package ng.inscripciones_sb.repository;

import ng.inscripciones_sb.model.Grupos;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GruposRepo extends MongoRepository<Grupos, String> {
    Optional<Grupos> findByLeaderDni(String dni);

    // Busca el grupo con el número más alto, ignorando los que no tienen número.
    Optional<Grupos> findTopByGroupNumberIsNotNullOrderByGroupNumberDesc();

    // Busca todos los grupos que aún no tienen un número asignado.
    List<Grupos> findByGroupNumberIsNull();
}
