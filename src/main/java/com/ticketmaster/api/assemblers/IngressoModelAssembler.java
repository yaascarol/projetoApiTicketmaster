package com.ticketmaster.api.assemblers;

import com.ticketmaster.api.controller.IngressoController;
import com.ticketmaster.api.dto.response.IngressoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class IngressoModelAssembler
        implements RepresentationModelAssembler<IngressoResponse, EntityModel<IngressoResponse>> {

    @Override
    public EntityModel<IngressoResponse> toModel(IngressoResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(IngressoController.class).buscarPorId(response.getId())).withSelfRel(),
                linkTo(methodOn(IngressoController.class).listar(null, null)).withRel("ingressos"),
                linkTo(methodOn(IngressoController.class).deletar(response.getId())).withRel("delete"));
    }
}
