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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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

    public List<Book> selectByAuthorId(Long authorId) {
        return dslContext
                .select()
                .from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(AUTHOR.ID.eq(authorId))
                .orderBy(BOOK.ISBN)
                .fetchInto(Book.class);
    }

    public Result<Record3<String, String, String>> selectRecordsByAuthorId(Long authorId) {
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

    public List<TitleAndAuthorName> selectTitleAndAuthorByAuthorId(Long authorId) {
        return dslContext
                .select(
                        BOOK.TITLE.as("TITLE")
                        , AUTHOR.NAME.as("AUTHOR_NAME")
                )
                .from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(AUTHOR.ID.eq(authorId))
                .orderBy(BOOK.ISBN)
                .fetchInto(TitleAndAuthorName.class);
    }

    @Transactional
    public void insertBook() {

        // jooqの "UpdatableRecord" によるINSERT
        BookRecord bookRecord = dslContext.newRecord(BOOK);
        bookRecord.setIsbn("001-8888880020");
        bookRecord.setAuthorId(4L);
        bookRecord.setTitle("Attack of Titan vol. 20");
        bookRecord.setPublishDate(java.sql.Date.valueOf("2016-08-01"));

        dslContext.insertInto(BOOK).set(bookRecord).execute();

        // ベタなINSERT
        dslContext.insertInto(BOOK)
                .set(BOOK.AUTHOR_ID, 4L)
                .set(BOOK.ISBN, "001-8888880021")
                .set(BOOK.TITLE, "Attack of Titan vol. 21")
                .set(BOOK.PUBLISH_DATE, java.sql.Date.valueOf("2016-12-01"))
                .execute();
    }

    @Transactional
    public void updateBook() {

        // ベタなUPDATE
        dslContext.update(BOOK)
                .set(BOOK.TITLE, "A Study in Scarlet") // 元データにはスペルミスがある
                .where(BOOK.ISBN.eq("001-0000000001"))
                .execute();

        // UpdatableRecordを使ってUPDATE
        BookRecord bookRecord = dslContext.newRecord(BOOK);
        bookRecord.setIsbn("001-0000000002");
        bookRecord.setTitle("The Sign of Four");
        bookRecord.update();
    }


    @Transactional
    public void duplicateInsert_ThisIsBug() {

        Long authorId = ZonedDateTime.now().toEpochSecond();

        // jooqではない別のO/Rマッパーを使ってもちゃんと同じトランザクションで処理される
        String sql = "INSERT INTO AUTHOR (ID, NAME) VALUES  (?, 'John Doe')";
        jdbcTemplate.update(sql, authorId);

        // jooqでのINSERT ただし上で実行済みの別のINSERTによってauthorIdが重複するので例外が発生する
        dslContext.insertInto(AUTHOR)
                .set(AUTHOR.ID, authorId)
                .set(AUTHOR.NAME, "John Doe (duplicated)")
                .execute();
    }

}
