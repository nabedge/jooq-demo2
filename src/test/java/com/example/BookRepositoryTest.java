package com.example;

import com.example.db.tables.pojos.Book;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record3;
import org.jooq.Result;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.example.db.tables.Author.AUTHOR;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
//@Transactional // わざとつけないでおく。データが汚れたら "mvn compile" でデータ層を全部作り直し。
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

        Long authorId = 2L;
        List<Book> books = bookRepository.selectByAuthorId(authorId);

        log.debug(books.toString());
        assertThat(books).asList().hasSize(2);
    }

    @Test
    public void selectRecordsByAuthorId() throws Exception {

        Long authorId = 2L;
        Result<Record3<String, String, String>> result = bookRepository.selectRecordsByAuthorId(authorId);

        result.forEach(record -> {
            log.debug("### {}", record.toString());
            assertThat(record.get(AUTHOR.NAME)).isEqualTo("Haruki Murakami");
        });
    }

    @Test
    public void selectTitleAndAuthorByAuthorId() throws Exception {

        Long authorId = 2L;

        List<TitleAndAuthorName> list = bookRepository.selectTitleAndAuthorByAuthorId(authorId);

        list.forEach(titleAndAuthor -> {
            log.debug("### {}", titleAndAuthor.toString());
            assertThat(titleAndAuthor.getAuthorName()).isEqualTo("Haruki Murakami");
        });
    }

    @Test
    public void insertBook() throws Exception {
        bookRepository.insertBook();
    }

    @Test
    public void updateBook() throws Exception {
        bookRepository.updateBook();
    }

    @Test
    public void test_insertDuplicateKey() throws Exception {

        // AUTHORテーブルの件数 あとで確認するため
        int count = bookRepository.countAuthor();

        try {

            // AUTHORテーブルに2回INSERTを投げるが、2回目のINSERTは重複例外が発生する

            log.debug("@@@@ ここから下にlog4jdbcがrollback発生のログを出すはず");
            bookRepository.duplicateInsert_ThisIsBug();
            Assert.fail();

        } catch (DuplicateKeyException e) {
            log.debug("@@@@ ここまで");

            assertThat(e.getMessage()).contains("duplicate key value violates");

        } finally {

            // 例外発生するとロールバックされるので、テーブルには何も追加されていないはず
            assertThat(bookRepository.countAuthor()).isEqualTo(count);
        }
    }
}