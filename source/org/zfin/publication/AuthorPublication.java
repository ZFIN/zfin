package org.zfin.publication;

import javax.persistence.*;
import org.zfin.publication.Author;

@Entity
@Table(name = "author_publication")
public class AuthorPublication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="authorpub_pk_id")
    private long id;

    @Column(name="authorpub_pub_zdb_id")
    private Publication publication;

    @Column(name="authorpub_author_id")
    private Author author;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
    
}
