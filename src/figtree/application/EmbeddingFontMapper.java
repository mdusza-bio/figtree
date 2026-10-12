package figtree.application;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.DefaultFontMapper;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Font mapper for "Embed fonts" in Export PDF (Etap 8.3).
 *
 * <p>It knows the TrueType fonts installed on this machine, so the fonts used on the
 * figure get embedded in the PDF. It also keeps italic and bold when the chosen family
 * has no separate italic/bold file: on screen Java slants or thickens the glyphs itself,
 * but iText's {@link DefaultFontMapper} then finds the regular file for the styled font
 * and the style silently disappears from the PDF. Such a font falls back here to the
 * matching standard PDF font (Times-Italic, Helvetica-Oblique, ...), the same ones used
 * when fonts are not embedded at all.
 */
public class EmbeddingFontMapper extends DefaultFontMapper {

    /** An empty mapper only knows the 14 standard PDF fonts, with the right style. */
    private final DefaultFontMapper standardFonts = new DefaultFontMapper();

    public EmbeddingFontMapper() {
        for (String dir : fontDirectories()) {
            try {
                if (new File(dir).isDirectory()) {
                    insertDirectory(dir);
                }
            } catch (RuntimeException e) {
                // ignore a broken font directory
            }
        }
    }

    /** Where Windows, macOS and Linux keep their TrueType fonts (not searched recursively). */
    static List<String> fontDirectories() {
        List<String> dirs = new ArrayList<String>();
        String home = System.getProperty("user.home");
        String windir = System.getenv("WINDIR");
        if (windir != null) {
            dirs.add(windir + File.separator + "Fonts");
        }
        dirs.add(home + File.separator + "AppData" + File.separator + "Local" + File.separator
                + "Microsoft" + File.separator + "Windows" + File.separator + "Fonts");
        dirs.add("/System/Library/Fonts");
        // Apple keeps Times New Roman, Arial and the other Microsoft fonts here:
        dirs.add("/System/Library/Fonts/Supplemental");
        dirs.add("/Library/Fonts");
        dirs.add(home + "/Library/Fonts");
        dirs.add("/usr/share/fonts/truetype");
        dirs.add(home + "/.fonts");
        dirs.add(System.getProperty("java.home") + File.separator + "lib" + File.separator + "fonts");
        return dirs;
    }

    @Override
    public BaseFont awtToPdf(Font font) {
        if (styledFaceMissing(font)) {
            return standardFonts.awtToPdf(font);
        }
        return super.awtToPdf(font);
    }

    /**
     * True when the font is italic or bold but the file found for it is the same file
     * as for the font without that style, i.e. the styled face is not installed and
     * Java only fakes it on screen.
     */
    boolean styledFaceMissing(Font font) {
        BaseFontParameters found = getBaseFontParameters(font.getFontName());
        if (found == null) {
            // not an installed TrueType font: DefaultFontMapper already picks a standard
            // PDF font with the right style
            return false;
        }
        if (font.isItalic() && sameFile(found, font.deriveFont(font.getStyle() & ~Font.ITALIC))) {
            return true;
        }
        if (font.isBold() && sameFile(found, font.deriveFont(font.getStyle() & ~Font.BOLD))) {
            return true;
        }
        return false;
    }

    private boolean sameFile(BaseFontParameters found, Font other) {
        BaseFontParameters p = getBaseFontParameters(other.getFontName());
        return p != null && p.fontName.equals(found.fontName);
    }
}
