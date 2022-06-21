package com.joerny.example.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
public class BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String simpleText;

    @Column(nullable = false)
    private String notNullableColumn;

    @NotNull
    private String notNullField;

    private Date simpleDate;

    private Double simpleDouble;

    private Float simpleFloat;

    @OneToOne(targetEntity = ChildEntity.class)
    private ChildEntity child;

    @ElementCollection(targetClass = SimpleEnum.class)
    private List<SimpleEnum> simpleEnum;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getSimpleText() {
        return simpleText;
    }

    public void setSimpleText(final String simpleText) {
        this.simpleText = simpleText;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<SimpleEnum> getSimpleEnum() {
        return simpleEnum;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setSimpleEnum(final List<SimpleEnum> simpleEnum) {
        this.simpleEnum = simpleEnum;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getSimpleDate() {
        return simpleDate;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setSimpleDate(final Date simpleDate) {
        this.simpleDate = simpleDate;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ChildEntity getChild() {
        return child;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setChild(final ChildEntity child) {
        this.child = child;
    }

    public Double getSimpleDouble() {
        return simpleDouble;
    }

    public void setSimpleDouble(Double simpleDouble) {
        this.simpleDouble = simpleDouble;
    }

    public Float getSimpleFloat() {
        return simpleFloat;
    }

    public void setSimpleFloat(Float simpleFloat) {
        this.simpleFloat = simpleFloat;
    }

    public String getNotNullableColumn() {
        return notNullableColumn;
    }

    public void setNotNullableColumn(String notNullableColumn) {
        this.notNullableColumn = notNullableColumn;
    }

    public String getNotNullField() {
        return notNullField;
    }

    public void setNotNullField(String notNullField) {
        this.notNullField = notNullField;
    }
}
