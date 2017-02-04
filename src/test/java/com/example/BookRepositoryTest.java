package com.example;

import com.example.db.tables.pojos.Book;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.SelectSeekStep1;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.example.db.tables.Book.BOOK;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@Slf4j
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void findByIsbn() throws Exception {

        String isbn = "001-9999990001";
        Book book = bookRepository.findByIsbn(isbn);

        log.debug(book.toString());
        assertThat(book.getIsbn()).isEqualTo(isbn);
    }

    @Test
    public void selectByAuthorId() throws Exception {

        Integer authorId = 2;
        List<Book> books = bookRepository.selectByAuthorId(authorId);

        log.debug(books.toString());
        assertThat(books).asList().hasSize(2);
    }

    @Test
    public void selectRecordsByAuthorId() throws Exception {
        Integer authorId = 2;
        Result<Record3<String, String, String>> result = bookRepository.selectRecordsByAuthorId(authorId);

        result.forEach(record -> {
            log.debug("### {}", record.toString());
            log.debug(record.get(BOOK.ISBN));
        });
//        log.debug(records.toString());
    }

}