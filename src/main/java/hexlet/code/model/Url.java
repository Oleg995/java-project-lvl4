package hexlet.code.model;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Url extends Model {
    @Id
    private long id;
    private String name;
    @WhenCreated
    private LocalDateTime createdAt;

    public Url(String name) {
        this.name = name;
    }

    public final long getId() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    public final LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
