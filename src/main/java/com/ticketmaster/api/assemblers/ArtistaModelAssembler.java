package com.ticketmaster.api.assemblers;

import com.ticketmaster.api.controller.ArtistaController;
import com.ticketmaster.api.dto.response.ArtistaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ArtistaModelAssembler
        implements RepresentationModelAssembler<ArtistaResponse, EntityModel<ArtistaResponse>> {

    @Override
    public EntityModel<ArtistaResponse> toModel(ArtistaResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ArtistaController.class).buscarPorId(response.getId())).withSelfRel(),
                linkTo(methodOn(ArtistaController.class).listar(null, null)).withRel("artistas"),
                linkTo(methodOn(ArtistaController.class).deletar(response.getId())).withRel("delete"));
    }
}
