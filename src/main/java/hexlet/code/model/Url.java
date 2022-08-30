package hexlet.code.model;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Url extends Model {
    @Id
    private long id;
    private final String name;
    @WhenCreated
    private LocalDateTime createdAt;
    @OneToMany
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

    public final long getId() {
        return this.id;
    }

    public final String getName()  {
        return this.name;
    }

    public final LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public final List<UrlCheck> getUrlChecks() {
        return this.urlChecks;
    }

    public final void addCheck(UrlCheck check) {
        urlChecks.add(check);
    }


    public final LocalDateTime getLastCheck() {
        if (!urlChecks.isEmpty()) {
            return urlChecks.get(0).getCreatedAt();
        }
        return null;
    }

    public final Integer getLastStatusCode() {
        if (!this.getUrlChecks().isEmpty()) {
            return this.getUrlChecks().get(0).getStatusCode();
        }
        return null;
    }
}
