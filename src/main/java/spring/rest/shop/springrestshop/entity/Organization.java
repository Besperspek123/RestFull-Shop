package spring.rest.shop.springrestshop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

import javax.swing.*;
import java.util.List;

@Entity
@Table(name = "organization")
@Data
public class Organization {
    public Organization(String name) {
        this.name = name;
    }

    public Organization() {
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @JsonIgnore
    @Column(name = "activity")
    private boolean activity;

    @JsonIgnore
    @OneToMany(mappedBy = "organization")
    private List<Product> productList;

    @JsonIgnore
    private ImageIcon logotype;



    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", owner=" + owner.getUsername() + // или owner.getId() или owner.getEmail() в зависимости от того, что вы хотите отобразить
                ", activity=" + activity +
                '}';
    }
}
