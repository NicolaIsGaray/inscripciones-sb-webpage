package ng.inscripciones_sb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Invitaciones {
    private String groupId;

    private String leaderName;
    private List<String> memberNames;
    private LocalDateTime sentAt;

    public Invitaciones(String id, String name, List<String> memberNames) {
    }
}

