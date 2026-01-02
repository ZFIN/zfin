package org.zfin.sequence.blast;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "blastdb_order")
public class DatabaseRelationship implements Comparable<DatabaseRelationship>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bdborder_pk_id", nullable = false)
    private Long id;

    @Column(name = "bdborder_order", nullable = false)
    private Integer order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdborder_parent_blastdb_zdb_id")
    private Database parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bdborder_child_blastdb_zdb_id", nullable = false)
    private Database child;

    public int compareTo(DatabaseRelationship databaseRelationship) {
        return (int) (getOrder() - databaseRelationship.getOrder());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DatabaseRelationship");
        sb.append("{order=").append(order);
        sb.append(", parent=").append(parent);
        sb.append(", child=").append(child);
        sb.append('}');
        return sb.toString();
    }
}
