package org.orm.project.entity;

import lombok.*;
import org.orm.project.core.annotations.*;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "statuses")
public class Status {
    @Id
    private Long id;
    @Column(name = "first_name")
    private String body;
    @Column(name = "last_name")
    private String lastName;
//    @OneToOne
//    private Person person;
}