package ng.inscripciones_sb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Grupos {
    @Id
    private String id;

    private Alumno leader;
    private List<Alumno> members = new ArrayList<>();
    private List<Alumno> pending = new ArrayList<>();
}
