package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
//Anotation that mean, User will be used by tag | IT POSSIBLE TO ADD (name="") PARAMETR TO USE IT IN JSF, default value will be name of class
@SessionScoped  //Visible only in session
public class User {

    private String username;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
