package edu.cda.project.ticklybackend.models.user;

import jakarta.persistence.*;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
}
