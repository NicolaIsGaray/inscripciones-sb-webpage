package ng.inscripciones_sb.service.grupos;

import ng.inscripciones_sb.model.Grupos;

import java.util.List;

public interface IGrupos {
    List<Grupos> listGrupos();
    Grupos createGroup(Grupos grupo);
    void deleteGroup(String id);
}