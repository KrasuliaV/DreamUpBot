package com.home.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@MappedSuperclass
@Access(AccessType.FIELD)
@Getter
@Setter
public abstract class AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected AbstractBaseEntity() {
    }
}
