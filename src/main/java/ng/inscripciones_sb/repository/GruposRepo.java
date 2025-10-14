package ng.inscripciones_sb.repository;

import ng.inscripciones_sb.model.Grupos;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GruposRepo extends MongoRepository<Grupos, String> {
    Optional<Grupos> findByLeaderDni(String dni);
}
