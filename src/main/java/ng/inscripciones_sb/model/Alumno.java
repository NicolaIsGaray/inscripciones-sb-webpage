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
@NoArgsConstructor
@AllArgsConstructor
public class Alumno {
    @Id
    private String id;

    private String dni;
    private String name;
    private String email;
    private Boolean hasGroup = false;
    private Boolean isAlone = false;
    private Boolean isRegistered = false;
    private List<Invitaciones> invitations = new ArrayList<>();
}
