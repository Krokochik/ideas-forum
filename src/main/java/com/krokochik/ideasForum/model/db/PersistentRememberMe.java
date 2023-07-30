package com.krokochik.ideasForum.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "persistent_logins")
public class PersistentRememberMe {
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
