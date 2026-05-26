package com.ticketmaster.api.assemblers;

import com.ticketmaster.api.controller.PedidoController;
import com.ticketmaster.api.dto.response.PedidoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PedidoModelAssembler
        implements RepresentationModelAssembler<PedidoResponse, EntityModel<PedidoResponse>> {

    @Override
    public EntityModel<PedidoResponse> toModel(PedidoResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(PedidoController.class).buscarPorId(response.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listar(null, null)).withRel("pedidos"),
                linkTo(methodOn(PedidoController.class).deletar(response.getId())).withRel("delete"));
    }
}
