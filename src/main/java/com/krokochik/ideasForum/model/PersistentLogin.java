package com.krokochik.ideasForum.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "persistent_logins")
public class PersistentLogin {
    @Id
    @Column(length = 64)
    String series;

    @Column(length = 64, nullable = false)
    String username;

    @Column(length = 64, nullable = false)
    String token;

    @Column(nullable = false)
    LocalDateTime lastUsed;
}
