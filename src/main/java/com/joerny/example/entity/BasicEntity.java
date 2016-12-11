package com.joerny.example.entity;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Entity
public class BasicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String simpleText;

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
}
