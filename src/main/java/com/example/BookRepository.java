package com.example;

import com.example.db.tables.pojos.Book;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.db.tables.Author.AUTHOR;
import static com.example.db.tables.Book.BOOK;

@Repository
@Slf4j
public class BookRepository {

    @Autowired
    private DSLContext dslContext;

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
                .select(BOOK.ISBN, BOOK.TITLE, AUTHOR.NAME)
                .from(BOOK)
                .join(AUTHOR).on(BOOK.AUTHOR_ID.eq(AUTHOR.ID))
                .where(AUTHOR.ID.eq(authorId))
                .orderBy(BOOK.ISBN)
                .fetch();
    }


}
