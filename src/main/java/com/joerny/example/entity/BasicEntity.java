package com.joerny.example.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String simpleText;

    private Date simpleDate;

    @ElementCollection(targetClass = SimpleEnum.class)
    private List<SimpleEnum> simpleEnum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSimpleText() {
        return simpleText;
    }

    public void setSimpleText(String simpleText) {
        this.simpleText = simpleText;
    }

    public List<SimpleEnum> getSimpleEnum() {
        return simpleEnum;
    }

    public void setSimpleEnum(List<SimpleEnum> simpleEnum) {
        this.simpleEnum = simpleEnum;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getSimpleDate() {
        return simpleDate;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setSimpleDate(Date simpleDate) {
        this.simpleDate = simpleDate;
    }
}
