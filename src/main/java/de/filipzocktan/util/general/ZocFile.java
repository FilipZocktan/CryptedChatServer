package de.filipzocktan.util.general;

import java.io.File;

/**
 * An Extended <code>java.io.File</code> File.
 *
 * @author Filip Zocktan @ Filip Zocktan Studios
 * @version 1.0
 * @see File
 * @since 20 01 2017 - 18:08:06
 */
public class ZocFile extends File {

    private static final long serialVersionUID = -1915641988282993304L;

    public ZocFile(String dir, String filename, Filetype type) {
        super(dir, filename + type.getEndung());
    }

}
