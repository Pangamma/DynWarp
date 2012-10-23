package mario182.dynwarp.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mario182.dynwarp.Main;

public class V1ToV2Converter implements FormatConverter{

    @Override
    public String getVersion() {
        return "File format v2";
    }

    @Override
    public String getHeader() {
        return "#DynWarp 0.1 by mario182 - File format v1";
    }

    @Override
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public boolean convert(Logger l, File f) {
        ArrayList<String> beforelines = new ArrayList<>();
        ArrayList<String> afterlines = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(f));
            {
                String line;
                while ((line = br.readLine()) != null){
                    if (line.startsWith("#")){ continue; }
                    beforelines.add(line);
                }
            }
            br.close();
            afterlines.add("#DynWarp by mario182 - File format v2");
            afterlines.add("#warpname"+SEPERATOR+"dynmapname"+SEPERATOR+"worldname"+SEPERATOR+"groups"+SEPERATOR+"x-coord"+SEPERATOR+"y-coord"+SEPERATOR+"z-coord"+SEPERATOR+"yaw"+SEPERATOR+"pitch"+SEPERATOR+"permission");
            for (String line : beforelines){
                String[] parts = line.split(String.valueOf(SEPERATOR));
                afterlines.add(parts[0]+SEPERATOR+parts[1]+SEPERATOR+parts[2]+SEPERATOR+SEPERATOR+parts[3]+SEPERATOR+parts[4]+SEPERATOR+parts[5]+SEPERATOR+parts[6]+SEPERATOR+parts[7]+SEPERATOR+(parts.length>=9?parts[8]:""));
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (String line : afterlines){
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            return true;
        }catch(IOException e){
            l.log(Level.SEVERE, "Error converting file!", e);
            return false;
        }
    }

}
