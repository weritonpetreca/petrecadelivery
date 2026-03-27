package com.petreca.petrecadelivery.delivery.tracking.domain.model;

import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryFulfilledEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPickedUpEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPlacedEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Aggregate Root da entidade Delivery.
 *
 * <p>Centraliza todas as regras de negócio relacionadas ao ciclo de vida
 * de uma entrega: criação, edição, publicação, rastreamento e conclusão.</p>
 *
 * <p>Estende {@link AbstractAggregateRoot} para suporte a Domain Events —
 * eventos registrados durante operações de domínio e publicados automaticamente
 * pelo Spring Data após a persistência via {@code saveAndFlush()}.</p>
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Setter(AccessLevel.PRIVATE)
@Getter
public class Delivery extends AbstractAggregateRoot<Delivery> {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private UUID courierId;

    private DeliveryStatus status;

    private OffsetDateTime placedAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime expectedDeliveryAt;
    private OffsetDateTime fulfilledAt;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;

    private Integer totalItems;

    /*
     * @Embedded — ContactPoint é um Value Object mapeado diretamente
     * na tabela de Delivery (sem tabela própria).
     *
     * @AttributeOverrides — necessário porque temos DOIS ContactPoints
     * (sender e recipient) na mesma tabela. Sem isso, o Hibernate
     * tentaria criar colunas com o mesmo nome (ex: duas colunas "zipCode")
     * e lançaria erro. Os overrides diferenciam: "sender_zip_code" vs
     * "recipient_zip_code".
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode",    column = @Column(name = "sender_zip_code")),
            @AttributeOverride(name = "street",     column = @Column(name = "sender_street")),
            @AttributeOverride(name = "number",     column = @Column(name = "sender_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "sender_complement")),
            @AttributeOverride(name = "name",       column = @Column(name = "sender_name")),
            @AttributeOverride(name = "phone",      column = @Column(name = "sender_phone"))
    })
    private ContactPoint sender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode",    column = @Column(name = "recipient_zip_code")),
            @AttributeOverride(name = "street",     column = @Column(name = "recipient_street")),
            @AttributeOverride(name = "number",     column = @Column(name = "recipient_number")),
            @AttributeOverride(name = "complement", column = @Column(name = "recipient_complement")),
            @AttributeOverride(name = "name",       column = @Column(name = "recipient_name")),
            @AttributeOverride(name = "phone",      column = @Column(name = "recipient_phone"))
    })
    private ContactPoint recipient;

    /*
     * CascadeType.ALL — operações na Delivery (persist, merge, remove)
     * são cascateadas para os Items. Se deletar a Delivery, os Items
     * são deletados junto.
     *
     * orphanRemoval = true — se um Item for removido da lista
     * (delivery.removeItem()), ele é automaticamente deletado do banco.
     *
     * mappedBy = "delivery" — indica que o lado dono do relacionamento
     * é o Item (que tem a FK "delivery_id" na sua tabela).
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "delivery")
    private List<Item> items = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────
    // Factory Method
    // ─────────────────────────────────────────────────────────────────

    /**
     * Cria uma nova entrega em estado de rascunho ({@link DeliveryStatus#DRAFT}).
     *
     * <p>Uso de factory method em vez de construtor público garante que
     * toda entidade nasce em um estado válido e consistente,
     * com ID gerado e valores iniciais seguros.</p>
     *
     * @return nova instância de {@link Delivery} com status {@code DRAFT}
     */
    public static Delivery draft() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus(DeliveryStatus.DRAFT);
        delivery.setTotalItems(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);
        return delivery;
    }

    // ─────────────────────────────────────────────────────────────────
    // Comportamentos de domínio — gerenciamento de itens
    // ─────────────────────────────────────────────────────────────────

    /**
     * Adiciona um item à entrega e recalcula o total de itens.
     *
     * @param name     nome do item
     * @param quantity quantidade do item (mínimo 1)
     * @return ID gerado para o item adicionado
     */
    public UUID addItem(String name, int quantity) {
        Item item = Item.brandNew(name, quantity, this);
        items.add(item);
        calculateTotalItems();
        return item.getId();
    }

    /**
     * Remove um item da entrega pelo seu ID e recalcula o total.
     *
     * @param itemId ID do item a ser removido
     */
    public void removeItem(UUID itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
        calculateTotalItems();
    }

    /**
     * Altera a quantidade de um item existente e recalcula o total.
     *
     * @param itemId   ID do item a ser alterado
     * @param quantity nova quantidade
     */
    public void changeItemQuantity(UUID itemId, int quantity) {
        Item item = getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Item not found: " + itemId));
        item.setQuantity(quantity);
        calculateTotalItems();
    }

    /**
     * Remove todos os itens da entrega.
     * Utilizado antes de uma reedição completa dos dados.
     */
    public void removeItems() {
        items.clear();
        calculateTotalItems();
    }

    // ─────────────────────────────────────────────────────────────────
    // Comportamentos de domínio — ciclo de vida da entrega
    // ─────────────────────────────────────────────────────────────────

    /**
     * Aplica os detalhes de preparação (endereços, custos, prazo estimado).
     * Só pode ser chamado enquanto a entrega estiver em {@link DeliveryStatus#DRAFT}.
     *
     * @param details detalhes calculados pelo serviço de preparação
     * @throws DomainException se a entrega não estiver em estado editável
     */
    public void editPreparationDetails(PreparationDetails details) {
        verifyIfCanBeEdited();
        setSender(details.getSender());
        setRecipient(details.getRecipient());
        setDistanceFee(details.getDistanceFee());
        setCourierPayout(details.getCourierPayout());
        setExpectedDeliveryAt(OffsetDateTime.now().plus(details.getExpectedDeliveryTime()));
        setTotalCost(this.getDistanceFee().add(this.getCourierPayout()));
    }

    /**
     * Publica a entrega, tornando-a disponível para atribuição a um entregador.
     * Registra um {@link DeliveryPlacedEvent} para publicação assíncrona via Kafka.
     *
     * @throws DomainException se a entrega não estiver preenchida ou não estiver em DRAFT
     */
    public void place() {
        verifyIfCanBePlaced();
        this.changeStatusTo(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlacedAt(OffsetDateTime.now());
        super.registerEvent(
                new DeliveryPlacedEvent(this.placedAt, this.id));
    }

    /**
     * Registra a retirada da entrega por um entregador.
     * Registra um {@link DeliveryPickedUpEvent} para publicação assíncrona via Kafka.
     *
     * @param courierId ID do entregador que retirou a entrega
     */
    public void pickUp(UUID courierId) {
        this.setCourierId(courierId);
        this.changeStatusTo(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetDateTime.now());
        super.registerEvent(
                new DeliveryPickedUpEvent(this.assignedAt, this.id));
    }

    /**
     * Marca a entrega como concluída.
     * Registra um {@link DeliveryFulfilledEvent} para publicação assíncrona via Kafka.
     */
    public void markAsDelivered() {
        this.changeStatusTo(DeliveryStatus.DELIVERED);
        this.setFulfilledAt(OffsetDateTime.now());
        super.registerEvent(
                new DeliveryFulfilledEvent(this.getFulfilledAt(), this.getId()));
    }

    // ─────────────────────────────────────────────────────────────────
    // Getters com proteção adicional
    // ─────────────────────────────────────────────────────────────────

    /**
     * Retorna uma visão imutável da lista de itens.
     * Impede que código externo modifique a lista diretamente,
     * forçando o uso dos métodos de domínio (addItem, removeItem...).
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    /**
     * Retorna os Domain Events registrados durante a operação atual.
     *
     * <p>Sobrescreve o método {@code protected} de {@link AbstractAggregateRoot}
     * para torná-lo acessível externamente, principalmente em testes unitários
     * que verificam se os eventos corretos foram registrados sem precisar
     * subir o contexto do Spring.</p>
     */
    @Override
    public Collection<Object> domainEvents() {
        return super.domainEvents();
    }

    // ─────────────────────────────────────────────────────────────────
    // Métodos privados de suporte
    // ─────────────────────────────────────────────────────────────────

    private void calculateTotalItems() {
        int totalItems = getItems().stream()
                .mapToInt(Item::getQuantity)
                .sum();
        setTotalItems(totalItems);
    }

    private void verifyIfCanBePlaced() {
        if (!isFilled()) {
            throw new DomainException(
                    "Delivery cannot be placed: preparation details are incomplete.");
        }
        if (!getStatus().equals(DeliveryStatus.DRAFT)) {
            throw new DomainException(
                    "Delivery cannot be placed: current status is " + getStatus());
        }
    }

    private void verifyIfCanBeEdited() {
        if (!getStatus().equals(DeliveryStatus.DRAFT)) {
            throw new DomainException(
                    "Delivery cannot be edited: current status is " + getStatus());
        }
    }

    /**
     * Verifica se os dados mínimos de preparação foram preenchidos.
     * totalCost > ZERO garante que editPreparationDetails() foi chamado,
     * já que draft() inicializa totalCost com ZERO.
     */
    private boolean isFilled() {
        return this.getSender() != null
                && this.getRecipient() != null
                && this.getTotalCost() != null
                && this.getTotalCost().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Aplica a transição de status, validando se é permitida pela state machine.
     *
     * @param newStatus status destino da transição
     * @throws DomainException se a transição não for permitida
     */
    private void changeStatusTo(DeliveryStatus newStatus) {
        if (newStatus != null && !this.getStatus().canChangeTo(newStatus)) {
            throw new DomainException(
                    "Invalid status transition from " + this.getStatus() + " to " + newStatus);
        }
        this.setStatus(newStatus);
    }

    // ─────────────────────────────────────────────────────────────────
    // Inner class — PreparationDetails (Value Object)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Value Object que agrupa os dados calculados durante a preparação da entrega.
     *
     * <p>Usar um objeto dedicado em vez de múltiplos parâmetros avulsos
     * deixa a assinatura de {@link #editPreparationDetails} legível e coesa,
     * além de facilitar futuras extensões sem quebrar a interface.</p>
     */
    @AllArgsConstructor
    @Builder
    @Getter
    public static class PreparationDetails {
        private ContactPoint sender;
        private ContactPoint recipient;
        private BigDecimal distanceFee;
        private BigDecimal courierPayout;
        private Duration expectedDeliveryTime;
    }
}