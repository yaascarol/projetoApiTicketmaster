package com.ticketmaster.api.assemblers;

import com.ticketmaster.api.controller.ItemPedidoController;
import com.ticketmaster.api.dto.response.ItemPedidoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ItemPedidoModelAssembler
        implements RepresentationModelAssembler<ItemPedidoResponse, EntityModel<ItemPedidoResponse>> {

    @Override
    public EntityModel<ItemPedidoResponse> toModel(ItemPedidoResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ItemPedidoController.class).buscarPorId(response.getId())).withSelfRel(),
                linkTo(methodOn(ItemPedidoController.class).listar(null, null)).withRel("itens-pedido"),
                linkTo(methodOn(ItemPedidoController.class).excluir(response.getId())).withRel("delete"));
    }
}
