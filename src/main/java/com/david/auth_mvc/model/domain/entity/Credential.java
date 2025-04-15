package com.david.auth_mvc.model.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "credential")
public class Credential {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(nullable = false, name = "is_access_oauth")
    private Boolean isAccesOauth;

    @Column(nullable = false, name = "is_verified")
    private Boolean isVerified;

    @OneToMany(targetEntity = AccessToken.class, cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id")
    private List<AccessToken> accessTokens;
}
