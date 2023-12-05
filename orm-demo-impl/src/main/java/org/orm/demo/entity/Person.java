package org.orm.demo.entity;

import lombok.Data;
import org.orm.demo.hibernate.annotations.Column;
import org.orm.demo.hibernate.annotations.Entity;
import org.orm.demo.hibernate.annotations.Id;
import org.orm.demo.hibernate.annotations.Table;

@Data
@Entity
@Table(name = "people")
public class Person {
    @Id
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
}
