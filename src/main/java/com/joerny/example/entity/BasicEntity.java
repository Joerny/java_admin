package com.joerny.example.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
public class BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String simpleText;

    private Date simpleDate;

    private Double simpleDouble;

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

    public List<SimpleEnum> getSimpleEnum() {
        return simpleEnum;
    }

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

    public ChildEntity getChild() {
        return child;
    }

    public void setChild(final ChildEntity child) {
        this.child = child;
    }

    public Double getSimpleDouble() {
        return simpleDouble;
    }

    public void setSimpleDouble(Double simpleDouble) {
        this.simpleDouble = simpleDouble;
    }
}
