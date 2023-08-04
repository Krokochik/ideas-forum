package com.krokochik.ideasforum.model.db;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@ToString(of = {"title", "author", "creationDate"})
@Entity
@Table(name="pst")
public class Post
{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id;

    @NotNull
    String title;
    @NotNull
    @Column(length = 10_000)
    String content;

    String fulltextTitle = "";
    @Column(length = 60_000)
    String fulltextContent = "";
    

    @NotNull
    String tags;
    @NotNull
    String author;

    @Temporal(TemporalType.DATE)
    Date creationDate = new Date(System.currentTimeMillis());
}