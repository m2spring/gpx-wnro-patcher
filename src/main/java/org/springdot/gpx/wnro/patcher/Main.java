package org.springdot.gpx.wnro.patcher;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main{

    private final static long OFFSET_DAYS = 7 * 1024; // 7 days a week * 1024 weeks

    private final static String FILENAME_DATE_FORMAT = "yyyyMMdd";
    private final static DateTimeFormatter FILENAME_DATE_FORMATTER = DateTimeFormatter.ofPattern(FILENAME_DATE_FORMAT);
    private final static Pattern DATE_REGEX = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\d");

    public static void main(String[] args) throws IOException{
        if (args != null){
            for (String arg : args){
                File f = new File(arg);
                if (!f.exists()){
                    System.out.println("no such file "+f);
                }else{
                    patchGPX(f);
                }
            }
        }
    }

    private static void patchGPX(File gpxF) throws IOException{
        GPX gpx = GPX.read(gpxF.getPath());

        GPX gpxNew = gpx.toBuilder()
            .trackFilter()
            .map(track -> track.toBuilder()
                .map(segment -> segment.toBuilder().map(Main::patchWP).build())
                .build()
            )
            .build()
            .build();

        File gpxFNew = patchFilename(gpxF);
        System.out.println(gpxF+" -> "+gpxFNew);
        GPX.writer(" ").write(gpxNew,gpxFNew.getPath());
    }

    private static WayPoint patchWP(WayPoint wp){
        return wp.toBuilder()
             .time(wp.getTime().map(t -> t.plusDays(OFFSET_DAYS)).orElse(null))
             .build();
    }

    private static File patchFilename(File f){
        String fn = f.getName();
        Matcher m = DATE_REGEX.matcher(fn);
        if (!m.find()) return f;

        String ymdNew = FILENAME_DATE_FORMATTER.format(LocalDate.parse(m.group(),FILENAME_DATE_FORMATTER).plusDays(OFFSET_DAYS));
        int p = m.start();
        String fnNew = fn.substring(0,p)+ymdNew+fn.substring(p+FILENAME_DATE_FORMAT.length());
        return new File(f.getParentFile(),fnNew);
    }
}
