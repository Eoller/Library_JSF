package main.java.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

@ManagedBean(eager = true)
@SessionScoped
public class LocaleChanger implements Serializable {

    private Locale currentLocale;

    public LocaleChanger() {
        currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }

    public void changeLocale(String localeCode) {
        currentLocale = new Locale(localeCode);
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }


}
