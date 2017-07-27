package main.java.controllers;

import main.java.beans.Genre;
import main.java.db.Database;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean(eager = true) //eager = true означает что будет проинициализированно до первого требования пользователя
@ApplicationScoped
public class GenreController implements Serializable {

    private ArrayList<Genre> genreList;

    public GenreController() {
        fillGenresAll();
    }

    private void fillGenresAll() {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        genreList = new ArrayList<Genre>();

        try {
            conn = Database.getConnection();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from library.genre order by name");
            while (rs.next()) {
                Genre genre = new Genre();
                genre.setName(rs.getString("name"));
                genre.setId(rs.getLong("id"));
                genreList.add(genre);
            }

        } catch (SQLException ex) {
            Logger.getLogger(GenreController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(GenreController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public ArrayList<Genre> getGenreList() {
        return genreList;
    }
}
