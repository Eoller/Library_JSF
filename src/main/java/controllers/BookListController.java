package main.java.controllers;

import main.java.beans.Book;
import main.java.db.Database;
import main.java.enums.SearchType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean(eager = true)
@SessionScoped
public class BookListController implements Serializable {

    private boolean requestFromPager;
    private int booksOnPage = 2;
    private int pageCount;
    private int selectedGenreId; // выбранный жанр
    private char selectedLetter; // выбранная буква алфавита
    private long selectedPageNumber = 1; // выбранный номер страницы в постраничной навигации
    private long totalBooksCount; // общее кол-во книг (не на текущей странице, а всего), нажно для постраничности
    private ArrayList<Integer> pageNumbers = new ArrayList<Integer>(); // общее кол-во книг (не на текущей странице, а всего), нажно для постраничности
    private SearchType searchType;// хранит выбранный тип поиска
    private String searchString; // хранит поисковую строку
    private ArrayList<Book> currentBookList; // текущий список книг для отображения
    private String currentSql;// последний выполнный sql без добавления limit
    private boolean editMode;

    public BookListController() {
        fillBooksAll();
        searchType = SearchType.AUTHOR;
    }

    private void fillBooksBySQL(String sql) {

        StringBuilder sqlBuilder = new StringBuilder(sql);

        currentSql = sql;

        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();

            System.out.println(requestFromPager);
            if (!requestFromPager) {

                rs = stmt.executeQuery(sqlBuilder.toString());
                rs.last();

                totalBooksCount = rs.getRow();
                fillPageNumbers(totalBooksCount, booksOnPage);

            }


            if (totalBooksCount > booksOnPage) {
                sqlBuilder.append(" limit ").append(selectedPageNumber * booksOnPage - booksOnPage).append(",").append(booksOnPage);
            }

            rs = stmt.executeQuery(sqlBuilder.toString());

            currentBookList = new ArrayList<Book>();

            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getLong("id"));
                book.setName(rs.getString("name"));
                book.setGenre(rs.getString("genre"));
                book.setIsbn(rs.getString("isbn"));
                book.setAuthor(rs.getString("author"));
                book.setPageCount(rs.getInt("page_count"));
                book.setPublishDate(rs.getInt("publish_year"));
                book.setPublisher(rs.getString("publisher"));
//              book.setImage(rs.getBytes("image"));
//              book.setContent(rs.getBytes("content"));
                book.setDescr(rs.getString("descr"));
                currentBookList.add(book);
            }

        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {

                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void fillBooksAll() {
        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, b.descr, "
                + "a.fio as author, g.name as genre, b.image from library.book b inner join library.author a on b.author_id=a.id "
                + "inner join library.genre g on b.genre_id=g.id inner join library.publisher p on b.publisher_id=p.id order by b.name");
    }

    private void submitValues(Character selectedLetter, long selectedPageNumber, int selectedGenreId, boolean requestFromPager) {
        this.selectedLetter = selectedLetter;
        this.selectedPageNumber = selectedPageNumber;
        this.selectedGenreId = selectedGenreId;
        this.requestFromPager = requestFromPager;

    }

    public void searchStringChanged(ValueChangeEvent e) {
        searchString = e.getNewValue().toString();
    }

    public void searchTypeChanged(ValueChangeEvent e) {
        searchType = (SearchType) e.getNewValue();
    }

    //<editor-fold desc="fillBooksBy...">
    public String fillBooksByGenre() {

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        submitValues(' ', 1, Integer.valueOf(params.get("genre_id")), false);

        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.descr, b.image from library.book b "
                + "inner join library.author a on b.author_id=a.id "
                + "inner join library.genre g on b.genre_id=g.id "
                + "inner join library.publisher p on b.publisher_id=p.id "
                + "where genre_id=" + selectedGenreId + " order by b.name ");


        return "books";
    }

    public String fillBooksByLetter() {

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedLetter = params.get("letter").charAt(0);

        submitValues(selectedLetter, 1, -1, false);


        fillBooksBySQL("select b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.descr, b.image from library.book b "
                + "inner join library.author a on b.author_id=a.id "
                + "inner join library.genre g on b.genre_id=g.id "
                + "inner join library.publisher p on b.publisher_id=p.id "
                + "where substr(b.name,1,1)='" + selectedLetter + "' order by b.name ");

        return "books";
    }

    public String fillBooksBySearch() {

        submitValues(' ', 1, -1, false);

        if (searchString.trim().length() == 0) {
            fillBooksAll();
            return "books";
        }

        StringBuilder sql = new StringBuilder("select b.descr, b.id,b.name,b.isbn,b.page_count,b.publish_year, p.name as publisher, a.fio as author, g.name as genre, b.image from library.book b "
                + "inner join library.author a on b.author_id=a.id "
                + "inner join library.genre g on b.genre_id=g.id "
                + "inner join library.publisher p on b.publisher_id=p.id ");

        if (searchType == SearchType.AUTHOR) {
            sql.append("where lower(a.fio) like '%" + searchString.toLowerCase() + "%' order by b.name ");

        } else if (searchType == SearchType.TITLE) {
            sql.append("where lower(b.name) like '%" + searchString.toLowerCase() + "%' order by b.name ");
        }


        fillBooksBySQL(sql.toString());


        return "books";
    }
    //</editor-fold>

    public void selectPage() {
        cancelEdit();
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedPageNumber = Integer.valueOf(params.get("page_number"));
        requestFromPager = true; //Показывает был ли запрос с постраничности.
        fillBooksBySQL(currentSql);
    }

    public byte[] getContent(int id) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;


        byte[] content = null;
        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("select content from library.book where id=" + id);
            while (rs.next()) {
                content = rs.getBytes("content");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Book.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {

                if (rs != null) {
                    rs.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(Book.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        return content;

    }

    public byte[] getImage(int id) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        byte[] image = null;

        try {
            conn = Database.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("select image from library.book where id=" + id);
            while (rs.next()) {
                image = rs.getBytes("image");
            }

        } catch (SQLException ex) {
            Logger.getLogger(Book.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {

                if (rs != null) {
                    rs.close();
                }

            } catch (SQLException ex) {
                Logger.getLogger(Book.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        return image;
    }

    public String updateBooks() {


        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = Database.getConnection();
            //приготовление запросов
            prepStmt = conn.prepareStatement("update library.book set name=?, isbn=?, page_count=?, publish_year=?, descr=? where id=?");


            for (Book book : currentBookList) {
                if (!book.isEdit()) continue;
                prepStmt.setString(1, book.getName());
                prepStmt.setString(2, book.getIsbn());
//                prepStmt.setString(3, book.getAuthor());
                prepStmt.setInt(3, book.getPageCount());
                prepStmt.setInt(4, book.getPublishDate());
//                prepStmt.setString(6, book.getPublisher());
                prepStmt.setString(5, book.getDescr());
                prepStmt.setLong(6, book.getId());
                prepStmt.addBatch(); //добавляет в стак запрос
            }


            prepStmt.executeBatch(); //выполнение всех запросов по цепочке


        } catch (SQLException ex) {
            Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(BookListController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        cancelEdit();
        return "books";
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void switchEditMode() {
        editMode = !editMode;
    }


    public void showEdit() {
        editMode = true;
    }

    public void cancelEdit() {
        editMode = false;
        for (Book book : currentBookList) {
            book.setEdit(false);
        }
    }

    public Character[] getRussianLetters() {
        Character[] letters = new Character[26];
        letters[0] = 'A';
        letters[1] = 'B';
        letters[2] = 'C';
        letters[3] = 'D';
        letters[4] = 'E';
        letters[5] = 'F';
        letters[6] = 'G';
        letters[7] = 'H';
        letters[8] = 'I';
        letters[9] = 'J';
        letters[10] = 'K';
        letters[11] = 'L';
        letters[12] = 'M';
        letters[13] = 'N';
        letters[14] = 'O';
        letters[15] = 'P';
        letters[16] = 'Q';
        letters[17] = 'R';
        letters[18] = 'S';
        letters[19] = 'T';
        letters[20] = 'U';
        letters[21] = 'V';
        letters[22] = 'W';
        letters[23] = 'X';
        letters[24] = 'Y';
        letters[25] = 'Z';

        return letters;
    }

    public void booksOnPageChanged(ValueChangeEvent e) {
        cancelEdit();
        requestFromPager = false;
        booksOnPage = Integer.valueOf(e.getNewValue().toString()).intValue();
        selectedPageNumber = 1;
        fillBooksBySQL(currentSql);
    }

    private void fillPageNumbers(long totalBooksCount, int booksCountOnPage) {

        if (totalBooksCount % booksCountOnPage == 0) {
            pageCount = booksCountOnPage > 0 ? (int) (totalBooksCount / booksCountOnPage) : 0;
        } else {
            pageCount = booksCountOnPage > 0 ? (int) (totalBooksCount / booksCountOnPage) + 1 : 0;
        }

        pageNumbers.clear();
        for (int i = 1; i <= pageCount; i++) {
            pageNumbers.add(i);
        }

    }

    public ArrayList<Integer> getPageNumbers() {
        return pageNumbers;
    }

    public void setPageNumbers(ArrayList<Integer> pageNumbers) {
        this.pageNumbers = pageNumbers;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public ArrayList<Book> getCurrentBookList() {
        return currentBookList;
    }

    public long getTotalBooksCount() {
        return totalBooksCount;
    }

    public void setTotalBooksCount(long booksCount) {
        this.totalBooksCount = booksCount;
    }

    public int getSelectedGenreId() {
        return selectedGenreId;
    }

    public void setSelectedGenreId(int selectedGenreId) {
        this.selectedGenreId = selectedGenreId;
    }

    public char getSelectedLetter() {
        return selectedLetter;
    }

    public void setSelectedLetter(char selectedLetter) {
        this.selectedLetter = selectedLetter;
    }

    public int getBooksOnPage() {
        return booksOnPage;
    }

    public void setBooksOnPage(int booksOnPage) {
        this.booksOnPage = booksOnPage;
    }

    public long getSelectedPageNumber() {
        return selectedPageNumber;
    }

    public void setSelectedPageNumber(long selectedPageNumber) {
        this.selectedPageNumber = selectedPageNumber;
    }
}
