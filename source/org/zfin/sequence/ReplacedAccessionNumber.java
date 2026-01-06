package org.zfin.sequence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "replaced_accession_number")
public class ReplacedAccessionNumber implements Serializable {

    @EmbeddedId
    private ReplacedAccessionNumberId id;

    public String getOldAccessionNumber() {
        return id != null ? id.getOldAccessionNumber() : null;
    }

    public void setOldAccessionNumber(String oldAccessionNumber) {
        if (id == null) {
            id = new ReplacedAccessionNumberId();
        }
        id.setOldAccessionNumber(oldAccessionNumber);
    }

    public String getNewAccessionNumber() {
        return id != null ? id.getNewAccessionNumber() : null;
    }

    public void setNewAccessionNumber(String newAccessionNumber) {
        if (id == null) {
            id = new ReplacedAccessionNumberId();
        }
        id.setNewAccessionNumber(newAccessionNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplacedAccessionNumber that = (ReplacedAccessionNumber) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Embeddable
    @Getter
    @Setter
    public static class ReplacedAccessionNumberId implements Serializable {

        @Column(name = "ran_old_acc_num")
        private String oldAccessionNumber;

        @Column(name = "ran_new_acc_num")
        private String newAccessionNumber;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ReplacedAccessionNumberId that = (ReplacedAccessionNumberId) o;

            if (oldAccessionNumber != null ? !oldAccessionNumber.equals(that.oldAccessionNumber) : that.oldAccessionNumber != null)
                return false;
            return newAccessionNumber != null ? newAccessionNumber.equals(that.newAccessionNumber) : that.newAccessionNumber == null;
        }

        @Override
        public int hashCode() {
            int result = oldAccessionNumber != null ? oldAccessionNumber.hashCode() : 0;
            result = 31 * result + (newAccessionNumber != null ? newAccessionNumber.hashCode() : 0);
            return result;
        }
    }
}
