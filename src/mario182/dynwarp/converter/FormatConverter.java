package mario182.dynwarp.converter;

import java.io.File;
import java.util.logging.Logger;
import mario182.dynwarp.Main;

public interface FormatConverter {

    public static final char SEPERATOR = Main.SEPERATOR;
    public static final char GROUPSEPEARTOR = Main.GROUPSEPERATOR;

    public String getVersion();

    public String getHeader();

    public boolean convert(Logger l, File f);

}
