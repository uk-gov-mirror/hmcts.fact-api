package uk.gov.hmcts.dts.fact.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "search_openingtime")
@Getter
@Setter
public class OpeningTime {
    @Id
    private Integer id;
    private String type;
    private String hours;
}
