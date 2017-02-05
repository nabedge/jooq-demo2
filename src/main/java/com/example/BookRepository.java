package com.example;

import com.example.db.tables.Author;
import com.example.db.tables.pojos.Book;
import com.example.db.tables.records.BookRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.example.db.tables.Author.AUTHOR;
import static com.example.db.tables.Book.BOOK;

@Repository
@Slf4j
public class BookRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int countAuthor() {
        return dslContext
                .selectCount()
                .from(AUTHOR)
                .execute();
    }

    public Book findByIsbn(String isbn) {
        return dslContext
                .select()
                .from(BOOK)
                .where(BOOK.ISBN.eq(isbn))
                .fetchOneInto(Book.class);
    }

    public List<Book> selectByAuthorId(Integer authorId) {
        return dslContext
                .select()
                .from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(AUTHOR.ID.eq(authorId))
                .orderBy(BOOK.ISBN)
                .fetchInto(Book.class);
    }

    public Result<Record3<String, String, String>> selectRecordsByAuthorId(Integer authorId) {
        return dslContext
                // 異なる２つのテーブル上の、3つのカラムの値をとる
                .select(
                        BOOK.ISBN
                        , BOOK.TITLE
                        , AUTHOR.NAME)
                .from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(AUTHOR.ID.eq(authorId))
                .orderBy(BOOK.ISBN)
                .fetch();
    }

    public List<TitleAndAuthorName> selectTitleAndAuthorByAuthorId(Integer authorId) {
        return dslContext
                .select(
                        BOOK.TITLE.as("title")
                        , AUTHOR.NAME.as("authorName")
                )
                .from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(AUTHOR.ID.eq(authorId))
                .orderBy(BOOK.ISBN)
                .fetchInto(TitleAndAuthorName.class);
    }

    @Transactional
    public void insertBook() {

        BookRecord bookRecord = new BookRecord();
        bookRecord.setIsbn("001-8888880002");
        bookRecord.setAuthorId(4);
        bookRecord.setTitle("Attack of Titan vol. 21");
        bookRecord.setPublishDate(java.sql.Date.valueOf("2016-12-01"));

        dslContext.insertInto(BOOK).set(bookRecord).execute();
    }

    @Transactional
    public void updateBook() {

    }


    @Transactional
    public void duplicateInsert_ThisIsBug() {

        Integer authorId = 99;

        // jooqではない別のO/Rマッパーを使ってもちゃんと同じトランザクションで処理される
        String sql = "INSERT INTO AUTHOR (ID, NAME) VALUES  (?, 'John Doe')";
        jdbcTemplate.update(sql, authorId);

        // jooqでのINSERT ただし上の別のINSERTによってauthorIdが重複するので例外が発生する
        dslContext.insertInto(AUTHOR)
                .set(AUTHOR.ID, authorId)
                .set(AUTHOR.NAME, "John Doe (duplicated)")
                .execute();
    }

}
