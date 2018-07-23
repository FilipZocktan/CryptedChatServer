package de.filipzocktan.util.general;

/**
 * Standardized Filetypes often in use at Filip Zocktan Studios.
 *
 * @author Filip Zocktan @ Filip Zocktan Studios
 * @version 1.1
 * @since 31 01 2017 - 22:09:11
 */
public enum Filetype {

    dir(""),
    fzas(".fzas"),
    schueler(".pupil"),
    yaml(".yml"),
    text(".txt"),
    errorlog(".fzaserror"),
    log(".fzaslog"),
    license_key(".fzaslicense");

    private String endung;

    private Filetype(String endung) {
        this.endung = endung;
    }

    public String getEndung() {
        return endung;
    }

}
