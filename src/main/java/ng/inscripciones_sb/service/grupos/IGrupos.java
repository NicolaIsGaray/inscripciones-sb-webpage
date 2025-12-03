package ng.inscripciones_sb.service.grupos;

import ng.inscripciones_sb.model.Grupos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface IGrupos {
    List<Grupos> listGrupos();
    Grupos createGroup(Grupos grupo);
    ByteArrayInputStream exportGruposToExcel() throws IOException;
    void deleteGroup(String id);
}
