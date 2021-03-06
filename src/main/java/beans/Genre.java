package main.java.beans;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@ManagedBean
@ApplicationScoped
public class Genre {

    private String name;
    private long id;

    public Genre() {
    }

    public Genre(String name, long id) {
        this.name = name;
        this.id = id;
    }

    //region get\set
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    //endregion


}
