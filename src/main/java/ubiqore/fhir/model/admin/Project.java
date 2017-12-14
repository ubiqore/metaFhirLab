package ubiqore.fhir.model.admin;

/**
 * Created by roky on 23/03/17.
 */
public class Project {

    public Project(String name, String id, String other) {
        this.name = name;
        this.id = id;
        this.other = other;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    private String name;
    private String id;
    private String other;
}
