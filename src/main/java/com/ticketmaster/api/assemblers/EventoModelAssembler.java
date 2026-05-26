package com.ticketmaster.api.assemblers;

import com.ticketmaster.api.controller.EventoController;
import com.ticketmaster.api.dto.response.EventoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class EventoModelAssembler
        implements RepresentationModelAssembler<EventoResponse, EntityModel<EventoResponse>> {

    @Override
    public EntityModel<EventoResponse> toModel(EventoResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(EventoController.class).buscarPorId(response.getId(), "v2")).withSelfRel(),
                linkTo(methodOn(EventoController.class).listar(null, "v2", null)).withRel("eventos"),
                linkTo(methodOn(EventoController.class).deletar(response.getId())).withRel("delete"));
    }
}
