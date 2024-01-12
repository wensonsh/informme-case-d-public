package de.dh.informme.hl7Mock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="hl7_mocks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hl7Mock {
    @Id
    private String mshId;

    @Column(columnDefinition = "LONGTEXT")
    private String message;
}
