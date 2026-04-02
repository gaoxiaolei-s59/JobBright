package org.puregxl.site.rag.testParse;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;


public class test {

    @Test
    void test() throws TikaException, IOException {
        Tika tika = new Tika();
        String text = tika.parseToString(new File("document.pdf"));
        System.out.println(text);
    }
}
